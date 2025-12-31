package common.dto.Reservation;

import java.io.Serializable;

public class InsertReservationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int reservationId;
    private final int confirmationCode;

    public InsertReservationResult(int reservationId, int confirmationCode) {
        this.reservationId = reservationId;
        this.confirmationCode = confirmationCode;
    }

    public int getReservationId() {
        return reservationId;
    }

    public int getConfirmationCode() {
        return confirmationCode;
    }
}
