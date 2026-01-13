package common.dto.Reservation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import common.entity.Bill;
import common.entity.Reservation;

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

    private ReservationResponse(boolean success, String message,
                                List<Reservation> reservations,
                                Integer reservationId,
                                Integer confirmationCode,
                                List<LocalDateTime> suggestedTimes,
                                Bill bill,
                                Double finalAmount) {
        this.success = success;
        this.message = message;
        this.reservations = reservations;
        this.reservationId = reservationId;
        this.confirmationCode = confirmationCode;
        this.suggestedTimes = suggestedTimes;
        this.bill = bill;
        this.finalAmount = finalAmount;
    }

    /* ---------------- Factories ---------------- */

    // generic ok/fail
    public static ReservationResponse ok(String message) {
        return new ReservationResponse(true, safeMsg(message, "OK"),
                null, null, null, null, null, null);
    }

    public static ReservationResponse fail(String message) {
        return new ReservationResponse(false, safeMsg(message, "Operation failed."),
                null, null, null, null, null, null);
    }

    // list payload
    public static ReservationResponse withReservations(boolean success, String message, List<Reservation> reservations) {
        return new ReservationResponse(success, safeMsg(message, success ? "OK" : "Operation failed."),
                reservations, null, null, null, null, null);
    }

    // create/join waitlist success (id + code)
    public static ReservationResponse created(int reservationId, int confirmationCode, String message) {
        return new ReservationResponse(true, safeMsg(message, "Created successfully."),
                null, reservationId, confirmationCode, null, null, null);
    }

    // create failure with suggestions
    public static ReservationResponse createFailedWithSuggestions(String message, List<LocalDateTime> suggestedTimes) {
        return new ReservationResponse(false, safeMsg(message, "No availability."),
                null, null, null, suggestedTimes, null, null);
    }

    // bill payload
    public static ReservationResponse billLoaded(Bill bill, String message) {
        if (bill == null) return fail("No bill found.");
        return new ReservationResponse(true, safeMsg(message, "Bill loaded."),
                null, null, null, null, bill, null);
    }

    // payment success (optional final amount)
    public static ReservationResponse paymentOk(String message, Double finalAmount) {
        return new ReservationResponse(true, safeMsg(message, "Payment successful."),
                null, null, null, null, null, finalAmount);
    }

    // one helper
    private static String safeMsg(String msg, String fallback) {
        return (msg == null || msg.isBlank()) ? fallback : msg;
    }
    
 // update result + return fresh list 
    public static ReservationResponse updated(boolean ok, String okMsg, String failMsg, List<Reservation> reservations) {
        return withReservations(ok, ok ? okMsg : failMsg, reservations);
    }

    // success/fail + empty list payload
    public static ReservationResponse emptyListFail(String message) {
        return withReservations(false, message, List.of());
    }
    public static ReservationResponse emptyListOk(String message) {
        return withReservations(true, message, List.of());
    }

    // "Your reservations loaded" / "No reservations found"
    public static ReservationResponse loadedOrEmpty(List<Reservation> list, String okMsg, String emptyMsg) {
        if (list == null || list.isEmpty()) {
            return withReservations(false, emptyMsg, List.of());
        }
        return withReservations(true, okMsg, list);
    }

    // resend confirmation: return success without payload
    public static ReservationResponse resendResult(int sentCount) {
        if (sentCount <= 0) return fail("No reservations found.");
        return ok("Sent " + sentCount + " code/s.");
    }

    // when you want id+code but no suggestions
    public static ReservationResponse createdOrFailed(CreateReservationResult r) {
        if (r == null) return fail("Operation failed.");
        if (r.isSuccess()) return created(r.getReservationId(), r.getConfirmationCode(), r.getMessage());
        return createFailedWithSuggestions(r.getMessage(), r.getSuggestions());
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
}
