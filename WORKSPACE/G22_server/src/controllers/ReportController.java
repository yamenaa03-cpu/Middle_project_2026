package controllers;

import java.sql.SQLException;
import java.util.List;

import common.entity.SubscriberReportEntry;
import common.entity.TimeReportEntry;
import dbController.DBController;

/**
 * Controller responsible for generating and retrieving restaurant reports.
 * <p>
 * This controller handles the generation and storage of monthly reports:
 * <ul>
 * <li><strong>Time Reports:</strong> Detailed reservation timing data including
 * scheduled times, actual check-in/out times, and session durations</li>
 * <li><strong>Subscriber Reports:</strong> Aggregated statistics per subscriber
 * including total reservations, completions, cancellations, and waitlist
 * entries</li>
 * </ul>
 * Reports are generated from live data and stored for historical access.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class ReportController {

	/**
	 * Database controller for data persistence operations.
	 */
	private final DBController db;

	/**
	 * Constructs a ReportController with the specified database controller.
	 *
	 * @param db the database controller for data access
	 */
	public ReportController(DBController db) {
		this.db = db;
	}

	/**
	 * Generates and stores both time and subscriber reports for a specific month.
	 * <p>
	 * This method is typically called by a scheduled task at the end of each month
	 * to archive the monthly statistics.
	 * </p>
	 *
	 * @param year  the year of the report
	 * @param month the month of the report (1-12)
	 * @throws SQLException if a database error occurs
	 */
	public void generateAndStoreMonthlyReports(int year, int month) throws SQLException {
		generateAndStoreTimeReport(year, month);
		generateAndStoreSubscriberReport(year, month);
	}

	/**
	 * Generates and stores the time report for a specific month.
	 * <p>
	 * Clears any existing stored report for the month before inserting new data.
	 * </p>
	 *
	 * @param year  the year of the report
	 * @param month the month of the report (1-12)
	 * @throws SQLException if a database error occurs
	 */
	public void generateAndStoreTimeReport(int year, int month) throws SQLException {
		List<TimeReportEntry> entries = db.getTimeReportForMonth(year, month);
		db.clearTimeReport(year, month);
		for (TimeReportEntry entry : entries) {
			db.insertTimeReportEntry(year, month, entry);
		}
	}

	/**
	 * Generates and stores the subscriber report for a specific month.
	 * <p>
	 * Clears any existing stored report for the month before inserting new data.
	 * </p>
	 *
	 * @param year  the year of the report
	 * @param month the month of the report (1-12)
	 * @throws SQLException if a database error occurs
	 */
	public void generateAndStoreSubscriberReport(int year, int month) throws SQLException {
		List<SubscriberReportEntry> entries = db.getSubscriberReportForMonth(year, month);
		db.clearSubscriberReport(year, month);
		for (SubscriberReportEntry entry : entries) {
			db.insertSubscriberReportEntry(year, month, entry);
		}
	}

	/**
	 * Retrieves the stored time report for a specific month.
	 *
	 * @param year  the year of the report
	 * @param month the month of the report (1-12)
	 * @return a list of time report entries for the specified month
	 * @throws SQLException if a database error occurs
	 */
	public List<TimeReportEntry> getStoredTimeReport(int year, int month) throws SQLException {
		return db.getStoredTimeReport(year, month);
	}

	/**
	 * Retrieves the stored subscriber report for a specific month.
	 *
	 * @param year  the year of the report
	 * @param month the month of the report (1-12)
	 * @return a list of subscriber report entries for the specified month
	 * @throws SQLException if a database error occurs
	 */
	public List<SubscriberReportEntry> getStoredSubscriberReport(int year, int month) throws SQLException {
		return db.getStoredSubscriberReport(year, month);
	}

	/**
	 * Checks if a stored time report exists for the specified month.
	 *
	 * @param year  the year to check
	 * @param month the month to check (1-12)
	 * @return true if a stored time report exists, false otherwise
	 * @throws SQLException if a database error occurs
	 */
	public boolean hasStoredTimeReport(int year, int month) throws SQLException {
		return db.hasStoredTimeReport(year, month);
	}

	/**
	 * Checks if a stored subscriber report exists for the specified month.
	 *
	 * @param year  the year to check
	 * @param month the month to check (1-12)
	 * @return true if a stored subscriber report exists, false otherwise
	 * @throws SQLException if a database error occurs
	 */
	public boolean hasStoredSubscriberReport(int year, int month) throws SQLException {
		return db.hasStoredSubscriberReport(year, month);
	}
}
