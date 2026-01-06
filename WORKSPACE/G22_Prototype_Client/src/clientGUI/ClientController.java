/*
package clientGUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import client.Client;

public class ClientController {
	
	 private Client client;   //Client object that handles clients activity 
	 
    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        System.out.println("ClientFrameController loaded ✔");
    }

    // ================= BUTTON ACTION =================
    @FXML
    private void onLoadClientReservation(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientGUI/ReservationPage.fxml")
            );

            Parent popupRoot = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("Make Reservation");
            

            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(
                    ((Node) event.getSource()).getScene().getWindow()
            );
            popupStage.setHeight(550);
            popupStage.setWidth(300);
            popupStage.setScene(new Scene(popupRoot));
            popupStage.setResizable(false);
            popupStage.showAndWait(); // BLOCKS background

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

*/
package clientGUI;

import client.Client;
import client.ClientUI;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ClientController
 *
 * Main controller for the client home page.
 * Responsibilities:
 *  - Hold a single Client instance
 *  - Open popup pages (Reservation, Login, Payment)
 *  - Pass the Client to child controllers
 *
 * DOES NOT:
 *  - Contain prototype logic
 *  - Handle reservations directly
 *
 * @author You
 */
public class ClientController {

    /** Shared client instance (used by all popups) */
    private Client client;
    @FXML private Button SignInButton;

    // ==========================================================
    // INITIALIZATION
    // ==========================================================
    @FXML
    public void initialize() {
        System.out.println("ClientController loaded ✔");

        // OPTIONAL: create client here if no login/connection screen exists
        try {
            client = new Client("localhost", 5555, null);
            client.openConnection();
            System.out.println("Client connected to server ✔");
        } catch (Exception e) {
            System.err.println("Could not connect to server");
            e.printStackTrace();
        }

    }

    /**
     * Allows another controller (or Main class)
     * to inject a Client instance.
     * This is the BEST PRACTICE approach.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    // ==========================================================
    // OPEN RESERVATION POPUP
    // ==========================================================
    @FXML
    private void onLoadClientReservation(ActionEvent event) {

        if (client == null || !client.isConnected()) {
            System.err.println("Client is not connected.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientGUI/ReservationPage.fxml")
            );

            Parent popupRoot = loader.load();

            // 👇 Inject client into popup controller
            ReservationController reservationController =
                    loader.getController();
            reservationController.setClient(client);

            Stage popupStage = new Stage();
            popupStage.setTitle("Make Reservation");

            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(
                    ((Node) event.getSource()).getScene().getWindow()
            );

            popupStage.setScene(new Scene(popupRoot));
            popupStage.setResizable(false);
            popupStage.sizeToScene();      // fits FXML perfectly
            popupStage.centerOnScreen();
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void onSignIn() {
    	 try {
    	        FXMLLoader loader = new FXMLLoader(
    	                getClass().getResource("/clientGUI/LoginPage.fxml")
    	        );

    	        Parent root = loader.load();
    	        Scene scene = new Scene(root);

    	        // CSS (enable later if needed)
    	        // scene.getStylesheets().add(
    	        //        getClass().getResource("/clientGUI/style.css").toExternalForm()
    	        // );

    	        Stage popupStage = new Stage();
    	        popupStage.setTitle("Bistro – Login");
    	        popupStage.initModality(Modality.APPLICATION_MODAL);
    	        popupStage.initOwner(SignInButton.getScene().getWindow());
    	        popupStage.setResizable(false);
    	        popupStage.setScene(scene);
    	        LoginController lc = loader.getController();

    	     // pass the client object so LoginController can send requests
    	     lc.setClient(client);


    	        popupStage.showAndWait();
    	        System.out.println("Sign in clicked");

    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    }
    
    public void onPersonalSpace() {
   	 try {
	        FXMLLoader loader = new FXMLLoader(
	                getClass().getResource("/clientGUI/personalSpace.fxml")
	        );

	        Parent root = loader.load();
	        Scene scene = new Scene(root);

	        // CSS (enable later if needed)
	        // scene.getStylesheets().add(
	        //        getClass().getResource("/clientGUI/style.css").toExternalForm()
	        // );

	        Stage popupStage = new Stage();
	        popupStage.setTitle("Bistro – Personal space");
	        popupStage.initModality(Modality.APPLICATION_MODAL);
	        popupStage.initOwner(SignInButton.getScene().getWindow());
	        popupStage.setResizable(false);
	        popupStage.setScene(scene);

	        popupStage.showAndWait();
	        System.out.println("personal space clicked");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
}
    @FXML
    public void onEmployeeDashboard() {
      	 try {
 	        FXMLLoader loader = new FXMLLoader(
 	                getClass().getResource("/clientGUI/EmployeeDashboard.fxml")
 	        );

 	        Parent root = loader.load();
 	        Scene scene = new Scene(root);

 	        // CSS (enable later if needed)
 	        // scene.getStylesheets().add(
 	        //        getClass().getResource("/clientGUI/style.css").toExternalForm()
 	        // );

 	        Stage popupStage = new Stage();
 	        popupStage.setTitle("Bistro – Employee Dashboard");
 	        popupStage.initModality(Modality.APPLICATION_MODAL);
 	        popupStage.initOwner(SignInButton.getScene().getWindow());
 	        popupStage.setResizable(false);
 	        popupStage.setScene(scene);

 	        popupStage.showAndWait();
 	        System.out.println("Employee Dashboard clicked");

 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
    }

    // ==========================================================
    // CLEAN SHUTDOWN (OPTIONAL BUT RECOMMENDED)
    // ==========================================================
    public void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                client.closeConnection();
                System.out.println("Client disconnected ✔");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
