package common.dto.RestaurantManagement;

import java.io.Serializable;

public class RestaurantManagementResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean success;
	private String message;
	private int newTableNumber;
	private int newOverrideId;

	public boolean isSuccess() { return success; }
	public String getMessage() { return message; }
	public int getNewTableNumber() { return newTableNumber; }
	public int getNewOverrideId() { return newOverrideId; }

	public static RestaurantManagementResult ok(String message) {
		RestaurantManagementResult r = new RestaurantManagementResult();
		r.success = true;
		r.message = message;
		return r;
	}

	public static RestaurantManagementResult tableAdded(int tableNumber, String message) {
		RestaurantManagementResult r = new RestaurantManagementResult();
		r.success = true;
		r.message = message;
		r.newTableNumber = tableNumber;
		return r;
	}

	public static RestaurantManagementResult overrideAdded(int overrideId, String message) {
		RestaurantManagementResult r = new RestaurantManagementResult();
		r.success = true;
		r.message = message;
		r.newOverrideId = overrideId;
		return r;
	}

	public static RestaurantManagementResult fail(String message) {
		RestaurantManagementResult r = new RestaurantManagementResult();
		r.success = false;
		r.message = message;
		return r;
	}
}
