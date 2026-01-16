package client;

import ocsf.client.AbstractClient;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import common.dto.Reservation.ReservationRequest;
import common.dto.Reservation.ReservationResponse;
import common.dto.RestaurantManagement.RestaurantManagementRequest;
import common.dto.RestaurantManagement.RestaurantManagementResponse;
import common.dto.UserAccount.UserAccountRequest;
import common.dto.UserAccount.UserAccountResponse;
import common.entity.Reservation;
import common.enums.ReservationOperation;
import common.enums.UserAccountOperation;
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
	private String Subscribername ;

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
        			if(authResp.getOperation() == UserAccountOperation.SUBSCRIBER_LOG_IN) {
        				if(authResp.isSuccess()) { 
        				System.out.println(authResp.getMessage());
        				Subscribername = authResp.getFullName();
        				String StringReslogin = "Welcome Back "+Subscribername+" !";
        				ui.displayMessage(StringReslogin);
        				}else {
            				ui.displayMessage(authResp.getMessage());

        				}
        			}
        			if(authResp.getOperation() == UserAccountOperation.EMPLOYEE_LOG_IN) {
        				String StringResloginemployee = "Welcome Back "+authResp.getFullName()+" !";
        				ui.displayMessage(StringResloginemployee);
        			}
        			
        			
        			if(authResp.getOperation() == UserAccountOperation.LOGOUT) {
        				String StringReslogout = "GoodBye "+Subscribername+" !";
        				Subscribername = null;
        				ui.displayMessage(StringReslogout);
        			}
        			if(authResp.getOperation() == UserAccountOperation.GET_SUBSCRIBER_PROFILE) {
        				System.out.println(authResp.getCustomer().getFullName());
        			}
            ui.handleAuthResponse(authResp);
            return;
        }

        if (msg instanceof ReservationResponse resResp) {
            ui.handleReservationResponse(resResp);
            ui.displayMessage(resResp.getMessage());
            return;
            
        }
        if (msg instanceof RestaurantManagementResponse ) {
        	RestaurantManagementResponse ResManResp = (RestaurantManagementResponse)msg;
            ui.handleRestaurantManagementResponse(ResManResp);
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
        	
        	public void employeeLogInRequest(String Username, String Password) {
        		try {
        			sendToServer(UserAccountRequest.createEmployeeLoginRequest(Username, Password));
        		}catch (IOException e) {
                    ui.displayMessage("Error sending employee login request: " + e.getMessage());
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
        			sendToServer(ReservationRequest.createGetSubscriberHistoryRequest());
        		}catch (IOException e) {
                    ui.displayMessage("Error Checking login Status: " + e.getMessage());
                }
        	}

        	public void requestUpdateCustomerProfile(String name, String phone, String email) {
        		try {
        			sendToServer(UserAccountRequest.createUpdateSubscriberProfileRequest(name, phone, email));
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
    
    //*********************WaitingList*********************
    public void requestWaitingListForGuest(int guests, String fullName, String phone,
			String email) {
    		sendRequest(ReservationRequest.createJoinGuestWaitlistRequest(guests, fullName, phone, email));
    }
    
    public void requestWaitingListForSub(int guests) {
    		sendRequest(ReservationRequest.createJoinWaitlistRequest(guests));
    }

    
    //*********************ReceiveTable*********************
    
	public void requestGetReceivableReservationByConfirmationCode(int code) {
		sendRequest(ReservationRequest.createGetReceivableReservationByConfirmationCodeRequest(code));
	}
	public void requestReceiveTable(int reservationId) {
		sendRequest(ReservationRequest.createReceiveTableRequest(reservationId));
			
		}
	public void ReceivableReservationRequest() {
		sendRequest(ReservationRequest.createGetReceivableReservationsRequest());

	}
	
	
	//*********************CheckOut*********************

	public void requestGetReservationForCheckoutByConfirmationCode(int code) {
		sendRequest(ReservationRequest.createGetPayableReservationByConfirmationCodeRequest(code));

	}

	public void requestGetCustomerReservationsForCheckout() {
		sendRequest(ReservationRequest.createGetPayableReservationsRequest());
		
	}
	
	public void requestGetBillForPaying(int reservationId) {
		sendRequest(ReservationRequest.createGetBillForPayingRequest(reservationId));
		
	}

	public void requestPayBill(Integer billId) {
		sendRequest(ReservationRequest.createPayBillByIdRequest(billId));
		
	}


	//EMPLOYEE DASHBOARD
	public void WaitingListRequest() {
		sendRequest(ReservationRequest.createGetWaitlistRequest());

	}

	public void getAllReservationsRequest() {
		sendRequest(ReservationRequest.createGetActiveReservationsRequest());

	}
	
	public void getCurrentDinersRequest() {
		try {
			sendToServer(UserAccountRequest.createGetCurrentDinersRequest());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void getAllTabelsRequest() {
		try {
			sendToServer(RestaurantManagementRequest.createGetAllTablesRequest());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		}
	
	public void AddTableRequest(int cap) {
		try {
			sendToServer(RestaurantManagementRequest.createAddTableRequest(cap));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void UpdateTableRequest(int tableNumber, int cap) {
		try {
			sendToServer(RestaurantManagementRequest.createUpdateTablerequest(tableNumber, cap));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
	
	public void DeleteTableRequest(int TableNumber) {
		try {
			sendToServer(RestaurantManagementRequest.createDeleteTableRequest(TableNumber));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void LoadOpeningHoursRequest() {
		try {
			sendToServer(RestaurantManagementRequest.createGetOpeningHoursRequest());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public void updateOpeningHours(DayOfWeek day, LocalTime open, LocalTime close, boolean closed) {
	    try {
	        sendToServer(RestaurantManagementRequest.createUpdateOpeningHoursRequest(day, open, close, closed));
	    } catch (IOException e) {
	        e.printStackTrace();
	         ui.displayMessage("Error updating opening hours: " + e.getMessage());
	    }
	}
		
	public void GetAllSubscribersRequest() {
		try {
			sendToServer(UserAccountRequest.createGetAllSubscribersRequest());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void lookupBySubscriptionCodeRequest(String value) {
		try {
			sendToServer(UserAccountRequest.createLookupCustomerBySubscriptionCodeRequest(value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public void lookupByPhoneRequest(String value) {
		try {
			sendToServer(UserAccountRequest.createLookupCustomerByPhoneRequest(value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public void lookupByEmailRequest(String value) {
		try {
			sendToServer(UserAccountRequest.createLookupCustomerByEmailRequest(value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}

	public void createCreateReservationOnBehalfRequest(int customerId, LocalDateTime dt, int guests) {
		sendRequest(ReservationRequest.createCreateReservationOnBehalfRequest(customerId, dt, guests));

	}
	public void createJoinWaitlistOnBehalfRequest(int customerId, int guests) {
		sendRequest(ReservationRequest.createJoinWaitlistOnBehalfRequest(customerId, guests));		
	}
	public void createGetCancellableReservationsOnBehalfRequest(int customerId) {
		sendRequest(ReservationRequest.createGetCancellableReservationsOnBehalfRequest(customerId));		
		
	}
	public void createCancelReservationOnBehalfRequest(int customerId, int reservationId) {
		sendRequest(ReservationRequest.createCancelReservationOnBehalfRequest(customerId, reservationId));		
		
	}
	public void createGetReceivableReservationsOnBehalfRequest(int customerId) {
		sendRequest(ReservationRequest.createGetReceivableReservationsOnBehalfRequest(customerId));		
		
	}
	public void createReceiveTableOnBehalfRequest(int customerId, int reservationId) {
		sendRequest(ReservationRequest.createReceiveTableOnBehalfRequest(customerId, reservationId));		
		
	}
	
	public void createGetPayableReservationsOnBehalfRequest(int customerId) {
		sendRequest(ReservationRequest.createGetPayableReservationsOnBehalfRequest(customerId));		
		
	}
	
	public void createGetBillForPayingOnBehalfRequest(int customerId, int reservationId) {
		sendRequest(ReservationRequest.createGetBillForPayingOnBehalfRequest(customerId, reservationId));		
		
	}
	public void createPayBillOnBehalfRequest(int customerId, Integer loadedBillId) {
		sendRequest(ReservationRequest.createPayBillOnBehalfRequest(customerId, loadedBillId));		
		
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
