package common.dto.UserAccount;

import java.io.Serializable;

import common.enums.LoggedInStatus;

public class UserAccountResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean success;
	private String message;
	private int subscriberId;
	private String fullName;
	private LoggedInStatus status;
	private String subscribtionCode;

	public UserAccountResponse(boolean success, String message, String fullName, LoggedInStatus status) {
		this.success = success;
		this.message = message;
		this.fullName = fullName;
		this.status = status;
	}

	public UserAccountResponse(boolean success, String message, String subscribtionCode) {
		this.success = success;
		this.message = message;
		this.subscribtionCode = subscribtionCode;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public Integer getSubscriberId() {
		return subscriberId;
	}

	public LoggedInStatus getLoggedInStatus() {
		return status;
	}

	public String getSubscribtionCode() {
		return subscribtionCode;
	}
}
