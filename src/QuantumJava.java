package us.hall.qcapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.lang.invoke.*;
import java.util.HashMap;
import java.util.List;

import org.redfx.strangefx.ui.QubitBoard;

public class QuantumJava extends Application {

	private final static MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	private final static MethodType mtype = MethodType.methodType(void.class, String[].class);
	private final static HashMap<String, Class> classes = new HashMap();
    private final static String name = "main";
	private static final String[] procArgs = new String[] {
		System.getProperty("java.home")+"/bin/java",
		"-cp",System.getProperty("java.class.path"),
		"org.redfx.javaqc.ch05.maryqubit.Main"
	};
    //private FXMLLoader iLoader = null;		// Import option FXML loader
    //private FXMLLoader exLoader = null;     // Export option FXML loader
	AnchorPane exportRoot = null; 
	private PythonProcess pythonAPI = null;
	
    static {
    	classes.put("CH02 hellostrange", org.redfx.javaqc.ch02.hellostrange.Main.class);
    	classes.put("CH03 paulix", org.redfx.javaqc.ch03.paulix.Main.class);
    	classes.put("CH03 paulixui", org.redfx.javaqc.ch03.paulixui.Main.class);
    	classes.put("CH04 hadamard", org.redfx.javaqc.ch04.hadamard.Main.class);
    	classes.put("CH04 hadamard2", org.redfx.javaqc.ch04.hadamard2.Main.class);
    	classes.put("CH05 bellstate", org.redfx.javaqc.ch05.bellstate.Main.class);
    	classes.put("CH05 classiccoin", org.redfx.javaqc.ch05.classiccoin.Main.class);
    	classes.put("CH05 cnot", org.redfx.javaqc.ch05.cnot.Main.class);
    	classes.put("CH05 maryqubit", org.redfx.javaqc.ch05.maryqubit.Main.class);
    	classes.put("CH05 quantumcoin", org.redfx.javaqc.ch05.quantumcoin.Main.class);
    	classes.put("CH06 classic", org.redfx.javaqc.ch06.classic.Main.class);
    	classes.put("CH06 classiccopy", org.redfx.javaqc.ch06.classiccopy.Main.class);
    	classes.put("CH06 hczmeasure", org.redfx.javaqc.ch06.hczmeasure.Main.class);
    	classes.put("CH06 repeater", org.redfx.javaqc.ch06.repeater.Main.class);
    	classes.put("CH06 teleport", org.redfx.javaqc.ch06.teleport.Main.class);
    	classes.put("CH07 add1", org.redfx.javaqc.ch07.add1.Main.class);
    	classes.put("CH07 add2", org.redfx.javaqc.ch07.add2.Main.class);
    	classes.put("CH07 randombit", org.redfx.javaqc.ch07.randombit.Main.class);
    	classes.put("CH07 randombitdebug", org.redfx.javaqc.ch07.randombitdebug.Main.class);
    	classes.put("CH08 bb84", org.redfx.javaqc.ch08.bb84.Main.class);
    	classes.put("CH08 guess", org.redfx.javaqc.ch08.guess.Main.class);
    	classes.put("CH08 haha", org.redfx.javaqc.ch08.haha.Main.class);
    	classes.put("CH08 naive", org.redfx.javaqc.ch08.naive.Main.class);
    	classes.put("CH08 superposition", org.redfx.javaqc.ch08.superposition.Main.class);
    	classes.put("CH09 applyoracle", org.redfx.javaqc.ch09.applyoracle.Main.class);
    	classes.put("CH09 deutsch", org.redfx.javaqc.ch09.deutsch.Main.class);
    	classes.put("CH09 deutschjozsa", org.redfx.javaqc.ch09.deutschjozsa.Main.class);
    	classes.put("CH09 function", org.redfx.javaqc.ch09.function.Main.class);
    	classes.put("CH09 oracle", org.redfx.javaqc.ch09.oracle.Main.class);
    	classes.put("CH09 reversibleX", org.redfx.javaqc.ch09.reversiblex.Main.class);
    	classes.put("CH10 classicsearch", org.redfx.javaqc.ch10.classicsearch.Main.class);
    	classes.put("CH10 grover", org.redfx.javaqc.ch10.grover.Main.class);
    	classes.put("CH10 groveroracle", org.redfx.javaqc.ch10.groveroracle.Main.class);
    	classes.put("CH10 quantumsearch", org.redfx.javaqc.ch10.quantumsearch.Main.class);
    	classes.put("CH10 stepbystepgrover", org.redfx.javaqc.ch10.stepbystepgrover.Main.class);
    	classes.put("CH11 classicfactor", com.javaqc.ch11.classicfactor.Main.class);
    	classes.put("CH11 quantumfactor", com.javaqc.ch11.quantumfactor.Main.class);
    	classes.put("CH11 semiclassicfactor", com.javaqc.ch11.semiclassicfactor.Main.class);
    	Runtime.getRuntime().addShutdownHook(new Completion());
    }
    private static Controller controller = null; 
    //private static ImportController importController = new ImportController(); 
    //private static ExportController exportController = new ExportController();
    private StrangeProgram spgm = null;
    
    @Override
    public void start(final Stage primaryStage) {
        try {
        	primaryStage.setTitle("QuantumJava");
        	FXMLLoader loader = new FXMLLoader(getClass().getResource("split.fxml"));
			AnchorPane root = (AnchorPane)loader.load();
			controller = (Controller)loader.getController();
			//iLoader.setController(importController);
			//importController = (Controller)iLoader.getController();
			//exportController = (Controller)exLoader.getController();
			//System.out.println("export controller " + exportController);
            Scene scene = new Scene(root);
			MenuBar menuBar = getMenuBar(primaryStage, controller);
            ((AnchorPane)scene.getRoot()).getChildren().addAll(menuBar);
            //Class clazz = org.redfx.strangefx.ui.Main.class;
            //scene.getStylesheets().add(clazz.getResource("/styles.css").toExternalForm());
            //ObservableList<String> sheets = scene.getStylesheets();
            //sheets.forEach(System.out::println);
            primaryStage.setScene(scene);
            primaryStage.show();
            controller.setPrimaryStage(primaryStage);
            controller.initializeControls();
        } catch(Exception e) {
            e.printStackTrace();
        }
        catch (Throwable tossed) {
        	tossed.printStackTrace();
        }
    }

	
	protected static Controller getController() {
		return controller;
	}

	
	MenuBar getMenuBar(Stage stage, final Controller controller) {
	    MenuBar menuBar = new MenuBar();
		final String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Mac")) {
			menuBar.useSystemMenuBarProperty().set(true);
		}
		Menu bookMenu = new Menu("Book");
		MenuItem hello = new MenuItem("CH02 hellostrange");
		MenuItem paulix = new MenuItem("CH03 paulix");
		MenuItem paulixui = new MenuItem("CH03 paulixui");
		MenuItem hadamard = new MenuItem("CH04 hadamard");
		MenuItem hadamard2 = new MenuItem("CH04 hadamard2");
		MenuItem bellstate = new MenuItem("CH05 bellstate");
		MenuItem classiccoin = new MenuItem("CH05 classiccoin");
		MenuItem cnot = new MenuItem("CH05 cnot");
		MenuItem maryqubit = new MenuItem("CH05 maryqubit");
		MenuItem quantumcoin = new MenuItem("CH05 quantumcoin");
		MenuItem classic = new MenuItem("CH06 classic");
		MenuItem classiccopy = new MenuItem("CH06 classiccopy");
		MenuItem hczmeasure = new MenuItem("CH06 hczmeasure");
		MenuItem repeater = new MenuItem("CH06 repeater");
		MenuItem teleport = new MenuItem("CH06 teleport");
		MenuItem add1 = new MenuItem("CH07 add1");
		MenuItem add2 = new MenuItem("CH07 add2");
		MenuItem randombit = new MenuItem("CH07 randombit");
		MenuItem randombitdebug = new MenuItem("CH07 randombitdebug");
		MenuItem bb84 = new MenuItem("CH08 bb84");
		MenuItem guess = new MenuItem("CH08 guess");
		MenuItem haha = new MenuItem("CH08 haha");
		MenuItem naive = new MenuItem("CH08 naive");
		MenuItem superposition = new MenuItem("CH08 superposition");
		MenuItem applyoracle = new MenuItem("CH09 applyoracle");
		MenuItem deutsch = new MenuItem("CH09 deutsch");
		MenuItem deutschjozsa = new MenuItem("CH09 deutschjozsa");
		MenuItem function = new MenuItem("CH09 function");
		MenuItem oracle = new MenuItem("CH09 oracle");
		MenuItem reversibleX = new MenuItem("CH09 reversibleX");
		MenuItem classicsearch = new MenuItem("CH10 classicsearch");
		MenuItem grover = new MenuItem("CH10 grover");
		MenuItem groveroracle = new MenuItem("CH10 groveroracle");
		MenuItem quantumsearch = new MenuItem("CH10 quantumsearch");
		MenuItem stepbystepgrover = new MenuItem("CH10 stepbystepgrover");
		MenuItem classicfactor = new MenuItem("CH11 classicfactor");
		MenuItem quantumfactor = new MenuItem("CH11 quantumfactor");
		MenuItem semiclassicfactor = new MenuItem("CH11 semiclassicfactor");
		bookMenu.getItems().addAll(hello, new SeparatorMenuItem(), paulix, paulixui, 
			new SeparatorMenuItem(), hadamard, hadamard2, new SeparatorMenuItem(),
			bellstate, classiccoin, cnot, maryqubit, quantumcoin, 
			new SeparatorMenuItem(), classic, classiccopy, hczmeasure, repeater, 
			teleport, new SeparatorMenuItem(), add1, add2, randombit, 
			randombitdebug, new SeparatorMenuItem(), bb84, guess, haha, naive, 
			superposition, new SeparatorMenuItem(), applyoracle, deutsch, 
			deutschjozsa, function, oracle, reversibleX, new SeparatorMenuItem(),
			classicsearch, grover, groveroracle, quantumsearch, stepbystepgrover,
			classicfactor, quantumfactor, semiclassicfactor);
		Menu fileMenu = new Menu("File");
		MenuItem importItem = new MenuItem("Import");
		//FXMLLoader loader = iLoader;
		importItem.setOnAction((e) -> {
			try {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Select QIS-XML");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XML", "*.xml"));
				File selectedFile = fileChooser.showOpenDialog(stage);
				if (selectedFile != null) {
					new StrangeProgram(selectedFile);
				}
				//XMLParser parser = new XMLParser(selectedFile);
				//AnchorPane importRoot = (AnchorPane)iLoader.load();
				//Stage istage = new Stage();
				//istage.setTitle("QIS-XML Import");
				//istage.setScene(new Scene(importRoot, 600, 375));
				//istage.show();
			}
			/*
			catch (IOException ioex) {
				ioex.printStackTrace();
			}
			*/
			catch (Throwable tossed) {
				tossed.printStackTrace();
			}
		});
/*
			Button filePicker = new Button("Select XML File");
			filePicker.setOnAction((e_pick) -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Select QIS-XML");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XML", "*.xml"));
				File selectedFile = fileChooser.showOpenDialog(stage);
			});
*/		
			//XMLParser parser = new XMLParser(selectedFile);
//		});		
		MenuItem exportItem = new MenuItem("Export...");
		exportItem.setOnAction((e) -> {
			FileChooser fileChooser = new FileChooser();
			StringBuilder title = new StringBuilder("Export ").append(stage.getTitle());
			title.append(" QIS-XML");
			fileChooser.setTitle(title.toString());
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("QIS-XML", "*.xml"));
			fileChooser.setInitialFileName(spgm.getFileName()+".xml");
			File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
            	Path outxml = file.toPath();
				try {
					//Stage exstage = new Stage();
					//exstage.setScene(new Scene(exportRoot, 600, 375));
					//exstage.setTitle(stage.getTitle());
					//exportController.setFileName(outxml.toString());
					if (spgm != null) {
						String className = spgm.getClassName();
						if (className != null) {
							controller.setClassName(className);
						}
					}
					//exstage.show();
					XMLDocument doc = new XMLDocument((VBox)controller.
													  getDisplayContainer().
													  getChildren().get(0),
													  controller.getMeta());
					byte[] b = doc.
							   toString().
							   getBytes();
					Files.write(outxml,b);
				} 
				catch (IOException ioex) {
					ioex.printStackTrace();
				}
            }
            else {
            	// TODO something
            }
		});
		exportItem.setDisable(true);
		MenuItem openItem = new MenuItem("Open...");
		openItem.setOnAction((e) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open QC Strange Program");
			File selectedFile = fileChooser.showOpenDialog(stage);
			if (selectedFile != null) {
				if (selectedFile.toString().endsWith(".py")) {
					if (pythonAPI == null) {
						pythonAPI = new PythonProcess();
					}
					try {
						System.out.println("Invoking qiskitRun");
						pythonAPI.qiskitRun(selectedFile.toString());
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					//qiskit.read("print('Hello, World!')");
					//StringBuilder cmd = new StringBuilder("exec(open(\"");
					//cmd.append(selectedFile.toString());
					//cmd.append("\").read(), globals())");
					//qiskit.read(cmd.toString());
					return;
				}
				spgm = new StrangeProgram(selectedFile);
				/**
				 * TODO the program shouldn't be processed on event thread?
				 */
				Platform.runLater(() -> spgm.process());
				/**
				 * TODO wait to enable until above is done?
				 */
				exportItem.setDisable(false);
			}
		});
		Menu openAsMenu = new Menu("Open As...");
		MenuItem openAsStrange = new MenuItem("Strange Java");
		MenuItem openAsQiskit = new MenuItem("Qiskit Python");
		MenuItem openAsCirq = new MenuItem("Cirq Python");
		openAsStrange.setOnAction((e) -> {
			System.out.println("Open As String");
		});
		openAsMenu.getItems().addAll(openAsStrange, openAsQiskit, openAsCirq);
		MenuItem saveItem = new MenuItem("Save...");
		saveItem.setOnAction((e) -> {
			System.out.println("Save not currently supported");
		});
		Menu saveAsMenu = new Menu("Save As...");
		MenuItem saveStrangeItem = new MenuItem("Strange Java");
		MenuItem saveQiskitItem = new MenuItem("Qiskit Python");
		MenuItem saveCirqItem = new MenuItem("Cirq Python");
		saveAsMenu.getItems().addAll(saveStrangeItem, saveQiskitItem, saveCirqItem);
		fileMenu.getItems().addAll(openItem, openAsMenu, saveItem, saveAsMenu, 
			new SeparatorMenuItem(), importItem, exportItem);
		menuBar.getMenus().addAll(fileMenu, bookMenu);
		List<MenuItem> items = bookMenu.getItems();
		for (MenuItem item : items) {
			item.setOnAction((e) -> {
				String itemText = ((MenuItem)e.getTarget()).getText();
				if (itemText.equals("CH05 maryqubit")) {
					try {
						ProcessBuilder pb = new ProcessBuilder(procArgs);
						Process p = pb.start();
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				else {
					Class clazz = classes.get(itemText);
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
			});           	
		}
		return menuBar;
	}
	
    public static void main(String[] args) {
        launch(args);
    }
    
    static class Completion extends Thread {
  		
  		public void run() {
  		}
  	}
}