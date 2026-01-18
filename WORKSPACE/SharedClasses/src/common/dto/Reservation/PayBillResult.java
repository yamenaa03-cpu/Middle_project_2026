package common.dto.Reservation;

import java.io.Serializable;

/**
 * Result object returned after attempting to pay a bill.
 * <p>
 * Contains the payment outcome, the final amount charged, and the table
 * capacity freed as a result of the payment. The freed capacity is used to
 * potentially notify waiting customers that a table is now available.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class PayBillResult implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Whether the payment succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * Final amount that was charged.
	 */
	private final Double finalAmount;

	/**
	 * Table capacity freed as a result of the payment.
	 */
	private int freedCapacity;

	/**
	 * ID of the reservation that was paid.
	 */
	private int reservationId;

	/**
	 * Private constructor used by factory methods.
	 *
	 * @param success       whether payment succeeded
	 * @param message       descriptive message
	 * @param reservationId the reservation ID
	 * @param finalAmount   amount charged
	 * @param freedCapacity capacity freed
	 */
	private PayBillResult(boolean success, String message, int reservationId, Double finalAmount, int freedCapacity) {
		this.success = success;
		this.message = message;
		this.reservationId = reservationId;
		this.finalAmount = finalAmount;
		this.freedCapacity = freedCapacity;
	}

	/**
	 * Returns whether the payment succeeded.
	 *
	 * @return true if payment succeeded
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Returns the message describing the result.
	 *
	 * @return result message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the final amount that was charged.
	 *
	 * @return final amount or null if payment failed
	 */
	public Double getFinalAmount() {
		return finalAmount;
	}

	/**
	 * Returns the table capacity freed as a result of payment.
	 * <p>
	 * This value is used to determine if waiting customers should be notified about
	 * available space.
	 * </p>
	 *
	 * @return freed capacity (number of seats)
	 */
	public int getFreedCapacity() {
		return freedCapacity;
	}

	/**
	 * Returns the reservation ID that was paid.
	 *
	 * @return reservation ID
	 */
	public int getReservationId() {
		return reservationId;
	}

	// ==================== Factory Methods ====================

	/**
	 * Factory method for successful payment.
	 *
	 * @param reservationId the reservation that was paid
	 * @param amount        final amount charged
	 * @param freedCapacity capacity freed (seats available)
	 * @return success result
	 */
	public static PayBillResult ok(int reservationId, double amount, int freedCapacity) {
		return new PayBillResult(true, "PAID", reservationId, amount, freedCapacity);
	}

	/**
	 * Factory method for failed payment.
	 *
	 * @param msg failure message
	 * @return failed result
	 */
	public static PayBillResult fail(String msg) {
		return new PayBillResult(false, msg, 0, null, 0);
	}
}
