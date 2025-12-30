package common.dto;

import java.io.Serializable;
import common.enums.AuthMethod;

public class CustomerAuthRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private AuthMethod method;

    // SUBSCRIPTION_CODE
    private String subscriptionCode;

    // GUEST
    private String fullName;
    private String phone;
    private String email;

    public static CustomerAuthRequest subscription(String code) {
        CustomerAuthRequest r = new CustomerAuthRequest();
        r.method = AuthMethod.SUBSCRIPTION_CODE;
        r.subscriptionCode = code;
        return r;
    }

    public static CustomerAuthRequest guest(String fullName, String phone, String email) {
        CustomerAuthRequest r = new CustomerAuthRequest();
        r.method = AuthMethod.GUEST;
        r.fullName = fullName;
        r.phone = phone;
        r.email = email;
        return r;
    }

    public AuthMethod getMethod() { return method; }
    public String getSubscriptionCode() { return subscriptionCode; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
