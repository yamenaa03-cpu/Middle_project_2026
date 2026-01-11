package common.dto.Authentication;

import java.io.Serializable;
import common.enums.AuthOperation;

/**
 * Request object used for customer authentication and profile operations.
 * Factory methods are provided for common request types.
 */
public class CustomerAuthRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private AuthOperation operation;

    // SUBSCRIPTION_CODE
    private String subscriptionCode;

    // GUEST
    private String fullName;
    private String phone;
    private String email;

    /**
     * Create a subscription-code authentication request.
     *
     * @param code subscription code
     * @return configured CustomerAuthRequest
     */
    public static CustomerAuthRequest subscription(String code) {
        CustomerAuthRequest r = new CustomerAuthRequest();
        r.operation = AuthOperation.SUBSCRIPTION_CODE;
        r.subscriptionCode = code;
        return r;
    }

    /**
     * Create a guest authentication request carrying guest contact info.
     */
    public static CustomerAuthRequest guest(String fullName, String phone, String email) {
        CustomerAuthRequest r = new CustomerAuthRequest();
        r.operation = AuthOperation.GUEST;
        r.fullName = fullName;
        r.phone = phone;
        r.email = email;
        return r;
    }
    /**
     * Request the current authenticated profile.
     *
     * @return configured request
     */
    public static CustomerAuthRequest getProfile() {
        CustomerAuthRequest req = new CustomerAuthRequest();
        req.operation = AuthOperation.GET_PROFILE;
        return req;
    }
    
    /**
     * Create a profile update request.
     *
     * @param fullName new full name
     * @param phone new phone
     * @param email new email
     * @return configured request
     */
    public static CustomerAuthRequest updateProfile(String fullName, String phone, String email) {
        CustomerAuthRequest req = new CustomerAuthRequest();
        req.operation = AuthOperation.UPDATE_PROFILE;
        req.fullName = fullName;
        req.phone = phone;
        req.email = email;
        return req;
    }
    /** @return requested auth operation */
    public AuthOperation getOperation() { return operation; }
    /** @return subscription code when applicable */
    public String getSubscriptionCode() { return subscriptionCode; }
    /** @return full name for guest/update requests */
    public String getFullName() { return fullName; }
    /** @return phone for guest/update requests */
    public String getPhone() { return phone; }
    /** @return email for guest/update requests */
    public String getEmail() { return email; }
}
