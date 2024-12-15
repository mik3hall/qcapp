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
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
//import py4j.GatewayServer;

public class QiskitProcess {

	private static Process p = null;
	private PrintStream procout;
	//private OutWaiter waiter = null;
	private	PrintStream out = System.out;
	private boolean qiskitReady = false;
	private Path env = null;
	StringTemplateGroup stg = null;
	
	public QiskitProcess() {
		p = init();
//		procout = new PrintStream(p.getOutputStream());
//		new OutThread(p.getInputStream(),"QiskitOutThread",out).start();
//		new OutThread(p.getErrorStream(),"QiskitErrThread",out).start();
		/*
		waiter = new OutWaiter(p, out);
		synchronized (waiter) { 
			try {
				waiter.rtwait();
			}
			catch (InterruptedException iex) {
				iex.printStackTrace();
			}
		}
		*/
		/*
		final BufferedReader procin =  
			new BufferedReader(new InputStreamReader(p.getInputStream()));	
		new Thread(new Runnable() {
			public void run() {
				String line = null;
				for (;;) {
					try {
						while ((line = procin.readLine(  )) != null) {
							System.out.println(line);
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					try { 
						synchronized(Thread.currentThread()) {
							Thread.currentThread().wait(100); 
						}
					}
					catch (InterruptedException iex) {
						iex.printStackTrace();
					}
				}			
			}
		},"Qiskit in").start(); 	
		*/
	}

	public void qiskitRun(String script) {
		System.out.println(script);
		StringTemplate executeT = stg.getInstanceOf("execute");
		executeT.setAttribute("venvPath", env.toString());
		executeT.setAttribute("script", script);
		try {
			Path qiskitScript = Files.createTempFile("qiskit_script", ".sh");
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(qiskitScript);
			permissions.add(PosixFilePermission.OWNER_EXECUTE);
			Files.setPosixFilePermissions(qiskitScript, permissions);
			FileWriter fileWriter = new FileWriter(qiskitScript.toString(), true);   
			BufferedWriter bw = new BufferedWriter(fileWriter);
			System.out.println(executeT);
			bw.write(executeT.toString());
			bw.close();
			String result = rtexec(new String[] { qiskitScript.toString() }, true);
			//System.out.println(result);
		}
		catch (IOException ioex) { ioex.printStackTrace(); }
		/*
		procout.println(read);
		procout.flush();
		synchronized(this) {
			try {
				Thread.currentThread().sleep(15);			
			}
			catch (InterruptedException iex) {}

		}
		procout.println("complete");
		procout.flush();
		*/
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
		ProcessBuilder pb; 
		try {
			env = Files.createTempDirectory("python_env");
			Path initScript = Files.createTempFile("init_script", ".sh");
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(initScript);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(initScript, permissions);
			//pb = new ProcessBuilder(new String[] { "bash", "-v", initScript.toString() });
			pb = new ProcessBuilder(initScript.toString());
			ClassLoader cl = getClass().getClassLoader();
			InputStream is = getClass().getResourceAsStream("qiskit.stg");
			stg = new StringTemplateGroup(new InputStreamReader(is),DefaultTemplateLexer.class);
			StringTemplate launchT = stg.getInstanceOf("launch");
			launchT.setAttribute("path",env.toString());
			FileWriter fileWriter = new FileWriter(initScript.toString(), true);   
    		BufferedWriter bw = new BufferedWriter(fileWriter);
    		System.out.println(launchT);
    		bw.write(launchT.toString()); // .replace('@','$'));
    		bw.close();
    		String result = rtexec(new String[] { initScript.toString() }, true);
    		//System.out.println(result);
    		return null;
			//return pb.start();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		throw new IllegalStateException("Failed to initialize QISKit python environment");
	}	
}
/*
class OutWaiter {
	int incomplete = 2;
	
	OutWaiter(Process proc,PrintStream out) {
		new OutThread(this,proc.getInputStream(),"QiskitOutThread",out).start();
		new OutThread(this,proc.getErrorStream(),"QiskitErrThread",out).start();
	}

	synchronized void postComplete() {
		incomplete--;
		if (incomplete == 0) notifyAll();
	} 
	
	public synchronized void rtwait() throws InterruptedException {
		if (incomplete > 0) super.wait(); 
	}
}
*/
class OutThread extends Thread {
	InputStream is;
	PrintStream out;
    InputStreamReader reader = null; 
    BufferedReader bufferedReader = null;

	OutThread(InputStream is, String name, PrintStream out) {
		reader = new InputStreamReader(is);
		bufferedReader = new BufferedReader(reader);
		this.out = out;
		setName(name);
	}

	public void run() {
		//byte[] buffer = new byte[256];
		String line;
		try {
			//int outlen = is.read(buffer, 0, buffer.length);
			while ((line = bufferedReader.readLine()) != null) {
				//out.write(buffer,0,outlen);
				//outlen = is.read(buffer, 0, buffer.length);
				out.println(line);
			}
		}
		catch (IOException ioex) { ioex.printStackTrace(); }	
//		waiter.postComplete();
	}
}