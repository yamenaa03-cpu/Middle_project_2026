package common.dto.Reservation;

import java.io.Serializable;

public class ReceiveTableResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private final boolean success;
	private final String message;
	private final Integer tableId;

	private ReceiveTableResult(boolean success, String message, Integer tableId) {
		this.success = success;
		this.message = message;
		this.tableId = tableId;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public Integer getTableNumber() {
		return tableId;
	}

	public static ReceiveTableResult ok(int tableId) {
		return new ReceiveTableResult(true, "✅ Table assigned successfully. Your table number is: " + tableId, tableId);
	}

	public static ReceiveTableResult fail(String msg) {
		return new ReceiveTableResult(false, msg, null);
	}
}
