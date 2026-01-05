package common.dto.Authentication;

import java.io.Serializable;

public class CustomerAuthResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Integer customerId;

    private CustomerAuthResult(boolean success, String message, Integer customerId) {
        this.success = success;
        this.message = message;
        this.customerId = customerId;
    }

    public static CustomerAuthResult ok(int customerId, String message) {
        return new CustomerAuthResult(true, message, customerId);
    }

    public static CustomerAuthResult fail(String message) {
        return new CustomerAuthResult(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getCustomerId() { return customerId; }
}
