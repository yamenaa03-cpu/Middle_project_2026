package clientGUI;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * RegisterSubscriberController handles new subscriber registration.
 * 
 * <p>This controller manages the registration popup where new customers
 * can sign up for a membership by providing their contact information.
 * 
 * <p>Required fields:
 * <ul>
 *   <li>Full name</li>
 *   <li>Phone number</li>
 *   <li>Email address</li>
 * </ul>
 * 
 * <p>Upon successful registration, the server generates a unique
 * 8-character membership code for the new subscriber.
 * 
 * @author G22 Team
 * @version 1.0
 * @see Client
 * @see ClientController
 */
public class RegisterSubscriberController {

    // ==========================================================
    // SERVER CONNECTION AND CONTROLLERS
    // ==========================================================
    
    /** Client instance for server communication */
        private Client client;
        
    /** Reference to main controller */
        private ClientController MainController;
        
    // ==========================================================
    // FXML UI COMPONENTS
    // ==========================================================
    
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
