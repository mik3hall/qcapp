package us.hall.qcapp;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
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

public class StrangeProgram {
	
	private final File programFile;
	private final static String name = "main";
	private final static MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	private final static MethodType mtype = MethodType.methodType(void.class, String[].class);
	
	public StrangeProgram(File f) {
		this.programFile = f;
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
}