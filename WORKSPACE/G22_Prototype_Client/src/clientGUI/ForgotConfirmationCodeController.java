package clientGUI;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotConfirmationCodeController {

    private Client client;
    public void setClient(Client client) { this.client = client; }

    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;
    ClientController mainController;
    
	public void setMainController(ClientController mainController) {
		this.mainController = mainController;
		
	}
    @FXML
    private void onClose() {
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    @FXML
    private void onResendCode() {
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (phone.isEmpty() && email.isEmpty()) {
            statusLabel.setText("Enter phone or email.");
            return;
        }
        
        client.requestResendConfirmationCodeRequest(phone,email);

    }

    private void close() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }


}
