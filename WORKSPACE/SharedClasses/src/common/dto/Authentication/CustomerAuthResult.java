package common.dto.Authentication;

import java.io.Serializable;

public class CustomerAuthResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Integer customerId;
    private final boolean newCustomer;

    private CustomerAuthResult(boolean success, String message, Integer customerId, boolean newCustomer) {
        this.success = success;
        this.message = message;
        this.customerId = customerId;
        this.newCustomer = newCustomer;
    }

    public static CustomerAuthResult ok(int customerId, boolean newCustomer, String message) {
        return new CustomerAuthResult(true, message, customerId, newCustomer);
    }

    public static CustomerAuthResult fail(String message) {
        return new CustomerAuthResult(false, message, null, false);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getCustomerId() { return customerId; }
    public boolean isNewCustomer() { return newCustomer; }
}
