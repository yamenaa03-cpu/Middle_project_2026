package common.dto.UserAccount;

import java.io.Serializable;

public class RegisterSubscriberResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private final boolean success;
	private final String message;

	// returned on success
	private final String subscriptionCode;

	private RegisterSubscriberResult(boolean success, String message, String subscriptionCode) {
		this.success = success;
		this.message = message;
		this.subscriptionCode = subscriptionCode;
	}

	public static RegisterSubscriberResult ok(String subscriptionCode) {
		return new RegisterSubscriberResult(true, "Subscriber registered successfully.", subscriptionCode);
	}

	public static RegisterSubscriberResult fail(String message) {
		return new RegisterSubscriberResult(false, message, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getSubscriptionCode() {
		return subscriptionCode;
	}

}
