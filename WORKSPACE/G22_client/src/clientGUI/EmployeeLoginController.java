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

/**
 * EmployeeLoginController handles employee/staff login functionality.
 * 
 * <p>This controller manages the employee login popup where staff members
 * can authenticate using their username and password credentials.
 * 
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate that username and password are provided</li>
 *   <li>Send employee login request to server via Client</li>
 *   <li>Display login status/error messages</li>
 *   <li>Close popup and redirect to employee dashboard on success</li>
 * </ul>
 * 
 * @author G22 Team
 * @version 1.0
 * @see Client
 * @see EmployeeDashboardController
 */
public class EmployeeLoginController {

    // ==========================================================
    // SERVER CONNECTION AND CONTROLLERS
    // ==========================================================
    
    /** Client instance for server communication */
    private Client client;
    
    /** Reference to main controller for navigation */
    private ClientController mainController;

    // ==========================================================
    // FXML UI COMPONENTS
    // ==========================================================
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // ==========================================================
    // SETTERS
    // ==========================================================
    
    /**
     * Sets the client instance for server communication.
     * 
     * @param client the Client instance to use for login requests
     */
    public void setClient(Client client) { this.client = client; }
    
    /**
     * Sets the main controller reference for navigation.
     * 
     * @param c the ClientController instance
     */
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

