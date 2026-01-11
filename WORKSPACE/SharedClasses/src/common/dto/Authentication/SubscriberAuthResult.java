package common.dto.Authentication;

import java.io.Serializable;

public class SubscriberAuthResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Integer subscriberId;
    private final String fullName;

    private SubscriberAuthResult(boolean success, String message, Integer subscriberId, String fullName) {
        this.success = success;
        this.message = message;
        this.subscriberId = subscriberId;
        this.fullName = fullName;
    }

    public static SubscriberAuthResult ok(int subscriberId, String fullName, String message) {
        return new SubscriberAuthResult(true, message, subscriberId, fullName);
    }

    public static SubscriberAuthResult fail(String message) {
        return new SubscriberAuthResult(false, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getSubscriberId() { return subscriberId; }
    public String getFullName() { return fullName; }
}
