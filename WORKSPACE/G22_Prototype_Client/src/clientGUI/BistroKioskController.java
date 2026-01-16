package clientGUI;

import java.io.IOException;

import client.Client;
import common.dto.Reservation.ReservationResponse;
import common.enums.ReservationOperation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BistroKioskController {
	private boolean keyboardOpened = false;
	private Client client;
	private ClientController clientController;
	private JoinWaitlistController JWLC;
	private ReceiveTableController RTC;
	private ReceiveTableConfirmationController rtcC;
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	public void setMainController(ClientController clientController) {
		this.clientController = clientController;
	}
	public ClientController getMainController() {
		return clientController;
	}
	public void setReceiveTableController (ReceiveTableController RTC) {
		this.RTC=RTC;
	}
	public void setJoinWaitlistController (JoinWaitlistController Jwlc) {
		this.JWLC=Jwlc;
	} 
	public void setReceiveTableConfController(ReceiveTableConfirmationController rtcC) {
		this.rtcC = rtcC;
	}
	
    private void openKeyboard() {
        try {
            new ProcessBuilder("cmd", "/c", "start", "osk").start(); // Windows OSK
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openKeyboardOnce() {
        openKeyboard();
    }

	
	
    @FXML
    private void onSignIn(ActionEvent event) {
    		openKeyboardOnce();
        if (clientController != null) {
            clientController.onSignIn(); 
        }
        
    }

    @FXML
    private void onReceiveTable(ActionEvent event) {
		if(clientController.isLoggedIn()) {
	    	 try {
	    	        FXMLLoader loader = new FXMLLoader(
	    	                getClass().getResource("/clientGUI/ReceiveTableConfirmation.fxml")
	    	        );

	    	        Parent root = loader.load();
	    	        Scene scene = new Scene(root);
	    	        
	    	        ReceiveTableConfirmationController rtcc = loader.getController();
	    	        setReceiveTableConfController(rtcc);
	    	        
	    	        rtcc.setClient(client);
	    	        rtcc.setMainController(this.getMainController());
	    	        
	    	        Stage popupStage = new Stage();
	    	        popupStage.setTitle("Bistro – Login");
	    	        popupStage.initModality(Modality.APPLICATION_MODAL);
	    	        popupStage.setResizable(false);
	    	        popupStage.setScene(scene);

	    	        clientController.getClient().ReceivableReservationRequest();

	    	        popupStage.showAndWait();
	    	        
	    	        System.out.println("Join Wait list clicked");

	    	    } catch (Exception e) {
	    	        e.printStackTrace();
	    	    }
		}else {
	    	 try {
   	        FXMLLoader loader = new FXMLLoader(
   	                getClass().getResource("/clientGUI/ReceiveTableGuest.fxml")
   	        );

   	        Parent root = loader.load();
   	        Scene scene = new Scene(root);

	        ReceiveTableController RTC = loader.getController();
	        setReceiveTableController(RTC);
	        
	        RTC.setClient(client);
	        RTC.setActiveBistroKioskController(this);

   	        Stage popupStage = new Stage();
   	        popupStage.setTitle("Bistro – Login");
   	        popupStage.initModality(Modality.APPLICATION_MODAL);
   	        popupStage.setResizable(false);
   	        popupStage.setScene(scene);

   	     
   	        popupStage.showAndWait();
   	        System.out.println("Join Wait list clicked");

   	    } catch (Exception e) {
   	        e.printStackTrace();
   	    }
		}
    }

    @FXML
    private void onJoinWaitingList(ActionEvent event) {
    		if(clientController.isLoggedIn()) {
    	    	 try {
    	    	        FXMLLoader loader = new FXMLLoader(
    	    	                getClass().getResource("/clientGUI/JoinWaitListSubs.fxml")
    	    	        );

    	    	        Parent root = loader.load();
    	    	        Scene scene = new Scene(root);
    	    	        
    	    	        JoinWaitlistController JWLC = loader.getController();
    	    	        JWLC.setActiveBistroKioskController(this);
    	    	        setJoinWaitlistController(JWLC);
    	    	        
    	    	        JWLC.setClient(client);
    	    	        JWLC.setActiveBistroKioskController(this);
    	    	        
    	    	        Stage popupStage = new Stage();
    	    	        popupStage.setTitle("Bistro – Login");
    	    	        popupStage.initModality(Modality.APPLICATION_MODAL);
    	    	        popupStage.setResizable(false);
    	    	        popupStage.setScene(scene);

    	    	     
    	    	        popupStage.showAndWait();
    	    	        
    	    	        setJoinWaitlistController(null); 
    	    	        System.out.println("Join Wait list as guest clicked");
    	    	        
    	    	    } catch (Exception e) {
    	    	        e.printStackTrace();
    	    	    }
    		}else {
   	    	 try {
	    	        FXMLLoader loader = new FXMLLoader(
	    	                getClass().getResource("/clientGUI/joinWaitListGuest.fxml")
	    	        );

	    	        Parent root = loader.load();
	    	        Scene scene = new Scene(root);

	    	        JoinWaitlistController JWLC = loader.getController();
	    	        JWLC.setActiveBistroKioskController(this);
	    	        setJoinWaitlistController(JWLC);
	    	        JWLC.setClient(client);
	    	        JWLC.setActiveBistroKioskController(this);

	    	        Stage popupStage = new Stage();
	    	        popupStage.setTitle("Bistro – Login");
	    	        popupStage.initModality(Modality.APPLICATION_MODAL);
	    	        popupStage.setResizable(false);
	    	        popupStage.setScene(scene);

	    	     
	    	        popupStage.showAndWait();
	    	        setJoinWaitlistController(null); 
	    	        System.out.println("Join Wait list clicked");

	    	    } catch (Exception e) {
	    	        e.printStackTrace();
	    	    }
    		}
    
    }

    @FXML
    private void onPayment(ActionEvent event) {
        if (clientController != null) {
    		clientController.onCheckOut(event); // delegate
    }
}
    

    @FXML
    private void onCancelReservation(ActionEvent event) {
        if (clientController != null) {
        		clientController.onDeleteReservation(event); // delegate
        }
    }

    public void onReservationResponse(ReservationResponse resp) {

        if (resp.getOperation() == ReservationOperation.JOIN_WAITLIST) {
            if (JWLC != null) JWLC.onReservationResponse(resp);
            return;
        }

        // when server returns list for subscriber
        if (resp.getOperation() == ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_RECEIVING) {
            if (RTC != null) RTC.onReservationResponse(resp);   // ✅ guest flow opens confirm screen
            if (rtcC != null) rtcC.setReservations(resp.getReservations()); // optional
            return;

            
        }

        // when server responds to the actual "mark seated"
        if (resp.getOperation() == ReservationOperation.RECEIVE_TABLE) {
            if (rtcC != null) rtcC.onReservationResponse(resp);
            if (RTC != null) RTC.onReservationResponse(resp); // guest code flow
        }
        if (resp.getOperation() == ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING) {
            if (rtcC != null) rtcC.setReservations(resp.getReservations());
            return;
        }
    }



	}
    
    






