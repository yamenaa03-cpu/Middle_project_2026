package common.dto.Authentication;

import java.io.Serializable;

public class CustomerAuthResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private int subscriberId;

    public CustomerAuthResponse(boolean success, String message, Integer subscriberId) {
        this.success = success;
        this.message = message;
        this.subscriberId = subscriberId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getSubscriberId() { return subscriberId; }
}
