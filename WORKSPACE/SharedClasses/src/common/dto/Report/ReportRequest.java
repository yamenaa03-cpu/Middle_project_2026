package common.dto.Report;

import java.io.Serializable;

import common.enums.ReportOperation;

/**
 * Request DTO for report operations sent from client to server.
 * <p>
 * This class is immutable from outside and can be created only through the
 * provided static factory methods. It supports requesting different types of
 * monthly reports including time-based and subscriber-based reports.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see ReportOperation
 * @see ReportResponse
 */
public class ReportRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The report operation being requested.
	 */
	private ReportOperation operation;

	/**
	 * The year for the requested report.
	 */
	private int year;

	/**
	 * The month for the requested report (1-12).
	 */
	private int month;

	/**
	 * Private constructor to enforce factory method usage.
	 */
	private ReportRequest() {
	}

	/**
	 * Creates a request for a time-based report.
	 * <p>
	 * Time reports show reservation statistics grouped by time slots for the
	 * specified month and year.
	 * </p>
	 *
	 * @param year  the year of the requested report
	 * @param month the month of the requested report (1-12)
	 * @return a ReportRequest configured for a time report
	 */
	public static ReportRequest createTimeReportRequest(int year, int month) {
		ReportRequest req = new ReportRequest();
		req.operation = ReportOperation.GET_TIME_REPORT;
		req.year = year;
		req.month = month;
		return req;
	}

	/**
	 * Creates a request for a subscriber-based report.
	 * <p>
	 * Subscriber reports show statistics about subscriber activity and behavior for
	 * the specified month and year.
	 * </p>
	 *
	 * @param year  the year of the requested report
	 * @param month the month of the requested report (1-12)
	 * @return a ReportRequest configured for a subscriber report
	 */
	public static ReportRequest createSubscriberReportRequest(int year, int month) {
		ReportRequest req = new ReportRequest();
		req.operation = ReportOperation.GET_SUBSCRIBER_REPORT;
		req.year = year;
		req.month = month;
		return req;
	}

	/**
	 * Returns the report operation type.
	 *
	 * @return the report operation
	 */
	public ReportOperation getOperation() {
		return operation;
	}

	/**
	 * Returns the year for the requested report.
	 *
	 * @return the report year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Returns the month for the requested report.
	 *
	 * @return the report month (1-12)
	 */
	public int getMonth() {
		return month;
	}
}
