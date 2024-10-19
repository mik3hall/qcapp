package us.hall.qcapp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class Controller implements Initializable
{

	StringBuilder sb = new StringBuilder();

	@FXML 
	private SplitPane splitPane;
	
    @FXML
    private TextArea console;

	@FXML
	private ScrollPane displayPane;
		
	@FXML
	private BorderPane renderPane;
	
	@FXML 
	private ImageView imageView;
	
    public void appendText(String valueOf) {
    	sb.append(valueOf);
    	if (valueOf.endsWith("\n")) {
    		String s = sb.toString();
    		if (!s.contains("WARNING:") && 
    			!s.contains("CssStyleHelper calculateValue")) {
        		Platform.runLater(() -> console.appendText(s));
        	}
        	sb.setLength(0);
        }
    }
	
	protected SplitPane getSplitPane() {
		return splitPane;
	}
	
	protected BorderPane getRenderPane() {
		return renderPane;
	}
	
	protected ScrollPane getDisplayPane() {
		return displayPane;
	}
	
	private ObservableList<Node> emptyPane = null;
	
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendText(String.valueOf((char)b));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
        console.setEditable(false);
        new Runnable() {
            @Override
            public void run() {
                System.err.println("JavaFX Platform initialized");
            }
        };
        final ImageView view = imageView;
        Platform.runLater(new Runnable() {
        	@Override
        	public void run() {
				try {
					ClassLoader cl = getClass().getClassLoader();
					InputStream is = cl.getResourceAsStream("strangelogo.png");
					BufferedInputStream bis = new BufferedInputStream(is);
					Image img = new Image(bis);
					view.setImage(img);
					bis.close();
					//emptyPane = renderPane.getChildren();
				}
				catch (IOException ioex) { ioex.printStackTrace(); }
			}});
    }

}