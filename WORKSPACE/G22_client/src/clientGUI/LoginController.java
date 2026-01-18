package clientGUI;

import client.Client;
import client.ClientUI;
import common.dto.UserAccount.UserAccountRequest;
import common.dto.UserAccount.UserAccountResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * LoginController handles subscriber login functionality.
 * 
 * <p>This controller manages the login popup window where subscribers
 * can enter their 8-character membership code to authenticate with the server.
 * 
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate membership code format (8 characters)</li>
 *   <li>Send login request to server via Client</li>
 *   <li>Display login status/error messages</li>
 *   <li>Close popup on successful login</li>
 * </ul>
 * 
 * @author G22 Team
 * @version 1.0
 * @see Client
 * @see UserAccountResponse
 */
public class LoginController {

    // ==========================================================
    // SERVER CONNECTION
    // ==========================================================
    
    /** Client instance for server communication */
    private Client client;
    
    // ==========================================================
    // FXML UI COMPONENTS
    // ==========================================================
    
    @FXML Button loginButton;
    @FXML TextField membershipCodeField;
    @FXML private Label statusLabel;

    // ==========================================================
    // SETTERS
    // ==========================================================
    
    /**
     * Sets the client instance for server communication.
     * 
     * @param client the Client instance to use for login requests
     */
    public void setClient(Client client) {
        this.client = client;
    }
    

        


        @FXML
        private void initialize() {
                membershipCodeField.setOnAction(e -> loginButton.fire());
        }
        @FXML
        private void onLogin() {
            String code = membershipCodeField.getText().trim();

            if (code.isEmpty()) {
                statusLabel.setText("Please enter your membership code.");
                return;
            }
            if(code.length() != 8) {
                statusLabel.setText("membership code Should be of lenght 8.");
                return;
            }
           

            try {
                statusLabel.setText("Logging in...");
                    client.requestLoginBySubscriptionCode(code);
            } catch (Exception e) {
                statusLabel.setText("Could not reach server.");
                e.printStackTrace();
            }
        }
        public void onAuthResponse(UserAccountResponse resp) {
                Platform.runLater(() -> {
                System.out.println("Login success = " + resp.isSuccess());

            statusLabel.setText("success = " + resp.isSuccess() + " | " + resp.getMessage());

            if (resp.isSuccess()) {
                // close if you want:
                 ((Stage) membershipCodeField.getScene().getWindow()).close();
            }
        
        });
        }
}
