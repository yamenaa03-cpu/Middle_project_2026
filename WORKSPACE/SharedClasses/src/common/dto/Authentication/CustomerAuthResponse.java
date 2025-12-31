package common.dto.Authentication;

import java.io.Serializable;

public class CustomerAuthResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Integer customerId;
    private boolean newCustomer;

    public CustomerAuthResponse(boolean success, String message, Integer customerId, boolean newCustomer) {
        this.success = success;
        this.message = message;
        this.customerId = customerId;
        this.newCustomer = newCustomer;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getCustomerId() { return customerId; }
    public boolean isNewCustomer() { return newCustomer; }
}
