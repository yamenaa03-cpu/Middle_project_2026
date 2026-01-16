package common.dto.Report;

import java.io.Serializable;
import java.util.List;

import common.enums.ReportOperation;
import common.enums.ReservationOperation;

public class ReportResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private final boolean success;
	private final String message;
	private final ReportOperation operation;

	private final List<TimeReportEntry> timeReportEntries;
	private final List<SubscriberReportEntry> subscriberReportEntries;

	private ReportResponse(boolean success, String message, ReportOperation operation,
			List<TimeReportEntry> timeReportEntries, List<SubscriberReportEntry> subscriberReportEntries) {
		this.success = success;
		this.message = message;
		this.operation = operation;
		this.timeReportEntries = timeReportEntries;
		this.subscriberReportEntries = subscriberReportEntries;
	}

	public static ReportResponse timeReport(List<TimeReportEntry> entries, int year, int month) {
		String msg = entries.isEmpty() ? "No data for " + month + "/" + year
				: "Time report for " + month + "/" + year + " (" + entries.size() + " entries)";
		return new ReportResponse(true, msg, ReportOperation.GET_TIME_REPORT, entries, null);
	}

	public static ReportResponse subscriberReport(List<SubscriberReportEntry> entries, int year, int month) {
		String msg = entries.isEmpty() ? "No subscriber data for " + month + "/" + year
				: "Subscriber report for " + month + "/" + year + " (" + entries.size() + " entries)";
		return new ReportResponse(true, msg, ReportOperation.GET_SUBSCRIBER_REPORT, null, entries);
	}

	public static ReportResponse fail(String message, ReportOperation operation) {
		return new ReportResponse(false, message, operation, null, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public ReportOperation getOperation() {
		return operation;
	}

	public List<TimeReportEntry> getTimeReportEntries() {
		return timeReportEntries;
	}

	public List<SubscriberReportEntry> getSubscriberReportEntries() {
		return subscriberReportEntries;
	}
}
