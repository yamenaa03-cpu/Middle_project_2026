package common.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a date-specific override for restaurant opening hours.
 * <p>
 * Date overrides take precedence over the regular weekly opening hours,
 * allowing for special schedules on specific dates such as holidays,
 * special events, or temporary closures. An override can either specify
 * custom hours or mark the date as completely closed.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see OpeningHours
 */
public class DateOverride implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Unique identifier for the override (primary key).
	 */
	private final int id;

	/**
	 * The specific date this override applies to.
	 */
	private final LocalDate date;

	/**
	 * Opening time for this date (null if closed).
	 */
	private final LocalTime openTime;

	/**
	 * Closing time for this date (null if closed).
	 */
	private final LocalTime closeTime;

	/**
	 * Whether the restaurant is closed on this date.
	 */
	private final boolean closed;

	/**
	 * Optional reason for the override (e.g., "Holiday", "Private Event").
	 */
	private final String reason;

	/**
	 * Constructs a DateOverride with all fields.
	 *
	 * @param id        unique identifier for the override
	 * @param date      the date this override applies to
	 * @param openTime  opening time (null if closed)
	 * @param closeTime closing time (null if closed)
	 * @param closed    whether the restaurant is closed
	 * @param reason    optional reason for the override
	 */
	public DateOverride(int id, LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed,
			String reason) {
		this.id = id;
		this.date = date;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.closed = closed;
		this.reason = reason;
	}

	/**
	 * Factory method to create a closed date override.
	 *
	 * @param id     unique identifier for the override
	 * @param date   the date to mark as closed
	 * @param reason reason for the closure
	 * @return a DateOverride marked as closed
	 */
	public static DateOverride closedDate(int id, LocalDate date, String reason) {
		return new DateOverride(id, date, null, null, true, reason);
	}

	/**
	 * Returns the unique identifier for this override.
	 *
	 * @return the override ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the date this override applies to.
	 *
	 * @return the override date
	 */
	public LocalDate getDate() {
		return date;
	}

	/**
	 * Returns the opening time for this date, or null if closed.
	 *
	 * @return opening time or null
	 */
	public LocalTime getOpenTime() {
		return openTime;
	}

	/**
	 * Returns the closing time for this date, or null if closed.
	 *
	 * @return closing time or null
	 */
	public LocalTime getCloseTime() {
		return closeTime;
	}

	/**
	 * Returns whether the restaurant is closed on this date.
	 *
	 * @return true if closed, false if open with custom hours
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Returns the reason for this override.
	 *
	 * @return the reason string, may be null
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Returns a string representation of the override for display.
	 *
	 * @return formatted string showing date, hours or closure status, and reason
	 */
	@Override
	public String toString() {
		if (closed) {
			return date + ": Closed (" + reason + ")";
		}
		return date + ": " + openTime + " - " + closeTime + " (" + reason + ")";
	}
}