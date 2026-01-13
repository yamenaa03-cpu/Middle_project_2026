package common.dto.UserAccount;

import java.io.Serializable;

import common.enums.EmployeeRole;
import common.enums.LoggedInStatus;
import common.enums.UserAccountOperation;

public class UserAccountRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private UserAccountOperation operation;

	// SUBSCRIPTION_CODE
	private String subscriptionCode;

	private String fullName;
	private String phone;
	private String email;

	private String username;

	private String password;

	public static UserAccountRequest createLogoutRequest() {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.LOGOUT;
		return r;
	}

	public static UserAccountRequest createSubscriberLogInRequest(String code) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.SUBSCRIBER_LOG_IN;
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

	public static UserAccountRequest createGetSubscriberProfileRequest() {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.GET_SUBSCRIBER_PROFILE;
		return r;
	}

	public static UserAccountRequest createEmployeeLoginRequest(String username, String password) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.EMPLOYEE_LOG_IN;
		r.username = username;
		r.password = password;
		return r;
	}
	
	public static UserAccountRequest lookupCustomerBySubscriptionCode(String subscriptionCode) {
        UserAccountRequest r = new UserAccountRequest();
        r.operation = UserAccountOperation.LOOKUP_CUSTOMER_BY_SUBSCRIPTION_CODE;
        r.subscriptionCode = subscriptionCode;
        return r;
    }

    public static UserAccountRequest lookupCustomerByPhone(String phone) {
        UserAccountRequest r = new UserAccountRequest();
        r.operation = UserAccountOperation.LOOKUP_CUSTOMER_BY_PHONE;
        r.phone = phone;
        return r;
    }

    public static UserAccountRequest lookupCustomerByEmail(String email) {
        UserAccountRequest r = new UserAccountRequest();
        r.operation = UserAccountOperation.LOOKUP_CUSTOMER_BY_EMAIL;
        r.email = email;
        return r;
    }

	public UserAccountOperation getOperation() {
		return operation;
	}

	public String getSubscriptionCode() {
		return subscriptionCode;
	}

	public String getFullName() {
		return fullName;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
