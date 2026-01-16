package common.dto.Report;

import java.util.List;

public class ReportResult {
	private final boolean success;
	private final String message;
	private final List<TimeReportEntry> timeEntries;
	private final List<SubscriberReportEntry> subscriberEntries;

	private ReportResult(boolean success, String message, List<TimeReportEntry> timeEntries,
			List<SubscriberReportEntry> subscriberEntries) {
		this.success = success;
		this.message = message;
		this.timeEntries = timeEntries;
		this.subscriberEntries = subscriberEntries;
	}

	public static ReportResult timeReportSuccess(List<TimeReportEntry> entries) {
		return new ReportResult(true, "Time report loaded", entries, null);
	}

	public static ReportResult subscriberReportSuccess(List<SubscriberReportEntry> entries) {
		return new ReportResult(true, "Subscriber report loaded", null, entries);
	}

	public static ReportResult fail(String message) {
		return new ReportResult(false, message, null, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public List<TimeReportEntry> getTimeEntries() {
		return timeEntries;
	}

	public List<SubscriberReportEntry> getSubscriberEntries() {
		return subscriberEntries;
	}
}
