package clientGUI;

import client.Client;
import client.ClientUI;
import common.dto.Authentication.SubscriberAuthRequest;
import common.dto.Authentication.SubscriberAuthResponse;
import common.enums.AuthOperation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

	    SubscriberAuthRequest req = SubscriberAuthRequest.createAuthRequest(code);

	    try {
	        client.sendToServer(req); // replace with your actual client instance
	        statusLabel.setText("Logging in...");
	    } catch (Exception e) {
	        statusLabel.setText("Could not reach server.");
	        e.printStackTrace();
	    }
	}
	public void onAuthResponse(SubscriberAuthResponse resp) {
	    System.out.println("Login success = " + resp.isSuccess());

	    statusLabel.setText("success = " + resp.isSuccess() + " | " + resp.getMessage());

	    if (resp.isSuccess()) {
	        // close if you want:
	        // ((Stage) membershipCodeField.getScene().getWindow()).close();
	    }
	}

}
