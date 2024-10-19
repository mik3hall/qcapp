package us.hall.qcapp;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class ImportController implements Initializable {

	@FXML
	private TextField displayName;
	
	@FXML
	private TextField className;
	
	@FXML
	private CheckBox renderOpt;
	
	@FXML   
	private Button xmlFileSelect;
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    }
    
}