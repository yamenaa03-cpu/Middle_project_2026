package common.dto.Reservation;

import java.io.Serializable;

import common.enums.ReservationStatus;

/**
 * Result object returned after attempting to cancel a reservation.
 * <p>
 * Contains the success flag, a human-readable message describing the outcome,
 * and the previous reservation status when the cancellation succeeds. The
 * previous status can be used to determine if any follow-up actions are needed
 * (e.g., notifying waiting customers when capacity is freed).
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class CancelReservationResult implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Whether the cancellation succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * The reservation status before cancellation (for capacity management).
	 */
	private ReservationStatus before;

	/**
	 * Private constructor for successful cancellation with status tracking.
	 *
	 * @param success whether the operation succeeded
	 * @param message descriptive message
	 * @param before  status before cancellation
	 */
	private CancelReservationResult(boolean success, String message, ReservationStatus before) {
		this.success = success;
		this.message = message;
		this.before = before;
	}

	/**
	 * Private constructor for failed cancellation.
	 *
	 * @param success whether the operation succeeded (false)
	 * @param message failure message
	 */
	private CancelReservationResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	/**
	 * Factory method for successful cancellation.
	 *
	 * @param msg    success message
	 * @param before the reservation status before cancellation
	 * @return success result with previous status
	 */
	public static CancelReservationResult ok(String msg, ReservationStatus before) {
		return new CancelReservationResult(true, msg, before);
	}

	/**
	 * Factory method for failed cancellation.
	 *
	 * @param msg failure message
	 * @return failed result
	 */
	public static CancelReservationResult fail(String msg) {
		return new CancelReservationResult(false, msg);
	}

	/**
	 * Returns whether the cancellation succeeded.
	 *
	 * @return true if cancelled successfully
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Returns a human-readable message describing the result.
	 *
	 * @return descriptive message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the reservation status before cancellation, if available.
	 * <p>
	 * This can be used to determine if capacity was freed (e.g., if the previous
	 * status was ACTIVE or NOTIFIED, a table slot may now be available).
	 * </p>
	 *
	 * @return previous reservation status or null
	 */
	public ReservationStatus getReservationStatusBefore() {
		return before;
	}
}
