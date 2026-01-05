package common.dto.Authentication;

import java.io.Serializable;

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

    public static CustomerAuthResult ok(int subscriberId, String message) {
        return new CustomerAuthResult(true, message, subscriberId);
    }

    public static CustomerAuthResult fail(String message) {
        return new CustomerAuthResult(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getSubscriberId() { return subscriberId; }
}
