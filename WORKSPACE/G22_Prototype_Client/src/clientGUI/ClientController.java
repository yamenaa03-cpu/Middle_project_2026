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
import common.dto.Reservation.ReservationResponse;
import common.dto.UserAccount.CustomerAuthResponse;
import common.entity.Reservation;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
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
public class ClientController implements ClientUI{

    /** Shared client instance (used by all popups) */
    private Client client;
    private LoginController activeLoginController;
    private Integer currentSubscriberId = null;//subscriber id if the client is connected
    private ReservationController activeReservationController;
    private PersonalSpaceController activePersonalSpaceController;

    @FXML private Button SignInButton;

    // ==========================================================
    // INITIALIZATION
    // ==========================================================
    @FXML
    public void initialize() {
        System.out.println("ClientController loaded ✔");

        // OPTIONAL: create client here if no login/connection screen exists
        try {
            client = new Client("localhost", 5555, this);
            client.openConnection();
            System.out.println("Client connected to server ✔");
            
        } catch (Exception e) {
            System.err.println("Could not connect to server");
            e.printStackTrace();
        }

    }
    
    public void setActivePersonalSpaceController(PersonalSpaceController c) {
        this.activePersonalSpaceController = c;
    }
    
    public void setActiveReservationController(ReservationController rc) {
        this.activeReservationController = rc;
    }
    
    public void setActiveLoginController(LoginController c) {
        this.activeLoginController = c;
    }

    /**
     * Allows another controller (or Main class)
     * to inject a Client instance.
     * This is the BEST PRACTICE approach.
     */
    public void setClient(Client client) {
        this.client = client;
    }
    
    public boolean isLoggedIn() {
        return currentSubscriberId != null;
    }

    public Integer getCurrentSubscriberId() {
        return currentSubscriberId;
    }

    public void logoutLocal() {
        currentSubscriberId = null;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/ReservationPage.fxml"));
            Parent popupRoot = loader.load();

            ReservationController reservationController = loader.getController();
            reservationController.setClient(client);
            reservationController.setMainController(this);
            setActiveReservationController(reservationController);

            Stage popupStage = new Stage();
            popupStage.setTitle("Make Reservation");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            popupStage.setScene(new Scene(popupRoot));
            popupStage.setResizable(false);
            popupStage.sizeToScene();
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
    	        LoginController lc = loader.getController();   // ✅ LoginController only
    	        lc.setClient(client);
    	        setActiveLoginController(lc);

    	     
    	        popupStage.showAndWait();
    	        System.out.println("Sign in clicked");

    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    }
    
    public void onPersonalSpace() {
   	 try {
   		 
         //  require login
         if (this == null || !isLoggedIn()) {
             Alert a = new Alert(Alert.AlertType.WARNING);
             a.setHeaderText(null);
             a.setContentText("You must log in before entering the personal space.");
             a.show();
             return;
         }
	        FXMLLoader loader = new FXMLLoader(
	                getClass().getResource("/clientGUI/personalSpace.fxml")
	        );

	        Parent root = loader.load();
	        Scene scene = new Scene(root);

	        
	        PersonalSpaceController pc = loader.getController();
	        pc.setClient(client);
	        pc.setMainController(this);
	        setActivePersonalSpaceController(pc);
	        pc.onOpen();
	        
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

	@Override
	public void displayMessage(String msg) {
	    Platform.runLater(() -> {
	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle("Message");
	        alert.setHeaderText(null);
	        alert.setContentText(msg);
	        alert.show(); // ✅ NOT showAndWait()
	    });
	}

	@Override
	public void displayReservations(List<Reservation> reservations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleAuthResponse(CustomerAuthResponse resp) {

	    if (resp.isSuccess()) {
	        currentSubscriberId = resp.getSubscriberId();  // ✅ save login state
	    } else {
	        currentSubscriberId = null;
	    }

	    if (activeLoginController != null) {
	        activeLoginController.onAuthResponse(resp);
	        if (resp.isSuccess()) {
	            activeLoginController = null;
	        }
	    }
	    if (activePersonalSpaceController != null) {
	    	activePersonalSpaceController.onAuthResponse(resp);
	    }
	}
	@Override
	public void handleReservationResponse(common.dto.Reservation.ReservationResponse resp) {
	    // Always jump to FX thread before touching any controller UI
	    Platform.runLater(() -> {
	        if (activeReservationController != null) {
	            activeReservationController.onReservationResponse(resp);
	        } else {
	            // fallback if popup isn't open
	            displayMessage(resp.getMessage());
	        }
	        if (activePersonalSpaceController != null) {
	            activePersonalSpaceController.onReservationResponse(resp);
	        }
	    });
	}



}
