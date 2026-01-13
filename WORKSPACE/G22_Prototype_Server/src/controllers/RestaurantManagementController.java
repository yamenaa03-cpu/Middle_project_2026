package controllers;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import common.entity.Table;
import common.entity.DateOverride;
import common.entity.OpeningHours;
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

	public int addTable(int seats) throws SQLException {
		if (seats <= 0) {
			throw new IllegalArgumentException("Seats must be positive.");
		}
		return db.addTable(seats);
	}

	public boolean updateTable(int tableNumber, int newSeats) throws SQLException {
		if (tableNumber <= 0 || newSeats <= 0) {
			return false;
		}
		return db.updateTableSeats(tableNumber, newSeats);
	}

	public boolean deleteTable(int tableNumber) throws SQLException {
		if (tableNumber <= 0) {
			return false;
		}
		return db.deleteTable(tableNumber);
	}

	// ======================== HOURS OPERATIONS ========================

	public List<OpeningHours> getOpeningHours() throws SQLException {
		return db.getOpeningHours();
	}

	public boolean updateOpeningHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime, boolean closed) throws SQLException {
		if (day == null) {
			return false;
		}
		if (!closed && (openTime == null || closeTime == null)) {
			return false;
		}
		return db.updateOpeningHours(day, openTime, closeTime, closed);
	}

	// ======================== DATE OVERRIDE OPERATIONS ========================

	public List<DateOverride> getDateOverrides() throws SQLException {
		return db.getDateOverrides();
	}

	public int addDateOverride(LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed, String reason) throws SQLException {
		if (date == null) {
			throw new IllegalArgumentException("Date is required.");
		}
		if (!closed && (openTime == null || closeTime == null)) {
			throw new IllegalArgumentException("Open and close times required for non-closed dates.");
		}
		return db.addDateOverride(date, openTime, closeTime, closed, reason);
	}

	public boolean updateDateOverride(int id, LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed, String reason) throws SQLException {
		if (id <= 0 || date == null) {
			return false;
		}
		if (!closed && (openTime == null || closeTime == null)) {
			return false;
		}
		return db.updateDateOverride(id, date, openTime, closeTime, closed, reason);
	}

	public boolean deleteDateOverride(int id) throws SQLException {
		if (id <= 0) {
			return false;
		}
		return db.deleteDateOverride(id);
	}
}
