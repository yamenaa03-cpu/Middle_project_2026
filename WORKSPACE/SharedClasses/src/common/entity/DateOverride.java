package common.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class DateOverride implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int id;
	private final LocalDate date;
	private final LocalTime openTime;
	private final LocalTime closeTime;
	private final boolean closed;
	private final String reason;

	public DateOverride(int id, LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed,
			String reason) {
		this.id = id;
		this.date = date;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.closed = closed;
		this.reason = reason;
	}

	public static DateOverride closedDate(int id, LocalDate date, String reason) {
		return new DateOverride(id, date, null, null, true, reason);
	}

	public int getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getOpenTime() {
		return openTime;
	}

	public LocalTime getCloseTime() {
		return closeTime;
	}

	public boolean isClosed() {
		return closed;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		if (closed) {
			return date + ": Closed (" + reason + ")";
		}
		return date + ": " + openTime + " - " + closeTime + " (" + reason + ")";
	}
}
