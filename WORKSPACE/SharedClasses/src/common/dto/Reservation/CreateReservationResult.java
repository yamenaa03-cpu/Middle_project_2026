package common.dto.Reservation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Result of attempting to create a reservation. Includes success flag, message
 * and optionally created ids or suggested alternative times.
 */
public class CreateReservationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Integer reservationId;       // null if fail
    private final Integer confirmationCode;    // null if fail
    private final List<LocalDateTime> suggestions; // empty if success

    /**
     * Construct a create result.
     *
     * @param success whether creation succeeded
     * @param message descriptive message
     * @param reservationId created reservation id or null
     * @param confirmationCode confirmation code or null
     * @param suggestions suggested alternative times when no space
     */
    public CreateReservationResult(boolean success, String message,
                                   Integer reservationId, Integer confirmationCode,
                                   List<LocalDateTime> suggestions) {
        this.success = success;
        this.message = message;
        this.reservationId = reservationId;
        this.confirmationCode = confirmationCode;
        this.suggestions = suggestions;
    }

    /** @return true if creation succeeded */
    public boolean isSuccess() { return success; }
    /** @return message describing the result */
    public String getMessage() { return message; }
    /** @return created reservation id or null */
    public Integer getReservationId() { return reservationId; }
    /** @return confirmation code or null */
    public Integer getConfirmationCode() { return confirmationCode; }
    /** @return suggested alternative times when applicable */
    public List<LocalDateTime> getSuggestions() { return suggestions; }

    /**
     * Factory for a successful creation.
     *
     * @param id created reservation id
     * @param code confirmation code
     * @return success result
     */
    public static CreateReservationResult ok(int id, int code) {
        return new CreateReservationResult(true, "Reservation created.", id, code, List.of());
    }

    /**
     * Factory for a failed creation.
     *
     * @param msg failure message
     * @return failed result
     */
    public static CreateReservationResult fail(String msg) {
        return new CreateReservationResult(false, msg, null, null, List.of());
    }

    /**
     * Factory used when no space is available and suggestions are provided.
     *
     * @param msg failure message
     * @param sug list of suggested LocalDateTime values
     * @return result containing suggestions
     */
    public static CreateReservationResult noSpace(String msg, List<LocalDateTime> sug) {
        return new CreateReservationResult(false, msg, null, null, sug);
    }
}
