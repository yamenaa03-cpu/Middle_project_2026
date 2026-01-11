package common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.enums.ReservationStatus;

/**
 * Represents a single Reservation entity stored in the database. Shared between
 * client & server.
 *
 * Notes: - Billing data is stored in Bill entity/table (not here). - status is
 * essential for flows: WAITING/NOTIFIED/ACTIVE/IN_PROGRESS/COMPLETED/CANCELED
 */
public class Reservation implements Serializable {

	private static final long serialVersionUID = 1L;

	// Core fields
	private final int reservationId; // PK
	private final LocalDateTime reservationDateTime; // reservation time (and can be used as seated_at when IN_PROGRESS)
	private final int numberOfGuests;
	private final int confirmationCode;
	private final int customerId; // FK to customer
	private final LocalDateTime createdAt; // created timestamp
	private final Integer tableId; // nullable

	// Lifecycle field
	private final ReservationStatus status; // IMPORTANT


	public Reservation(int reservationId, LocalDateTime reservationDateTime, int numberOfGuests, int confirmationCode,
			int customerId, LocalDateTime createdAt, Integer tableId, ReservationStatus status) {
		this.reservationId = reservationId;
		this.reservationDateTime = reservationDateTime;
		this.numberOfGuests = numberOfGuests;
		this.confirmationCode = confirmationCode;
		this.customerId = customerId;
		this.createdAt = createdAt;
		this.tableId = tableId;
		this.status = status;
	}

	/**
	 * Construct a Reservation entity.
	 *
	 * @param reservationId unique identifier of the reservation
	 * @param reservationDateTime date and time of the reservation
	 * @param numberOfGuests number of guests for the reservation
	 * @param confirmationCode numeric confirmation code given to the customer
	 * @param customerId id of the customer who made the reservation
	 * @param createdAt timestamp when the reservation was created
	 * @param tableId optional table id assigned to the reservation (nullable)
	 * @param status current lifecycle status of the reservation
	 */
	public int getReservationId() {
		return reservationId;
	}

	/**
	 * Returns the reservation identifier.
	 *
	 * @return reservation id
	 */

	public LocalDateTime getReservationDateTime() {
		return reservationDateTime;
	}

	/**
	 * Returns the date and time of the reservation.
	 *
	 * @return reservation date and time
	 */

	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	/**
	 * Returns the number of guests for this reservation.
	 *
	 * @return number of guests
	 */

	public int getConfirmationCode() {
		return confirmationCode;
	}

	/**
	 * Returns the numeric confirmation code provided to the customer.
	 *
	 * @return confirmation code
	 */

	public int getCustomerId() {
		return customerId;
	}

	/**
	 * Returns the id of the customer who created the reservation.
	 *
	 * @return customer id
	 */

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Returns the creation timestamp for the reservation.
	 *
	 * @return creation timestamp
	 */

	public Integer getTableId() {
		return tableId;
	}

	/**
	 * Returns the assigned table id, or null if none assigned.
	 *
	 * @return table id or null
	 */

	public ReservationStatus getStatus() {
		return status;
	}

	/**
	 * Returns the current lifecycle status of the reservation.
	 *
	 * @return reservation status
	 */

	@Override
	public String toString() {
		return "Reservation #" + reservationId + " | status=" + status + " | dateTime=" + reservationDateTime
				+ " | guests=" + numberOfGuests + " | confirmationCode=" + confirmationCode + " | customerId="
				+ customerId + " | createdAt=" + createdAt + " | tableId=" + tableId;
	}
}
