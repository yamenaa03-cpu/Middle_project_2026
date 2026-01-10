package common.dto.Reservation;

import java.io.Serializable;

import common.enums.ReservationStatus;

public class CancelReservationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private ReservationStatus before;
    
    private CancelReservationResult(boolean success, String message, ReservationStatus before) {
        this.success = success;
        this.message = message;
        this.before = before;
    }

    private CancelReservationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static CancelReservationResult ok(String msg, ReservationStatus before) {
        return new CancelReservationResult(true, msg, before);
    }

    public static CancelReservationResult fail(String msg) {
        return new CancelReservationResult(false, msg);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public ReservationStatus getReservationStatusBefore() { return before; }
}

