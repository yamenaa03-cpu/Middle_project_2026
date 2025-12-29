package common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.enums.ReservationOperation;

/**
 * A message sent from the client to the server containing
 * the requested operation and relevant parameters.
 * Shared between both projects (client & server).
 * @author: Yamen Abu Ahmad
 * @version 1.0
 */

/*This class */
public class ReservationRequest implements Serializable{
	
	 private static final long serialVersionUID = 1L;
	
    private ReservationOperation operation;
	
    private int reservationId;
    private LocalDateTime reservationDate;
    private int numberOfGuests;
    private int customerId; 
    
    /*identify the request as an instance of Reservation request class and saves the GET_ALL_RESERVATIONS operation
     * as a field in the class
    */
    public static ReservationRequest createGetAllReservationsRequest() {
        ReservationRequest req = new ReservationRequest();
        req.operation = ReservationOperation.GET_ALL_RESERVATIONS;
        return req;
    }
    
    /*identify the request as an instance of Reservation request class and saves the UPDATE_RESERVATION_FIELDS operation
      as a field in the class*/
    public static ReservationRequest createUpdateReservationRequest(int ReservationNumber,
            LocalDateTime newDate,
            int newGuests) {
    	
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.UPDATE_RESERVATION_FIELDS;
		req.reservationId = ReservationNumber;
		req.reservationDate = newDate;
		req.numberOfGuests = newGuests;
		return req;
    }
    
    /*identify the request as an instance of Reservation request class and saves the CREATE_RESERVATION_FIELDS operation
    as a field in the class*/
    public static ReservationRequest createCreateReservationRequest(
            int customerId, LocalDateTime date, int guests) {

        ReservationRequest req = new ReservationRequest();
        req.operation = ReservationOperation.CREATE_RESERVATION;
        req.customerId = customerId;
        req.reservationDate = date;
        req.numberOfGuests = guests;
        return req;
    }
    
   // private ReservationRequest() {}
    //Getters for the class fields
    public ReservationOperation getOperation() { return operation; }
    public int getReservationId() { return reservationId; }
    public LocalDateTime getReservationDate() { return reservationDate; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public int getCustomerId() { return customerId; }
}
