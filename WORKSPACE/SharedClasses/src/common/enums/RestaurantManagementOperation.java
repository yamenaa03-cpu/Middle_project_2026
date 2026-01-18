package common.enums;

/**
 * Enumeration of restaurant management operations used in client-server request messages.
 * <p>
 * Each operation type indicates what action the client is requesting from the server
 * regarding restaurant configuration including tables, hours, and date overrides.
 * These operations are typically restricted to manager-level employees.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum RestaurantManagementOperation {
	// ======================== Table operations ========================
	
	/**
	 * Request to retrieve all tables in the restaurant.
	 */
	GET_ALL_TABLES,

	/**
	 * Request to add a new table with specified seating capacity.
	 */
	ADD_TABLE,

	/**
	 * Request to update the seating capacity of an existing table.
	 */
	UPDATE_TABLE,

	/**
	 * Request to delete a table from the restaurant.
	 */
	DELETE_TABLE,

	// ======================== Opening hours operations ========================
	
	/**
	 * Request to retrieve the regular opening hours for all days of the week.
	 */
	GET_OPENING_HOURS,

	/**
	 * Request to update the opening hours for a specific day of the week.
	 */
	UPDATE_OPENING_HOURS,

	// ======================== Date override operations ========================
	
	/**
	 * Request to retrieve all date-specific overrides.
	 */
	GET_DATE_OVERRIDES,

	/**
	 * Request to add a new date-specific override for hours or closure.
	 */
	ADD_DATE_OVERRIDE,

	/**
	 * Request to update an existing date override.
	 */
	UPDATE_DATE_OVERRIDE,

	/**
	 * Request to delete a date override.
	 */
	DELETE_DATE_OVERRIDE
}