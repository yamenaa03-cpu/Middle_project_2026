package common.dto.UserAccount;

import java.io.Serializable;
import common.enums.UserAccountOperation;

public class UserAccountRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private UserAccountOperation operation;

    // SUBSCRIPTION_CODE
    private String subscriptionCode;
    
    private String fullName;
    private String phone;
    private String email;

    public static UserAccountRequest createLogoutRequest() {
        UserAccountRequest r = new UserAccountRequest();
        r.operation = UserAccountOperation.LOGOUT;
        return r;
    }
    
    public static UserAccountRequest createSubscriberLogInRequest(String code) {
        UserAccountRequest r = new UserAccountRequest();
        r.operation = UserAccountOperation.SUBSCRIPTION_CODE;
        r.subscriptionCode = code;
        return r;
    }
    
    public static UserAccountRequest createLoggedInStatusRequest() {
    	UserAccountRequest r = new UserAccountRequest();
    	r.operation = UserAccountOperation.LOGGED_IN_STATUS;
    	return r;
    }
    
    public static UserAccountRequest createRegisterSubscriberRequest(String fullName, String phone, String email) {
    	UserAccountRequest r = new UserAccountRequest();
        r.operation = UserAccountOperation.REGISTER_SUBSCRIBER;
        r.fullName = fullName;
        r.phone = phone;
        r.email = email;
        return r;
    }

    public UserAccountOperation getOperation() { return operation; }
    public String getSubscriptionCode() { return subscriptionCode; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
