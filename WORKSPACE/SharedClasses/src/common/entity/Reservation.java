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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Integer getTableId() {
		return tableId;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "Reservation #" + reservationId + " | status=" + status + " | dateTime=" + reservationDateTime
				+ " | guests=" + numberOfGuests + " | confirmationCode=" + confirmationCode + " | customerId="
				+ customerId + " | createdAt=" + createdAt + " | tableId=" + tableId;
	}
}
