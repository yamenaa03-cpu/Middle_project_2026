package common.dto.Reservation;

import java.io.Serializable;

import common.enums.ReservationStatus;

/**
 * Result object returned after attempting to cancel a reservation. Contains
 * success flag, human-readable message and the previous reservation status
 * when available.
 */
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

    /**
     * Returns whether the cancellation succeeded.
     *
     * @return true if cancelled successfully
     */
    public boolean isSuccess() { return success; }

    /**
     * Human readable message describing the result.
     *
     * @return descriptive message
     */
    public String getMessage() { return message; }

    /**
     * Returns the reservation status before cancellation, if available.
     *
     * @return previous reservation status or null
     */
    public ReservationStatus getReservationStatusBefore() { return before; }
}

