package clientGUI;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * ForgotConfirmationCodeController handles confirmation code recovery.
 * 
 * <p>This controller manages the popup where customers can request
 * their reservation confirmation code to be resent if they've forgotten it.
 * 
 * <p>Recovery options:
 * <ul>
 *   <li>By phone number</li>
 *   <li>By email address</li>
 * </ul>
 * 
 * <p>The server will look up reservations matching the provided
 * contact information and resend the confirmation code.
 * 
 * @author G22 Team
 * @version 1.0
 * @see Client
 * @see ReceiveTableController
 * @see CancelReservationController
 */
public class ForgotConfirmationCodeController {

    // ==========================================================
    // SERVER CONNECTION
    // ==========================================================
    
    /** Client instance for server communication */
    private Client client;
    
    /**
     * Sets the client instance for server communication.
     * 
     * @param client the Client instance
     */
    public void setClient(Client client) { this.client = client; }

    // ==========================================================
    // FXML UI COMPONENTS
    // ==========================================================
    
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;
    
    // ==========================================================
    // CONTROLLERS
    // ==========================================================
    
    /** Reference to main controller */
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
