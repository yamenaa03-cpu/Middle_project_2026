package client;

import ocsf.client.AbstractClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import common.dto.Authentication.CustomerAuthRequest;
import common.dto.Authentication.CustomerAuthResponse;
import common.dto.Reservation.ReservationRequest;
import common.dto.Reservation.ReservationResponse;
import common.entity.Reservation;
/**
 * The Client class extends the OCSF AbstractClient framework.
 * It is responsible for:
 *  - Connecting to the server
 *  - Sending requests (ReservationRequest objects)
 *  - Receiving responses from the server (ReservationResponse)
 *  - Forwarding results/messages to the GUI through ClientUI
 *@version 1.0
 */

public class Client extends AbstractClient {

    private ClientUI ui;

    public Client(String host, int port, ClientUI ui) {
        super(host, port);
        this.ui = ui;
    }
    /**
     * This method automatically runs whenever the server sends a message.
     * It handles responses from the server as an instance of ReservationResponse object
     * 
     *
     * @param msg The object received from the server
     * 
     */
    @Override
    protected void handleMessageFromServer(Object msg) {

        if (msg instanceof CustomerAuthResponse authResp) {
        		ui.displayMessage(authResp.getMessage());
            ui.handleAuthResponse(authResp);
            return;
        }

        if (msg instanceof ReservationResponse resResp) {
            ui.handleReservationResponse(resResp);   // ✅ THIS is the missing link
            return;
            
        }

        ui.displayMessage("Unknown message from server: " + msg);
    }
    
    public void requestLoginBySubscriptionCode(String code) {
        try {
            sendToServer(common.dto.Authentication.CustomerAuthRequest.subscription(code));
        } catch (IOException e) {
            ui.displayMessage("Error sending login request: " + e.getMessage());
        }
    }
    
    /**
     * Sends a request to retrieve all reservations from the database.
     */
    public void requestAllReservations() {
        ReservationRequest req = ReservationRequest.createGetAllReservationsRequest();
        sendRequest(req);
    }
    
    /**
     * Sends a request (to the sever) to update an Reservation in the database.
     *
     * @param reservationId The reservation ID to update
     * @param newDate     The new date to set
     * @param newGuests   The new guest count
     */
    
    public void requestUpdateReservation(int reservationId, LocalDateTime newDateTime, int newGuests) {
        ReservationRequest req = ReservationRequest.createUpdateReservationRequest(reservationId, newDateTime, newGuests);
        sendRequest(req);
    }
    
    /**
     * Sends a request (to the sever) to update an Reservation in the database.
     *
     * @param reservationId The reservation ID to update
     * @param newDate     The new date to set
     * @param newGuests   The new guest count
     */
    
    public void requestCreateReservation(int customerId, LocalDateTime dateTime, int guests) {
        ReservationRequest req = ReservationRequest.createCreateReservationRequest(dateTime, guests);
        sendRequest(req);
    }
    
    public void requestCreateGuestReservation(LocalDateTime dateTime, int guests,
            String fullName, String phone, String email) {
		ReservationRequest req =
		ReservationRequest.createGuestCreateReservationRequest(dateTime, guests, fullName, phone, email);
		sendRequest(req);
	}
    
    public void requestCustomerProfile() {
        try {
            sendToServer(CustomerAuthRequest.getProfile());
        } catch (IOException e) {
            ui.displayMessage("Error: " + e.getMessage());
        }
    }

    public void requestUpdateCustomerProfile(String fullName, String phone, String email) {
        try {
            sendToServer(CustomerAuthRequest.updateProfile(fullName, phone, email));
        } catch (IOException e) {
            ui.displayMessage("Error: " + e.getMessage());
        }
    }

    
   
    public void requestCustomerReservations() { sendRequest(ReservationRequest.createGetCustomerReservationsRequest()); }
 
    public void requestCancelReservation(int reservationId, int confirmationCode) {
        sendRequest(ReservationRequest.createCancelReservationRequest(reservationId));
    }
    public void requestCheckout(int confirmationCode) {
        sendRequest(ReservationRequest.createPayBillRequest(confirmationCode));
    }


    /**
     * sends a request object to the server.
     * catches any Exception while doing so
     *
     * @param req The ReservationRequest object to send
     */
    
    private void sendRequest(ReservationRequest req) {
        try {
            sendToServer(req);
        } catch (IOException e) {
            ui.displayMessage("Error sending request: " + e.getMessage());
        }
    }

}
