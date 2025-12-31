package common.dto.Authentication;

import java.io.Serializable;
import common.enums.AuthOperation;

public class CustomerAuthRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private AuthOperation operation;

    // SUBSCRIPTION_CODE
    private String subscriptionCode;

    // GUEST
    private String fullName;
    private String phone;
    private String email;

    public static CustomerAuthRequest subscription(String code) {
        CustomerAuthRequest r = new CustomerAuthRequest();
        r.operation = AuthOperation.SUBSCRIPTION_CODE;
        r.subscriptionCode = code;
        return r;
    }

    public static CustomerAuthRequest guest(String fullName, String phone, String email) {
        CustomerAuthRequest r = new CustomerAuthRequest();
        r.operation = AuthOperation.GUEST;
        r.fullName = fullName;
        r.phone = phone;
        r.email = email;
        return r;
    }

    public AuthOperation getOperation() { return operation; }
    public String getSubscriptionCode() { return subscriptionCode; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
