package us.hall.qcapp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Console implements Initializable
{

    @FXML
    private TextArea console;

	@FXML 
	private ImageView imageView;
	
    public void appendText(String valueOf) {
        Platform.runLater(() -> console.appendText(valueOf));
    }

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
				}
				catch (IOException ioex) { ioex.printStackTrace(); }
			}});
    }

}