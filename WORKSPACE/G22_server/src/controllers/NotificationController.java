package controllers;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import common.dto.Notification.CustomerContactInfo;
import common.dto.Notification.NotificationResult;
import common.dto.Reservation.ReservationBasicInfo;
import common.entity.Bill;
import common.entity.Reservation;
import common.enums.NotificationType;
import dbController.DBController;
import server.ServerUI;

/**
 * Controller responsible for sending notifications to customers.
 * <p>
 * This controller handles all customer communication including reservation
 * confirmations, reminders, table availability alerts, billing notifications,
 * and cancellation notices. Notifications are simulated via the
 * {@link ServerUI} interface, which displays messages to demonstrate email and
 * SMS delivery.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class NotificationController {

	/**
	 * Reference to the server UI for displaying notification messages.
	 */
	private final ServerUI ui;

	/**
	 * Database controller for accessing customer and reservation data.
	 */
	private final DBController db;

	/**
	 * Constructs a NotificationController with the specified dependencies.
	 *
	 * @param ui the server UI interface for displaying notifications
	 * @param db the database controller for data access
	 */
	public NotificationController(ServerUI ui, DBController db) {
		this.ui = ui;
		this.db = db;
	}

	/**
	 * Resolves a reservation ID from a confirmation code.
	 *
	 * @param confirmationCode the confirmation code to look up
	 * @return the reservation ID, or null if not found or invalid
	 * @throws SQLException if a database error occurs
	 */
	private Integer resolveReservationIdByCode(int confirmationCode) throws SQLException {
		if (confirmationCode <= 0)
			return null;
		Reservation res = db.findReservationByConfirmationCode(confirmationCode);
		return (res == null) ? null : res.getReservationId();
	}

	/**
	 * Sends a reservation confirmation notification to the customer.
	 *
	 * @param reservationId the ID of the reservation to confirm
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendReservationConfirmation(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);

		if (info == null)
			return NotificationResult.reservationNotFound();
		if (contact == null)
			return NotificationResult.noContactInfo();

		String msg = buildMessage(NotificationType.RESERVATION_CONFIRMATION, info.fullName, info.dateTime, info.guests,
				info.confirmationCode, null, null);

		return send(contact, msg);
	}

	/**
	 * Resends a reservation confirmation notification to the customer.
	 *
	 * @param reservationId the ID of the reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult resendReservationConfirmation(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);

		if (info == null)
			return NotificationResult.reservationNotFound();
		if (contact == null)
			return NotificationResult.noContactInfo();

		String msg = buildMessage(NotificationType.RESEND_CONFIRMATION, info.fullName, info.dateTime, info.guests,
				info.confirmationCode, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a reservation reminder notification to the customer.
	 * <p>
	 * This is typically sent 2 hours before the reservation time.
	 * </p>
	 *
	 * @param reservationId the ID of the reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendReservationReminder(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);

		if (info == null)
			return NotificationResult.reservationNotFound();
		if (contact == null)
			return NotificationResult.noContactInfo();

		String msg = buildMessage(NotificationType.RESERVATION_REMINDER, info.fullName, info.dateTime, info.guests,
				info.confirmationCode, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a table available notification to a customer on the waitlist.
	 *
	 * @param reservationId the ID of the waitlist reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendTableAvailable(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);

		if (info == null)
			return NotificationResult.reservationNotFound();
		if (contact == null)
			return NotificationResult.noContactInfo();

		String msg = buildMessage(NotificationType.TABLE_AVAILABLE, info.fullName, null, null, info.confirmationCode,
				null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a table received confirmation notification to the customer.
	 *
	 * @param reservationId the ID of the reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendTableReceived(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

		if (contact == null)
			return NotificationResult.noContactInfo();

		String name = contact.getFullName() != null ? contact.getFullName() : "Customer";

		String msg = buildMessage(NotificationType.TABLE_RECEIVED, name, null, null, null, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a bill notification to the customer with amount details.
	 *
	 * @param reservationId the ID of the reservation
	 * @param bill          the bill containing payment amounts
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendBillSent(int reservationId, Bill bill) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);

		if (info == null)
			return NotificationResult.reservationNotFound();
		if (contact == null)
			return NotificationResult.noContactInfo();

		String msg = buildMessage(NotificationType.BILL_SENT, info.fullName, null, null, info.confirmationCode,
				bill.getAmountBeforeDiscount(), bill.getFinalAmount());

		return send(contact, msg);
	}

	/**
	 * Sends a payment success notification to the customer.
	 *
	 * @param reservationId the ID of the reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendPaymentSuccess(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

		if (contact == null)
			return NotificationResult.noContactInfo();

		String name = contact.getFullName() != null ? contact.getFullName() : "Customer";

		String msg = buildMessage(NotificationType.PAYMENT_SUCCESS, name, null, null, null, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a reservation cancellation notification to the customer.
	 *
	 * @param reservationId the ID of the cancelled reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendReservationCanceled(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

		if (contact == null)
			return NotificationResult.noContactInfo();

		String name = contact.getFullName() != null ? contact.getFullName() : "Customer";

		String msg = buildMessage(NotificationType.RESERVATION_CANCELED, name, null, null, null, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a no-show cancellation notification to the customer.
	 * <p>
	 * This notification is sent when a reservation is automatically cancelled
	 * because the customer did not arrive within the grace period.
	 * </p>
	 *
	 * @param reservationId the ID of the cancelled reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendReservationCanceledDueToNoShow(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

		if (contact == null)
			return NotificationResult.noContactInfo();

		String name = contact.getFullName() != null ? contact.getFullName() : "Customer";

		String msg = buildMessage(NotificationType.RESERVATION_CANCELED_NO_SHOW, name, null, null, null, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a reservation cancellation notification using a confirmation code.
	 *
	 * @param confirmationCode the confirmation code of the reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendReservationCanceledByCode(int confirmationCode) throws SQLException {
		Integer resId = resolveReservationIdByCode(confirmationCode);
		if (resId == null) {
			ui.displayMessage("❌ RESERVATION_CANCELED not sent: invalid confirmationCode=" + confirmationCode);
			return NotificationResult.fail("Invalid confirmation code: " + confirmationCode);
		}
		return sendReservationCanceled(resId);
	}

	/**
	 * Sends a notification when a reservation is moved to the waiting list.
	 * <p>
	 * This can occur when restaurant capacity changes affect existing reservations.
	 * </p>
	 *
	 * @param reservationId the ID of the affected reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendReservationMovedToWaiting(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);

		if (info == null)
			return NotificationResult.reservationNotFound();
		if (contact == null)
			return NotificationResult.noContactInfo();

		String msg = buildMessage(NotificationType.RESERVATION_MOVED_TO_WAITING, info.fullName, info.dateTime,
				info.guests, info.confirmationCode, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a cancellation notification when opening hours change affects a
	 * reservation.
	 *
	 * @param reservationId the ID of the affected reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendReservationCanceledDueToHoursChange(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);

		if (info == null)
			return NotificationResult.reservationNotFound();
		if (contact == null)
			return NotificationResult.noContactInfo();

		String msg = buildMessage(NotificationType.RESERVATION_CANCELED_HOURS_CHANGE, info.fullName, info.dateTime,
				null, null, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a cancellation notification when a date override affects a reservation.
	 *
	 * @param reservationId the ID of the affected reservation
	 * @return the result of the notification attempt
	 * @throws SQLException if a database error occurs
	 */
	public NotificationResult sendReservationCanceledDueToDateOverride(int reservationId) throws SQLException {
		CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);

		if (info == null)
			return NotificationResult.reservationNotFound();
		if (contact == null)
			return NotificationResult.noContactInfo();

		String msg = buildMessage(NotificationType.RESERVATION_CANCELED_DATE_OVERRIDE, info.fullName, info.dateTime,
				null, null, null, null);

		return send(contact, msg);
	}

	/**
	 * Sends a notification message to the customer via available channels.
	 * <p>
	 * This method simulates sending notifications via email and/or SMS by
	 * displaying the message content through the server UI. If email is available,
	 * it is used; if phone is available, SMS is simulated.
	 * </p>
	 *
	 * @param contact the customer's contact information
	 * @param message the message content to send
	 * @return the result indicating success and channel used
	 */
	private NotificationResult send(CustomerContactInfo contact, String message) {

		if (message == null || message.isBlank()) {
			ui.displayMessage("❌ Notification NOT sent: empty message.");
			return NotificationResult.emptyMessage();
		}

		if (contact == null) {
			ui.displayMessage("❌ Notification NOT sent: contact info is null.");
			return NotificationResult.noContactInfo();
		}

		boolean hasEmail = contact.getEmail() != null && !contact.getEmail().isBlank();
		boolean hasPhone = contact.getPhone() != null && !contact.getPhone().isBlank();

		if (!hasEmail && !hasPhone) {
			ui.displayMessage("❌ Notification NOT sent: no email/phone for customerId=" + contact.getCustomerId());
			return NotificationResult.noContactInfo();
		}

		String channel = null;

		if (hasEmail) {
			ui.displayMessage("📧 [EMAIL] To: " + contact.getEmail() + "\n" + message);
			channel = "EMAIL";
		}

		if (hasPhone) {
			ui.displayMessage("📱 [SMS] To: " + contact.getPhone() + "\n" + message);
			channel = (channel == null) ? "SMS" : channel + "+SMS";
		}

		return NotificationResult.sent("Notification sent via " + channel, channel);
	}

	/**
	 * Date/time formatter for notification messages.
	 */
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	/**
	 * Builds a notification message based on the notification type and parameters.
	 *
	 * @param type             the type of notification
	 * @param name             the customer's name
	 * @param dateTime         the reservation date/time (if applicable)
	 * @param guests           the number of guests (if applicable)
	 * @param confirmationCode the confirmation code (if applicable)
	 * @param amountBefore     the amount before discount (for billing)
	 * @param finalAmount      the final amount after discount (for billing)
	 * @return the formatted notification message
	 */
	public static String buildMessage(NotificationType type, String name, LocalDateTime dateTime, Integer guests,
			Integer confirmationCode, Double amountBefore, Double finalAmount) {

		if (name == null || name.isBlank()) {
			name = "Customer";
		}

		return switch (type) {

		case RESERVATION_CONFIRMATION -> """
				Hello %s,

				Your reservation has been successfully created.

				Reservation details:
				• Date & Time: %s
				• Number of Guests: %d
				• Confirmation Code: %d

				Please keep this code safe.

				Restaurant Management
				""".formatted(name, format(dateTime), guests, confirmationCode);

		case RESEND_CONFIRMATION -> """
				Hello %s,

				Your reservation confirmation and code resend.

				Reservation details:
				• Date & Time: %s
				• Number of Guests: %d
				• Confirmation Code: %d

				Please keep this code safe.

				Restaurant Management
				""".formatted(name, format(dateTime), guests, confirmationCode);

		case RESERVATION_REMINDER -> """
				Hello %s,

				This is a friendly reminder that your reservation is scheduled in 2 hours.

				• Date & Time: %s
				• Number of Guests: %d

				We look forward to welcoming you.

				Restaurant Management
				""".formatted(name, format(dateTime), guests);

		case TABLE_AVAILABLE -> """
				Hello %s,

				A table is now available for you.

				Please arrive as soon as possible and present your confirmation code:
				%d

				Restaurant Management
				""".formatted(name, confirmationCode);

		case TABLE_RECEIVED -> """
				Hello %s,

				Your table has been successfully received.
				We wish you a pleasant dining experience.

				Restaurant Management
				""".formatted(name);

		case BILL_SENT -> """
				Hello %s,

				Your bill is ready.

				• Amount before discount: %.2f
				• Final amount to pay: %.2f

				Please proceed to payment using your confirmation code.

				Restaurant Management
				""".formatted(name, amountBefore, finalAmount);

		case PAYMENT_SUCCESS -> """
				Hello %s,

				Your payment has been successfully completed.

				Thank you for visiting us.
				We hope to see you again soon.

				Restaurant Management
				""".formatted(name);

		case RESERVATION_CANCELED -> """
				Hello %s,

				Your reservation has been canceled.

				If you wish to make a new reservation,
				you are welcome to do so at any time.

				Restaurant Management
				""".formatted(name);

		case RESERVATION_MOVED_TO_WAITING -> """
				Hello %s,

				Due to a change in the restaurant seating capacity, your reservation has been moved to the waiting list.

				Reservation details:
				• Date & Time: %s
				• Number of Guests: %d
				• Confirmation Code: %d

				We will notify you as soon as a table becomes available.

				Restaurant Management
				""".formatted(name, format(dateTime), guests, confirmationCode);

		case RESERVATION_CANCELED_HOURS_CHANGE -> """
				Hello %s,

				Due to a change in the restaurant opening hours, your reservation has been canceled.

				We apologize for the inconvenience and invite you to make a new reservation
				within the updated opening hours.

				Restaurant Management
				""".formatted(name);

		case RESERVATION_CANCELED_DATE_OVERRIDE -> """
				Hello %s,

				Due to a special schedule change on %s, your reservation has been canceled.

				We apologize for the inconvenience and hope to see you on another date.

				Restaurant Management
				""".formatted(name, format(dateTime));

		case RESERVATION_CANCELED_NO_SHOW -> """
				Hello %s,

				Your reservation has been automatically canceled
				because you did not arrive within the allowed grace period.

				If you wish to make a new reservation,
				you are welcome to do so at any time.

				Restaurant Management
				""".formatted(name);

		};
	}

	/**
	 * Formats a LocalDateTime for display in notification messages.
	 *
	 * @param dt the date/time to format
	 * @return the formatted string, or "N/A" if null
	 */
	private static String format(LocalDateTime dt) {
		return dt == null ? "N/A" : dt.format(FORMATTER);
	}
}
