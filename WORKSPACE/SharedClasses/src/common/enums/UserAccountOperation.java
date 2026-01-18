package common.enums;

/**
 * Enumeration of user account operations used in client-server request messages.
 * <p>
 * Each operation type indicates what action the client is requesting from the server
 * regarding user authentication, registration, and profile management.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum UserAccountOperation {
	/**
	 * Request to authenticate a subscriber using their subscription code.
	 */
	SUBSCRIBER_LOG_IN,

	/**
	 * Request to authenticate an employee using username and password.
	 */
	EMPLOYEE_LOG_IN,

	/**
	 * Request to check the current login status of the session.
	 */
    LOGGED_IN_STATUS,

    /**
     * Request to log out and clear the current session.
     */
    LOGOUT,

    /**
     * Request to register a new subscriber account.
     */
    REGISTER_SUBSCRIBER,

    /**
     * Request to retrieve the logged-in subscriber's profile information.
     */
    GET_SUBSCRIBER_PROFILE,

    /**
     * Employee operation to look up a customer by their subscription code.
     */
    LOOKUP_CUSTOMER_BY_SUBSCRIPTION_CODE,

    /**
     * Employee operation to look up a customer by their phone number.
     */
    LOOKUP_CUSTOMER_BY_PHONE,

    /**
     * Employee operation to look up a customer by their email address.
     */
    LOOKUP_CUSTOMER_BY_EMAIL,

    /**
     * Request to update the logged-in subscriber's profile information.
     */
    UPDATE_SUBSCRIBER_PROFILE,

    /**
     * Employee operation to retrieve a list of all registered subscribers.
     */
    GET_ALL_SUBSCRIBERS,

    /**
     * Employee operation to retrieve customers currently dining (with IN_PROGRESS reservations).
     */
    GET_CURRENT_DINERS
}