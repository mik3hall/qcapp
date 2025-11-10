package us.hall.qcapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

public class PythonProcess {

	public enum PythonAPI {
		QisKit,
		Cirq
	}
	
	private static Process p = null;
	private PrintStream procout;
	private	PrintStream out = System.out;
	private Path env = null, wrapperPath = null;
	StringTemplateGroup stg = null;
	
	public PythonProcess() {
		p = init();
	}

	public void pythonRun(String script) {
		System.out.println(script);
	}
	
	public void qiskitRun(String script) {
		System.out.println(script);
		StringTemplate executeT = stg.getInstanceOf("execute");
		executeT.setAttribute("venvPath", env.toString());
		executeT.setAttribute("wrapperPath", wrapperPath.toString());
		executeT.setAttribute("script", script);
		try {
			Path qiskitScript = Files.createTempFile("qiskit_script", ".sh");
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(qiskitScript);
			permissions.add(PosixFilePermission.OWNER_EXECUTE);
			Files.setPosixFilePermissions(qiskitScript, permissions);
			FileWriter fileWriter = new FileWriter(qiskitScript.toString(), true);   
			BufferedWriter bw = new BufferedWriter(fileWriter);
			Path xml = Files.createTempFile("xmlout",".xml");
			executeT.setAttribute("xmlfile", xml.toString());
			System.out.println(executeT);
			bw.write(executeT.toString());
			bw.close();
			String result = rtexec(new String[] { qiskitScript.toString() }, true);
			System.out.println(xml + " " + Files.size(xml));
			new StrangeProgram(xml.toFile());
			//System.out.println(result);
		}
		catch (IOException ioex) { ioex.printStackTrace(); }
	}
	
	public void cirqRun(String script) {
	
	}

	private Optional<String> findAPIImport(String filePath) {
        try (Stream<String>lines = Files.lines(Paths.get(filePath))) {
            	return lines.filter(line -> (line.contains("qiskit") || line.contains("cirq")) && line.contains("import"))
                .findFirst(); // Stops after finding the first match
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return Optional.empty(); 
        }
    }
    
    private PythonAPI findAPI(String filePath) {
    	Optional<String> api = findAPIImport(filePath);
        if (api.isPresent()) {
        	if (api.get().contains("qiskit")) return PythonAPI.QisKit;
        	return PythonAPI.Cirq;
    	}
    	return null;
    }
	
	
	private static String rtexec(String[] args, boolean echo) {
       try {
           StringBuffer execout = new StringBuffer();
           Process proc = Runtime.getRuntime().exec(args);
           proc.waitFor();
           InputStream inout = proc.getInputStream();
           InputStream inerr = proc.getErrorStream();
           byte []buffer = new byte[256];
           while (true) {
               int stderrLen = inerr.read(buffer, 0, buffer.length);
               if (stderrLen > 0) {
               	   if (echo) {
               	   	System.out.println(new String(buffer,0,stderrLen));
               	   }
               	   else {
                   	execout.append(new String(buffer,0,stderrLen)); 
                   }                  
               }
               int stdoutLen = inout.read(buffer, 0, buffer.length);
               if (stdoutLen > 0) {
               	   if (echo) {
               	   	System.out.println(new String(buffer,0,stdoutLen));
               	   }
               	   else {
                   	execout.append(new String(buffer,0,stdoutLen));
                   }
               }
               if (stderrLen < 0 && stdoutLen < 0)
                   break;
           }   
           return execout.toString();
       }
       catch(Throwable tossed) { tossed.printStackTrace(); }
       return "-";
    }
   
	private final Process init() {
		InputStream is = null;
		ProcessBuilder pb; 
		try {
			env = Files.createTempDirectory("python_env");
			Path initScript = Files.createTempFile("init_script", ".sh");
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(initScript);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(initScript, permissions);
			//pb = new ProcessBuilder(new String[] { "bash", "-v", initScript.toString() });
			pb = new ProcessBuilder(initScript.toString());
			is = getClass().getResourceAsStream("python.stg");
			stg = new StringTemplateGroup(new InputStreamReader(is),DefaultTemplateLexer.class);
			StringTemplate launchT = stg.getInstanceOf("launch");
			launchT.setAttribute("path",env.toString());
			FileWriter fileWriter = new FileWriter(initScript.toString(), true);   
    		BufferedWriter bw = new BufferedWriter(fileWriter);
    		System.out.println(launchT);
    		bw.write(launchT.toString()); // .replace('@','$'));
    		bw.close();
    		String result = rtexec(new String[] { initScript.toString() }, true);
    		is = getClass().getResourceAsStream("qiskit_wrapper.py");
    		if (is == null) {
				// Handle case where resource is not found
				System.err.println("Resource not found: qiskit_wrapper.py");
			}
			wrapperPath = Files.createTempFile("qiskit_wrapper", ".py");
			Files.copy(is, wrapperPath, StandardCopyOption.REPLACE_EXISTING);
			is.close();
			return null;
    		//System.out.println(result);
			//return pb.start();
		}
		catch (Exception ex) {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException ioex) {
					ioex.printStackTrace();
				}
			}
			else {
				ex.printStackTrace();
			}
		}
		throw new IllegalStateException("Failed to initialize QISKit python environment");
	}	
}