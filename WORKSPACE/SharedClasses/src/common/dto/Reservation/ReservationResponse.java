package common.dto.Reservation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import common.entity.Bill;
import common.entity.Reservation;
import common.enums.ReservationOperation;

public class ReservationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;

    // payloads (optional)
    private final List<Reservation> reservations;
    private final Integer reservationId;
    private final Integer confirmationCode;
    private final List<LocalDateTime> suggestedTimes;

    private final Bill bill;
    private final Double finalAmount;
    private final ReservationOperation operation;

    private ReservationResponse(boolean success, String message,
                                List<Reservation> reservations,
                                Integer reservationId,
                                Integer confirmationCode,
                                List<LocalDateTime> suggestedTimes,
                                Bill bill,
                                Double finalAmount, ReservationOperation operation) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
        this.reservationId = reservationId;
        this.confirmationCode = confirmationCode;
        this.suggestedTimes = suggestedTimes;
        this.bill = bill;
        this.finalAmount = finalAmount;
        this.operation = operation;
    }

    /* ---------------- Factories ---------------- */

    // generic ok/fail
    public static ReservationResponse ok(String message, ReservationOperation operation) {
        return new ReservationResponse(true, safeMsg(message, "OK"),
                null, null, null, null, null, null, operation);
    }

    public static ReservationResponse fail(String message, ReservationOperation operation) {
        return new ReservationResponse(false, safeMsg(message, "Operation failed."),
                null, null, null, null, null, null, operation);
    }

    // list payload
    public static ReservationResponse withReservations(boolean success, String message, List<Reservation> reservations, ReservationOperation operation) {
        return new ReservationResponse(success, safeMsg(message, success ? "OK" : "Operation failed."),
                reservations, null, null, null, null, null, operation);
    }

    // create/join waitlist success (id + code)
    public static ReservationResponse created(int reservationId, int confirmationCode, String message, ReservationOperation operation) {
        return new ReservationResponse(true, safeMsg(message, "Created successfully."),
                null, reservationId, confirmationCode, null, null, null, operation);
    }

    // create failure with suggestions
    public static ReservationResponse createFailedWithSuggestions(String message, List<LocalDateTime> suggestedTimes, ReservationOperation operation) {
        return new ReservationResponse(false, safeMsg(message, "No availability."),
                null, null, null, suggestedTimes, null, null, operation);
    }

    // bill payload
    public static ReservationResponse billLoaded(Bill bill, String message, ReservationOperation operation) {
        if (bill == null) return fail("No bill found.", operation);
        return new ReservationResponse(true, safeMsg(message, "Bill loaded."),
                null, null, null, null, bill, null, operation);
    }

    // payment success (optional final amount)
    public static ReservationResponse paymentOk(String message, Double finalAmount, ReservationOperation operation) {
        return new ReservationResponse(true, safeMsg(message, "Payment successful."),
                null, null, null, null, null, finalAmount, operation);
    }

    // one helper
    private static String safeMsg(String msg, String fallback) {
        return (msg == null || msg.isBlank()) ? fallback : msg;
    }
    
 // update result + return fresh list 
    public static ReservationResponse updated(boolean ok, String okMsg, String failMsg, List<Reservation> reservations, ReservationOperation operation) {
        return withReservations(ok, ok ? okMsg : failMsg, reservations, operation);
    }

    // success/fail + empty list payload
    public static ReservationResponse emptyListFail(String message, ReservationOperation operation) {
        return withReservations(false, message, List.of(), operation);
    }
    public static ReservationResponse emptyListOk(String message, ReservationOperation operation) {
        return withReservations(true, message, List.of(), operation);
    }

    // "Your reservations loaded" / "No reservations found"
    public static ReservationResponse loadedOrEmpty(List<Reservation> list, String okMsg, String emptyMsg, ReservationOperation operation) {
        if (list == null || list.isEmpty()) {
            return withReservations(false, emptyMsg, List.of(), operation);
        }
        return withReservations(true, okMsg, list, operation);
    }

    // resend confirmation: return success without payload
    public static ReservationResponse resendResult(int sentCount, ReservationOperation operation) {
        if (sentCount <= 0) return fail("No reservations found.", operation);
        return ok("Sent " + sentCount + " code/s.", operation);
    }

    // when you want id+code but no suggestions
    public static ReservationResponse createdOrFailed(CreateReservationResult r, ReservationOperation operation) {
        if (r == null) return fail("Operation failed.", operation);
        if (r.isSuccess()) return created(r.getReservationId(), r.getConfirmationCode(), r.getMessage(), operation);
        return createFailedWithSuggestions(r.getMessage(), r.getSuggestions(), operation);
    }


    /* ---------------- Getters ---------------- */

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public List<Reservation> getReservations() { return reservations; }

    public Integer getReservationId() { return reservationId; }
    public Integer getConfirmationCode() { return confirmationCode; }

    public List<LocalDateTime> getSuggestedTimes() { return suggestedTimes; }

    public Bill getBill() { return bill; }
    public Double getFinalAmount() { return finalAmount; }
    public ReservationOperation getOperation() { return operation; }
}
