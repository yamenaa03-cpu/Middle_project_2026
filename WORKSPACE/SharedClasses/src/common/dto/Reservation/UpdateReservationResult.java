package common.dto.Reservation;

import java.io.Serializable;

/**
 * Result of attempting to update a reservation.
 */
public class UpdateReservationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Integer reservationId;

    private UpdateReservationResult(boolean success, String message, Integer reservationId) {
        this.success = success;
        this.message = message;
        this.reservationId = reservationId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getReservationId() { return reservationId; }

    public static UpdateReservationResult ok(int reservationId) {
        return new UpdateReservationResult(true, "Reservation updated successfully.", reservationId);
    }

    public static UpdateReservationResult ok(int reservationId, String message) {
        return new UpdateReservationResult(true, message, reservationId);
    }

    public static UpdateReservationResult fail(String message) {
        return new UpdateReservationResult(false, message, null);
    }
}
