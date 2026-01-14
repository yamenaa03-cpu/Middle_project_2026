package clientGUI;

import java.io.IOException;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class BistroKioskController {
	private boolean keyboardOpened = false;
	private Client client;
	private ClientController clientController;
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	public void setMainController(ClientController clientController) {
		this.clientController = clientController;
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
        // TODO: implement
    }

    @FXML
    private void onJoinWaitingList(ActionEvent event) {
        // TODO: implement
    }

    @FXML
    private void onPayment(ActionEvent event) {
        // TODO: implement
    }

    @FXML
    private void onCancelReservation(ActionEvent event) {
        if (clientController != null) {
        		clientController.onDeleteReservation(event); // delegate
        }
    }





}
