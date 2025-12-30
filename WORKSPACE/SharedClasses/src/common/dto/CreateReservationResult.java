package common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class CreateReservationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Integer reservationId;       // null if fail
    private final Integer confirmationCode;    // null if fail
    private final List<LocalDateTime> suggestions; // empty if success

    public CreateReservationResult(boolean success, String message,
                                   Integer reservationId, Integer confirmationCode,
                                   List<LocalDateTime> suggestions) {
        this.success = success;
        this.message = message;
        this.reservationId = reservationId;
        this.confirmationCode = confirmationCode;
        this.suggestions = suggestions;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getReservationId() { return reservationId; }
    public Integer getConfirmationCode() { return confirmationCode; }
    public List<LocalDateTime> getSuggestions() { return suggestions; }

    public static CreateReservationResult ok(int id, int code) {
        return new CreateReservationResult(true, "Reservation created.", id, code, List.of());
    }

    public static CreateReservationResult fail(String msg) {
        return new CreateReservationResult(false, msg, null, null, List.of());
    }

    public static CreateReservationResult noSpace(String msg, List<LocalDateTime> sug) {
        return new CreateReservationResult(false, msg, null, null, sug);
    }
}
