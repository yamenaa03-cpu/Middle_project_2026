package common.dto.Report;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TimeReportEntry represents a single reservation record in a time-based report.
 * Each entry includes scheduling information, actual check-in and check-out times,
 * customer details, and calculated time metrics.
 */
public class TimeReportEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int reservationId;
	private final LocalDateTime scheduledTime;
	private final LocalDateTime checkedInAt;
	private final LocalDateTime checkedOutAt;
	private final int numberOfGuests;
	private final String customerName;
	private final boolean isSubscriber;

    /**
     * Constructs a new {@link TimeReportEntry} with reservation timing details.
     *
     * @param reservationId  unique reservation identifier
     * @param scheduledTime  scheduled reservation time
     * @param checkedInAt    actual check-in time 
     * @param checkedOutAt   actual check-out time 
     * @param numberOfGuests number of guests
     * @param customerName   customer's full name
     * @param isSubscriber   {@code true} if the customer is a subscriber
     */
	public TimeReportEntry(int reservationId, LocalDateTime scheduledTime, LocalDateTime checkedInAt,
			LocalDateTime checkedOutAt, int numberOfGuests, String customerName, boolean isSubscriber) {
		this.reservationId = reservationId;
		this.scheduledTime = scheduledTime;
		this.checkedInAt = checkedInAt;
		this.checkedOutAt = checkedOutAt;
		this.numberOfGuests = numberOfGuests;
		this.customerName = customerName;
		this.isSubscriber = isSubscriber;
	}

	public int getReservationId() {
		return reservationId;
	}

	public LocalDateTime getScheduledTime() {
		return scheduledTime;
	}

	public LocalDateTime getCheckedInAt() {
		return checkedInAt;
	}

	public LocalDateTime getCheckedOutAt() {
		return checkedOutAt;
	}

	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	public String getCustomerName() {
		return customerName;
	}

	public boolean isSubscriber() {
		return isSubscriber;
	}

	public long getArrivalDelayMinutes() {
		if (scheduledTime == null || checkedInAt == null)
			return 0;
		return java.time.Duration.between(scheduledTime, checkedInAt).toMinutes();
	}

	public long getSessionDurationMinutes() {
		if (checkedInAt == null || checkedOutAt == null)
			return 0;
		return java.time.Duration.between(checkedInAt, checkedOutAt).toMinutes();
	}
}
