package common.dto.Reservation;

/**
 * Lightweight value object representing a waiting-list candidate.
 */
public class WaitingCandidate {
    public final int reservationId;
    public final int customerId;
    public final int guests;

    /**
     * Create a waiting candidate.
     *
     * @param reservationId reservation identifier
     * @param customerId customer identifier
     * @param guests number of guests in the reservation
     */
    public WaitingCandidate(int reservationId, int customerId, int guests) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.guests = guests;
    }
}
