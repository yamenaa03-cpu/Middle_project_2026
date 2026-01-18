package common.dto.Reservation;

import java.time.LocalDateTime;

/**
 * Simple DTO containing basic reservation information used in lightweight
 * responses and notifications.
 */
public class ReservationBasicInfo {
	public final String fullName;
	public final LocalDateTime dateTime;
	public final int guests;
	public final int confirmationCode;

	/**
	 * Construct basic reservation info.
	 *
	 * @param fullName         customer's full name
	 * @param dateTime         reservation date/time
	 * @param guests           number of guests
	 * @param confirmationCode confirmation code
	 */
	public ReservationBasicInfo(String fullName, LocalDateTime dateTime, int guests, int confirmationCode) {
		this.fullName = fullName;
		this.dateTime = dateTime;
		this.guests = guests;
		this.confirmationCode = confirmationCode;
	}
}