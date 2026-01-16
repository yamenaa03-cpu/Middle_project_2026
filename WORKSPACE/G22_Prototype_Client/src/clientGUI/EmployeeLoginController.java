package clientGUI;

import client.Client;
import common.dto.UserAccount.UserAccountResponse;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmployeeLoginController {
	 private Client client;
	    private ClientController mainController;

	    @FXML private TextField usernameField;
	    @FXML private PasswordField passwordField;
	    @FXML private Label errorLabel;

	    public void setClient(Client client) { this.client = client; }
	    public void setMainController(ClientController c) { this.mainController = c; }

	    @FXML
	    private void onLogin(Event e) {
	        String user = usernameField.getText();
	        String pass = passwordField.getText();

	        if (user == null || user.isBlank() || pass == null || pass.isBlank()) {
	            errorLabel.setText("Please enter username and password.");
	            return;
	        }

	        errorLabel.setText("Signing in...");
	        client.employeeLogInRequest(user, pass);

	    }

	    public void onEmployeeLoginResponse(UserAccountResponse resp) {
	        Platform.runLater(() -> {
	            if (!resp.isSuccess()) {
	                errorLabel.setText(resp.getMessage());
	                return;
	            }

	            errorLabel.setText("");
	    	    if (resp.isSuccess()) {
	    	        // close if you want:
	    	         ((Stage) usernameField.getScene().getWindow()).close();
	    	    }
	            
	            
	        });
	    }


	    @FXML
	    private void onBack() {
	        // TODO: close window or navigate back
	        System.out.println("Back clicked");
	    }
	}

