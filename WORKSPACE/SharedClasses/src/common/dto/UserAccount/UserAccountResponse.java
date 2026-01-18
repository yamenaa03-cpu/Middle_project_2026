package common.dto.UserAccount;

import java.io.Serializable;
import java.util.List;

import common.entity.Customer;
import common.enums.EmployeeRole;
import common.enums.LoggedInStatus;
import common.enums.UserAccountOperation;

/**
 * Response DTO for user account operations sent from server to client.
 * <p>
 * This class uses the factory pattern to create appropriate responses for
 * different account operations. Each response includes success status, a
 * descriptive message, and operation-specific data such as user information,
 * subscription codes, or customer lists.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see UserAccountOperation
 * @see UserAccountRequest
 */
public class UserAccountResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Whether the operation succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * Subscriber ID for login/status operations.
	 */
	private final Integer subscriberId;

	/**
	 * User's full name.
	 */
	private final String fullName;

	/**
	 * Current login status.
	 */
	private final LoggedInStatus status;

	/**
	 * Generated subscription code for registration.
	 */
	private final String subscriptionCode;

	/**
	 * Single customer result for profile/lookup operations.
	 */
	private final Customer customer;

	/**
	 * List of customers for bulk operations.
	 */
	private final List<Customer> customers;

	/**
	 * Employee ID for employee login operations.
	 */
	private final Integer employeeId;

	/**
	 * Employee role for employee login operations.
	 */
	private final EmployeeRole employeeRole;

	/**
	 * The operation this response is for.
	 */
	private final UserAccountOperation operation;

	/**
	 * Private constructor used by factory methods.
	 */
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

	// ==================== Factory Methods ====================

	/**
	 * Creates a successful subscriber login response.
	 *
	 * @param subscriberId the logged-in subscriber's ID
	 * @param fullName     the subscriber's full name
	 * @return success response for login
	 */
	public static UserAccountResponse loginOk(Integer subscriberId, String fullName) {
		return new UserAccountResponse(true, fullName + "\nSubscriber Login successful.", subscriberId, fullName,
				LoggedInStatus.SUBSCRIBER, null, null, null, null, null, UserAccountOperation.SUBSCRIBER_LOG_IN);
	}

	/**
	 * Creates a failed subscriber login response.
	 *
	 * @param message failure message
	 * @return failed response for login
	 */
	public static UserAccountResponse loginFail(String message) {
		return new UserAccountResponse(false, safeMsg(message, "Login failed."), null, null,
				LoggedInStatus.NOT_LOGGED_IN, null, null, null, null, null, UserAccountOperation.SUBSCRIBER_LOG_IN);
	}

	/**
	 * Creates a status response for not logged in state.
	 *
	 * @return status response
	 */
	public static UserAccountResponse statusNotLoggedIn() {
		return new UserAccountResponse(true, "Not logged in.", null, null, LoggedInStatus.NOT_LOGGED_IN, null, null,
				null, null, null, UserAccountOperation.LOGGED_IN_STATUS);
	}

	/**
	 * Creates a status response for logged-in subscriber.
	 *
	 * @param subscriberId the subscriber's ID
	 * @param fullName     the subscriber's name
	 * @return status response
	 */
	public static UserAccountResponse statusSubscriber(Integer subscriberId, String fullName) {
		return new UserAccountResponse(true, "Subscriber is logged in.", subscriberId, fullName,
				LoggedInStatus.SUBSCRIBER, null, null, null, null, null, UserAccountOperation.LOGGED_IN_STATUS);
	}

	/**
	 * Creates a status response for logged-in manager.
	 *
	 * @param employeeId the employee's ID
	 * @param fullName   the manager's name
	 * @return status response
	 */
	public static UserAccountResponse statusManager(Integer employeeId, String fullName) {
		return new UserAccountResponse(true, "Manager is logged in.", null, fullName, LoggedInStatus.MANAGER, null,
				null, null, employeeId, EmployeeRole.MANAGER, UserAccountOperation.LOGGED_IN_STATUS);
	}

	/**
	 * Creates a status response for logged-in representative.
	 *
	 * @param employeeId the employee's ID
	 * @param fullName   the representative's name
	 * @return status response
	 */
	public static UserAccountResponse statusRep(Integer employeeId, String fullName) {
		return new UserAccountResponse(true, "Rep is logged in.", null, fullName, LoggedInStatus.REPRESENTATIVE, null,
				null, null, employeeId, EmployeeRole.REPRESENTATIVE, UserAccountOperation.LOGGED_IN_STATUS);
	}

	/**
	 * Creates a successful logout response.
	 *
	 * @return success response for logout
	 */
	public static UserAccountResponse logoutOk() {
		return new UserAccountResponse(true, "Logged out successfully.", null, null, LoggedInStatus.NOT_LOGGED_IN, null,
				null, null, null, null, UserAccountOperation.LOGOUT);
	}

	/**
	 * Creates an already logged out response.
	 *
	 * @return response indicating already logged out
	 */
	public static UserAccountResponse alreadyLoggedOut() {
		return new UserAccountResponse(false, "Already logged out.", null, null, LoggedInStatus.NOT_LOGGED_IN, null,
				null, null, null, null, UserAccountOperation.LOGOUT);
	}

	/**
	 * Creates a successful registration response.
	 *
	 * @param subscriptionCode the generated subscription code
	 * @return success response with subscription code
	 */
	public static UserAccountResponse registerOk(String subscriptionCode) {
		return new UserAccountResponse(true, "Subscriber registered successfully.", null, null, null, subscriptionCode,
				null, null, null, null, UserAccountOperation.REGISTER_SUBSCRIBER);
	}

	/**
	 * Creates a failed registration response.
	 *
	 * @param message failure message
	 * @return failed response for registration
	 */
	public static UserAccountResponse registerFail(String message) {
		return new UserAccountResponse(false, safeMsg(message, "Subscriber registration failed."), null, null, null,
				null, null, null, null, null, UserAccountOperation.REGISTER_SUBSCRIBER);
	}

	/**
	 * Creates a successful subscriber profile response.
	 *
	 * @param c the customer profile data
	 * @return success response with profile
	 */
	public static UserAccountResponse subscriberProfileOk(Customer c) {
		return new UserAccountResponse(true, "Subscriber profile loaded.", c.getCustomerId(), c.getFullName(),
				LoggedInStatus.SUBSCRIBER, null, c, null, null, null, UserAccountOperation.GET_SUBSCRIBER_PROFILE);
	}

	/**
	 * Creates a failed subscriber profile response.
	 *
	 * @param msg failure message
	 * @return failed response
	 */
	public static UserAccountResponse subscriberProfileFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Profile not found."), null, null, null, null, null, null,
				null, null, UserAccountOperation.GET_SUBSCRIBER_PROFILE);
	}

	/**
	 * Creates a successful employee login response.
	 *
	 * @param employeeId   the employee's ID
	 * @param employeeRole the employee's role
	 * @param fullName     the employee's name
	 * @return success response for employee login
	 */
	public static UserAccountResponse employeeLoginOk(int employeeId, EmployeeRole employeeRole, String fullName) {
		LoggedInStatus st = (employeeRole == EmployeeRole.MANAGER) ? LoggedInStatus.MANAGER
				: LoggedInStatus.REPRESENTATIVE;

		return new UserAccountResponse(true, fullName + "\nEmployee login successful.", null, fullName, st, null, null,
				null, employeeId, employeeRole, UserAccountOperation.EMPLOYEE_LOG_IN);
	}

	/**
	 * Creates a failed employee login response.
	 *
	 * @param msg failure message
	 * @return failed response
	 */
	public static UserAccountResponse employeeLoginFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Employee login failed."), null, null,
				LoggedInStatus.NOT_LOGGED_IN, null, null, null, null, null, UserAccountOperation.EMPLOYEE_LOG_IN);
	}

	/**
	 * Creates a customer found response for lookup operations.
	 *
	 * @param c         the found customer
	 * @param operation the lookup operation type
	 * @return success response with customer data
	 */
	public static UserAccountResponse customerFound(Customer c, UserAccountOperation operation) {
		return new UserAccountResponse(true, "Customer found.", c.getCustomerId(), c.getFullName(), null, null, c, null,
				null, null, operation);
	}

	/**
	 * Creates a customer not found response.
	 *
	 * @param msg       failure message
	 * @param operation the lookup operation type
	 * @return failed response
	 */
	public static UserAccountResponse customerNotFound(String msg, UserAccountOperation operation) {
		return new UserAccountResponse(false, safeMsg(msg, "Customer not found."), null, null, null, null, null, null,
				null, null, operation);
	}

	/**
	 * Creates a generic failure response.
	 *
	 * @param message   failure message
	 * @param operation the operation that failed
	 * @return failed response
	 */
	public static UserAccountResponse fail(String message, UserAccountOperation operation) {
		return new UserAccountResponse(false, safeMsg(message, "Operation failed."), null, null, null, null, null, null,
				null, null, operation);
	}

	/**
	 * Creates a successful profile update response.
	 *
	 * @param c the updated customer data
	 * @return success response
	 */
	public static UserAccountResponse updateProfileOk(Customer c) {
		return new UserAccountResponse(true, "Profile updated successfully.", c.getCustomerId(), c.getFullName(),
				LoggedInStatus.SUBSCRIBER, null, c, null, null, null, UserAccountOperation.UPDATE_SUBSCRIBER_PROFILE);
	}

	/**
	 * Creates a failed profile update response.
	 *
	 * @param msg failure message
	 * @return failed response
	 */
	public static UserAccountResponse updateProfileFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Profile update failed."), null, null, null, null, null,
				null, null, null, UserAccountOperation.UPDATE_SUBSCRIBER_PROFILE);
	}

	/**
	 * Creates a subscribers loaded response.
	 *
	 * @param subscribers list of subscribers
	 * @return success response with subscribers list
	 */
	public static UserAccountResponse subscribersLoaded(List<Customer> subscribers) {
		String msg = subscribers.isEmpty() ? "No subscribers found." : "Subscribers loaded.";
		return new UserAccountResponse(true, msg, null, null, null, null, null, subscribers, null, null,
				UserAccountOperation.GET_ALL_SUBSCRIBERS);
	}

	/**
	 * Creates a subscribers load failure response.
	 *
	 * @param msg failure message
	 * @return failed response
	 */
	public static UserAccountResponse subscribersLoadFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Failed to load subscribers."), null, null, null, null, null,
				null, null, null, UserAccountOperation.GET_ALL_SUBSCRIBERS);
	}

	/**
	 * Creates a current diners loaded response.
	 *
	 * @param diners list of current diners
	 * @return success response with diners list
	 */
	public static UserAccountResponse dinersLoaded(List<Customer> diners) {
		String msg = diners.isEmpty() ? "No current diners." : "Current diners loaded.";
		return new UserAccountResponse(true, msg, null, null, null, null, null, diners, null, null,
				UserAccountOperation.GET_CURRENT_DINERS);
	}

	/**
	 * Creates a diners load failure response.
	 *
	 * @param msg failure message
	 * @return failed response
	 */
	public static UserAccountResponse dinersLoadFail(String msg) {
		return new UserAccountResponse(false, safeMsg(msg, "Failed to load diners."), null, null, null, null, null,
				null, null, null, UserAccountOperation.GET_CURRENT_DINERS);
	}

	/**
	 * Returns a safe message, using fallback if input is null or blank.
	 */
	private static String safeMsg(String msg, String fallback) {
		return (msg == null || msg.isBlank()) ? fallback : msg;
	}

	// ==================== Getters ====================

	/**
	 * Returns whether the operation succeeded.
	 *
	 * @return true if successful
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Returns the result message.
	 *
	 * @return descriptive message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the subscriber ID if applicable.
	 *
	 * @return subscriber ID or null
	 */
	public Integer getSubscriberId() {
		return subscriberId;
	}

	/**
	 * Returns the user's full name.
	 *
	 * @return full name or null
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Returns the current login status.
	 *
	 * @return login status
	 */
	public LoggedInStatus getLoggedInStatus() {
		return status;
	}

	/**
	 * Returns the subscription code for registration.
	 *
	 * @return subscription code or null
	 */
	public String getSubscriptionCode() {
		return subscriptionCode;
	}

	/**
	 * Returns the customer data for profile/lookup operations.
	 *
	 * @return customer or null
	 */
	public Customer getCustomer() {
		return customer;
	}

	/**
	 * Returns the customers list for bulk operations.
	 *
	 * @return customers list or null
	 */
	public List<Customer> getCustomers() {
		return customers;
	}

	/**
	 * Returns the employee ID if applicable.
	 *
	 * @return employee ID or null
	 */
	public Integer getEmployeeId() {
		return employeeId;
	}

	/**
	 * Returns the employee role if applicable.
	 *
	 * @return employee role or null
	 */
	public EmployeeRole getEmployeeRole() {
		return employeeRole;
	}

	/**
	 * Returns the operation this response is for.
	 *
	 * @return the operation type
	 */
	public UserAccountOperation getOperation() {
		return operation;
	}

}
