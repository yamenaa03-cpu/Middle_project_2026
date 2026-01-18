package controllers;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import common.entity.Table;
import common.entity.DateOverride;
import common.entity.OpeningHours;
import common.dto.RestaurantManagement.RestaurantManagementResult;
import dbController.DBController;

/**
 * Controller responsible for managing restaurant configuration settings.
 * <p>
 * This controller handles operations related to:
 * <ul>
 * <li>Table management (add, update, delete tables)</li>
 * <li>Opening hours management (configure regular weekly hours)</li>
 * <li>Date overrides (special hours or closures for specific dates)</li>
 * </ul>
 * All operations interact with the database through the {@link DBController}.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class RestaurantManagementController {

	/**
	 * Database controller for data persistence operations.
	 */
	private final DBController db;

	/**
	 * Constructs a RestaurantManagementController with the specified database
	 * controller.
	 *
	 * @param db the database controller for data access
	 */
	public RestaurantManagementController(DBController db) {
		this.db = db;
	}

	// ======================== TABLE OPERATIONS ========================

	/**
	 * Retrieves all tables configured in the restaurant.
	 *
	 * @return a list of all tables with their numbers and capacities
	 * @throws SQLException if a database error occurs
	 */
	public List<Table> getAllTables() throws SQLException {
		return db.getAllTables();
	}

	/**
	 * Adds a new table to the restaurant.
	 *
	 * @param seats the seating capacity of the new table (must be positive)
	 * @return the result containing the new table number on success
	 * @throws SQLException if a database error occurs
	 */
	public RestaurantManagementResult addTable(int seats) throws SQLException {
		if (seats <= 0) {
			return RestaurantManagementResult.fail("Seats must be a positive number.");
		}
		int newTableNumber = db.addTable(seats);
		if (newTableNumber > 0) {
			return RestaurantManagementResult.tableAdded(newTableNumber,
					"Table " + newTableNumber + " added successfully.");
		}
		return RestaurantManagementResult.fail("Failed to add table.");
	}

	/**
	 * Updates the seating capacity of an existing table.
	 *
	 * @param tableNumber the table number to update
	 * @param newSeats    the new seating capacity (must be positive)
	 * @return the result indicating success or failure
	 * @throws SQLException if a database error occurs
	 */
	public RestaurantManagementResult updateTable(int tableNumber, int newSeats) throws SQLException {
		if (tableNumber <= 0) {
			return RestaurantManagementResult.fail("Invalid table number.");
		}
		if (newSeats <= 0) {
			return RestaurantManagementResult.fail("Seats must be a positive number.");
		}
		boolean updated = db.updateTableCapacity(tableNumber, newSeats);
		if (updated) {
			return RestaurantManagementResult.ok("Table " + tableNumber + " updated to " + newSeats + " seats.");
		}
		return RestaurantManagementResult.fail("Table " + tableNumber + " not found.");
	}

	/**
	 * Deletes a table from the restaurant.
	 * <p>
	 * Tables that are currently in use (have active reservations) cannot be
	 * deleted.
	 * </p>
	 *
	 * @param tableNumber the table number to delete
	 * @return the result indicating success or failure
	 * @throws SQLException if a database error occurs
	 */
	public RestaurantManagementResult deleteTable(int tableNumber) throws SQLException {
		if (tableNumber <= 0) {
			return RestaurantManagementResult.fail("Invalid table number.");
		}
		boolean deleted = db.deleteTable(tableNumber);
		if (deleted) {
			return RestaurantManagementResult.ok("Table " + tableNumber + " deleted successfully.");
		}
		return RestaurantManagementResult.fail("Table " + tableNumber + " not found or currently in use.");
	}

	// ======================== HOURS OPERATIONS ========================

	/**
	 * Retrieves the regular opening hours for all days of the week.
	 *
	 * @return a list of opening hours for each day
	 * @throws SQLException if a database error occurs
	 */
	public List<OpeningHours> getOpeningHours() throws SQLException {
		return db.getOpeningHours();
	}

	/**
	 * Updates the opening hours for a specific day of the week.
	 *
	 * @param day       the day of the week to update
	 * @param openTime  the opening time (required if not closed)
	 * @param closeTime the closing time (required if not closed)
	 * @param closed    whether the restaurant is closed on this day
	 * @return the result indicating success or failure
	 * @throws SQLException if a database error occurs
	 */
	public RestaurantManagementResult updateOpeningHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime,
			boolean closed) throws SQLException {
		if (day == null) {
			return RestaurantManagementResult.fail("Day of week is required.");
		}
		if (!closed && (openTime == null || closeTime == null)) {
			return RestaurantManagementResult.fail("Open and close times are required for non-closed days.");
		}
		boolean updated = db.updateOpeningHours(day, openTime, closeTime, closed);
		if (updated) {
			String status = closed ? "marked as closed" : "updated to " + openTime + " - " + closeTime;
			return RestaurantManagementResult.ok(day + " " + status + ".");
		}
		return RestaurantManagementResult.fail("Failed to update hours for " + day + ".");
	}

	// ======================== DATE OVERRIDE OPERATIONS ========================

	/**
	 * Retrieves all date-specific overrides for restaurant hours.
	 *
	 * @return a list of all date overrides
	 * @throws SQLException if a database error occurs
	 */
	public List<DateOverride> getDateOverrides() throws SQLException {
		return db.getDateOverrides();
	}

	/**
	 * Adds a date-specific override for restaurant hours.
	 * <p>
	 * Date overrides take precedence over regular weekly opening hours. They can be
	 * used for holidays, special events, or temporary closures.
	 * </p>
	 *
	 * @param date      the date for the override
	 * @param openTime  the opening time (required if not closed)
	 * @param closeTime the closing time (required if not closed)
	 * @param closed    whether the restaurant is closed on this date
	 * @param reason    optional reason for the override
	 * @return the result containing the new override ID on success
	 * @throws SQLException if a database error occurs
	 */
	public RestaurantManagementResult addDateOverride(LocalDate date, LocalTime openTime, LocalTime closeTime,
			boolean closed, String reason) throws SQLException {
		if (date == null) {
			return RestaurantManagementResult.fail("Date is required.");
		}
		if (!closed && (openTime == null || closeTime == null)) {
			return RestaurantManagementResult.fail("Open and close times are required for non-closed dates.");
		}
		int overrideId = db.addDateOverride(date, openTime, closeTime, closed, reason);
		if (overrideId > 0) {
			return RestaurantManagementResult.overrideAdded(overrideId,
					"Override for " + date + " added successfully.");
		}
		return RestaurantManagementResult.fail("Failed to add override. Date may already exist.");
	}

	/**
	 * Updates an existing date override.
	 *
	 * @param id        the ID of the override to update
	 * @param date      the date for the override
	 * @param openTime  the opening time (required if not closed)
	 * @param closeTime the closing time (required if not closed)
	 * @param closed    whether the restaurant is closed on this date
	 * @param reason    optional reason for the override
	 * @return the result indicating success or failure
	 * @throws SQLException if a database error occurs
	 */
	public RestaurantManagementResult updateDateOverride(int id, LocalDate date, LocalTime openTime,
			LocalTime closeTime, boolean closed, String reason) throws SQLException {
		if (id <= 0) {
			return RestaurantManagementResult.fail("Invalid override ID.");
		}
		if (date == null) {
			return RestaurantManagementResult.fail("Date is required.");
		}
		if (!closed && (openTime == null || closeTime == null)) {
			return RestaurantManagementResult.fail("Open and close times are required for non-closed dates.");
		}
		boolean updated = db.updateDateOverride(id, date, openTime, closeTime, closed, reason);
		if (updated) {
			return RestaurantManagementResult.ok("Override for " + date + " updated successfully.");
		}
		return RestaurantManagementResult.fail("Override not found.");
	}

	/**
	 * Deletes a date override.
	 *
	 * @param id the ID of the override to delete
	 * @return the result indicating success or failure
	 * @throws SQLException if a database error occurs
	 */
	public RestaurantManagementResult deleteDateOverride(int id) throws SQLException {
		if (id <= 0) {
			return RestaurantManagementResult.fail("Invalid override ID.");
		}
		boolean deleted = db.deleteDateOverride(id);
		if (deleted) {
			return RestaurantManagementResult.ok("Override deleted successfully.");
		}
		return RestaurantManagementResult.fail("Override not found.");
	}
}
