package common.dto.Reservation;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.enums.ReservationOperation;

/**
 * A message sent from the client to the server containing the requested
 * operation and relevant parameters. Shared between both projects (client &
 * server).
 * 
 * @author: Yamen Abu Ahmad
 * @version 1.0
 */

/* This class */
public class ReservationRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private ReservationOperation operation;

	private int reservationId;
	private LocalDateTime reservationDateTime;
	private int numberOfGuests;
	private int customerId;
	
	private int billId;

	// Guest identification fields (for CREATE_GUEST_RESERVATION / JOIN_WAITLIST,
	// etc.)
	private String fullName;
	private String phone;
	private String email;
	private int confirmationCode;

	/*
	 * identify the request as an instance of Reservation request class and saves
	 * the GET_ALL_RESERVATIONS operation as a field in the class
	 */
	public static ReservationRequest createGetAllReservationsRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_ALL_RESERVATIONS;
		return req;
	}

	/*
	 * identify the request as an instance of Reservation request class and saves
	 * the UPDATE_RESERVATION_FIELDS operation as a field in the class
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

	/*
	 * identify the request as an instance of Reservation request class and saves
	 * the CREATE_RESERVATION_FIELDS operation as a field in the class
	 */
	public static ReservationRequest createCreateReservationRequest(LocalDateTime dateTime, int guests) {

		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.CREATE_RESERVATION;
		req.reservationDateTime = dateTime;
		req.numberOfGuests = guests;
		return req;
	}

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

	public static ReservationRequest createResendConfirmationCodeRequest(String phone, String email) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.RESEND_CONFIRMATION_CODE;
		req.phone = phone;
		req.email = email;
		return req;
	}

	public static ReservationRequest createGetCancellableReservationsRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION;
		return req;
	}

	public static ReservationRequest createGetCancellableReservationByConfirmationCodeRequest(int confirmationCode) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CANCELLATION;
		req.confirmationCode = confirmationCode;
		return req;
	}

	/*
	 * identify the request as an instance of Reservation request class and saves
	 * the CANCEL_RESERVATION_FIELDS operation as a field in the class
	 */
	public static ReservationRequest createCancelReservationRequest(int reservationId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.CANCEL_RESERVATION;
		req.reservationId = reservationId;
		return req;
	}

	public static ReservationRequest createGetReceivableReservationsRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING;
		return req;
	}

	public static ReservationRequest createGetReceivableReservationByConfirmationCodeRequest(int confirmationCode) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_RECEIVING;
		req.confirmationCode = confirmationCode;
		return req;
	}

	public static ReservationRequest createReceiveTableRequest(int reservationId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.RECEIVE_TABLE;
		req.reservationId = reservationId;
		return req;
	}

	public static ReservationRequest createJoinWaitlistRequest(int guests) {

		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.JOIN_WAITLIST;
		req.numberOfGuests = guests;
		return req;
	}

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
	
	public static ReservationRequest createGetPayableReservationsRequest() {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT;
		return req;
	}
	
	public static ReservationRequest createGetPayableReservationByConfirmationCodeRequest(int confirmationCode) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CHECKOUT;
		req.confirmationCode = confirmationCode;
		return req;
	}
	
	public static ReservationRequest createGetBillForPayingRequest(int reservationId) {
	    ReservationRequest req = new ReservationRequest();
	    req.operation = ReservationOperation.GET_BILL_FOR_PAYING;
	    req.reservationId = reservationId;
	    return req;
	}

	public static ReservationRequest createPayBillByIdRequest(int billId) {
		ReservationRequest req = new ReservationRequest();
		req.operation = ReservationOperation.PAY_BILL;
		req.billId = billId;
		return req;
	}

	// private ReservationRequest() {}
	// Getters for the class fields
	public ReservationOperation getOperation() {
		return operation;
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

	// Guest getters (needed by CREATE_GUEST_RESERVATION case)
	public String getFullName() {
		return fullName;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public int getConfirmationCode() {
		return confirmationCode;
	}
	
	public int getBillId() {
		return billId;
	}

}
