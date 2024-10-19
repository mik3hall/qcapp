package us.hall.qcapp;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.invoke.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.spi.ToolProvider;
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
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import us.hall.qcapp.parts.*;

public class StrangeProgram {
	
	private final File programFile;
	private final static String name = "main";
	private final static MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	private final static MethodType mtype = MethodType.methodType(void.class, String[].class);
	
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
		XMLParser parser = new XMLParser(f);
		Circuit circuit = parser.getCircuit();
		ArrayList<Step> steps = circuit.getSteps();
		for (Step step : steps) {
			ArrayList<Operation> operations = step.getOperations();
			for (Operation operation : operations) {
				GateDefinition gate = operation.getGate();
				System.out.println(":: " + gate.getName());
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
			programT.setAttribute("nqbit",parser.getNQbit());
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
					pGateNum++;
				}
				else {
					stepOut.append("\tStep step");
					stepOut.append(stepNum);
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
						System.out.println(map);
						stepOut.append(maps.get(qidx).getQubit()-1);
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
				System.out.println(stepOut);
				pgmBody.append(stepOut);
				stepOut.setLength(0);
			}
			System.out.println(programT.toString());
			System.out.println(pgmBody);
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public int compile(Path destPath) throws IOException {
		System.out.println("StrangeProgram: Compiling...");		
		Optional<ToolProvider> javac = ToolProvider.findFirst( "javac" );
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
		try {
			MethodHandle handle = publicLookup.findStatic(clazz, name, mtype);
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