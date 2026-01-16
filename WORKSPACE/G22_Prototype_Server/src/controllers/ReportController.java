package controllers;

import java.sql.SQLException;
import java.util.List;

import common.dto.Report.TimeReportEntry;
import common.dto.Report.SubscriberReportEntry;
import dbController.DBController;

public class ReportController {

	private final DBController db;

	public ReportController(DBController db) {
		this.db = db;
	}

	public void generateAndStoreMonthlyReports(int year, int month) throws SQLException {
		generateAndStoreTimeReport(year, month);
		generateAndStoreSubscriberReport(year, month);
	}

	public void generateAndStoreTimeReport(int year, int month) throws SQLException {
		List<TimeReportEntry> entries = db.getTimeReportForMonth(year, month);
		db.clearTimeReport(year, month);
		for (TimeReportEntry entry : entries) {
			db.insertTimeReportEntry(year, month, entry);
		}
	}

	public void generateAndStoreSubscriberReport(int year, int month) throws SQLException {
		List<SubscriberReportEntry> entries = db.getSubscriberReportForMonth(year, month);
		db.clearSubscriberReport(year, month);
		for (SubscriberReportEntry entry : entries) {
			db.insertSubscriberReportEntry(year, month, entry);
		}
	}

	public List<TimeReportEntry> getStoredTimeReport(int year, int month) throws SQLException {
		return db.getStoredTimeReport(year, month);
	}

	public List<SubscriberReportEntry> getStoredSubscriberReport(int year, int month) throws SQLException {
		return db.getStoredSubscriberReport(year, month);
	}

	public boolean hasStoredTimeReport(int year, int month) throws SQLException {
		return db.hasStoredTimeReport(year, month);
	}

	public boolean hasStoredSubscriberReport(int year, int month) throws SQLException {
		return db.hasStoredSubscriberReport(year, month);
	}
}
