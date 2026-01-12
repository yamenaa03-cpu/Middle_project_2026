package common.dto.Reservation;

import java.io.Serializable;

/**
 * Result object returned after attempting to pay a bill. Contains payment
 * outcome, final amount charged and any capacity freed as a result.
 */
public class PayBillResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private final boolean success;
	private final String message;
	private final Double finalAmount;
	private int freedCapacity;
	private int reservationId;

	private PayBillResult(boolean success, String message, int reservationId, Double finalAmount, int freedCapacity) {
		this.success = success;
		this.message = message;
		this.reservationId = reservationId;
		this.finalAmount = finalAmount;
		this.freedCapacity = freedCapacity;
	}

	/**
	 * Whether the payment succeeded.
	 *
	 * @return true if payment succeeded
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Message describing the result.
	 *
	 * @return message string
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Final amount that was charged (when available).
	 *
	 * @return final amount or null
	 */
	public Double getFinalAmount() {
		return finalAmount;
	}

	/**
	 * Capacity freed as a result of payment (e.g., table capacity).
	 *
	 * @return freed capacity units
	 */
	public int getFreedCapacity() {
		return freedCapacity;
	}
	
	
	public int getReservationId() {
		return reservationId;
	}

	// ===== Factory methods =====

	/**
	 * Successful payment result.
	 *
	 * @param amount        final amount charged
	 * @param freedCapacity capacity freed
	 * @return success result
	 */
	public static PayBillResult ok(int reservationId, double amount, int freedCapacity) {
		return new PayBillResult(true, "PAID", reservationId, amount, freedCapacity);
	}

	/**
	 * Failed payment result with message.
	 *
	 * @param msg failure message
	 * @return failed result
	 */
	public static PayBillResult fail(String msg) {
		return new PayBillResult(false, msg, 0, null, 0);
	}
}
