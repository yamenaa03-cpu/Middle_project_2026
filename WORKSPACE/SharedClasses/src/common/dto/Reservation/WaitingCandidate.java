package common.dto.Reservation;

public class WaitingCandidate {
    public final int reservationId;
    public final int customerId;
    public final int guests;

    public WaitingCandidate(int reservationId, int customerId, int guests) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.guests = guests;
    }
}
