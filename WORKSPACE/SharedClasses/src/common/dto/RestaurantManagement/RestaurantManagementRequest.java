package common.dto.RestaurantManagement;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import common.enums.RestaurantManagementOperation;

/**
 * Request DTO for restaurant management operations sent from client to server.
 * <p>
 * This class uses the factory pattern to create requests for different
 * management operations including table management, opening hours
 * configuration, and date override management. Each factory method sets the
 * appropriate operation type and required fields.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see RestaurantManagementOperation
 * @see RestaurantManagementResponse
 */
public class RestaurantManagementRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The management operation being requested.
	 */
	private RestaurantManagementOperation operation;

	/**
	 * Table number for table operations.
	 */
	private int tableNumber;

	/**
	 * Seating capacity for table operations.
	 */
	private int seats;

	/**
	 * Day of week for opening hours operations.
	 */
	private DayOfWeek dayOfWeek;

	/**
	 * Opening time for hours/override operations.
	 */
	private LocalTime openTime;

	/**
	 * Closing time for hours/override operations.
	 */
	private LocalTime closeTime;

	/**
	 * Whether the restaurant is closed (for hours/override operations).
	 */
	private boolean closed;

	/**
	 * Override ID for update/delete override operations.
	 */
	private int overrideId;

	/**
	 * Date for date override operations.
	 */
	private LocalDate overrideDate;

	/**
	 * Reason for date override.
	 */
	private String reason;

	// ==================== Getters ====================

	/**
	 * Returns the operation type for this request.
	 *
	 * @return the management operation
	 */
	public RestaurantManagementOperation getOperation() {
		return operation;
	}

	/**
	 * Returns the table number for table operations.
	 *
	 * @return table number
	 */
	public int getTableNumber() {
		return tableNumber;
	}

	/**
	 * Returns the seating capacity for table operations.
	 *
	 * @return number of seats
	 */
	public int getSeats() {
		return seats;
	}

	/**
	 * Returns the day of week for opening hours operations.
	 *
	 * @return day of week
	 */
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	/**
	 * Returns the opening time.
	 *
	 * @return opening time
	 */
	public LocalTime getOpenTime() {
		return openTime;
	}

	/**
	 * Returns the closing time.
	 *
	 * @return closing time
	 */
	public LocalTime getCloseTime() {
		return closeTime;
	}

	/**
	 * Returns whether the day/date should be marked as closed.
	 *
	 * @return true if closed
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Returns the override ID for update/delete operations.
	 *
	 * @return override ID
	 */
	public int getOverrideId() {
		return overrideId;
	}

	/**
	 * Returns the date for override operations.
	 *
	 * @return override date
	 */
	public LocalDate getOverrideDate() {
		return overrideDate;
	}

	/**
	 * Returns the reason for the override.
	 *
	 * @return reason string
	 */
	public String getReason() {
		return reason;
	}

	// ==================== Table Factory Methods ====================

	/**
	 * Creates a request to get all tables.
	 *
	 * @return request for all tables
	 */
	public static RestaurantManagementRequest createGetAllTablesRequest() {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.GET_ALL_TABLES;
		return req;
	}

	/**
	 * Creates a request to add a new table.
	 *
	 * @param seats the seating capacity for the new table
	 * @return request for adding a table
	 */
	public static RestaurantManagementRequest createAddTableRequest(int seats) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.ADD_TABLE;
		req.seats = seats;
		return req;
	}

	/**
	 * Creates a request to update a table's seating capacity.
	 *
	 * @param tableNumber the table to update
	 * @param newSeats    the new seating capacity
	 * @return request for updating a table
	 */
	public static RestaurantManagementRequest createUpdateTablerequest(int tableNumber, int newSeats) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.UPDATE_TABLE;
		req.tableNumber = tableNumber;
		req.seats = newSeats;
		return req;
	}

	/**
	 * Creates a request to delete a table.
	 *
	 * @param tableNumber the table to delete
	 * @return request for deleting a table
	 */
	public static RestaurantManagementRequest createDeleteTableRequest(int tableNumber) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.DELETE_TABLE;
		req.tableNumber = tableNumber;
		return req;
	}

	// ==================== Hours Factory Methods ====================

	/**
	 * Creates a request to get opening hours for all days.
	 *
	 * @return request for opening hours
	 */
	public static RestaurantManagementRequest createGetOpeningHoursRequest() {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.GET_OPENING_HOURS;
		return req;
	}

	/**
	 * Creates a request to update opening hours for a specific day.
	 *
	 * @param day    the day of week to update
	 * @param open   opening time
	 * @param close  closing time
	 * @param closed whether the restaurant is closed
	 * @return request for updating hours
	 */
	public static RestaurantManagementRequest createUpdateOpeningHoursRequest(DayOfWeek day, LocalTime open,
			LocalTime close, boolean closed) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.UPDATE_OPENING_HOURS;
		req.dayOfWeek = day;
		req.openTime = open;
		req.closeTime = close;
		req.closed = closed;
		return req;
	}

	// ==================== Date Override Factory Methods ====================

	/**
	 * Creates a request to get all date overrides.
	 *
	 * @return request for date overrides
	 */
	public static RestaurantManagementRequest createGetDateOverridesRequest() {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.GET_DATE_OVERRIDES;
		return req;
	}

	/**
	 * Creates a request to add a date override.
	 *
	 * @param date   the date to override
	 * @param open   opening time (null if closed)
	 * @param close  closing time (null if closed)
	 * @param closed whether the restaurant is closed
	 * @param reason reason for the override
	 * @return request for adding an override
	 */
	public static RestaurantManagementRequest createAddDateOverrideRequest(LocalDate date, LocalTime open,
			LocalTime close, boolean closed, String reason) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.ADD_DATE_OVERRIDE;
		req.overrideDate = date;
		req.openTime = open;
		req.closeTime = close;
		req.closed = closed;
		req.reason = reason;
		return req;
	}

	/**
	 * Creates a request to update an existing date override.
	 *
	 * @param id     the override ID to update
	 * @param date   the date for the override
	 * @param open   opening time (null if closed)
	 * @param close  closing time (null if closed)
	 * @param closed whether the restaurant is closed
	 * @param reason reason for the override
	 * @return request for updating an override
	 */
	public static RestaurantManagementRequest createUpdateDateOverrideRequest(int id, LocalDate date, LocalTime open,
			LocalTime close, boolean closed, String reason) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.UPDATE_DATE_OVERRIDE;
		req.overrideId = id;
		req.overrideDate = date;
		req.openTime = open;
		req.closeTime = close;
		req.closed = closed;
		req.reason = reason;
		return req;
	}

	/**
	 * Creates a request to delete a date override.
	 *
	 * @param id the override ID to delete
	 * @return request for deleting an override
	 */
	public static RestaurantManagementRequest createDeleteDateOverrideRequest(int id) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.DELETE_DATE_OVERRIDE;
		req.overrideId = id;
		return req;
	}
}
