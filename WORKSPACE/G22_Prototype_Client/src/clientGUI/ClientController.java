
package clientGUI;

import client.Client;
import client.ClientUI;
import common.dto.Reservation.ReservationResponse;
import common.dto.UserAccount.UserAccountResponse;
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
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
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
    private CancelReservationController ActiveCancelReservationGuestController;
    private CancelReservationConfirmController ActiveCancelReservationConfirmController;
    private BistroKioskController ActiveBistroKioskController;
    private RegisterSubscriberController ActiveRegisterSubscriberController;
    private boolean waitingSubscriberReservationsForCancel = false;


    @FXML private Button SignInButton;
    @FXML private Button logoutBtn;

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
    
    public void setActiveCancelReservationGuestController(CancelReservationController Crc) {
        this.ActiveCancelReservationGuestController = Crc;
    }
    
    public void setActiveCancelReservationConfirmController(CancelReservationConfirmController Crcc) {
        this.ActiveCancelReservationConfirmController = Crcc;
    }
    
    public void setActiveBistroKioskController(BistroKioskController Kctr) {
    		this.ActiveBistroKioskController = Kctr;
    }
    
    public void setActiveRegisterSubscriberController(RegisterSubscriberController RSctr) {
		this.ActiveRegisterSubscriberController = RSctr;
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
    public void onDeleteReservation(ActionEvent event) {

        if (client == null || !client.isConnected()) {
            displayMessage("Not connected to server.");
            return;
        }

        if (isLoggedIn()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/CancelReservationFinalConfir.fxml"));
                Parent popupRoot = loader.load();

                CancelReservationConfirmController confirmCtrl = loader.getController();
                confirmCtrl.setClient(client);
                confirmCtrl.setMainController(this);
                setActiveCancelReservationConfirmController(confirmCtrl);

                Stage popupStage = new Stage();
                popupStage.setTitle("Cancel Reservation");
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
                popupStage.setScene(new Scene(popupRoot));
                popupStage.setResizable(false);

                waitingSubscriberReservationsForCancel = true;
                client.requstGetCancellableReservationsRequest();

                popupStage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
            }
        

        } else {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/CancelReservationGuest.fxml"));
                Parent popupRoot = loader.load();

                CancelReservationController guestCtrl = loader.getController();
                guestCtrl.setClient(client);
                guestCtrl.setMainController(this);
                setActiveCancelReservationGuestController(guestCtrl);

                Stage popupStage = new Stage();
                popupStage.setTitle("Cancel Reservation");
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
    }

    @FXML
    public void onRegisterSubscriber(ActionEvent event) {
        try {
            MenuItem item = (MenuItem) event.getSource();
            Window owner = item.getParentPopup().getOwnerWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/RegisterSubscriber.fxml"));
            Parent root = loader.load();
            
            RegisterSubscriberController RSCtrl = loader.getController();
            RSCtrl.setClient(client);
            RSCtrl.setMainController(this);
            setActiveRegisterSubscriberController(RSCtrl);
            
            
            Stage popupStage = new Stage();
            popupStage.initOwner(owner);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (Exception e) {
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
    @FXML
    public void onLogout() {
    		client.requestLogout();

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
    @FXML
    public void onOpenKiosk() {
    	 try {
  	        FXMLLoader loader = new FXMLLoader(
  	                getClass().getResource("/clientGUI/BistroKiosk.fxml")
  	        );
  	        Parent root = loader.load();
  	        Scene scene = new Scene(root);
  	        
  	        
            BistroKioskController KCtrl = loader.getController();
            KCtrl.setClient(client);
            KCtrl.setMainController(this);
            setActiveBistroKioskController(KCtrl);
            
            



  	        Stage popupStage = new Stage();
  	        popupStage.setTitle("Bistro – Kiosk");
  	        popupStage.initModality(Modality.APPLICATION_MODAL);
  	        popupStage.initOwner(SignInButton.getScene().getWindow());
  	        popupStage.setResizable(true);
  	        popupStage.setScene(scene);

  	        popupStage.showAndWait();
  	        System.out.println("Kisok Opened");

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
	public void handleAuthResponse(UserAccountResponse resp) {

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
	    	//activePersonalSpaceController.onAuthResponse(resp);
	    }
	}
	@Override
	public void handleReservationResponse(ReservationResponse resp) {
	    Platform.runLater(() -> {
	    		
	        // 1) Subscriber cancel flow: load list into table
	        if (waitingSubscriberReservationsForCancel) {
	            waitingSubscriberReservationsForCancel = false;

	            if (ActiveCancelReservationConfirmController != null) {
	                ActiveCancelReservationConfirmController.setReservations(resp.getReservations());
	            } else {
	                displayMessage("Cancel page is not open.");
	            }
	            return;
	        }

	        // 2) Guest flow (lookup by confirmation code)
	        if (ActiveCancelReservationGuestController != null) {
	            ActiveCancelReservationGuestController.onReservationResponse(resp);
	        }

	        // 3) Reservation page
	        if (activeReservationController != null) {
	            activeReservationController.onReservationResponse(resp);
	        }

	        // 4) Personal space
	        if (activePersonalSpaceController != null) {
	            activePersonalSpaceController.onReservationResponse(resp);
	        }

	        // 5) If confirm page is open and it sent cancel request
	        if (ActiveCancelReservationConfirmController != null) {
	            ActiveCancelReservationConfirmController.onReservationResponse(resp);
	        }
	    });
	}

	private Reservation pickCancellable(List<Reservation> list) {
	    if (list == null) return null;

	    for (Reservation r : list) {
	        if (r == null || r.getStatus() == null) continue;

	        switch (r.getStatus()) {
	            case ACTIVE:
	            case WAITING:
	            case NOTIFIED:
	            case IN_PROGRESS:
	                return r;
	            default:
	                break;
	        }
	    }
	    return null;
	}





}
