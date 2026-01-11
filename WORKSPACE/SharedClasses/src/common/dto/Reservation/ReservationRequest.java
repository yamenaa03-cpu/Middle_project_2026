package common.dto.Reservation;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.enums.ReservationOperation;

/**
 * Message object sent from client to server describing a reservation-related
 * operation and relevant parameters. Factory methods are provided for common
 * request types.
 */
public class ReservationRequest implements Serializable{
    
 	 private static final long serialVersionUID = 1L;
    
    private ReservationOperation operation;
    
    private int reservationId;
    private LocalDateTime reservationDateTime;
    private int numberOfGuests;
    
    // Guest identification fields (for CREATE_GUEST_RESERVATION / JOIN_WAITLIST, etc.)
    private String fullName;
    private String phone;
    private String email;
    private int confirmationCode;

    /**
     * Create a request asking for all reservations.
     *
     * @return configured ReservationRequest
     */
    public static ReservationRequest createGetAllReservationsRequest() {
        ReservationRequest req = new ReservationRequest();
        req.operation = ReservationOperation.GET_ALL_RESERVATIONS;
        return req;
    }
    
    /**
     * Create an update request for an existing reservation.
     *
     * @param ReservationNumber reservation id to update
     * @param newDateTime new date/time value
     * @param newGuests new guest count
     * @return configured ReservationRequest
     */
    public static ReservationRequest createUpdateReservationRequest(int ReservationNumber,
            LocalDateTime newDateTime,
            int newGuests) {
    	
    	ReservationRequest req = new ReservationRequest();
    	req.operation = ReservationOperation.UPDATE_RESERVATION_FIELDS;
    	req.reservationId = ReservationNumber;
    	req.reservationDateTime = newDateTime;
    	req.numberOfGuests = newGuests;
    	return req;
    }
    
    /**
     * Create a request for obtaining the current customer's reservations.
     *
     * @return configured ReservationRequest
     */
    public static ReservationRequest createGetCustomerReservationsRequest() {
        ReservationRequest r = new ReservationRequest();
        r.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS;
        return r;
    }

    /**
     * Create a request to create a reservation for the logged-in customer.
     *
     * @param dateTime desired reservation time
     * @param guests number of guests
     * @return configured ReservationRequest
     */
    public static ReservationRequest createCreateReservationRequest(
    		LocalDateTime dateTime, int guests) {

        ReservationRequest req = new ReservationRequest();
        req.operation = ReservationOperation.CREATE_RESERVATION;
        req.reservationDateTime = dateTime;
        req.numberOfGuests = guests;
        return req;
    }
    
    /**
     * Create a reservation request for a guest (non-logged-in) user.
     */
    public static ReservationRequest createGuestCreateReservationRequest(
            LocalDateTime dateTime, int guests,
            String fullName, String phone, String email) {

        ReservationRequest req = new ReservationRequest();
        req.operation = ReservationOperation.CREATE_RESERVATION;
        req.reservationDateTime = dateTime;
        req.numberOfGuests = guests;
        req.fullName = fullName;
        req.phone = phone;
        req.email = email;
        return req;
    }

    /**
     * Create a cancel reservation request for the given reservation id.
     *
     * @param reservationId id to cancel
     * @return configured ReservationRequest
     */
    public static ReservationRequest createCancelReservationRequest(int reservationId) {
        ReservationRequest req = new ReservationRequest();
        req.operation = ReservationOperation.CANCEL_RESERVATION;
        req.reservationId = reservationId;
        return req;
    }

    /**
     * Create a request to mark a table as received using the confirmation code.
     *
     * @param confirmationCode numeric confirmation provided to customer
     * @return configured ReservationRequest
     */
    public static ReservationRequest createReceiveTableRequest(int confirmationCode) {
        ReservationRequest req = new ReservationRequest();
        req.operation = ReservationOperation.RECEIVE_TABLE;
        req.confirmationCode = confirmationCode;
        return req;
    }
    
    /**
     * Create a billing/checkout request using the confirmation code.
     *
     * @param confirmationCode numeric confirmation provided to customer
     * @return configured ReservationRequest
     */
    public static ReservationRequest createPayBillRequest(int confirmationCode) {
        ReservationRequest req = new ReservationRequest();
        req.operation = ReservationOperation.CHECKOUT;
        req.confirmationCode = confirmationCode;
        return req;
    }
    
   // private ReservationRequest() {}
    //Getters for the class fields
    /**
     * Returns the requested operation.
     *
     * @return reservation operation
     */
    public ReservationOperation getOperation() { return operation; }

    /**
     * Returns reservation id when applicable.
     *
     * @return reservation id
     */
    public int getReservationId() { return reservationId; }

    /**
     * Returns the requested reservation date/time when applicable.
     *
     * @return reservation date/time
     */
    public LocalDateTime getReservationDateTime() { return reservationDateTime; }

    /**
     * Returns the number of guests when applicable.
     *
     * @return guest count
     */
    public int getNumberOfGuests() { return numberOfGuests; }
    
    // Guest getters (needed by CREATE_GUEST_RESERVATION case)
    /** Returns guest full name. */
    public String getFullName() { return fullName; }
    /** Returns guest phone. */
    public String getPhone() { return phone; }
    /** Returns guest email. */
    public String getEmail() { return email; }
    /** Returns confirmation code when present. */
    public int getConfirmationCode() { return confirmationCode; }

}
