package common.dto.Reservation;

import java.io.Serializable;

public class CancelReservationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;

    private CancelReservationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static CancelReservationResult ok(String msg) {
        return new CancelReservationResult(true, msg);
    }

    public static CancelReservationResult fail(String msg) {
        return new CancelReservationResult(false, msg);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

