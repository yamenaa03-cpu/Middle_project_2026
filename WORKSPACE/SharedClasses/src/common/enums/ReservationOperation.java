package common.enums;

import java.io.Serializable;

/**
 * Enumeration of reservation-related operations used in client-server request messages.
 * <p>
 * Each operation type indicates what action the client is requesting from the server
 * regarding reservation management. This enum is used in {@code ReservationRequest}
 * to identify the specific operation to perform.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum ReservationOperation implements Serializable  {
    /**
     * Request to retrieve all active (non-cancelled, non-completed) reservations.
     * Restricted to employee access.
     */
    GET_ACTIVE_RESERVATIONS,

    /**
     * Request to retrieve all customers on the waiting list.
     * Restricted to employee access.
     */
    GET_WAITLIST,

    /**
     * Request to update the date/time and guest count of an existing reservation.
     */
    UPDATE_RESERVATION_FIELDS,

    /**
     * Request to create a new reservation.
     * Can be used by subscribers, guests, or employees on behalf of customers.
     */
    CREATE_RESERVATION,

    /**
     * Request to cancel an existing reservation.
     */
    CANCEL_RESERVATION,

    /**
     * Request to retrieve a customer's reservations that are eligible for cancellation.
     */
    GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION,

    /**
     * Request to retrieve a customer's reservations that are ready for table reception.
     */
    GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING,

    /**
     * Request to retrieve a customer's reservations that are ready for checkout/payment.
     */
    GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT,

    /**
     * Request to resend the confirmation code to a customer via email/SMS.
     */
    RESEND_CONFIRMATION_CODE,

    /**
     * Request to find a cancellable reservation by its confirmation code.
     */
    GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CANCELLATION,

    /**
     * Request to find a reservation ready for receiving by its confirmation code.
     */
    GET_RESERVATION_By_CONFIRMATION_CODE_FOR_RECEIVING,

    /**
     * Request to find a reservation ready for checkout by its confirmation code.
     */
    GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CHECKOUT,

    /**
     * Request to join the waiting list when no immediate table is available.
     */
    JOIN_WAITLIST,

    /**
     * Request to check in and receive an assigned table.
     */
    RECEIVE_TABLE,

    /**
     * Request to retrieve or generate the bill for an in-progress reservation.
     */
    GET_BILL_FOR_PAYING,

    /**
     * Request to process payment for a bill.
     */
    PAY_BILL,

    /**
     * Request to retrieve the reservation history for a subscriber.
     */
    GET_SUBSCRIBER_HISTORY
}