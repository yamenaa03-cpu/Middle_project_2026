package common.dto.UserAccount;

import java.io.Serializable;
import java.util.List;

import common.entity.Customer;
import common.enums.EmployeeRole;
import common.enums.LoggedInStatus;
import common.enums.UserAccountOperation;

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
	private final List<Customer> customers;

	private final Integer employeeId;
	private final EmployeeRole employeeRole;

	private final UserAccountOperation operation;

	private UserAccountResponse(boolean success, String message, Integer subscriberId, String fullName,
			LoggedInStatus status, String subscriptionCode, Customer customer, List<Customer> customers,
			Integer employeeId, EmployeeRole employeeRole, UserAccountOperation operation) {
		this.success = success;
		this.message = message;
		this.subscriberId = subscriberId;
		this.fullName = fullName;
		this.status = status;
		this.subscriptionCode = subscriptionCode;
		this.customer = customer;
		this.customers = customers;
		this.employeeId = employeeId;
		this.employeeRole = employeeRole;
		this.operation = operation;
	}

	/* ---------- Factories ---------- */

	// LOGIN success
	public static UserAccountResponse loginOk(Integer subscriberId, String fullName) {
		return new UserAccountResponse(true, fullName + "\nSubscriber Login successful.", subscriberId, fullName,
				LoggedInStatus.SUBSCRIBER, null, null, null, null, null, UserAccountOperation.SUBSCRIBER_LOG_IN);
	}

	// LOGIN fail
	public static UserAccountResponse loginFail(String message) {
		return new UserAccountResponse(false, safeMsg(message, "Login failed."), null, null,
				LoggedInStatus.NOT_LOGGED_IN, null, null, null, null, null, UserAccountOperation.SUBSCRIBER_LOG_IN);
	}

	// STATUS: not logged in
	public static UserAccountResponse statusNotLoggedIn() {
		return new UserAccountResponse(true, "Not logged in.", null, null, LoggedInStatus.NOT_LOGGED_IN, null, null,
				null, null, null, UserAccountOperation.LOGGED_IN_STATUS);
	}

	// STATUS: subscriber logged in
	public static UserAccountResponse statusSubscriber(Integer subscriberId, String fullName) {
		return new UserAccountResponse(true, "Subscriber is logged in.", subscriberId, fullName,
				LoggedInStatus.SUBSCRIBER, null, null, null, null, null, UserAccountOperation.LOGGED_IN_STATUS);
	}

	public static UserAccountResponse statusManager(Integer employeeId, String fullName) {
		return new UserAccountResponse(true, "Manager is logged in.", null, fullName, LoggedInStatus.MANAGER, null,
				null, null, employeeId, EmployeeRole.MANAGER, UserAccountOperation.LOGGED_IN_STATUS);
	}

	public static UserAccountResponse statusRep(Integer employeeId, String fullName) {
		return new UserAccountResponse(true, "Rep is logged in.", null, fullName, LoggedInStatus.REPRESENTATIVE, null,
				null, null, employeeId, EmployeeRole.REPRESENTATIVE, UserAccountOperation.LOGGED_IN_STATUS);
	}

	// LOGOUT success
	public static UserAccountResponse logoutOk() {
		return new UserAccountResponse(true, "Logged out successfully.", null, null, LoggedInStatus.NOT_LOGGED_IN, null,
				null, null, null, null, UserAccountOperation.LOGOUT);
	}

	// LOGOUT already logged out
	public static UserAccountResponse alreadyLoggedOut() {
		return new UserAccountResponse(false, "Already logged out.", null, null, LoggedInStatus.NOT_LOGGED_IN, null,
				null, null, null, null, UserAccountOperation.LOGOUT);
	}

	// REGISTER success
	public static UserAccountResponse registerOk(String subscriptionCode) {
		return new UserAccountResponse(true, "Subscriber registered successfully.", null, null, null, subscriptionCode,
				null, null, null, null, UserAccountOperation.REGISTER_SUBSCRIBER);
	}

	// REGISTER fail
	public static UserAccountResponse registerFail(String message) {
		return new UserAccountResponse(false, safeMsg(message, "Subscriber registration failed."), null, null, null,
				null, null, null, null, null, UserAccountOperation.REGISTER_SUBSCRIBER);
	}

	public static UserAccountResponse subscriberProfileOk(Customer c) {
		return new UserAccountResponse(true, "Subscriber profile loaded.", c.getCustomerId(), c.getFullName(),
				LoggedInStatus.SUBSCRIBER, null, c, null, null, null, UserAccountOperation.GET_SUBSCRIBER_PROFILE);
	}

	public static UserAccountResponse subscriberProfileFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Profile not found."), null, null, null, null, null, null,
				null, null, UserAccountOperation.GET_SUBSCRIBER_PROFILE);
	}

	public static UserAccountResponse employeeLoginOk(int employeeId, EmployeeRole employeeRole, String fullName) {
		LoggedInStatus st = (employeeRole == EmployeeRole.MANAGER) ? LoggedInStatus.MANAGER
				: LoggedInStatus.REPRESENTATIVE;

		return new UserAccountResponse(true, fullName + "\nEmployee login successful.", null, fullName, st, null, null,
				null, employeeId, employeeRole, UserAccountOperation.EMPLOYEE_LOG_IN);
	}

	public static UserAccountResponse employeeLoginFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Employee login failed."), null, null,
				LoggedInStatus.NOT_LOGGED_IN, null, null, null, null, null, UserAccountOperation.EMPLOYEE_LOG_IN);
	}

	public static UserAccountResponse customerFound(Customer c, UserAccountOperation operation) {
		return new UserAccountResponse(true, "Customer found.", c.getCustomerId(), c.getFullName(), null, null, c, null,
				null, null, operation);
	}

	public static UserAccountResponse customerNotFound(String msg, UserAccountOperation operation) {
		return new UserAccountResponse(false, safeMsg(msg, "Customer not found."), null, null, null, null, null, null,
				null, null, operation);
	}

	// generic fail (fallback)
	public static UserAccountResponse fail(String message, UserAccountOperation operation) {
		return new UserAccountResponse(false, safeMsg(message, "Operation failed."), null, null, null, null, null, null,
				null, null, operation);
	}

	// UPDATE_SUBSCRIBER_PROFILE success
	public static UserAccountResponse updateProfileOk(Customer c) {
		return new UserAccountResponse(true, "Profile updated successfully.", c.getCustomerId(), c.getFullName(),
				LoggedInStatus.SUBSCRIBER, null, c, null, null, null, UserAccountOperation.UPDATE_SUBSCRIBER_PROFILE);
	}

	// UPDATE_SUBSCRIBER_PROFILE fail
	public static UserAccountResponse updateProfileFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Profile update failed."), null, null, null, null, null,
				null, null, null, UserAccountOperation.UPDATE_SUBSCRIBER_PROFILE);
	}

	// GET_ALL_SUBSCRIBERS success
	public static UserAccountResponse subscribersLoaded(List<Customer> subscribers) {
		String msg = subscribers.isEmpty() ? "No subscribers found." : "Subscribers loaded.";
		return new UserAccountResponse(true, msg, null, null, null, null, null, subscribers, null, null,
				UserAccountOperation.GET_ALL_SUBSCRIBERS);
	}

	// GET_ALL_SUBSCRIBERS fail
	public static UserAccountResponse subscribersLoadFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Failed to load subscribers."), null, null, null, null, null,
				null, null, null, UserAccountOperation.GET_ALL_SUBSCRIBERS);
	}

	// GET_CURRENT_DINERS success
	public static UserAccountResponse dinersLoaded(List<Customer> diners) {
		String msg = diners.isEmpty() ? "No current diners." : "Current diners loaded.";
		return new UserAccountResponse(true, msg, null, null, null, null, null, diners, null, null,
				UserAccountOperation.GET_CURRENT_DINERS);
	}

	// GET_CURRENT_DINERS fail
	public static UserAccountResponse dinersLoadFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Failed to load diners."), null, null, null, null, null,
				null, null, null, UserAccountOperation.GET_CURRENT_DINERS);
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

	public List<Customer> getCustomers() {
		return customers;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public EmployeeRole getEmployeeRole() {
		return employeeRole;
	}

	public UserAccountOperation getOperation() {
		return operation;
	}

}
