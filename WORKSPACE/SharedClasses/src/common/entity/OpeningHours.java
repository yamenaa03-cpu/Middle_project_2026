package common.entity;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents the regular opening hours for a specific day of the week.
 * <p>
 * This entity stores the default operating schedule for the restaurant.
 * Each day of the week has its own OpeningHours entry specifying when
 * the restaurant opens and closes, or whether it's closed entirely.
 * Date-specific overrides ({@link DateOverride}) take precedence over
 * these regular hours.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see DateOverride
 */
public class OpeningHours implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The day of the week this schedule applies to.
	 */
	private final DayOfWeek dayOfWeek;

	/**
	 * The opening time for this day (null if closed).
	 */
	private final LocalTime openTime;

	/**
	 * The closing time for this day (null if closed).
	 * <p>
	 * Note: If closing time is before opening time (e.g., 02:00),
	 * it indicates the restaurant stays open past midnight.
	 * </p>
	 */
	private final LocalTime closeTime;

	/**
	 * Whether the restaurant is closed on this day.
	 */
	private final boolean closed;

	/**
	 * Constructs an OpeningHours entry for a specific day.
	 *
	 * @param dayOfWeek the day of the week
	 * @param openTime  opening time (null if closed)
	 * @param closeTime closing time (null if closed)
	 * @param closed    whether the restaurant is closed
	 */
	public OpeningHours(DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime, boolean closed) {
		this.dayOfWeek = dayOfWeek;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.closed = closed;
	}

	/**
	 * Factory method to create a closed day entry.
	 *
	 * @param day the day of the week to mark as closed
	 * @return an OpeningHours entry marked as closed
	 */
	public static OpeningHours closedDay(DayOfWeek day) {
		return new OpeningHours(day, null, null, true);
	}

	/**
	 * Returns the day of the week this entry applies to.
	 *
	 * @return the day of the week
	 */
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	/**
	 * Returns the opening time for this day, or null if closed.
	 *
	 * @return opening time or null
	 */
	public LocalTime getOpenTime() {
		return openTime;
	}

	/**
	 * Returns the closing time for this day, or null if closed.
	 *
	 * @return closing time or null
	 */
	public LocalTime getCloseTime() {
		return closeTime;
	}

	/**
	 * Returns whether the restaurant is closed on this day.
	 *
	 * @return true if closed, false if open
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Returns a string representation of the opening hours for display.
	 *
	 * @return formatted string showing day and hours or closed status
	 */
	@Override
	public String toString() {
		if (closed) {
			return dayOfWeek + ": Closed";
		}
		return dayOfWeek + ": " + openTime + " - " + closeTime;
	}
}