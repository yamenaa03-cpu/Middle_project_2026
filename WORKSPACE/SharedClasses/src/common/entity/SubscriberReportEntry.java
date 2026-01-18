package common.entity;

import java.io.Serializable;

import common.dto.Report.ReportResponse;

/**
 * Represents a single row in a subscriber-based report.
 * <p>
 * Each entry contains aggregated reservation statistics for a specific
 * subscriber within a given reporting period. Used for analyzing subscriber
 * activity and engagement patterns.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see ReportResponse
 */
public class SubscriberReportEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Unique identifier for the customer.
	 */
	private final int customerId;

	/**
	 * Full name of the customer.
	 */
	private final String customerName;

	/**
	 * Subscription code for the subscriber.
	 */
	private final String subscriptionCode;

	/**
	 * Total number of reservations made in the period.
	 */
	private final int totalReservations;

	/**
	 * Number of reservations completed (checked out).
	 */
	private final int completedReservations;

	/**
	 * Number of reservations cancelled.
	 */
	private final int cancelledReservations;

	/**
	 * Number of waitlist entries in the period.
	 */
	private final int waitlistEntries;

	/**
	 * Constructs a SubscriberReportEntry with aggregated statistics.
	 *
	 * @param customerId            unique customer identifier
	 * @param customerName          customer's full name
	 * @param subscriptionCode      subscriber's subscription code
	 * @param totalReservations     total reservations in period
	 * @param completedReservations completed reservations count
	 * @param cancelledReservations cancelled reservations count
	 * @param waitlistEntries       waitlist entries count
	 */
	public SubscriberReportEntry(int customerId, String customerName, String subscriptionCode, int totalReservations,
			int completedReservations, int cancelledReservations, int waitlistEntries) {
		this.customerId = customerId;
		this.customerName = customerName;
		this.subscriptionCode = subscriptionCode;
		this.totalReservations = totalReservations;
		this.completedReservations = completedReservations;
		this.cancelledReservations = cancelledReservations;
		this.waitlistEntries = waitlistEntries;
	}

	/**
	 * Returns the customer ID.
	 *
	 * @return unique customer identifier
	 */
	public int getCustomerId() {
		return customerId;
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
	 * Returns the subscription code.
	 *
	 * @return subscriber's unique code
	 */
	public String getSubscriptionCode() {
		return subscriptionCode;
	}

	/**
	 * Returns the total number of reservations.
	 *
	 * @return total reservation count
	 */
	public int getTotalReservations() {
		return totalReservations;
	}

	/**
	 * Returns the number of completed reservations.
	 *
	 * @return completed reservation count
	 */
	public int getCompletedReservations() {
		return completedReservations;
	}

	/**
	 * Returns the number of cancelled reservations.
	 *
	 * @return cancelled reservation count
	 */
	public int getCancelledReservations() {
		return cancelledReservations;
	}

	/**
	 * Returns the number of waitlist entries.
	 *
	 * @return waitlist entry count
	 */
	public int getWaitlistEntries() {
		return waitlistEntries;
	}
}
