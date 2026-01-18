package common.enums;

/**
 * Enumeration of reservation types based on how the reservation was created.
 * <p>
 * This classification affects billing (subscribers get discounts) and
 * how the reservation is processed in the system.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum ReservationType {
	/**
	 * Reservation made in advance through the booking system.
	 * The customer scheduled a specific date and time beforehand.
	 */
	ADVANCE,

	/**
	 * Walk-in reservation created when a customer arrives at the restaurant.
	 * May also include customers joining the waitlist on-site.
	 */
	WALKIN 
}