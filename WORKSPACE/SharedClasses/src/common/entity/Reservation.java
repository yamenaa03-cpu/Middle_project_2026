package common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a single Order entity stored in the database. Contains all fields
 * taken from the Order table and used by both client and server.
 * 
 * @author Yamen abu Ahmad
 * @version 1.0
 * 
 */

public class Reservation implements Serializable {

	private static final long serialVersionUID = 1L;

	// Reservation fields
	private int reservationId; // PK
	private LocalDateTime reservationDateTime; // date of reservation
	private int numberOfGuests;
	private int confirmationCode;
	private int customerId; // FK to Customer
	private LocalDateTime createdAt; // date reservation was created
	private Integer tableId;

	public Reservation(int reservationId, LocalDateTime reservationDate, int numberOfGuests, int confirmationCode,
			int customerId, LocalDateTime createdAt, Integer tableId) {

		this.reservationId = reservationId;
		this.reservationDateTime = reservationDate;
		this.numberOfGuests = numberOfGuests;
		this.confirmationCode = confirmationCode;
		this.customerId = customerId;
		this.createdAt = createdAt;
		this.tableId = tableId;
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

	@Override
	public String toString() {
		return "Reservation #" + reservationId + " | date=" + reservationDateTime + " | guests=" + numberOfGuests
				+ " | confirmationCode=" + confirmationCode + " | customerId=" + customerId + " | createdAt="
				+ createdAt + " | tableId=" + tableId;
	}
}
