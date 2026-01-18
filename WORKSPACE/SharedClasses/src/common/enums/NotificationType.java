package common.enums;


/**
 * Enumeration of notification types that can be sent to customers.
 * <p>
 * Each type corresponds to a specific event in the reservation lifecycle
 * and determines the message content and format sent to the customer.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum NotificationType {
    /**
     * Initial confirmation sent when a reservation is successfully created.
     */
    RESERVATION_CONFIRMATION,

    /**
     * Re-sent confirmation when a customer requests their confirmation code again.
     */
    RESEND_CONFIRMATION,

    /**
     * Reminder sent approximately 2 hours before the reservation time.
     */
    RESERVATION_REMINDER,

    /**
     * Notification sent to waitlist customers when a table becomes available.
     */
    TABLE_AVAILABLE,

    /**
     * Confirmation sent when a customer checks in and receives their table.
     */
    TABLE_RECEIVED,

    /**
     * Notification containing bill details when payment is requested.
     */
    BILL_SENT,

    /**
     * Confirmation sent when payment is successfully processed.
     */
    PAYMENT_SUCCESS,

    /**
     * Notification sent when a reservation is cancelled by the customer.
     */
    RESERVATION_CANCELED,

    /**
     * Notification sent when a reservation is moved to the waiting list
     * due to capacity changes.
     */
    RESERVATION_MOVED_TO_WAITING,

    /**
     * Notification sent when a reservation is cancelled due to
     * changes in opening hours.
     */
    RESERVATION_CANCELED_HOURS_CHANGE,

    /**
     * Notification sent when a reservation is cancelled due to
     * a date-specific closure or override.
     */
    RESERVATION_CANCELED_DATE_OVERRIDE,

    /**
     * Notification sent when a reservation is automatically cancelled
     * because the customer did not arrive within the grace period.
     */
    RESERVATION_CANCELED_NO_SHOW
}