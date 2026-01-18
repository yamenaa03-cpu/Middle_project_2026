package common.dto.UserAccount;

import java.io.Serializable;

import common.enums.EmployeeRole;
import common.enums.LoggedInStatus;
import common.enums.UserAccountOperation;

/**
 * Request DTO for user account operations sent from client to server.
 * <p>
 * This class uses the factory pattern to create requests for different account
 * operations including login, logout, registration, profile management, and
 * customer lookups. Each factory method sets the appropriate operation type and
 * required fields.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see UserAccountOperation
 * @see UserAccountResponse
 */
public class UserAccountRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The operation type being requested.
	 */
	private UserAccountOperation operation;

	/**
	 * Subscription code for subscriber login or lookup.
	 */
	private String subscriptionCode;

	/**
	 * Customer's full name for registration or updates.
	 */
	private String fullName;

	/**
	 * Customer's phone number for registration, updates, or lookup.
	 */
	private String phone;

	/**
	 * Customer's email address for registration, updates, or lookup.
	 */
	private String email;

	/**
	 * Employee username for employee login.
	 */
	private String username;

	/**
	 * Employee password for employee login.
	 */
	private String password;

	/**
	 * Creates a logout request.
	 *
	 * @return request for logout operation
	 */
	public static UserAccountRequest createLogoutRequest() {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.LOGOUT;
		return r;
	}

	/**
	 * Creates a subscriber login request.
	 *
	 * @param code the subscription code to authenticate with
	 * @return request for subscriber login
	 */
	public static UserAccountRequest createSubscriberLogInRequest(String code) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.SUBSCRIBER_LOG_IN;
		r.subscriptionCode = code;
		return r;
	}

	/**
	 * Creates a request to check current login status.
	 *
	 * @return request for login status check
	 */
	public static UserAccountRequest createLoggedInStatusRequest() {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.LOGGED_IN_STATUS;
		return r;
	}

	/**
	 * Creates a subscriber registration request.
	 *
	 * @param fullName the subscriber's full name
	 * @param phone    the subscriber's phone number
	 * @param email    the subscriber's email address
	 * @return request for subscriber registration
	 */
	public static UserAccountRequest createRegisterSubscriberRequest(String fullName, String phone, String email) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.REGISTER_SUBSCRIBER;
		r.fullName = fullName;
		r.phone = phone;
		r.email = email;
		return r;
	}

	/**
	 * Creates a request to get the current subscriber's profile.
	 *
	 * @return request for subscriber profile
	 */
	public static UserAccountRequest createGetSubscriberProfileRequest() {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.GET_SUBSCRIBER_PROFILE;
		return r;
	}

	/**
	 * Creates an employee login request.
	 *
	 * @param username the employee's username
	 * @param password the employee's password
	 * @return request for employee login
	 */
	public static UserAccountRequest createEmployeeLoginRequest(String username, String password) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.EMPLOYEE_LOG_IN;
		r.username = username;
		r.password = password;
		return r;
	}

	/**
	 * Creates a customer lookup request by subscription code (employee only).
	 *
	 * @param subscriptionCode the code to search for
	 * @return request for customer lookup
	 */
	public static UserAccountRequest createLookupCustomerBySubscriptionCodeRequest(String subscriptionCode) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.LOOKUP_CUSTOMER_BY_SUBSCRIPTION_CODE;
		r.subscriptionCode = subscriptionCode;
		return r;
	}

	/**
	 * Creates a customer lookup request by phone number (employee only).
	 *
	 * @param phone the phone number to search for
	 * @return request for customer lookup
	 */
	public static UserAccountRequest createLookupCustomerByPhoneRequest(String phone) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.LOOKUP_CUSTOMER_BY_PHONE;
		r.phone = phone;
		return r;
	}

	/**
	 * Creates a customer lookup request by email (employee only).
	 *
	 * @param email the email to search for
	 * @return request for customer lookup
	 */
	public static UserAccountRequest createLookupCustomerByEmailRequest(String email) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.LOOKUP_CUSTOMER_BY_EMAIL;
		r.email = email;
		return r;
	}

	/**
	 * Creates a subscriber profile update request.
	 *
	 * @param fullName new full name (null to keep current)
	 * @param phone    new phone number (null to keep current)
	 * @param email    new email address (null to keep current)
	 * @return request for profile update
	 */
	public static UserAccountRequest createUpdateSubscriberProfileRequest(String fullName, String phone, String email) {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.UPDATE_SUBSCRIBER_PROFILE;
		r.fullName = fullName;
		r.phone = phone;
		r.email = email;
		return r;
	}

	/**
	 * Creates a request to get all subscribers (employee only).
	 *
	 * @return request for all subscribers list
	 */
	public static UserAccountRequest createGetAllSubscribersRequest() {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.GET_ALL_SUBSCRIBERS;
		return r;
	}

	/**
	 * Creates a request to get current diners (employee only).
	 *
	 * @return request for current diners list
	 */
	public static UserAccountRequest createGetCurrentDinersRequest() {
		UserAccountRequest r = new UserAccountRequest();
		r.operation = UserAccountOperation.GET_CURRENT_DINERS;
		return r;
	}

	/**
	 * Returns the operation type for this request.
	 *
	 * @return the user account operation
	 */
	public UserAccountOperation getOperation() {
		return operation;
	}

	/**
	 * Returns the subscription code if provided.
	 *
	 * @return subscription code or null
	 */
	public String getSubscriptionCode() {
		return subscriptionCode;
	}

	/**
	 * Returns the full name if provided.
	 *
	 * @return full name or null
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Returns the phone number if provided.
	 *
	 * @return phone number or null
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Returns the email address if provided.
	 *
	 * @return email address or null
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Returns the username for employee login.
	 *
	 * @return username or null
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns the password for employee login.
	 *
	 * @return password or null
	 */
	public String getPassword() {
		return password;
	}
}
