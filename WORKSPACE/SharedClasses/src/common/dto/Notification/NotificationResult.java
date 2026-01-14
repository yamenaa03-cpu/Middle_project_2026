package common.dto.Notification;

import java.io.Serializable;

/**
 * Result of attempting to send a notification.
 */
public class NotificationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final String channel; // "EMAIL", "SMS", or null

    private NotificationResult(boolean success, String message, String channel) {
        this.success = success;
        this.message = message;
        this.channel = channel;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getChannel() { return channel; }

    public static NotificationResult sentViaEmail(String message) {
        return new NotificationResult(true, message, "EMAIL");
    }

    public static NotificationResult sentViaSms(String message) {
        return new NotificationResult(true, message, "SMS");
    }

    public static NotificationResult sent(String message, String channel) {
        return new NotificationResult(true, message, channel);
    }

    public static NotificationResult fail(String message) {
        return new NotificationResult(false, message, null);
    }

    public static NotificationResult reservationNotFound() {
        return new NotificationResult(false, "Reservation not found.", null);
    }

    public static NotificationResult noContactInfo() {
        return new NotificationResult(false, "No contact information available (no email or phone).", null);
    }

    public static NotificationResult emptyMessage() {
        return new NotificationResult(false, "Cannot send empty message.", null);
    }
}
