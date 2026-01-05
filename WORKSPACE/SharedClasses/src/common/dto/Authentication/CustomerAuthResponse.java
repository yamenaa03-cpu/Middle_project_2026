package common.dto.Authentication;

import java.io.Serializable;

public class CustomerAuthResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Integer customerId;

    public CustomerAuthResponse(boolean success, String message, Integer customerId) {
        this.success = success;
        this.message = message;
        this.customerId = customerId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getCustomerId() { return customerId; }
}
