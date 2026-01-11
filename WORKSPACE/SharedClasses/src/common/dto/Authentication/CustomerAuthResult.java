package common.dto.Authentication;

import java.io.Serializable;

/**
 * Result returned after attempting to authenticate a customer. Contains
 * success flag, message and subscriber identifier when applicable.
 */
public class CustomerAuthResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final int subscriberId;

    private CustomerAuthResult(boolean success, String message, Integer subscriberId) {
        this.success = success;
        this.message = message;
        this.subscriberId = subscriberId;
    }

    /**
     * Successful authentication result.
     *
     * @param subscriberId id of subscriber
     * @param message human-readable message
     * @return success result
     */
    public static CustomerAuthResult ok(int subscriberId, String message) {
        return new CustomerAuthResult(true, message, subscriberId);
    }

    /**
     * Failed authentication result.
     *
     * @param message failure message
     * @return failed result
     */
    public static CustomerAuthResult fail(String message) {
        return new CustomerAuthResult(false, message, null);
    }

    /** @return true if authentication succeeded */
    public boolean isSuccess() { return success; }
    /** @return message describing result */
    public String getMessage() { return message; }
    /** @return subscriber id when available */
    public Integer getSubscriberId() { return subscriberId; }
}
