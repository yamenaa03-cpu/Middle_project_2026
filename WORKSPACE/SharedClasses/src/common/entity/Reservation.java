package common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.enums.ReservationStatus;
import common.enums.ReservationType;

/**
 * Represents a single Reservation entity stored in the database. Shared between
 * client & server.
 *
 * Fields match the reservation table: reservation_id, reservation_datetime,
 * number_of_guests, confirmation_code, customer_id, table_id, created_at,
 * status, reminder_sent, type, checked_in_at, checked_out_at
 */
public class Reservation implements Serializable {

	private static final long serialVersionUID = 1L;

	// Core fields
	private final int reservationId; // PK
	private final LocalDateTime reservationDateTime; // reservation_datetime
	private final int numberOfGuests; // number_of_guests
	private final int confirmationCode; // confirmation_code
	private final int customerId; // customer_id (FK)
	private final Integer tableId; // table_id (nullable)
	private final LocalDateTime createdAt; // created_at

	// Status/type
	private final ReservationStatus status; // status
	private final ReservationType type; // type (ADVANCE/WALKIN)

	// Reminder flag
	private final boolean reminderSent; // reminder_sent

	// Time tracking (nullable)
	private final LocalDateTime checkedInAt; // checked_in_at
	private final LocalDateTime checkedOutAt; // checked_out_at

	/**
	 * Full constructor (matches DB table 1:1).
	 */
	public Reservation(int reservationId, LocalDateTime reservationDateTime, int numberOfGuests, int confirmationCode,
			int customerId, Integer tableId, LocalDateTime createdAt, ReservationStatus status, boolean reminderSent,
			ReservationType type, LocalDateTime checkedInAt, LocalDateTime checkedOutAt) {

		this.reservationId = reservationId;
		this.reservationDateTime = reservationDateTime;
		this.numberOfGuests = numberOfGuests;
		this.confirmationCode = confirmationCode;
		this.customerId = customerId;
		this.tableId = tableId;
		this.createdAt = createdAt;
		this.status = status;
		this.reminderSent = reminderSent;
		this.type = type;
		this.checkedInAt = checkedInAt;
		this.checkedOutAt = checkedOutAt;
	}

	// Getters

	public int getReservationId() {
		return reservationId;
	}

	public LocalDateTime getReservationDateTime() {
		return reservationDateTime;
	}

	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	public int getConfirmationCode() {
		return confirmationCode;
	}

	public int getCustomerId() {
		return customerId;
	}

	public Integer getTableId() {
		return tableId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public ReservationType getType() {
		return type;
	}

	public boolean isReminderSent() {
		return reminderSent;
	}

	public LocalDateTime getCheckedInAt() {
		return checkedInAt;
	}

	public LocalDateTime getCheckedOutAt() {
		return checkedOutAt;
	}

	@Override
	public String toString() {
		return "Reservation #" + reservationId + " | type=" + type + " | status=" + status + " | dateTime="
				+ reservationDateTime + " | guests=" + numberOfGuests + " | confirmationCode=" + confirmationCode
				+ " | customerId=" + customerId + " | tableId=" + tableId + " | createdAt=" + createdAt
				+ " | reminderSent=" + reminderSent + " | checkedInAt=" + checkedInAt + " | checkedOutAt="
				+ checkedOutAt;
	}
}
