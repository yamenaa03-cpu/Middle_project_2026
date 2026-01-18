package common.dto.RestaurantManagement;

import java.io.Serializable;

/**
 * Result object for individual restaurant management operations.
 * <p>
 * This class provides a simple success/failure indication with an optional
 * message and identifiers for newly created resources (tables or overrides).
 * Used internally by the controller layer before constructing full responses.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see RestaurantManagementResponse
 */
public class RestaurantManagementResult implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Whether the operation succeeded.
	 */
	private boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private String message;

	/**
	 * New table number when a table is added.
	 */
	private int newTableNumber;

	/**
	 * New override ID when an override is added.
	 */
	private int newOverrideId;

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
	 * Returns the new table number after adding a table.
	 *
	 * @return new table number (0 if not applicable)
	 */
	public int getNewTableNumber() {
		return newTableNumber;
	}

	/**
	 * Returns the new override ID after adding an override.
	 *
	 * @return new override ID (0 if not applicable)
	 */
	public int getNewOverrideId() {
		return newOverrideId;
	}

	/**
	 * Creates a generic success result.
	 *
	 * @param message success message
	 * @return success result
	 */
	public static RestaurantManagementResult ok(String message) {
		RestaurantManagementResult r = new RestaurantManagementResult();
		r.success = true;
		r.message = message;
		return r;
	}

	/**
	 * Creates a success result for table addition.
	 *
	 * @param tableNumber the number assigned to the new table
	 * @param message     success message
	 * @return success result with table number
	 */
	public static RestaurantManagementResult tableAdded(int tableNumber, String message) {
		RestaurantManagementResult r = new RestaurantManagementResult();
		r.success = true;
		r.message = message;
		r.newTableNumber = tableNumber;
		return r;
	}

	/**
	 * Creates a success result for override addition.
	 *
	 * @param overrideId the ID assigned to the new override
	 * @param message    success message
	 * @return success result with override ID
	 */
	public static RestaurantManagementResult overrideAdded(int overrideId, String message) {
		RestaurantManagementResult r = new RestaurantManagementResult();
		r.success = true;
		r.message = message;
		r.newOverrideId = overrideId;
		return r;
	}

	/**
	 * Creates a failure result.
	 *
	 * @param message failure message
	 * @return failed result
	 */
	public static RestaurantManagementResult fail(String message) {
		RestaurantManagementResult r = new RestaurantManagementResult();
		r.success = false;
		r.message = message;
		return r;
	}
}
