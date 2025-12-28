package common.entity;

import java.io.Serializable;
import java.time.LocalDate;

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
	
    private int ReservationNumber;
    private LocalDate newReservationDate;
    private int newNumberOfGuests;
    
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
            LocalDate newDate,
            int newGuests) {
    	
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.UPDATE_RESERVATION_FIELDS;
		req.ReservationNumber = ReservationNumber;
		req.newReservationDate = newDate;
		req.newNumberOfGuests = newGuests;
		return req;
    }
    
   // private ReservationRequest() {}
    //Getters for the class fields
    public ReservationOperation getOperation() { return operation; }
    public int getReservationNumber() { return ReservationNumber; }
    public LocalDate getNewReservationDate() { return newReservationDate; }
    public int getNewNumberOfGuests() { return newNumberOfGuests; }
    
}
