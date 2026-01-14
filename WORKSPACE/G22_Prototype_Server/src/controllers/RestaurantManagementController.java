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

public class RestaurantManagementController {

	private final DBController db;

	public RestaurantManagementController(DBController db) {
		this.db = db;
	}

	// ======================== TABLE OPERATIONS ========================

	public List<Table> getAllTables() throws SQLException {
		return db.getAllTables();
	}

	public RestaurantManagementResult addTable(int seats) throws SQLException {
		if (seats <= 0) {
			return RestaurantManagementResult.fail("Seats must be a positive number.");
		}
		int newTableNumber = db.addTable(seats);
		if (newTableNumber > 0) {
			return RestaurantManagementResult.tableAdded(newTableNumber, "Table " + newTableNumber + " added successfully.");
		}
		return RestaurantManagementResult.fail("Failed to add table.");
	}

	public RestaurantManagementResult updateTable(int tableNumber, int newSeats) throws SQLException {
		if (tableNumber <= 0) {
			return RestaurantManagementResult.fail("Invalid table number.");
		}
		if (newSeats <= 0) {
			return RestaurantManagementResult.fail("Seats must be a positive number.");
		}
		boolean updated = db.updateTableSeats(tableNumber, newSeats);
		if (updated) {
			return RestaurantManagementResult.ok("Table " + tableNumber + " updated to " + newSeats + " seats.");
		}
		return RestaurantManagementResult.fail("Table " + tableNumber + " not found.");
	}

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

	public List<OpeningHours> getOpeningHours() throws SQLException {
		return db.getOpeningHours();
	}

	public RestaurantManagementResult updateOpeningHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime, boolean closed) throws SQLException {
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

	public List<DateOverride> getDateOverrides() throws SQLException {
		return db.getDateOverrides();
	}

	public RestaurantManagementResult addDateOverride(LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed, String reason) throws SQLException {
		if (date == null) {
			return RestaurantManagementResult.fail("Date is required.");
		}
		if (!closed && (openTime == null || closeTime == null)) {
			return RestaurantManagementResult.fail("Open and close times are required for non-closed dates.");
		}
		int overrideId = db.addDateOverride(date, openTime, closeTime, closed, reason);
		if (overrideId > 0) {
			return RestaurantManagementResult.overrideAdded(overrideId, "Override for " + date + " added successfully.");
		}
		return RestaurantManagementResult.fail("Failed to add override. Date may already exist.");
	}

	public RestaurantManagementResult updateDateOverride(int id, LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed, String reason) throws SQLException {
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
