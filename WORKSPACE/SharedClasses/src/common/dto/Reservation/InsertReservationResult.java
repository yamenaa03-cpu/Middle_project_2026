package common.dto.Reservation;

import java.io.Serializable;

/**
 * Result object returned after inserting a reservation, containing the new
 * reservation id and confirmation code.
 */
public class InsertReservationResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int reservationId;
	private final int confirmationCode;

	/**
	 * Construct an insert result.
	 *
	 * @param reservationId    created reservation id
	 * @param confirmationCode confirmation code assigned to the reservation
	 */
	public InsertReservationResult(int reservationId, int confirmationCode) {
		this.reservationId = reservationId;
		this.confirmationCode = confirmationCode;
	}

	/**
	 * Returns the reservation id.
	 *
	 * @return reservation id
	 */
	public int getReservationId() {
		return reservationId;
	}

	/**
	 * Returns the confirmation code.
	 *
	 * @return confirmation code
	 */
	public int getConfirmationCode() {
		return confirmationCode;
	}
}
