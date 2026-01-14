package common.dto.Report;

import java.io.Serializable;

public class SubscriberReportEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int customerId;
	private final String customerName;
	private final String subscriptionCode;
	private final int totalReservations;
	private final int completedReservations;
	private final int cancelledReservations;
	private final int waitlistEntries;

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

	public int getCustomerId() {
		return customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getSubscriptionCode() {
		return subscriptionCode;
	}

	public int getTotalReservations() {
		return totalReservations;
	}

	public int getCompletedReservations() {
		return completedReservations;
	}

	public int getCancelledReservations() {
		return cancelledReservations;
	}

	public int getWaitlistEntries() {
		return waitlistEntries;
	}
}
