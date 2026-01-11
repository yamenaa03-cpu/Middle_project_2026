package common.dto.Authentication;

import java.io.Serializable;
import common.enums.AuthOperation;

public class SubscriberAuthRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private AuthOperation operation;

    // SUBSCRIPTION_CODE
    private String subscriptionCode;

    public static SubscriberAuthRequest createAuthRequest(String code) {
        SubscriberAuthRequest r = new SubscriberAuthRequest();
        r.operation = AuthOperation.SUBSCRIPTION_CODE;
        r.subscriptionCode = code;
        return r;
    }
    
    public static SubscriberAuthRequest createLoggedInStatusRequest() {
    	SubscriberAuthRequest r = new SubscriberAuthRequest();
    	r.operation = AuthOperation.LOGGED_IN_STATUS;
    	return r;
    }

    public AuthOperation getOperation() { return operation; }
    public String getSubscriptionCode() { return subscriptionCode; }
}
