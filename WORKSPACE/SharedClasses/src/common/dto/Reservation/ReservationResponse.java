package common.dto.Reservation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import common.entity.Bill;
import common.entity.Reservation;
import common.enums.ReservationOperation;

/**
 * Response DTO for reservation operations sent from server to client.
 * <p>
 * This class uses the factory pattern to create appropriate responses for
 * different reservation operations. Each response includes success status, a
 * descriptive message, and operation-specific data such as reservations,
 * confirmation codes, bills, or suggested times.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see ReservationOperation
 * @see ReservationRequest
 */
public class ReservationResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Whether the operation succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * List of reservations for list operations.
	 */
	private final List<Reservation> reservations;

	/**
	 * Created reservation ID.
	 */
	private final Integer reservationId;

	/**
	 * Confirmation code for created reservations.
	 */
	private final Integer confirmationCode;

	/**
	 * Suggested alternative times when requested time is unavailable.
	 */
	private final List<LocalDateTime> suggestedTimes;

	/**
	 * Bill data for payment operations.
	 */
	private final Bill bill;

	/**
	 * Final amount paid.
	 */
	private final Double finalAmount;

	/**
	 * The operation this response is for.
	 */
	private final ReservationOperation operation;

	/**
	 * Assigned table ID for table receiving operations.
	 */
	private final Integer tableId;

	/**
	 * Private constructor used by factory methods.
	 */
	private ReservationResponse(boolean success, String message, List<Reservation> reservations, Integer reservationId,
			Integer confirmationCode, List<LocalDateTime> suggestedTimes, Bill bill, Double finalAmount,
			Integer tableId, ReservationOperation operation) {
		this.success = success;
		this.message = message;
		this.reservations = reservations;
		this.reservationId = reservationId;
		this.confirmationCode = confirmationCode;
		this.suggestedTimes = suggestedTimes;
		this.bill = bill;
		this.finalAmount = finalAmount;
		this.operation = operation;
		this.tableId = tableId;
	}

	// ==================== Factory Methods ====================

	/**
	 * Creates a generic success response.
	 *
	 * @param message   success message
	 * @param operation the operation that succeeded
	 * @return success response
	 */
	public static ReservationResponse ok(String message, ReservationOperation operation) {
		return new ReservationResponse(true, safeMsg(message, "OK"), null, null, null, null, null, null, null,
				operation);
	}

	/**
	 * Creates a generic failure response.
	 *
	 * @param message   failure message
	 * @param operation the operation that failed
	 * @return failed response
	 */
	public static ReservationResponse fail(String message, ReservationOperation operation) {
		return new ReservationResponse(false, safeMsg(message, "Operation failed."), null, null, null, null, null, null,
				null, operation);
	}

	/**
	 * Creates a response with a list of reservations.
	 *
	 * @param success      whether the operation succeeded
	 * @param message      descriptive message
	 * @param reservations list of reservations
	 * @param operation    the operation type
	 * @return response with reservations
	 */
	public static ReservationResponse withReservations(boolean success, String message, List<Reservation> reservations,
			ReservationOperation operation) {
		return new ReservationResponse(success, safeMsg(message, success ? "OK" : "Operation failed."), reservations,
				null, null, null, null, null, null, operation);
	}

	/**
	 * Creates a successful reservation creation response.
	 *
	 * @param reservationId    the created reservation ID
	 * @param confirmationCode the generated confirmation code
	 * @param message          success message
	 * @param operation        the operation type
	 * @return success response with IDs
	 */
	public static ReservationResponse created(int reservationId, int confirmationCode, String message,
			ReservationOperation operation) {
		return new ReservationResponse(true, safeMsg(message, "Created successfully."), null, reservationId,
				confirmationCode, null, null, null, null, operation);
	}

	/**
	 * Creates a failed creation response with suggested alternative times.
	 *
	 * @param message        failure message
	 * @param suggestedTimes list of alternative available times
	 * @param operation      the operation type
	 * @return failed response with suggestions
	 */
	public static ReservationResponse createFailedWithSuggestions(String message, List<LocalDateTime> suggestedTimes,
			ReservationOperation operation) {
		return new ReservationResponse(false, safeMsg(message, "No availability."), null, null, null, suggestedTimes,
				null, null, null, operation);
	}

	/**
	 * Creates a response with bill data.
	 *
	 * @param bill      the bill entity
	 * @param message   descriptive message
	 * @param operation the operation type
	 * @return response with bill data
	 */
	public static ReservationResponse billLoaded(Bill bill, String message, ReservationOperation operation) {
		if (bill == null)
			return fail("No bill found.", operation);
		return new ReservationResponse(true, safeMsg(message, "Bill loaded."), null, null, null, null, bill, null, null,
				operation);
	}

	/**
	 * Creates a successful payment response.
	 *
	 * @param message     success message
	 * @param finalAmount the amount that was paid
	 * @param operation   the operation type
	 * @return success response with amount
	 */
	public static ReservationResponse paymentOk(String message, Double finalAmount, ReservationOperation operation) {
		return new ReservationResponse(true, safeMsg(message, "Payment successful."), null, null, null, null, null,
				finalAmount, null, operation);
	}

	/**
	 * Returns a safe message, using fallback if input is null or blank.
	 */
	private static String safeMsg(String msg, String fallback) {
		return (msg == null || msg.isBlank()) ? fallback : msg;
	}

	/**
	 * Creates an update result response with fresh reservation list.
	 *
	 * @param ok           whether update succeeded
	 * @param okMsg        success message
	 * @param failMsg      failure message
	 * @param reservations updated reservation list
	 * @param operation    the operation type
	 * @return response with result
	 */
	public static ReservationResponse updated(boolean ok, String okMsg, String failMsg, List<Reservation> reservations,
			ReservationOperation operation) {
		return withReservations(ok, ok ? okMsg : failMsg, reservations, operation);
	}

	/**
	 * Creates a failed response with empty list.
	 *
	 * @param message   failure message
	 * @param operation the operation type
	 * @return failed response with empty list
	 */
	public static ReservationResponse emptyListFail(String message, ReservationOperation operation) {
		return withReservations(false, message, List.of(), operation);
	}

	/**
	 * Creates a success response with empty list.
	 *
	 * @param message   success message
	 * @param operation the operation type
	 * @return success response with empty list
	 */
	public static ReservationResponse emptyListOk(String message, ReservationOperation operation) {
		return withReservations(true, message, List.of(), operation);
	}

	/**
	 * Creates a response based on whether the list is empty or not.
	 *
	 * @param list      the reservations list
	 * @param okMsg     message if list has items
	 * @param emptyMsg  message if list is empty
	 * @param operation the operation type
	 * @return appropriate response
	 */
	public static ReservationResponse loadedOrEmpty(List<Reservation> list, String okMsg, String emptyMsg,
			ReservationOperation operation) {
		if (list == null || list.isEmpty()) {
			return withReservations(false, emptyMsg, List.of(), operation);
		}
		return withReservations(true, okMsg, list, operation);
	}

	/**
	 * Creates a response for resend confirmation code operation.
	 *
	 * @param sentCount number of confirmation codes sent
	 * @param operation the operation type
	 * @return response indicating how many codes were sent
	 */
	public static ReservationResponse resendResult(int sentCount, ReservationOperation operation) {
		if (sentCount <= 0)
			return fail("No reservations found.", operation);
		return ok("Sent " + sentCount + " code/s.", operation);
	}

	/**
	 * Creates a response from a CreateReservationResult.
	 *
	 * @param r         the creation result
	 * @param operation the operation type
	 * @return appropriate response based on result
	 */
	public static ReservationResponse createdOrFailed(CreateReservationResult r, ReservationOperation operation) {
		if (r == null)
			return fail("Operation failed.", operation);
		if (r.isSuccess())
			return created(r.getReservationId(), r.getConfirmationCode(), r.getMessage(), operation);
		return createFailedWithSuggestions(r.getMessage(), r.getSuggestions(), operation);
	}

	/**
	 * Creates a successful table assignment response.
	 *
	 * @param tableNumber the assigned table number
	 * @param message     success message
	 * @return response with table number
	 */
	public static ReservationResponse tableAssigned(int tableNumber, String message) {
		return new ReservationResponse(true, safeMsg(message, "Table Assigned\nYour table number is " + tableNumber),
				null, null, null, null, null, null, tableNumber, ReservationOperation.RECEIVE_TABLE);
	}

	// ==================== Getters ====================

	/**
	 * Returns whether the operation succeeded.
	 *
	 * @return true if successful
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Returns the result message.
	 *
	 * @return descriptive message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the list of reservations.
	 *
	 * @return reservations or null
	 */
	public List<Reservation> getReservations() {
		return reservations;
	}

	/**
	 * Returns the created reservation ID.
	 *
	 * @return reservation ID or null
	 */
	public Integer getReservationId() {
		return reservationId;
	}

	/**
	 * Returns the confirmation code.
	 *
	 * @return confirmation code or null
	 */
	public Integer getConfirmationCode() {
		return confirmationCode;
	}

	/**
	 * Returns the suggested alternative times.
	 *
	 * @return list of suggested times or null
	 */
	public List<LocalDateTime> getSuggestedTimes() {
		return suggestedTimes;
	}

	/**
	 * Returns the bill data.
	 *
	 * @return bill or null
	 */
	public Bill getBill() {
		return bill;
	}

	/**
	 * Returns the final amount paid.
	 *
	 * @return final amount or null
	 */
	public Double getFinalAmount() {
		return finalAmount;
	}

	/**
	 * Returns the operation this response is for.
	 *
	 * @return the operation type
	 */
	public ReservationOperation getOperation() {
		return operation;
	}
}
