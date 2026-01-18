package clientGUI;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * ManagementDashboardController provides the manager/admin dashboard interface.
 * 
 * <p>This controller serves as the main navigation hub for restaurant managers,
 * providing access to administrative functions after successful employee login.
 * 
 * <p>Features:
 * <ul>
 *   <li><b>Role Display</b> - Shows the logged-in user's role</li>
 *   <li><b>Register Subscriber</b> - Navigate to subscriber registration</li>
 *   <li><b>Employee Dashboard</b> - Access staff management interface</li>
 *   <li><b>Statistics</b> - View reports and analytics</li>
 *   <li><b>Logout</b> - End current session</li>
 * </ul>
 * 
 * <p>This controller delegates actual operations to {@link ClientController}
 * which manages the navigation and popup windows.
 * 
 * @author G22 Team
 * @version 1.0
 * @see ClientController
 * @see EmployeeDashboardController
 * @see ManagerStatisticsController
 */
public class ManagementDashboardController {

    // ==========================================================
    // FXML UI COMPONENTS
    // ==========================================================
    
    /** Content pane for loading sub-views */
    @FXML private StackPane contentPane;
    
    /** Label displaying current user's role */
    @FXML private Label lblRole;
    
    /** Button to open statistics view */
    @FXML private Button Statbtn;

    // ==========================================================
    // SERVER CONNECTION AND CONTROLLERS
    // ==========================================================
    
    /** Client instance for server communication */
    private Client client; 
    
    /** Reference to main client controller */
    private ClientController cc;
    
    /**
     * Sets the client instance for server communication.
     * 
     * @param client the Client instance
     */
    public void setClient(Client client) {
        this.client = client;
    }
    
    /**
     * Sets the main client controller reference.
     * 
     * @param mainController the ClientController instance
     */
    public void setClientMain(ClientController mainController) {
        this.cc = mainController;
    }
    
    // ==========================================================
    // CONFIGURATION
    // ==========================================================
    
    /**
     * Sets the role text to display after login.
     * Called after successful employee authentication.
     * 
     * @param role the role name to display (e.g., "Manager", "Staff")
     */
    public void setRoleText(String role) {
        lblRole.setText("Role: " + role);
    }

    // ==========================================================
    // NAVIGATION ACTIONS
    // ==========================================================

    /**
     * Opens the subscriber registration popup.
     * Delegates to ClientController for actual navigation.
     * 
     * @param event the action event from button click
     */
    @FXML
    private void openRegisterSubscriber(ActionEvent event) {
        cc.onRegisterSubscriber(event);
    }

    /**
     * Opens the employee dashboard for staff management.
     * Delegates to ClientController for actual navigation.
     */
    @FXML
    private void openEmployeeDashboards() {
        cc.onEmployeeDashboard();
    }

    /**
     * Opens the statistics/reports view.
     * Delegates to ClientController for actual navigation.
     * 
     * @param event the action event from button click
     */
    @FXML
    private void openStatistics(ActionEvent event) {
        cc.onOpenStatistics(event);
    }

    // ==========================================================
    // SESSION MANAGEMENT
    // ==========================================================

    /**
     * Handles user logout.
     * Shows goodbye message and sends logout request to server.
     */
    @FXML
    private void onLogout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Logout");
        a.setContentText("GoodBye !");
        a.showAndWait();
        client.requestLogout();
    }

    // ==========================================================
    // HELPER METHODS
    // ==========================================================

    /**
     * Displays an error alert dialog.
     * 
     * @param header the alert header text
     * @param msg the error message content
     */
    private void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }

}
