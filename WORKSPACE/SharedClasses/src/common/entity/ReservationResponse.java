package common.entity;

import java.io.Serializable;
import java.util.List;

public class ReservationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<Reservation> reservations;  

    public ReservationResponse(boolean success, String message, List<Reservation> reservations) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Reservation> getReservations() { return reservations; }
}