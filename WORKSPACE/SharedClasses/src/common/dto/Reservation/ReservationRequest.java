package common.dto.Reservation;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.enums.ReservationOperation;

/**
 * Request DTO for reservation operations sent from client to server.
 * <p>
 * This class uses the factory pattern to create requests for different
 * reservation operations. Each factory method sets the appropriate operation
 * type and required parameters. The class supports both direct customer
 * operations and employee "on-behalf" operations for managing customer
 * reservations.
 * </p>
 *
 * <h2>Supported Operations:</h2>
 * <ul>
 * <li>Create and manage reservations</li>
 * <li>Join and manage waitlist entries</li>
 * <li>Cancel reservations</li>
 * <li>Receive table assignments</li>
 * <li>View and pay bills</li>
 * <li>View reservation history</li>
 * </ul>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see ReservationOperation
 * @see ReservationResponse
 */
public class ReservationRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The reservation operation being requested.
	 */
	private ReservationOperation operation;

	/**
	 * The reservation ID for operations on existing reservations.
	 */
	private int reservationId;

	/**
	 * The requested date and time for the reservation.
	 */
	private LocalDateTime reservationDateTime;

	/**
	 * Number of guests in the party.
	 */
	private int numberOfGuests;

	/**
	 * Target customer ID for employee on-behalf operations.
	 */
	private Integer targetCustomerId;

	/**
	 * Bill ID for payment operations.
	 */
	private int billId;

	/**
	 * Flag indicating if this is an employee on-behalf operation.
	 */
	boolean isOnBehalf = false;

	/**
	 * Guest's full name for guest reservations.
	 */
	private String fullName;

	/**
	 * Guest's phone number for identification.
	 */
	private String phone;

	/**
	 * Guest's email address for identification.
	 */
	private String email;

	/**
	 * Confirmation code for lookup operations.
	 */
	private int confirmationCode;

	/**
	 * Target subscription code for employee lookups.
	 */
	private String targetSubscriptionCode;

	/**
	 * Creates a request to get all active reservations (employee only).
	 *
	 * @return request for active reservations
	 */
	public static ReservationRequest createGetActiveReservationsRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_ACTIVE_RESERVATIONS;
		return req;
	}

	/**
	 * Creates a request to get all waitlist entries (employee only).
	 *
	 * @return request for waitlist
	 */
	public static ReservationRequest createGetWaitlistRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_WAITLIST;
		return req;
	}

	/**
	 * Creates a request to update an existing reservation.
	 *
	 * @param ReservationNumber the reservation ID to update
	 * @param newDateTime       the new date and time
	 * @param newGuests         the new guest count
	 * @return request for updating reservation
	 */
	public static ReservationRequest createUpdateReservationRequest(int ReservationNumber, LocalDateTime newDateTime,
			int newGuests) {

		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.UPDATE_RESERVATION_FIELDS;
		req.reservationId = ReservationNumber;
		req.reservationDateTime = newDateTime;
		req.numberOfGuests = newGuests;
		return req;
	}

	/**
	 * Creates a reservation request for a logged-in subscriber.
	 *
	 * @param dateTime the requested date and time
	 * @param guests   number of guests
	 * @return request for creating a reservation
	 */
	public static ReservationRequest createCreateReservationRequest(LocalDateTime dateTime, int guests) {

		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.CREATE_RESERVATION;
		req.reservationDateTime = dateTime;
		req.numberOfGuests = guests;
		return req;
	}

	/**
	 * Creates a reservation request for a guest (not logged in).
	 *
	 * @param dateTime the requested date and time
	 * @param guests   number of guests
	 * @param fullName guest's full name
	 * @param phone    guest's phone number
	 * @param email    guest's email address
	 * @return request for creating a guest reservation
	 */
	public static ReservationRequest createCreateGuestReservationRequest(LocalDateTime dateTime, int guests,
			String fullName, String phone, String email) {

		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.CREATE_RESERVATION;
		req.reservationDateTime = dateTime;
		req.numberOfGuests = guests;
		req.fullName = fullName;
		req.phone = phone;
		req.email = email;
		return req;
	}

	/**
	 * Creates a request to resend confirmation code.
	 *
	 * @param phone customer's phone number
	 * @param email customer's email address
	 * @return request for resending confirmation
	 */
	public static ReservationRequest createResendConfirmationCodeRequest(String phone, String email) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.RESEND_CONFIRMATION_CODE;
		req.phone = phone;
		req.email = email;
		return req;
	}

	/**
	 * Creates a request to get cancellable reservations for the logged-in customer.
	 *
	 * @return request for cancellable reservations
	 */
	public static ReservationRequest createGetCancellableReservationsRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION;
		return req;
	}

	/**
	 * Creates a request to find a cancellable reservation by confirmation code.
	 *
	 * @param confirmationCode the confirmation code to look up
	 * @return request for finding cancellable reservation
	 */
	public static ReservationRequest createGetCancellableReservationByConfirmationCodeRequest(int confirmationCode) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CANCELLATION;
		req.confirmationCode = confirmationCode;
		return req;
	}

	/**
	 * Creates a request to cancel a reservation.
	 *
	 * @param reservationId the reservation to cancel
	 * @return request for cancellation
	 */
	public static ReservationRequest createCancelReservationRequest(int reservationId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.CANCEL_RESERVATION;
		req.reservationId = reservationId;
		return req;
	}

	/**
	 * Creates a request to get reservations ready for table receiving.
	 *
	 * @return request for receivable reservations
	 */
	public static ReservationRequest createGetReceivableReservationsRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING;
		return req;
	}

	/**
	 * Creates a request to find a receivable reservation by confirmation code.
	 *
	 * @param confirmationCode the confirmation code to look up
	 * @return request for finding receivable reservation
	 */
	public static ReservationRequest createGetReceivableReservationByConfirmationCodeRequest(int confirmationCode) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_RECEIVING;
		req.confirmationCode = confirmationCode;
		return req;
	}

	/**
	 * Creates a request to receive a table assignment.
	 *
	 * @param reservationId the reservation to check in
	 * @return request for table receiving
	 */
	public static ReservationRequest createReceiveTableRequest(int reservationId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.RECEIVE_TABLE;
		req.reservationId = reservationId;
		return req;
	}

	/**
	 * Creates a request to join the waitlist for a subscriber.
	 *
	 * @param guests number of guests
	 * @return request for joining waitlist
	 */
	public static ReservationRequest createJoinWaitlistRequest(int guests) {

		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.JOIN_WAITLIST;
		req.numberOfGuests = guests;
		return req;
	}

	/**
	 * Creates a request for a guest to join the waitlist.
	 *
	 * @param guests   number of guests
	 * @param fullName guest's full name
	 * @param phone    guest's phone number
	 * @param email    guest's email address
	 * @return request for guest joining waitlist
	 */
	public static ReservationRequest createJoinGuestWaitlistRequest(int guests, String fullName, String phone,
			String email) {

		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.JOIN_WAITLIST;
		req.numberOfGuests = guests;
		req.fullName = fullName;
		req.phone = phone;
		req.email = email;
		return req;
	}

	/**
	 * Creates a request to get reservations ready for checkout/payment.
	 *
	 * @return request for payable reservations
	 */
	public static ReservationRequest createGetPayableReservationsRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT;
		return req;
	}

	/**
	 * Creates a request to find a payable reservation by confirmation code.
	 *
	 * @param confirmationCode the confirmation code to look up
	 * @return request for finding payable reservation
	 */
	public static ReservationRequest createGetPayableReservationByConfirmationCodeRequest(int confirmationCode) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CHECKOUT;
		req.confirmationCode = confirmationCode;
		return req;
	}

	/**
	 * Creates a request to get or generate a bill for payment.
	 *
	 * @param reservationId the reservation to get bill for
	 * @return request for bill retrieval
	 */
	public static ReservationRequest createGetBillForPayingRequest(int reservationId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_BILL_FOR_PAYING;
		req.reservationId = reservationId;
		return req;
	}

	/**
	 * Creates a request to pay a bill.
	 *
	 * @param billId the bill to pay
	 * @return request for bill payment
	 */
	public static ReservationRequest createPayBillByIdRequest(int billId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.PAY_BILL;
		req.billId = billId;
		return req;
	}

	// ==================== Employee On-Behalf Factory Methods ====================

	/**
	 * Creates a reservation on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer to create reservation for
	 * @param dateTime         the requested date and time
	 * @param guests           number of guests
	 * @return request for on-behalf reservation creation
	 */
	public static ReservationRequest createCreateReservationOnBehalfRequest(int targetCustomerId,
			LocalDateTime dateTime, int guests) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.CREATE_RESERVATION;
		req.targetCustomerId = targetCustomerId;
		req.reservationDateTime = dateTime;
		req.numberOfGuests = guests;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Creates a waitlist entry on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer to add to waitlist
	 * @param guests           number of guests
	 * @return request for on-behalf waitlist join
	 */
	public static ReservationRequest createJoinWaitlistOnBehalfRequest(int targetCustomerId, int guests) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.JOIN_WAITLIST;
		req.targetCustomerId = targetCustomerId;
		req.numberOfGuests = guests;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Gets cancellable reservations on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer to get reservations for
	 * @return request for on-behalf cancellable reservations
	 */
	public static ReservationRequest createGetCancellableReservationsOnBehalfRequest(int targetCustomerId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION;
		req.targetCustomerId = targetCustomerId;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Cancels a reservation on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer who owns the reservation
	 * @param reservationId    the reservation to cancel
	 * @return request for on-behalf cancellation
	 */
	public static ReservationRequest createCancelReservationOnBehalfRequest(int targetCustomerId, int reservationId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.CANCEL_RESERVATION;
		req.targetCustomerId = targetCustomerId;
		req.reservationId = reservationId;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Gets receivable reservations on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer to get reservations for
	 * @return request for on-behalf receivable reservations
	 */
	public static ReservationRequest createGetReceivableReservationsOnBehalfRequest(int targetCustomerId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING;
		req.targetCustomerId = targetCustomerId;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Receives a table on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer to check in
	 * @param reservationId    the reservation to check in
	 * @return request for on-behalf table receiving
	 */
	public static ReservationRequest createReceiveTableOnBehalfRequest(int targetCustomerId, int reservationId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.RECEIVE_TABLE;
		req.targetCustomerId = targetCustomerId;
		req.reservationId = reservationId;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Gets payable reservations on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer to get reservations for
	 * @return request for on-behalf payable reservations
	 */
	public static ReservationRequest createGetPayableReservationsOnBehalfRequest(int targetCustomerId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT;
		req.targetCustomerId = targetCustomerId;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Gets a bill on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer who owns the reservation
	 * @param reservationId    the reservation to get bill for
	 * @return request for on-behalf bill retrieval
	 */
	public static ReservationRequest createGetBillForPayingOnBehalfRequest(int targetCustomerId, int reservationId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_BILL_FOR_PAYING;
		req.targetCustomerId = targetCustomerId;
		req.reservationId = reservationId;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Pays a bill on behalf of a customer (employee only).
	 *
	 * @param targetCustomerId the customer who owns the bill
	 * @param billId           the bill to pay
	 * @return request for on-behalf bill payment
	 */
	public static ReservationRequest createPayBillOnBehalfRequest(int targetCustomerId, int billId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.PAY_BILL;
		req.targetCustomerId = targetCustomerId;
		req.billId = billId;
		req.isOnBehalf = true;
		return req;
	}

	/**
	 * Creates a request to get the subscriber's reservation history.
	 *
	 * @return request for subscriber history
	 */
	public static ReservationRequest createGetSubscriberHistoryRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_SUBSCRIBER_HISTORY;
		return req;
	}

	// ==================== Getters ====================

	/**
	 * Returns the operation type for this request.
	 *
	 * @return the reservation operation
	 */
	public ReservationOperation getOperation() {
		return operation;
	}

	/**
	 * Returns the reservation ID if applicable.
	 *
	 * @return reservation ID
	 */
	public int getReservationId() {
		return reservationId;
	}

	/**
	 * Returns the requested date and time.
	 *
	 * @return reservation date/time
	 */
	public LocalDateTime getReservationDateTime() {
		return reservationDateTime;
	}

	/**
	 * Returns the number of guests.
	 *
	 * @return guest count
	 */
	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	/**
	 * Returns the guest's full name.
	 *
	 * @return full name or null
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Returns the phone number.
	 *
	 * @return phone number or null
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Returns the email address.
	 *
	 * @return email address or null
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Returns the confirmation code for lookup.
	 *
	 * @return confirmation code
	 */
	public int getConfirmationCode() {
		return confirmationCode;
	}

	/**
	 * Returns the bill ID for payment operations.
	 *
	 * @return bill ID
	 */
	public int getBillId() {
		return billId;
	}

	/**
	 * Returns the target subscription code for employee lookups.
	 *
	 * @return subscription code or null
	 */
	public String getTargetSubscriptionCode() {
		return targetSubscriptionCode;
	}

	/**
	 * Returns the target customer ID for on-behalf operations.
	 *
	 * @return target customer ID or null
	 */
	public Integer getTargetCustomerId() {
		return targetCustomerId;
	}
}
