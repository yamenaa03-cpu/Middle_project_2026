package common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import common.entity.Reservation;

public class ReservationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<Reservation> reservations;  
    private int reservationId;
	private int confirmationCode;

	private List<LocalDateTime> suggestedTimes;

    public ReservationResponse(boolean success, String message, List<Reservation> reservations) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
    }
    
    public ReservationResponse(boolean success, String message, List<Reservation> reservations, int createdReservationId) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
        this.reservationId = createdReservationId;
    }
    
    // CREATE
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

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Reservation> getReservations() { return reservations; }
    public int getReservationId() { return reservationId; }
    public int getConfirmationCode() { return confirmationCode; }
    public List<LocalDateTime> getSuggestedTimes() { return suggestedTimes; }
}