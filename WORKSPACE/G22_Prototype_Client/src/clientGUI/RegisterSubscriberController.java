package clientGUI;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterSubscriberController {
	private Client client;
	private ClientController MainController;
	
	@FXML private TextField fullNameField;
	@FXML private TextField phoneField;
	@FXML private TextField emailField;
	 
	public void setClient(Client client) {
		this.client = client;
	}
	public void setMainController(ClientController clientController) {
		this.MainController = MainController;
	}
	
	@FXML
	public void onRegister(){
		String FullName = fullNameField.getText().trim();
		String PhoneNumField = phoneField.getText().trim();
		String EmailField = emailField.getText().trim();
		
		client.requestRegisterSubscriber(FullName, PhoneNumField, EmailField);
	}
	
	@FXML
	public void onBack(ActionEvent event){
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    stage.close();
	}





}
