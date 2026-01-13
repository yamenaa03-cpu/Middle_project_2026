package common.dto.UserAccount;

import java.io.Serializable;

public class SubscriberLogInResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Integer subscriberId;
    private final String fullName;

    private SubscriberLogInResult(boolean success, String message, Integer subscriberId, String fullName) {
        this.success = success;
        this.message = message;
        this.subscriberId = subscriberId;
        this.fullName = fullName;
    }

    public static SubscriberLogInResult ok(int subscriberId, String fullName, String message) {
        return new SubscriberLogInResult(true, message, subscriberId, fullName);
    }

    public static SubscriberLogInResult fail(String message) {
        return new SubscriberLogInResult(false, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getSubscriberId() { return subscriberId; }
    public String getFullName() { return fullName; }
}
