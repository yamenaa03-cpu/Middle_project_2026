package common.enums;

import java.io.Serializable;

/*
 * This class operates as the */

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
