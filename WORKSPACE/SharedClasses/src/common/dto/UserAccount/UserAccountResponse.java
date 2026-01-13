package common.dto.UserAccount;

import java.io.Serializable;

import common.entity.Customer;
import common.enums.EmployeeRole;
import common.enums.LoggedInStatus;

public class UserAccountResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private final boolean success;
	private final String message;

	// for login/status/logout
	private final Integer subscriberId;
	private final String fullName;
	private final LoggedInStatus status;

	// for register
	private final String subscriptionCode;

	private final Customer customer;

	private final Integer employeeId;
	private final EmployeeRole employeeRole;

	private UserAccountResponse(boolean success, String message, Integer subscriberId, String fullName,
			LoggedInStatus status, String subscriptionCode, Customer customer, Integer employeeId,
			EmployeeRole employeeRole) {
		this.success = success;
		this.message = message;
		this.subscriberId = subscriberId;
		this.fullName = fullName;
		this.status = status;
		this.subscriptionCode = subscriptionCode;
		this.customer = customer;
		this.employeeId = employeeId;
		this.employeeRole = employeeRole;
	}

	/* ---------- Factories ---------- */

	// LOGIN success
	public static UserAccountResponse loginOk(Integer subscriberId, String fullName) {
		return new UserAccountResponse(true, "Login successful.", subscriberId, fullName, LoggedInStatus.SUBSCRIBER,
				null, null, null, null);
	}

	// LOGIN fail
	public static UserAccountResponse loginFail(String message) {
		return new UserAccountResponse(false, safeMsg(message, "Login failed."), null, null,
				LoggedInStatus.NOT_LOGGED_IN, null, null, null, null);
	}

	// STATUS: not logged in
	public static UserAccountResponse statusNotLoggedIn() {
		return new UserAccountResponse(true, "Not logged in.", null, null, LoggedInStatus.NOT_LOGGED_IN, null, null,
				null, null);
	}

	// STATUS: subscriber logged in
	public static UserAccountResponse statusSubscriber(Integer subscriberId, String fullName) {
		return new UserAccountResponse(true, "Subscriber is logged in.", subscriberId, fullName,
				LoggedInStatus.SUBSCRIBER, null, null, null, null);
	}
	
	public static UserAccountResponse statusManager(Integer employeeId, String fullName) {
		return new UserAccountResponse(true, "Manager is logged in.", null, fullName,
				LoggedInStatus.MANAGER, null, null, employeeId, EmployeeRole.MANAGER);
	}
	
	public static UserAccountResponse statusRep(Integer employeeId, String fullName) {
		return new UserAccountResponse(true, "Rep is logged in.", null, fullName,
				LoggedInStatus.REPRESENTATIVE, null, null, employeeId, EmployeeRole.REPRESENTATIVE);
	}

	// LOGOUT success
	public static UserAccountResponse logoutOk() {
		return new UserAccountResponse(true, "Logged out successfully.", null, null, LoggedInStatus.NOT_LOGGED_IN, null,
				null, null, null);
	}

	// LOGOUT already logged out
	public static UserAccountResponse alreadyLoggedOut() {
		return new UserAccountResponse(false, "Already logged out.", null, null, LoggedInStatus.NOT_LOGGED_IN, null,
				null, null, null);
	}

	// REGISTER success
	public static UserAccountResponse registerOk(String subscriptionCode) {
		return new UserAccountResponse(true, "Subscriber registered successfully.", null, null, null, subscriptionCode,
				null, null, null);
	}

	// REGISTER fail
	public static UserAccountResponse registerFail(String message) {
		return new UserAccountResponse(false, safeMsg(message, "Subscriber registration failed."), null, null, null,
				null, null, null, null);
	}

	public static UserAccountResponse subscriberProfileOk(Customer c) {
		return new UserAccountResponse(true, "Subscriber profile loaded.", c.getCustomerId(), c.getFullName(),
				LoggedInStatus.SUBSCRIBER, null, c, null, null);
	}

	public static UserAccountResponse subscriberProfileFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Profile not found."), null, null, null, null, null, null,
				null);
	}

	public static UserAccountResponse employeeLoginOk(int employeeId, EmployeeRole employeeRole, String fullName) {
		LoggedInStatus st = (employeeRole == EmployeeRole.MANAGER) ? LoggedInStatus.MANAGER
				: LoggedInStatus.REPRESENTATIVE;

		return new UserAccountResponse(true, "Employee login successful.", null, fullName, st, null, null, employeeId,
				employeeRole);
	}

	public static UserAccountResponse employeeLoginFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Employee login failed."), null, null,
				LoggedInStatus.NOT_LOGGED_IN, null, null, null, null);
	}
	
    public static UserAccountResponse customerFound(Customer c) {
        return new UserAccountResponse(true, "Customer found.", c.getCustomerId(), c.getFullName(),
                null, null, c, null, null);
    }

    public static UserAccountResponse customerNotFound(String msg) {
        return new UserAccountResponse(false, safeMsg(msg, "Customer not found."), null, null, null, null, null, null,
                null);
    }

	// generic fail (fallback)
	public static UserAccountResponse fail(String message) {
		return new UserAccountResponse(false, safeMsg(message, "Operation failed."), null, null, null, null, null, null,
				null);
	}

	private static String safeMsg(String msg, String fallback) {
		return (msg == null || msg.isBlank()) ? fallback : msg;
	}

	/* ---------- Getters ---------- */

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public Integer getSubscriberId() {
		return subscriberId;
	}

	public String getFullName() {
		return fullName;
	}

	public LoggedInStatus getLoggedInStatus() {
		return status;
	}

	public String getSubscriptionCode() {
		return subscriptionCode;
	}

	public Customer getCustomer() {
		return customer;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public EmployeeRole getEmployeeRole() {
		return employeeRole;
	}

}
