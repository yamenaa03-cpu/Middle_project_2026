package common.dto.Report;

import java.io.Serializable;
import java.util.List;

import common.entity.SubscriberReportEntry;
import common.entity.TimeReportEntry;
import common.enums.ReportOperation;

/**
 * Response DTO for report operations sent from server to client.
 * <p>
 * Depending on the report operation, only one of the report entry lists will be
 * populated. For time reports, the timeReportEntries list will contain data.
 * For subscriber reports, the subscriberReportEntries list will contain data.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see ReportOperation
 * @see ReportRequest
 * @see TimeReportEntry
 * @see SubscriberReportEntry
 */
public class ReportResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Whether the report generation succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * The operation this response is for.
	 */
	private final ReportOperation operation;

	/**
	 * List of time report entries (populated for time reports).
	 */
	private final List<TimeReportEntry> timeReportEntries;

	/**
	 * List of subscriber report entries (populated for subscriber reports).
	 */
	private final List<SubscriberReportEntry> subscriberReportEntries;

	/**
	 * Private constructor used by factory methods.
	 *
	 * @param success                 indicates whether the request succeeded
	 * @param message                 descriptive result message
	 * @param operation               report operation type
	 * @param timeReportEntries       list of time report entries (nullable)
	 * @param subscriberReportEntries list of subscriber report entries (nullable)
	 */
	private ReportResponse(boolean success, String message, ReportOperation operation,
			List<TimeReportEntry> timeReportEntries, List<SubscriberReportEntry> subscriberReportEntries) {
		this.success = success;
		this.message = message;
		this.operation = operation;
		this.timeReportEntries = timeReportEntries;
		this.subscriberReportEntries = subscriberReportEntries;
	}

	/**
	 * Factory method to create a time report response.
	 *
	 * @param entries list of time report entries
	 * @param year    the report year
	 * @param month   the report month
	 * @return response containing time report data
	 */
	public static ReportResponse timeReport(List<TimeReportEntry> entries, int year, int month) {
		String msg = entries.isEmpty() ? "No data for " + month + "/" + year
				: "Time report for " + month + "/" + year + " (" + entries.size() + " entries)";
		return new ReportResponse(true, msg, ReportOperation.GET_TIME_REPORT, entries, null);
	}

	/**
	 * Factory method to create a subscriber report response.
	 *
	 * @param entries list of subscriber report entries
	 * @param year    the report year
	 * @param month   the report month
	 * @return response containing subscriber report data
	 */
	public static ReportResponse subscriberReport(List<SubscriberReportEntry> entries, int year, int month) {
		String msg = entries.isEmpty() ? "No subscriber data for " + month + "/" + year
				: "Subscriber report for " + month + "/" + year + " (" + entries.size() + " entries)";
		return new ReportResponse(true, msg, ReportOperation.GET_SUBSCRIBER_REPORT, null, entries);
	}

	/**
	 * Factory method to create a failure response.
	 *
	 * @param message   failure message
	 * @param operation the operation that failed
	 * @return failed response
	 */
	public static ReportResponse fail(String message, ReportOperation operation) {
		return new ReportResponse(false, message, operation, null, null);
	}

	/**
	 * Returns whether the report generation succeeded.
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
	 * Returns the operation this response is for.
	 *
	 * @return the report operation
	 */
	public ReportOperation getOperation() {
		return operation;
	}

	/**
	 * Returns the time report entries.
	 * <p>
	 * This list is populated only for GET_TIME_REPORT operations.
	 * </p>
	 *
	 * @return list of time report entries or null
	 */
	public List<TimeReportEntry> getTimeReportEntries() {
		return timeReportEntries;
	}

	/**
	 * Returns the subscriber report entries.
	 * <p>
	 * This list is populated only for GET_SUBSCRIBER_REPORT operations.
	 * </p>
	 *
	 * @return list of subscriber report entries or null
	 */
	public List<SubscriberReportEntry> getSubscriberReportEntries() {
		return subscriberReportEntries;
	}
}
