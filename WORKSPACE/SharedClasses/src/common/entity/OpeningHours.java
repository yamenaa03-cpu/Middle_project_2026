package common.entity;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class OpeningHours implements Serializable {
	private static final long serialVersionUID = 1L;

	private final DayOfWeek dayOfWeek;
	private final LocalTime openTime;
	private final LocalTime closeTime;
	private final boolean closed;

	public OpeningHours(DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime, boolean closed) {
		this.dayOfWeek = dayOfWeek;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.closed = closed;
	}

	public static OpeningHours closedDay(DayOfWeek day) {
		return new OpeningHours(day, null, null, true);
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
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

	@Override
	public String toString() {
		if (closed) {
			return dayOfWeek + ": Closed";
		}
		return dayOfWeek + ": " + openTime + " - " + closeTime;
	}
}
