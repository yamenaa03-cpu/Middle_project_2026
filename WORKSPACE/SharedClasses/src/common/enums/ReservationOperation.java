package common.enums;

import java.io.Serializable;

/**
 * Enumeration of reservation-related operations used in request messages.
 */
public enum ReservationOperation implements Serializable  {
    GET_ALL_RESERVATIONS,
    UPDATE_RESERVATION_FIELDS,
    CREATE_RESERVATION,
    CANCEL_RESERVATION,
    GET_CUSTOMER_RESERVATIONS,
    JOIN_WAITLIST,
    RECEIVE_TABLE,
    CHECKOUT
}
