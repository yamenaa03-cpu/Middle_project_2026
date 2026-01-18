package common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.enums.ReservationStatus;
import common.enums.ReservationType;

/**
 * Represents a single Reservation entity stored in the database.
 * <p>
 * This immutable entity is shared between client and server, containing all
 * information about a customer's table reservation including timing, status,
 * and tracking information. The fields match the database reservation table
 * structure.
 * </p>
 *
 * <h2>Database Fields Mapping:</h2>
 * <ul>
 *   <li>reservation_id - primary key</li>
 *   <li>reservation_datetime - scheduled date and time</li>
 *   <li>number_of_guests - party size</li>
 *   <li>confirmation_code - unique code for customer reference</li>
 *   <li>customer_id - foreign key to customer table</li>
 *   <li>table_id - assigned table (nullable until check-in)</li>
 *   <li>created_at - timestamp when reservation was created</li>
 *   <li>status - current reservation state</li>
 *   <li>reminder_sent - whether 2-hour reminder was sent</li>
 *   <li>type - ADVANCE or WALKIN</li>
 *   <li>checked_in_at - actual arrival time</li>
 *   <li>checked_out_at - departure time after payment</li>
 * </ul>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see ReservationStatus
 * @see ReservationType
 */
public class Reservation implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Primary key identifier for the reservation.
	 */
	private final int reservationId;

	/**
	 * Scheduled date and time for the reservation.
	 */
	private final LocalDateTime reservationDateTime;

	/**
	 * Number of guests in the party.
	 */
	private final int numberOfGuests;

	/**
	 * Unique confirmation code provided to the customer.
	 */
	private final int confirmationCode;

	/**
	 * Foreign key reference to the customer who made the reservation.
	 */
	private final int customerId;

	/**
	 * Assigned table ID (nullable until check-in or for waitlist).
	 */
	private final Integer tableId;

	/**
	 * Timestamp when the reservation was created.
	 */
	private final LocalDateTime createdAt;

	/**
	 * Current status of the reservation in its lifecycle.
	 */
	private final ReservationStatus status;

	/**
	 * Type of reservation (advance booking or walk-in).
	 */
	private final ReservationType type;

	/**
	 * Flag indicating whether the 2-hour reminder notification was sent.
	 */
	private final boolean reminderSent;

	/**
	 * Actual check-in time when the customer arrived (nullable).
	 */
	private final LocalDateTime checkedInAt;

	/**
	 * Check-out time when the customer completed payment and left (nullable).
	 */
	private final LocalDateTime checkedOutAt;

	/**
	 * Constructs a complete Reservation entity with all database fields.
	 *
	 * @param reservationId       unique identifier for the reservation
	 * @param reservationDateTime scheduled date and time
	 * @param numberOfGuests      number of guests in the party
	 * @param confirmationCode    confirmation code for customer reference
	 * @param customerId          ID of the customer who made the reservation
	 * @param tableId             assigned table ID (may be null)
	 * @param createdAt           timestamp when created
	 * @param status              current reservation status
	 * @param reminderSent        whether reminder has been sent
	 * @param type                reservation type (ADVANCE or WALKIN)
	 * @param checkedInAt         actual arrival time (may be null)
	 * @param checkedOutAt        departure time (may be null)
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

	/**
	 * Returns the unique reservation identifier.
	 *
	 * @return the reservation ID (primary key)
	 */
	public int getReservationId() {
		return reservationId;
	}

	/**
	 * Returns the scheduled date and time of the reservation.
	 *
	 * @return the reservation date/time
	 */
	public LocalDateTime getReservationDateTime() {
		return reservationDateTime;
	}

	/**
	 * Returns the number of guests in the party.
	 *
	 * @return guest count
	 */
	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	/**
	 * Returns the confirmation code provided to the customer.
	 *
	 * @return the confirmation code
	 */
	public int getConfirmationCode() {
		return confirmationCode;
	}

	/**
	 * Returns the customer ID associated with this reservation.
	 *
	 * @return customer ID (foreign key)
	 */
	public int getCustomerId() {
		return customerId;
	}

	/**
	 * Returns the assigned table ID, or null if not yet assigned.
	 *
	 * @return table ID or null
	 */
	public Integer getTableId() {
		return tableId;
	}

	/**
	 * Returns the timestamp when the reservation was created.
	 *
	 * @return creation timestamp
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Returns the current status of the reservation.
	 *
	 * @return reservation status
	 */
	public ReservationStatus getStatus() {
		return status;
	}

	/**
	 * Returns the reservation type (ADVANCE or WALKIN).
	 *
	 * @return reservation type
	 */
	public ReservationType getType() {
		return type;
	}

	/**
	 * Returns whether the 2-hour reminder notification has been sent.
	 *
	 * @return true if reminder was sent
	 */
	public boolean isReminderSent() {
		return reminderSent;
	}

	/**
	 * Returns the actual check-in time, or null if not checked in.
	 *
	 * @return check-in timestamp or null
	 */
	public LocalDateTime getCheckedInAt() {
		return checkedInAt;
	}

	/**
	 * Returns the check-out time, or null if not checked out.
	 *
	 * @return check-out timestamp or null
	 */
	public LocalDateTime getCheckedOutAt() {
		return checkedOutAt;
	}

	/**
	 * Returns a string representation of the reservation for debugging purposes.
	 *
	 * @return formatted string with all reservation details
	 */
	@Override
	public String toString() {
		return "Reservation #" + reservationId + " | type=" + type + " | status=" + status + " | dateTime="
				+ reservationDateTime + " | guests=" + numberOfGuests + " | confirmationCode=" + confirmationCode
				+ " | customerId=" + customerId + " | tableId=" + tableId + " | createdAt=" + createdAt
				+ " | reminderSent=" + reminderSent + " | checkedInAt=" + checkedInAt + " | checkedOutAt="
				+ checkedOutAt;
	}
}
