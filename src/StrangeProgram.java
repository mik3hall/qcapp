package us.hall.qcapp;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import javax.tools.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import us.hall.qcapp.parts.*;


public class StrangeProgram {
	
	private final File programFile;
	private final static String methodName = "main";
	private final static MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	private final static MethodType mtype = MethodType.methodType(void.class, String[].class);
	private final static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final static DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        
	private String fileName = null;
	private String className = null;
	
	public StrangeProgram(File f) {
		this.programFile = f;
		String fstr = f.toString();
		try {
			if (!fstr.endsWith(".java")) {
				if (fstr.endsWith(".xml")) {
					importXML(f);
				}
				else {
					System.out.println("StrangeProgram filename must have a .java extension");
				}
				return;
			}
			int pathend = fstr.lastIndexOf(System.getProperty("file.separator"))+1;
			int extidx = fstr.lastIndexOf(".");
			this.fileName = fstr.substring(pathend,extidx);
		}
		catch (Throwable tossed) {
			tossed.printStackTrace();
		}
	}
	
	
	public String getFileName() {
		return fileName;
	}
	
	public void process() {
		System.out.println("StrangeProgram: Process file " + programFile + " ...");
		try {
			int result = 0;
			// Create destination directory		
			Path destPath = Files.createTempDirectory("dest");
			result = compile(destPath);
			if (result == 0) {
				invoke(destPath);
			}
		}
		catch (Exception ex) { ex.printStackTrace(); }
	}
	
	private void importXML(File f) {
		boolean displayCircuit = true;
		XMLParser parser = new XMLParser(f);
		Circuit circuit = parser.getCircuit();
		ArrayList<Step> steps = circuit.getSteps();
		for (Step step : steps) {
			ArrayList<Operation> operations = step.getOperations();
			//System.out.println("operations " + operations);
			for (Operation operation : operations) {
				//System.out.println("operation " + operation);
				GateDefinition gate = operation.getGate();
				//System.out.println("gate definition " + gate);
				//System.out.println(":: " + gate.getName());
			}
		}
		steps = circuit.getSteps();
		StringTemplateGroup stg = null;
		try {
			ClassLoader cl = getClass().getClassLoader();
			InputStream is = getClass().getResourceAsStream("strange.stg");
			stg = new StringTemplateGroup(new InputStreamReader(is),DefaultTemplateLexer.class);
			StringTemplate programT = stg.getInstanceOf("sourceIn");
			programT.setAttribute("gateImports",parser.getGateNames());
			String fileName = f.getName();
			int index = fileName.lastIndexOf(".");
			String className = fileName.substring(0,1).toUpperCase()+fileName.substring(1,index);
			programT.setAttribute("className", className);
			programT.setAttribute("nqbit",parser.getNQbit()); // Not sure why 1 too big
			int stepNum = 0;
			int pGateNum = 0;
			ArrayList<String> stepNames = new ArrayList<String>();
			StringBuilder pgmBody = new StringBuilder();
			for (Step step : steps) {
				ArrayList<Operation> operations = step.getOperations();
				StringBuilder stepOut = new StringBuilder();
				if (operations.get(0).getGate().getName().equals("ProbabilitiesGate")) {
					stepOut.append("\tStep p");
					stepOut.append(pGateNum);
					stepNames.add("p"+pGateNum);
					pGateNum++;
				}
				else {
					stepOut.append("\tStep step");
					stepOut.append(stepNum);
					stepNames.add("step"+stepNum);
					stepNum++;
				}
				stepOut.append(" = new Step(");
				for (Operation operation : operations) {
					GateDefinition gate = operation.getGate();
					String gateName = gate.getName();
					stepOut.append("new ");
					stepOut.append(gate.getName()).append("(");
					ArrayList<Map> maps = operation.getMaps();
					int qidx = 0;
					for (Map map : maps) {
						//System.out.println(map);
						stepOut.append(maps.get(qidx).getQubit());
						stepOut.append(",");
						qidx++;
					}
					if (stepOut.charAt(stepOut.length()-1) == ',') {
						stepOut.setLength(stepOut.length()-1);
					}
					stepOut.append("),");
				}
				if (stepOut.charAt(stepOut.length()-1) == ',') {
					stepOut.setLength(stepOut.length()-1);
				}
				stepOut.append(");\n");
				//System.out.println(stepOut);
				pgmBody.append(stepOut);
				stepOut.setLength(0);
			}
			pgmBody.append("\tprogram.addSteps(").append(String.join(",",stepNames));
			pgmBody.append(");\n");
			pgmBody.append("\tQuantumExecutionEnvironment qee = new SimpleQuantumExecutionEnvironment();\n");
        	pgmBody.append("\tResult result = qee.runProgram(program);\n");
			if (displayCircuit) {
				pgmBody.append("\t\tUtil.renderProgram(program, \"Strange\");\n");
			}	
			pgmBody.append("\t}\n");
			pgmBody.append("}");
			
			//System.out.println(programT.toString());
			//System.out.println(pgmBody);
			StringBuilder pgm = new StringBuilder(programT.toString());
			pgm.append("\n").append(pgmBody.toString());
			String source = pgm.toString();
			load(source, className);
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void load(String source, String className) throws ClassNotFoundException, 
										InstantiationException, IllegalAccessException {
		//System.out.println(source);
		String qualifiedClassName = "us.hall.strange." + className;
    	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    	DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    	InMemoryFileManager manager = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null));

    	List<JavaFileObject> sourceFiles = Collections.singletonList(new JavaSourceFromString(qualifiedClassName, source));

    	JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, null, null, sourceFiles);

    	boolean result = task.call();
		//System.out.println(result);
		if (!result) {
			diagnostics.getDiagnostics()
			  .forEach(d -> System.out.println(String.valueOf(d)));
		} else {
			ClassLoader classLoader = manager.getClassLoader(null);
			Class<?> clazz = classLoader.loadClass(qualifiedClassName);
			invoke(clazz);
		}
	}
	
	public int compile(Path destPath) throws IOException {
		System.out.println("StrangeProgram: Compiling...");		
		Optional<java.util.spi.ToolProvider> javac = java.util.spi.ToolProvider.findFirst( "javac" );
		int result = javac.get().run(
        	System.out,
       	 	System.err,
        	"-cp",
        	System.getProperty("java.class.path"),
        	"-d",
        	destPath.toString(),
        	programFile.toString()
		);	
		if (result == 0) {
			System.out.println("StrangeProgram: Compilation success.");
		}
		else {
			System.out.println("StrangeProgram: Compilation failed result="+result);
		}
		return result;
	}
	
	/**
	 * invoke compiled class at destination path
	 **/
	public void invoke(Path destPath) throws IOException, ClassNotFoundException {
		System.out.println("StrangeProgram: Invoking...");
		List<String> classFiles;

        try (Stream<Path> walk = Files.walk(destPath)) {
            classFiles = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString())
                    .filter(f -> f.endsWith(".class"))
                    .collect(Collectors.toList());
        }
        String classFile = classFiles.get(0);
        URLClassLoader ucl = new URLClassLoader(new URL[] {destPath.toUri().toURL()});
        int parentLength = destPath.toString().length();
        String className = classFile.substring(parentLength+1,classFile.length()-6);
        className = className.replace(File.separatorChar,'.');
        this.className = className;
        Class clazz = ucl.loadClass(className);
        invoke(clazz);
	}
	
	private void invoke(Class clazz) {
		try {
			MethodHandle handle = publicLookup.findStatic(clazz, methodName, mtype);
			handle.invoke(new String[0]);
		}
		catch (IllegalStateException ise) {
			if (!ise.getMessage().equals("Toolkit already initialized")) {
				ise.printStackTrace();
			}
		}
		catch (Throwable tossed) {
			tossed.printStackTrace();
		}	
	}
	
	public String getClassName() {
		return className;
	}
}

class JavaSourceFromString extends SimpleJavaFileObject {
    final String code;

    JavaSourceFromString(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}

class JavaClassAsBytes extends SimpleJavaFileObject {

    protected ByteArrayOutputStream bos =
        new ByteArrayOutputStream();

    public JavaClassAsBytes(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/')
            + kind.extension), kind);
    }

    public byte[] getBytes() {
        return bos.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() {
        return bos;
    }
}

class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private java.util.Map<String, JavaClassAsBytes> compiledClasses;
	private ClassLoader loader; 

 	public InMemoryFileManager(StandardJavaFileManager standardManager) {
		super(standardManager);
		this.compiledClasses = new Hashtable<>();
		this.loader = new InMemoryClassLoader(this.getClass().getClassLoader(), this);
	}
	
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
        String className, Kind kind, FileObject sibling) {

        JavaClassAsBytes classAsBytes = new JavaClassAsBytes(className, kind);
        compiledClasses.put(className, classAsBytes);

        return classAsBytes;
    }
    
    public java.util.Map<String, JavaClassAsBytes> getBytesMap() {
        return compiledClasses;
    }
    
    @Override
	public ClassLoader getClassLoader(Location location) {
    	return loader;
	}
}

class InMemoryClassLoader extends ClassLoader {

    private InMemoryFileManager manager;

    public InMemoryClassLoader(ClassLoader parent, InMemoryFileManager manager) {
        super(parent);
        this.manager = java.util.Objects.requireNonNull(manager, "manager must not be null");
    }
    
    @Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		java.util.Map<String, JavaClassAsBytes> compiledClasses = manager.getBytesMap();

		if (compiledClasses.containsKey(name)) {
			byte[] bytes = compiledClasses.get(name).getBytes();
			return defineClass(name, bytes, 0, bytes.length);
		} else {
			throw new ClassNotFoundException();
		}
	}
}