package common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.dto.Report.ReportResponse;

/**
 * Represents a single reservation record in a time-based report.
 * <p>
 * Each entry includes scheduling information, actual check-in and check-out
 * times, customer details, and calculated time metrics. Used for analyzing
 * reservation timing patterns and customer behavior.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see ReportResponse
 */
public class TimeReportEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Unique identifier for the reservation.
	 */
	private final int reservationId;

	/**
	 * Scheduled reservation date and time.
	 */
	private final LocalDateTime scheduledTime;

	/**
	 * Actual check-in time when customer arrived.
	 */
	private final LocalDateTime checkedInAt;

	/**
	 * Actual check-out time when customer left.
	 */
	private final LocalDateTime checkedOutAt;

	/**
	 * Number of guests in the party.
	 */
	private final int numberOfGuests;

	/**
	 * Full name of the customer.
	 */
	private final String customerName;

	/**
	 * Whether the customer is a subscriber.
	 */
	private final boolean isSubscriber;

	/**
	 * Constructs a new TimeReportEntry with reservation timing details.
	 *
	 * @param reservationId  unique reservation identifier
	 * @param scheduledTime  scheduled reservation time
	 * @param checkedInAt    actual check-in time
	 * @param checkedOutAt   actual check-out time
	 * @param numberOfGuests number of guests
	 * @param customerName   customer's full name
	 * @param isSubscriber   true if the customer is a subscriber
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

	/**
	 * Returns the reservation ID.
	 *
	 * @return unique reservation identifier
	 */
	public int getReservationId() {
		return reservationId;
	}

	/**
	 * Returns the scheduled reservation time.
	 *
	 * @return scheduled date and time
	 */
	public LocalDateTime getScheduledTime() {
		return scheduledTime;
	}

	/**
	 * Returns the actual check-in time.
	 *
	 * @return check-in timestamp or null if not checked in
	 */
	public LocalDateTime getCheckedInAt() {
		return checkedInAt;
	}

	/**
	 * Returns the actual check-out time.
	 *
	 * @return check-out timestamp or null if not checked out
	 */
	public LocalDateTime getCheckedOutAt() {
		return checkedOutAt;
	}

	/**
	 * Returns the number of guests in the party.
	 *
	 * @return guest count
	 */
	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	/**
	 * Returns the customer's name.
	 *
	 * @return customer full name
	 */
	public String getCustomerName() {
		return customerName;
	}

	/**
	 * Returns whether the customer is a subscriber.
	 *
	 * @return true if subscriber, false if guest
	 */
	public boolean isSubscriber() {
		return isSubscriber;
	}

	/**
	 * Calculates the arrival delay in minutes.
	 * <p>
	 * Positive values indicate the customer arrived late. Negative values indicate
	 * early arrival.
	 * </p>
	 *
	 * @return delay in minutes, or 0 if times are not available
	 */
	public long getArrivalDelayMinutes() {
		if (scheduledTime == null || checkedInAt == null)
			return 0;
		return java.time.Duration.between(scheduledTime, checkedInAt).toMinutes();
	}

	/**
	 * Calculates the total session duration in minutes.
	 * <p>
	 * Duration from check-in to check-out.
	 * </p>
	 *
	 * @return session duration in minutes, or 0 if times are not available
	 */
	public long getSessionDurationMinutes() {
		if (checkedInAt == null || checkedOutAt == null)
			return 0;
		return java.time.Duration.between(checkedInAt, checkedOutAt).toMinutes();
	}
}
