package us.hall.qcapp;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class ExportController implements Initializable {

	@FXML
	private TextField displayName;
	
	@FXML
	private TextField className;
	
	@FXML
	private CheckBox renderOpt;
	
	@FXML   
	private Button xmlFileSave;
	
	@FXML 
	private TextField xmlFileName;
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    }
    
    public void setDisplayName(String name) {
    	displayName.setText(name);
    }
    
    public void setClassName(String name) {
    	className.setText(name);
    }
    
    public void setFileName(String name) {
    	xmlFileName.setText(name);
    }
    
    public boolean renderOn() {
    	return renderOpt.isSelected();
    }
}