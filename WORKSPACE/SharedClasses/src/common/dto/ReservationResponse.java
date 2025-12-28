package common.dto;

import java.io.Serializable;
import java.util.List;

import common.entity.Reservation;

public class ReservationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<Reservation> reservations;  
    private Integer createdReservationId;

    public ReservationResponse(boolean success, String message, List<Reservation> reservations) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
    }
    
    public ReservationResponse(boolean success, String message, List<Reservation> reservations, int createdReservationId) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
        this.createdReservationId = createdReservationId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Reservation> getReservations() { return reservations; }
    public int getCreatedReservationId() { return createdReservationId; }
}