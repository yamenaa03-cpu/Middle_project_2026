package common.dto.UserAccount;

import java.io.Serializable;

/**
 * Result object for subscriber registration operations.
 * <p>
 * Contains the outcome of registering a new subscriber, including the generated
 * subscription code on success.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class RegisterSubscriberResult implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Whether the registration succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * Generated subscription code on successful registration.
	 */
	private final String subscriptionCode;

	/**
	 * Private constructor used by factory methods.
	 */
	private RegisterSubscriberResult(boolean success, String message, String subscriptionCode) {
		this.success = success;
		this.message = message;
		this.subscriptionCode = subscriptionCode;
	}

	/**
	 * Creates a successful registration result.
	 *
	 * @param subscriptionCode the generated subscription code
	 * @return success result with subscription code
	 */
	public static RegisterSubscriberResult ok(String subscriptionCode) {
		return new RegisterSubscriberResult(true, "Subscriber registered successfully.", subscriptionCode);
	}

	/**
	 * Creates a failed registration result.
	 *
	 * @param message failure message
	 * @return failed result
	 */
	public static RegisterSubscriberResult fail(String message) {
		return new RegisterSubscriberResult(false, message, null);
	}

	/**
	 * Returns whether the registration succeeded.
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
	 * Returns the generated subscription code on success.
	 *
	 * @return subscription code or null if failed
	 */
	public String getSubscriptionCode() {
		return subscriptionCode;
	}

}
