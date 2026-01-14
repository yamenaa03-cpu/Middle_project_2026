package client;

import ocsf.client.AbstractClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import common.dto.Reservation.ReservationRequest;
import common.dto.Reservation.ReservationResponse;
import common.dto.UserAccount.UserAccountRequest;
import common.dto.UserAccount.UserAccountResponse;
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
    private boolean loggedin;

    public Client(String host, int port, ClientUI ui) {
        super(host, port);
        this.ui = ui;
    }
    
    public boolean getLoginStatus() {
    		return loggedin;
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

        if (msg instanceof UserAccountResponse ) {
        	UserAccountResponse authResp = (UserAccountResponse)msg;

        			String StringRes = authResp.getMessage();
        			ui.displayMessage(StringRes);
        			
            ui.handleAuthResponse(authResp);
            return;
        }

        if (msg instanceof ReservationResponse resResp) {
            ui.handleReservationResponse(resResp);   // ✅ THIS is the missing link
            return;
            
        }

        ui.displayMessage("Unknown message from server: " + msg);
    }
    // *********************AUTHINTICATION*********************
    //request login for the subscriber
    public void requestLoginBySubscriptionCode(String code) {
        try {
            sendToServer(UserAccountRequest.createSubscriberLogInRequest(code));
        } catch (IOException e) {
            ui.displayMessage("Error sending login request: " + e.getMessage());
        }
    }
    
    
    public void requestLoggedInStatus() {
    		try {
    			sendToServer(UserAccountRequest.createLoggedInStatusRequest());
    		}catch (IOException e) {
                ui.displayMessage("Error Checking login Status: " + e.getMessage());
            }
    }
    		
        	public void requestLogout() {
        		try {
        			sendToServer(UserAccountRequest.createLogoutRequest());
        		}catch (IOException e) {
                    ui.displayMessage("Error sending logOut request: " + e.getMessage());
                }
    	
        	}
        	
    //*********************CostumerProfile*********************
        	

        	public void requestCustomerProfile() {
        		try {
        			sendToServer(UserAccountRequest.createGetSubscriberProfileRequest());
        		}catch (IOException e) {
                    ui.displayMessage("Error Checking login Status: " + e.getMessage());
                }
        		
        	}
        	
        	public void requestCustomerReservations() {
        		try {
        			sendToServer(ReservationRequest);
        		}catch (IOException e) {
                    ui.displayMessage("Error Checking login Status: " + e.getMessage());
                }
        	}

        	public void requestUpdateCustomerProfile(String name, String phone, String email) {
        		try {
        			sendToServer(UserAccountRequest);
        		}catch (IOException e) {
                    ui.displayMessage("Error Checking login Status: " + e.getMessage());
                }
        		        		
        	}
        	
        	public void requestRegisterSubscriber(String fullName, String phone, String email) {
        		try {
        			sendToServer(UserAccountRequest.createRegisterSubscriberRequest(fullName, phone, email));
        		}catch (IOException e) {
                    ui.displayMessage("Error Checking login Status: " + e.getMessage());
                }
        	}

    //*************************************************************
    
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
    
	public void requestCreateGuestReservation(LocalDateTime reservationDateTime, int guestCount, String name,
			String phone, String email) {
        ReservationRequest req = ReservationRequest.createCreateGuestReservationRequest(reservationDateTime, guestCount, name, phone, email);
        sendRequest(req);
		
	}
    /**
     * Sends a request (to the sever) to Get the Cancellable reservations for a guest accourding to confirmation code.
     *
     * @param code The Confirmation code

     */
	public void requestGetCancellableReservationByConfirmationCode(int code) {
		ReservationRequest req = ReservationRequest.createGetCancellableReservationByConfirmationCodeRequest(code);
		sendRequest(req);
	}
	public void requstGetCancellableReservationsRequest() {
		ReservationRequest req = ReservationRequest.createGetCancellableReservationsRequest();
		sendRequest(req);		
	}
	
	public void requestResendConfirmationCodeRequest(String phone, String email) {
		ReservationRequest req = ReservationRequest.createResendConfirmationCodeRequest(phone, email);
		sendRequest(req);
	}
    public void requestCancelReservation(int reservationId) {
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
