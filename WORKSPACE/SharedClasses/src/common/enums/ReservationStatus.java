package common.enums;


/**
 * Enumeration of lifecycle states for a reservation.
 * <p>
 * A reservation progresses through these states from creation to completion,
 * with various transitions possible based on customer actions and system events.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum ReservationStatus {
    /**
     * Reservation is confirmed and waiting for the scheduled time.
     * The customer has not yet arrived.
     */
    ACTIVE,

    /**
     * Reservation has been cancelled by the customer, system, or staff.
     * This is a terminal state.
     */
    CANCELED,

    /**
     * Customer has checked in and is currently dining.
     * A table has been assigned and the visit is in progress.
     */
    IN_PROGRESS,

    /**
     * Customer is on the waiting list, awaiting table availability.
     * No specific time slot is assigned.
     */
    WAITING,

    /**
     * A table has become available for a waiting customer.
     * The customer has been notified and should arrive soon.
     */
    NOTIFIED,

    /**
     * The dining experience is finished and payment has been processed.
     * This is a terminal state.
     */
    COMPLETED
}