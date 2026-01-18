package common.dto.RestaurantManagement;

import java.io.Serializable;
import java.util.List;
import common.entity.Table;
import common.enums.RestaurantManagementOperation;
import common.entity.DateOverride;
import common.entity.OpeningHours;

/**
 * Response DTO for restaurant management operations sent from server to client.
 * <p>
 * This class uses the factory pattern to create appropriate responses for
 * different management operations. Each response includes success status, a
 * descriptive message, the operation type, and operation-specific data such as
 * tables, opening hours, or date overrides.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see RestaurantManagementOperation
 * @see RestaurantManagementRequest
 */
public class RestaurantManagementResponse implements Serializable {
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
	 * List of tables for table operations.
	 */
	private List<Table> tables;

	/**
	 * List of opening hours for hours operations.
	 */
	private List<OpeningHours> openingHours;

	/**
	 * List of date overrides for override operations.
	 */
	private List<DateOverride> dateOverrides;

	/**
	 * New table number when a table is added.
	 */
	private int newTableNumber;

	/**
	 * The operation this response is for.
	 */
	private RestaurantManagementOperation operation;

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
	 * Returns the list of tables.
	 *
	 * @return tables list or null
	 */
	public List<Table> getTables() {
		return tables;
	}

	/**
	 * Returns the list of opening hours.
	 *
	 * @return opening hours list or null
	 */
	public List<OpeningHours> getOpeningHours() {
		return openingHours;
	}

	/**
	 * Returns the list of date overrides.
	 *
	 * @return date overrides list or null
	 */
	public List<DateOverride> getDateOverrides() {
		return dateOverrides;
	}

	/**
	 * Returns the new table number after adding a table.
	 *
	 * @return new table number
	 */
	public int getNewTableNumber() {
		return newTableNumber;
	}

	/**
	 * Returns the operation this response is for.
	 *
	 * @return the operation type
	 */
	public RestaurantManagementOperation getOperation() {
		return operation;
	}

	// ==================== Table Response Factories ====================

	/**
	 * Creates a tables loaded response.
	 *
	 * @param tables the list of tables
	 * @return success response with tables
	 */
	public static RestaurantManagementResponse tablesLoaded(List<Table> tables) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Tables loaded.";
		resp.tables = tables;
		resp.operation = RestaurantManagementOperation.GET_ALL_TABLES;
		return resp;
	}

	/**
	 * Creates a table added response.
	 *
	 * @param newTableNumber the number assigned to the new table
	 * @param tables         updated list of all tables
	 * @return success response with new table number
	 */
	public static RestaurantManagementResponse tableAdded(int newTableNumber, List<Table> tables) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Table added successfully.";
		resp.newTableNumber = newTableNumber;
		resp.tables = tables;
		resp.operation = RestaurantManagementOperation.ADD_TABLE;
		return resp;
	}

	/**
	 * Creates a table updated response.
	 *
	 * @param tables updated list of all tables
	 * @return success response
	 */
	public static RestaurantManagementResponse tableUpdated(List<Table> tables) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Table updated.";
		resp.tables = tables;
		resp.operation = RestaurantManagementOperation.UPDATE_TABLE;
		return resp;
	}

	/**
	 * Creates a table deleted response.
	 *
	 * @param tables updated list of all tables
	 * @return success response
	 */
	public static RestaurantManagementResponse tableDeleted(List<Table> tables) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Table deleted.";
		resp.tables = tables;
		resp.operation = RestaurantManagementOperation.DELETE_TABLE;
		return resp;
	}

	// ==================== Hours Response Factories ====================

	/**
	 * Creates an opening hours loaded response.
	 *
	 * @param hours list of opening hours for all days
	 * @return success response with hours
	 */
	public static RestaurantManagementResponse hoursLoaded(List<OpeningHours> hours) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Opening hours loaded.";
		resp.openingHours = hours;
		resp.operation = RestaurantManagementOperation.GET_OPENING_HOURS;
		return resp;
	}

	/**
	 * Creates an opening hours updated response.
	 *
	 * @param hours updated list of opening hours
	 * @return success response
	 */
	public static RestaurantManagementResponse hoursUpdated(List<OpeningHours> hours) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Hours updated.";
		resp.openingHours = hours;
		resp.operation = RestaurantManagementOperation.UPDATE_OPENING_HOURS;
		return resp;
	}

	// ==================== Override Response Factories ====================

	/**
	 * Creates a date overrides loaded response.
	 *
	 * @param overrides list of all date overrides
	 * @return success response with overrides
	 */
	public static RestaurantManagementResponse overridesLoaded(List<DateOverride> overrides) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Date overrides loaded.";
		resp.dateOverrides = overrides;
		resp.operation = RestaurantManagementOperation.GET_DATE_OVERRIDES;
		return resp;
	}

	/**
	 * Creates an override added response.
	 *
	 * @param overrides updated list of all overrides
	 * @return success response
	 */
	public static RestaurantManagementResponse overrideAdded(List<DateOverride> overrides) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Override added.";
		resp.dateOverrides = overrides;
		resp.operation = RestaurantManagementOperation.ADD_DATE_OVERRIDE;
		return resp;
	}

	/**
	 * Creates an override updated response.
	 *
	 * @param overrides updated list of all overrides
	 * @return success response
	 */
	public static RestaurantManagementResponse overrideUpdated(List<DateOverride> overrides) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Override updated.";
		resp.dateOverrides = overrides;
		resp.operation = RestaurantManagementOperation.UPDATE_DATE_OVERRIDE;
		return resp;
	}

	/**
	 * Creates an override deleted response.
	 *
	 * @param overrides updated list of all overrides
	 * @return success response
	 */
	public static RestaurantManagementResponse overrideDeleted(List<DateOverride> overrides) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Override deleted.";
		resp.dateOverrides = overrides;
		resp.operation = RestaurantManagementOperation.DELETE_DATE_OVERRIDE;
		return resp;
	}

	// ==================== Error Response Factory ====================

	/**
	 * Creates a failure response for any management operation.
	 *
	 * @param message   failure message
	 * @param operation the operation that failed
	 * @return failed response
	 */
	public static RestaurantManagementResponse fail(String message, RestaurantManagementOperation operation) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = false;
		resp.message = message;
		resp.operation = operation;
		return resp;
	}
}
