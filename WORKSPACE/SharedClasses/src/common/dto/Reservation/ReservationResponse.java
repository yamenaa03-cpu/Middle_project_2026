package common.dto.Reservation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import common.entity.Bill;
import common.entity.Reservation;

/**
 * Response wrapper for reservation-related operations. Carries a success flag,
 * message and optional payload fields depending on the operation performed.
 */
public class ReservationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<Reservation> reservations;  
    private int reservationId;
	private int confirmationCode;
	private Double finalAmount;
	private Bill bill;

	
	private List<LocalDateTime> suggestedTimes;

	/**
	 * Basic response carrying success and message.
	 *
	 * @param success whether the operation succeeded
	 * @param message human-readable message
	 */
	public ReservationResponse(boolean success, String message) {
	    this.success = success;
	    this.message = message;
	    this.reservations = null;
	}


    /**
     * Response including a list of reservations.
     *
     * @param success whether the operation succeeded
     * @param message message describing the result
     * @param reservations list of reservations returned
     */
    public ReservationResponse(boolean success, String message, List<Reservation> reservations) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
    }
    
    /**
     * Response including reservations and the id of a created reservation.
     *
     * @param success whether the operation succeeded
     * @param message message describing the result
     * @param reservations list of reservations returned
     * @param createdReservationId created reservation id
     */
    public ReservationResponse(boolean success, String message, List<Reservation> reservations, int createdReservationId) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
        this.reservationId = createdReservationId;
    }
    
    /**
     * Response used for create operations that may include suggestions.
     *
     * @param success whether the operation succeeded
     * @param message message describing the result
     * @param reservationId created reservation id (nullable)
     * @param confirmationCode confirmation code (nullable)
     * @param suggestedTimes optional alternative time suggestions
     */
    public ReservationResponse(boolean success, String message,
                               Integer reservationId,
                               Integer confirmationCode,
                               List<LocalDateTime> suggestedTimes) {
        this.success = success;
        this.message = message;
        this.reservationId = reservationId;
        this.confirmationCode = confirmationCode;
        this.suggestedTimes = suggestedTimes;
    }

    public ReservationResponse(boolean success, String message, Bill bill) {
        this.success = success;
        this.message = message;
        this.bill = bill;
    }

    
    /**
     * Whether the operation succeeded.
     *
     * @return true when successful
     */
    public boolean isSuccess() { return success; }

    /**
     * Human readable message describing the response.
     *
     * @return message string
     */
    public String getMessage() { return message; }

    /**
     * Returns reservations payload when present.
     *
     * @return list of reservations or null
     */
    public List<Reservation> getReservations() { return reservations; }

    /**
     * Returns the reservation id included in the response when applicable.
     *
     * @return reservation id
     */
    public int getReservationId() { return reservationId; }

    /**
     * Returns the confirmation code included in create responses.
     *
     * @return confirmation code
     */
    public int getConfirmationCode() { return confirmationCode; }

    /**
     * Returns suggested alternative times when applicable.
     *
     * @return list of suggested LocalDateTime values
     */
    public List<LocalDateTime> getSuggestedTimes() { return suggestedTimes; }

    /**
     * Returns final amount related to billing flows.
     *
     * @return final amount or null
     */
    public Double getFinalAmount() { return finalAmount; }
    
    public Bill getBill() { return bill; }

}