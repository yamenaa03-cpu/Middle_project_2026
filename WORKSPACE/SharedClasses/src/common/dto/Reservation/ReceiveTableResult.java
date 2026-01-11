package common.dto.Reservation;

import java.io.Serializable;

/**
 * Result object for receive-table operations. Indicates success and provides
 * a message.
 */
public class ReceiveTableResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;

    private ReceiveTableResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Whether the operation succeeded.
     *
     * @return true when table was successfully received
     */
    public boolean isSuccess() { return success; }

    /**
     * Descriptive message for the result.
     *
     * @return message string
     */
    public String getMessage() { return message; }

    /**
     * Factory for successful result.
     *
     * @return ok result
     */
    public static ReceiveTableResult ok() {
        return new ReceiveTableResult(true, "TABLE_RECEIVED");
    }

    /**
     * Factory for failed result with custom message.
     *
     * @param msg failure message
     * @return failed result
     */
    public static ReceiveTableResult fail(String msg) {
        return new ReceiveTableResult(false, msg);
    }
}
