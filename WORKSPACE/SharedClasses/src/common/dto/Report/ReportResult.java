package common.dto.Report;

import java.util.List;

import common.entity.SubscriberReportEntry;
import common.entity.TimeReportEntry;

/**
 * Result object for internal report processing within the application layer.
 * <p>
 * This class wraps the result of generating a report before it is converted to
 * a ReportResponse for client communication. Only one type of report entries
 * will be populated at a time based on the requested report type.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see TimeReportEntry
 * @see SubscriberReportEntry
 */
public class ReportResult {

	/**
	 * Whether the report generation succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * List of time report entries (for time reports).
	 */
	private final List<TimeReportEntry> timeEntries;

	/**
	 * List of subscriber report entries (for subscriber reports).
	 */
	private final List<SubscriberReportEntry> subscriberEntries;

	/**
	 * Private constructor used by factory methods.
	 *
	 * @param success           whether generation succeeded
	 * @param message           descriptive message
	 * @param timeEntries       time report entries (nullable)
	 * @param subscriberEntries subscriber report entries (nullable)
	 */
	private ReportResult(boolean success, String message, List<TimeReportEntry> timeEntries,
			List<SubscriberReportEntry> subscriberEntries) {
		this.success = success;
		this.message = message;
		this.timeEntries = timeEntries;
		this.subscriberEntries = subscriberEntries;
	}

	/**
	 * Creates a successful time report result.
	 *
	 * @param entries list of time report entries
	 * @return success result with time entries
	 */
	public static ReportResult timeReportSuccess(List<TimeReportEntry> entries) {
		return new ReportResult(true, "Time report loaded", entries, null);
	}

	/**
	 * Creates a successful subscriber report result.
	 *
	 * @param entries list of subscriber report entries
	 * @return success result with subscriber entries
	 */
	public static ReportResult subscriberReportSuccess(List<SubscriberReportEntry> entries) {
		return new ReportResult(true, "Subscriber report loaded", null, entries);
	}

	/**
	 * Creates a failure result.
	 *
	 * @param message failure message
	 * @return failed result
	 */
	public static ReportResult fail(String message) {
		return new ReportResult(false, message, null, null);
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
	 * Returns the time report entries.
	 *
	 * @return list of time entries or null
	 */
	public List<TimeReportEntry> getTimeEntries() {
		return timeEntries;
	}

	/**
	 * Returns the subscriber report entries.
	 *
	 * @return list of subscriber entries or null
	 */
	public List<SubscriberReportEntry> getSubscriberEntries() {
		return subscriberEntries;
	}
}
