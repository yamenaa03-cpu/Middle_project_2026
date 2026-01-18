package common.dto.Notification;

import java.io.Serializable;

/**
 * Result of attempting to send a notification to a customer.
 * <p>
 * This class encapsulates the outcome of a notification operation, including
 * whether it succeeded, a descriptive message, and which channel (EMAIL or SMS)
 * was used for delivery.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class NotificationResult implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Whether the notification was sent successfully.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * The channel used for delivery: "EMAIL", "SMS", "EMAIL+SMS", or null.
	 */
	private final String channel;

	/**
	 * Private constructor used by factory methods.
	 *
	 * @param success whether the operation succeeded
	 * @param message descriptive message
	 * @param channel delivery channel used
	 */
	private NotificationResult(boolean success, String message, String channel) {
		this.success = success;
		this.message = message;
		this.channel = channel;
	}

	/**
	 * Returns whether the notification was sent successfully.
	 *
	 * @return true if sent successfully
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Returns the descriptive message about the result.
	 *
	 * @return result message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the channel used for delivery.
	 *
	 * @return channel string or null if failed
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Factory method for successful email notification.
	 *
	 * @param message success message
	 * @return success result with EMAIL channel
	 */
	public static NotificationResult sentViaEmail(String message) {
		return new NotificationResult(true, message, "EMAIL");
	}

	/**
	 * Factory method for successful SMS notification.
	 *
	 * @param message success message
	 * @return success result with SMS channel
	 */
	public static NotificationResult sentViaSms(String message) {
		return new NotificationResult(true, message, "SMS");
	}

	/**
	 * Factory method for successful notification via specified channel.
	 *
	 * @param message success message
	 * @param channel the channel used (EMAIL, SMS, or both)
	 * @return success result
	 */
	public static NotificationResult sent(String message, String channel) {
		return new NotificationResult(true, message, channel);
	}

	/**
	 * Factory method for a failed notification attempt.
	 *
	 * @param message failure message
	 * @return failed result
	 */
	public static NotificationResult fail(String message) {
		return new NotificationResult(false, message, null);
	}

	/**
	 * Factory method for when the reservation was not found.
	 *
	 * @return failed result with standard message
	 */
	public static NotificationResult reservationNotFound() {
		return new NotificationResult(false, "Reservation not found.", null);
	}

	/**
	 * Factory method for when no contact information is available.
	 *
	 * @return failed result with standard message
	 */
	public static NotificationResult noContactInfo() {
		return new NotificationResult(false, "No contact information available (no email or phone).", null);
	}

	/**
	 * Factory method for when the message content is empty.
	 *
	 * @return failed result with standard message
	 */
	public static NotificationResult emptyMessage() {
		return new NotificationResult(false, "Cannot send empty message.", null);
	}
}
