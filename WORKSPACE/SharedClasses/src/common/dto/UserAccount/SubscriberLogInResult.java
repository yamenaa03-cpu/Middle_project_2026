package common.dto.UserAccount;

import java.io.Serializable;

/**
 * Result object for subscriber login operations.
 * <p>
 * Contains the outcome of authenticating a subscriber via their subscription
 * code, including the subscriber ID and full name on success.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class SubscriberLogInResult implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Whether the login succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * Subscriber ID on successful login.
	 */
	private final Integer subscriberId;

	/**
	 * Subscriber's full name on successful login.
	 */
	private final String fullName;

	/**
	 * Private constructor used by factory methods.
	 */
	private SubscriberLogInResult(boolean success, String message, Integer subscriberId, String fullName) {
		this.success = success;
		this.message = message;
		this.subscriberId = subscriberId;
		this.fullName = fullName;
	}

	/**
	 * Creates a successful login result.
	 *
	 * @param subscriberId the authenticated subscriber's ID
	 * @param fullName     the subscriber's full name
	 * @param message      success message
	 * @return success result with subscriber info
	 */
	public static SubscriberLogInResult ok(int subscriberId, String fullName, String message) {
		return new SubscriberLogInResult(true, message, subscriberId, fullName);
	}

	/**
	 * Creates a failed login result.
	 *
	 * @param message failure message
	 * @return failed result
	 */
	public static SubscriberLogInResult fail(String message) {
		return new SubscriberLogInResult(false, message, null, null);
	}

	/**
	 * Returns whether the login succeeded.
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
	 * Returns the subscriber ID on success.
	 *
	 * @return subscriber ID or null if failed
	 */
	public Integer getSubscriberId() {
		return subscriberId;
	}

	/**
	 * Returns the subscriber's full name on success.
	 *
	 * @return full name or null if failed
	 */
	public String getFullName() {
		return fullName;
	}
}
