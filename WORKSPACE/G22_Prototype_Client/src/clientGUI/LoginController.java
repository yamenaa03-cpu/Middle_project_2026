package clientGUI;

import client.Client;
import client.ClientUI;
import common.dto.Authentication.CustomerAuthRequest;
import common.dto.Authentication.CustomerAuthResponse;
import common.enums.AuthOperation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }
    
	@FXML private TextField membershipCodeField;
	@FXML private Label statusLabel;

	@FXML
	private void onLogin() {
	    String code = membershipCodeField.getText().trim();

	    if (code.isEmpty()) {
	        statusLabel.setText("Please enter your membership code.");
	        return;
	    }
	    if (client == null || !client.isConnected()) {
	        statusLabel.setText("Not connected to server. Please reopen the app / try again.");
	        return;
	    }


	    try {
	    		statusLabel.setText("Logging in...");
	        client.requestLoginBySubscriptionCode(code); // replace with your actual client instance
	        
	    } catch (Exception e) {
	        statusLabel.setText("Could not reach server.");
	        e.printStackTrace();
	    }
	}
	public void onAuthResponse(CustomerAuthResponse resp) {
	    Platform.runLater(() -> {
	        statusLabel.setText(resp.getMessage());

	        if (resp.isSuccess()) {
	            // close the login window
	            Stage st = (Stage) statusLabel.getScene().getWindow();
	            st.close();
	        }
	    });
	}

}
