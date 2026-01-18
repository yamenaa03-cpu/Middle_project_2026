package controllers;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import common.dto.Reservation.CancelReservationResult;
import common.dto.Reservation.CreateReservationResult;
import common.dto.Reservation.InsertReservationResult;
import common.dto.Reservation.PayBillResult;
import common.dto.Reservation.ReceiveTableResult;
import common.dto.Reservation.ReservationBasicInfo;
import common.dto.Reservation.UpdateReservationResult;
import common.dto.Reservation.WaitingCandidate;
import common.entity.Bill;
import common.entity.DateOverride;
import common.entity.OpeningHours;
import common.entity.Reservation;
import common.entity.SubscriberReportEntry;
import common.entity.TimeReportEntry;
import common.enums.ReservationStatus;
import dbController.DBController;

/**
 * Business logic controller for all reservation-related operations.
 * <p>
 * This controller handles the complete reservation lifecycle including:
 * </p>
 * <ul>
 * <li>Creating advance reservations and walk-in waitlist entries</li>
 * <li>Validating availability and opening hours</li>
 * <li>Table assignment and check-in (receiving tables)</li>
 * <li>Bill generation and payment processing</li>
 * <li>Cancellation and no-show handling</li>
 * <li>Waitlist management and customer notifications</li>
 * <li>Manager reports generation</li>
 * </ul>
 *
 * <h2>Key Business Rules:</h2>
 * <ul>
 * <li>Reservations must be at least 1 hour in advance</li>
 * <li>Reservations cannot be more than 1 month ahead</li>
 * <li>Time slots are in 30-minute intervals</li>
 * <li>Each reservation has a 2-hour (120 min) duration</li>
 * <li>Subscribers receive a 10% discount on bills</li>
 * </ul>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 * @see DBController
 */
public class ReservationController {

	/**
	 * Database controller for all data access operations.
	 */
	private final DBController db;

	/**
	 * Default reservation duration in minutes (2 hours).
	 */
	private static final int DURATION_MIN = 120;

	/**
	 * Default opening time if database has no configuration.
	 */
	private static final LocalTime DEFAULT_OPEN = LocalTime.of(10, 0);

	/**
	 * Default closing time if database has no configuration (next day 2 AM).
	 */
	private static final LocalTime DEFAULT_CLOSE = LocalTime.of(2, 0);

	/**
	 * Constructs a ReservationController with the given database controller.
	 *
	 * @param db the database controller for data access
	 */
	public ReservationController(DBController db) {
		this.db = db;
	}

	/**
	 * Retrieves all active reservations (ACTIVE, NOTIFIED, IN_PROGRESS status).
	 *
	 * @return list of active reservations ordered by date/time
	 * @throws SQLException if database access fails
	 */
	public List<Reservation> getAllActiveReservations() throws SQLException {
		return db.getActiveReservations();
	}

	/**
	 * Retrieves all waitlist reservations (WAITING status).
	 *
	 * @return list of waiting reservations ordered by creation time
	 * @throws SQLException if database access fails
	 */
	public List<Reservation> getWaitlistReservations() throws SQLException {
		return db.getWaitlistReservations();
	}

	/**
	 * Retrieves reservation history for a specific subscriber.
	 *
	 * @param customerId the subscriber's customer ID
	 * @return list of all reservations for the customer, or empty list if invalid
	 *         ID
	 * @throws SQLException if database access fails
	 */
	public List<Reservation> getSubscriberHistory(int customerId) throws SQLException {
		if (customerId <= 0)
			return new ArrayList<>();
		return db.getReservationHistoryByCustomerId(customerId);
	}

	/**
	 * Updates an existing reservation's date/time and guest count.
	 * <p>
	 * Validates the new time against opening hours and 30-minute slot requirements.
	 * </p>
	 *
	 * @param reservationId the reservation ID to update
	 * @param newDateTime   the new date and time
	 * @param newGuests     the new number of guests
	 * @return result indicating success or failure with message
	 * @throws SQLException if database access fails
	 */
	public UpdateReservationResult updateReservation(int reservationId, LocalDateTime newDateTime, int newGuests)
			throws SQLException {

		if (reservationId <= 0)
			return UpdateReservationResult.fail("Invalid reservation ID.");
		if (newDateTime == null)
			return UpdateReservationResult.fail("Date and time is required.");
		if (newGuests <= 0)
			return UpdateReservationResult.fail("Number of guests must be positive.");

		String hoursError = validateOpeningHours(newDateTime);
		if (hoursError != null)
			return UpdateReservationResult.fail(hoursError);

		int m = newDateTime.getMinute();
		if (m != 0 && m != 30)
			return UpdateReservationResult.fail("Time must be in 30-minute intervals.");

		boolean updated = db.updateReservationFields(reservationId, newDateTime, newGuests);
		if (!updated)
			return UpdateReservationResult.fail("Reservation not found or could not be updated.");

		return UpdateReservationResult.ok(reservationId);
	}

	/**
	 * Creates a reservation for a guest (non-subscriber).
	 * <p>
	 * If the guest already exists (by phone/email), uses existing customer record.
	 * Otherwise creates a new guest customer record.
	 * </p>
	 *
	 * @param dateTime the requested date and time
	 * @param guests   number of guests
	 * @param fullName guest's full name
	 * @param phone    guest's phone number
	 * @param email    guest's email address
	 * @return result with reservation ID and confirmation code, or failure
	 * @throws SQLException if database access fails
	 */
	public CreateReservationResult createGuestReservation(LocalDateTime dateTime, int guests, String fullName,
			String phone, String email) throws SQLException {
		if (guests <= 0)
			return CreateReservationResult.fail("Invalid number of guests.");

		int customerId;
		try {
			customerId = getOrCreateGuestCustomerId(fullName, phone, email);
		} catch (IllegalArgumentException e) {
			return CreateReservationResult.fail(e.getMessage());
		}

		return createReservation(customerId, dateTime, guests);
	}

	/**
	 * Creates a reservation for an existing customer (subscriber or guest).
	 * <p>
	 * Validates the request against business rules and checks availability. If no
	 * space is available, returns suggested alternative times.
	 * </p>
	 *
	 * @param customerId the customer ID
	 * @param start      the requested date and time
	 * @param guests     number of guests
	 * @return result with reservation ID and confirmation code, or failure with
	 *         suggestions
	 * @throws SQLException if database access fails
	 */
	public CreateReservationResult createReservation(int customerId, LocalDateTime start, int guests)
			throws SQLException {

		if (customerId <= 0)
			return CreateReservationResult.fail("Invalid customer id.");

		String err = validateRequest(start, guests);
		if (err != null)
			return CreateReservationResult.fail(err);

		if (!isAvailableAt(start, guests)) {
			List<LocalDateTime> sug = suggestTimes(start, guests);
			return CreateReservationResult.noSpace("No space at requested time.", sug);
		}

		InsertReservationResult r = db.insertReservation(customerId, start, guests);
		if (r == null)
			return CreateReservationResult.fail("Insert failed.");

		return CreateReservationResult.ok(r.getReservationId(), r.getConfirmationCode());
	}

	/**
	 * Validates a reservation request against business rules.
	 *
	 * @param start  the requested date and time
	 * @param guests number of guests
	 * @return error message if validation fails, null if valid
	 * @throws SQLException if database access fails
	 */
	private String validateRequest(LocalDateTime start, int guests) throws SQLException {
		if (start == null)
			return "Missing date/time.";
		if (guests <= 0)
			return "Guests must be positive.";

		LocalDateTime now = LocalDateTime.now();

		if (start.isBefore(now.plusHours(1)))
			return "Reservation must be at least 1 hour from now.";

		if (start.isAfter(now.plusMonths(1)))
			return "Reservation cannot be more than 1 month ahead.";

		int m = start.getMinute();
		if (m != 0 && m != 30)
			return "Time must be in 30-minute intervals.";

		String hoursError = validateOpeningHours(start);
		if (hoursError != null)
			return hoursError;

		return null;
	}

	/**
	 * Validates that a reservation time falls within opening hours.
	 * <p>
	 * Checks date overrides first, then regular opening hours, then defaults.
	 * </p>
	 *
	 * @param start the reservation start time
	 * @return error message if outside hours, null if valid
	 * @throws SQLException if database access fails
	 */
	private String validateOpeningHours(LocalDateTime start) throws SQLException {
		LocalDate date = start.toLocalDate();
		LocalDateTime end = start.plusMinutes(DURATION_MIN);

		DateOverride override = db.getDateOverrideForDate(date);
		if (override != null) {

			String reason = override.getReason();
			String reasonSuffix = (reason != null && !reason.isBlank()) ? " Reason: " + reason + "." : "";

			if (override.isClosed()) {
				return "Restaurant is closed on " + date + "." + reasonSuffix;
			}

			String err = validateTimeRange(start, end, override.getOpenTime(), override.getCloseTime());
			if (err != null) {
				return err + reasonSuffix;
			}

			return null;
		}

		DayOfWeek dayOfWeek = date.getDayOfWeek();
		OpeningHours hours = db.getOpeningHoursForDay(dayOfWeek);
		if (hours != null) {
			if (hours.isClosed()) {
				return "Restaurant is closed on " + dayOfWeek + "s.";
			}
			return validateTimeRange(start, end, hours.getOpenTime(), hours.getCloseTime());
		}

		return validateTimeRange(start, end, DEFAULT_OPEN, DEFAULT_CLOSE);
	}

	/**
	 * Validates that a reservation fits within the given time range.
	 * <p>
	 * Handles both normal hours and cross-midnight hours.
	 * </p>
	 *
	 * @param startDT reservation start date/time
	 * @param endDT   reservation end date/time
	 * @param open    opening time
	 * @param close   closing time
	 * @return error message if outside range, null if valid
	 */
	private String validateTimeRange(LocalDateTime startDT, LocalDateTime endDT, LocalTime open, LocalTime close) {

		if (open == null || close == null) {
			return "Restaurant hours not configured.";
		}

		LocalDate date = startDT.toLocalDate();

		boolean crossesMidnight = close.isBefore(open);

		if (!crossesMidnight) {
			if (!endDT.toLocalDate().equals(date)) {
				return "Restaurant is closed at that time (open " + open + " - " + close + ").";
			}

			LocalTime start = startDT.toLocalTime();
			LocalTime end = endDT.toLocalTime();

			if (start.isBefore(open) || end.isAfter(close)) {
				return "Restaurant is closed at that time (open " + open + " - " + close + ").";
			}
			return null;
		}

		LocalDateTime openDT = LocalDateTime.of(date, open);
		LocalDateTime closeDT = LocalDateTime.of(date.plusDays(1), close);

		if (startDT.isBefore(openDT)) {
			return "Restaurant is closed at that time (open " + open + " - " + close + ").";
		}

		if (endDT.isAfter(closeDT)) {
			return "Restaurant is closed at that time (open " + open + " - " + close + ").";
		}

		return null;
	}

	/**
	 * Checks if a set of reservations can be feasibly assigned to available tables.
	 * <p>
	 * Uses a greedy algorithm: sort reservations by size (largest first), assign
	 * each to the smallest available table that fits.
	 * </p>
	 *
	 * @param tableCaps         list of available table capacities
	 * @param reservationGuests list of guest counts for overlapping reservations
	 * @return true if all reservations can be seated
	 */
	private boolean feasible(List<Integer> tableCaps, List<Integer> reservationGuests) {
		reservationGuests.sort((a, b) -> Integer.compare(b, a));

		java.util.TreeMap<Integer, Integer> capCount = new java.util.TreeMap<>();
		for (int c : tableCaps)
			capCount.merge(c, 1, Integer::sum);

		for (int g : reservationGuests) {
			Integer cap = capCount.ceilingKey(g);
			if (cap == null)
				return false;

			int cnt = capCount.get(cap);
			if (cnt == 1)
				capCount.remove(cap);
			else
				capCount.put(cap, cnt - 1);
		}
		return true;
	}

	/**
	 * Suggests up to 3 alternative times near the requested time.
	 *
	 * @param start     the originally requested time
	 * @param newGuests number of guests
	 * @return list of available alternative times
	 * @throws SQLException if database access fails
	 */
	private List<LocalDateTime> suggestTimes(LocalDateTime start, int newGuests) throws SQLException {
		List<LocalDateTime> suggestions = new ArrayList<>();

		for (int i = 1; i <= 12; i++) {
			LocalDateTime plus = start.plusMinutes(30L * i);
			LocalDateTime minus = start.minusMinutes(30L * i);

			if (isCandidateOk(plus, newGuests) && !suggestions.contains(plus)) {
				suggestions.add(plus);
				if (suggestions.size() >= 3)
					break;
			}

			if (isCandidateOk(minus, newGuests) && !suggestions.contains(minus)) {
				suggestions.add(minus);
				if (suggestions.size() >= 3)
					break;
			}
		}

		suggestions.sort(LocalDateTime::compareTo);
		return suggestions;
	}

	/**
	 * Checks if a candidate time is valid for a reservation.
	 *
	 * @param cand      candidate date/time
	 * @param newGuests number of guests
	 * @return true if the time is valid and has availability
	 * @throws SQLException if database access fails
	 */
	private boolean isCandidateOk(LocalDateTime cand, int newGuests) throws SQLException {
		if (cand.getSecond() != 0 || cand.getNano() != 0)
			return false;
		int m = cand.getMinute();
		if (m != 0 && m != 30)
			return false;

		if (validateOpeningHours(cand) != null)
			return false;

		LocalDateTime now = LocalDateTime.now();
		if (cand.isBefore(now.plusHours(1)))
			return false;
		if (cand.isAfter(now.plusMonths(1)))
			return false;

		return isAvailableAt(cand, newGuests);
	}

	/**
	 * Loads reservations that can be cancelled for a customer.
	 *
	 * @param customerId the customer ID
	 * @return list of cancellable reservations
	 * @throws SQLException if database access fails
	 */
	public List<Reservation> loadReservationsForCancellation(int customerId) throws SQLException {
		return db.getCancellableReservationsByCustomerId(customerId);
	}

	/**
	 * Finds a cancellable reservation by confirmation code.
	 *
	 * @param confirmationCode the confirmation code
	 * @return the reservation if found and cancellable, null otherwise
	 * @throws SQLException if database access fails
	 */
	public Reservation getReservationForCancellationByCode(int confirmationCode) throws SQLException {
		return db.getCancellableReservationByCode(confirmationCode);
	}

	/**
	 * Loads reservations ready for table receiving (check-in) for a customer.
	 *
	 * @param customerId the customer ID
	 * @return list of receivable reservations
	 * @throws SQLException if database access fails
	 */
	public List<Reservation> loadReservationsForReceiving(int customerId) throws SQLException {
		return db.getReceivableReservationsByCustomerId(customerId);
	}

	/**
	 * Finds a receivable reservation by confirmation code.
	 *
	 * @param confirmationCode the confirmation code
	 * @return the reservation if found and receivable, null otherwise
	 * @throws SQLException if database access fails
	 */
	public Reservation getReservationForReceivingByCode(int confirmationCode) throws SQLException {
		return db.getReceivableReservationByCode(confirmationCode);
	}

	/**
	 * Loads reservations ready for checkout (payment) for a customer.
	 *
	 * @param customerId the customer ID
	 * @return list of payable reservations
	 * @throws SQLException if database access fails
	 */
	public List<Reservation> loadReservationsForCheckout(int customerId) throws SQLException {
		return db.getPayableReservationsByCustomerId(customerId);
	}

	/**
	 * Finds a payable reservation by confirmation code.
	 *
	 * @param confirmationCode the confirmation code
	 * @return the reservation if found and payable, null otherwise
	 * @throws SQLException if database access fails
	 */
	public Reservation getReservationForCheckoutByCode(int confirmationCode) throws SQLException {
		return db.getPayableReservationByCode(confirmationCode);
	}

	/**
	 * Gets or creates a bill for paying.
	 * <p>
	 * If a bill already exists, returns it. Otherwise computes and creates a new
	 * bill.
	 * </p>
	 *
	 * @param reservationId the reservation ID
	 * @return the bill entity, or null if reservation not found
	 * @throws SQLException if database access fails
	 */
	public Bill getOrCreateBillForPaying(int reservationId) throws SQLException {
		Bill bill = db.findBillByReservationId(reservationId);
		if (bill == null) {
			Reservation r = db.findReservationById(reservationId);
			if (r == null)
				return null;
			bill = computeBill(r);
		}
		return bill;
	}

	/**
	 * Cancels a reservation.
	 * <p>
	 * Only ACTIVE, NOTIFIED, or WAITING reservations can be cancelled.
	 * </p>
	 *
	 * @param reservationId the reservation ID to cancel
	 * @return result indicating success or failure with previous status
	 * @throws SQLException if database access fails
	 */
	public CancelReservationResult cancelReservation(int reservationId) throws SQLException {

		String statusStr = db.getReservationStatus(reservationId);
		if (statusStr == null)
			return CancelReservationResult.fail("Reservation not found.");

		ReservationStatus status = ReservationStatus.valueOf(statusStr);

		if (status == ReservationStatus.CANCELED)
			return CancelReservationResult.fail("Reservation already canceled.");

		if (status == ReservationStatus.COMPLETED)
			return CancelReservationResult.fail("Cannot cancel a completed reservation.");

		if (status == ReservationStatus.IN_PROGRESS)
			return CancelReservationResult.fail("Cannot cancel in progress reservation.");

		boolean ok = db.updateReservationStatus(reservationId, ReservationStatus.CANCELED.name());
		return ok ? CancelReservationResult.ok("Reservation canceled.", status)
				: CancelReservationResult.fail("Cancel failed.");
	}

	/**
	 * Cancels a guest reservation by confirmation code.
	 *
	 * @param confirmationCode the confirmation code
	 * @return result indicating success or failure
	 * @throws SQLException if database access fails
	 */
	public CancelReservationResult cancelGuestReservation(int confirmationCode) throws SQLException {
		Reservation reservation = db.findReservationByConfirmationCode(confirmationCode);

		if (reservation == null)
			return CancelReservationResult.fail("Invalid confirmation code.");

		int reservationId = reservation.getReservationId();
		String statusStr = db.getReservationStatus(reservationId);
		ReservationStatus status = ReservationStatus.valueOf(statusStr);

		if (status != ReservationStatus.WAITING && status != ReservationStatus.NOTIFIED
				&& status != ReservationStatus.ACTIVE) {
			return CancelReservationResult.fail("Reservation cannot be canceled.");
		}

		boolean ok = db.updateReservationStatus(reservationId, ReservationStatus.CANCELED.name());
		return ok ? CancelReservationResult.ok("Reservation canceled.", status)
				: CancelReservationResult.fail("Cancel failed.");
	}

	/**
	 * Gets or creates a guest customer ID.
	 * <p>
	 * Looks up existing customer by phone or email. If not found, creates new guest
	 * record.
	 * </p>
	 *
	 * @param fullName guest's full name
	 * @param phone    guest's phone number
	 * @param email    guest's email address
	 * @return the customer ID
	 * @throws SQLException             if database access fails
	 * @throws IllegalArgumentException if neither phone nor email is provided
	 */
	private int getOrCreateGuestCustomerId(String fullName, String phone, String email) throws SQLException {
		boolean phoneEmpty = (phone == null || phone.isBlank());
		boolean emailEmpty = (email == null || email.isBlank());

		if (phoneEmpty && emailEmpty) {
			throw new IllegalArgumentException("Phone or Email is required.");
		}

		String normPhone = phoneEmpty ? null : phone.trim();
		String normEmail = emailEmpty ? null : email.trim().toLowerCase();

		Integer existingId = db.findCustomerIdByPhoneOrEmail(normPhone, normEmail);
		if (existingId != null)
			return existingId;

		String safeName = (fullName == null || fullName.isBlank()) ? "Guest" : fullName.trim();
		return db.createGuestCustomer(safeName, normPhone, normEmail);
	}

	/**
	 * Adds a subscriber to the waitlist.
	 * <p>
	 * If a table is immediately available, creates a NOTIFIED reservation instead
	 * (GO_NOW flow). Otherwise creates a WAITING entry.
	 * </p>
	 *
	 * @param subscriberId   the subscriber's customer ID
	 * @param numberOfGuests number of guests
	 * @return result with reservation ID and confirmation code
	 * @throws SQLException if database access fails
	 */
	public CreateReservationResult joinWaitlist(Integer subscriberId, int numberOfGuests) throws SQLException {
		if (subscriberId == null || subscriberId <= 0)
			return CreateReservationResult.fail("Invalid customer id.");
		if (numberOfGuests <= 0)
			return CreateReservationResult.fail("Invalid number of guests.");

		if (isAvailableAt(LocalDateTime.now(), numberOfGuests)) {
			Integer tableId = db.findAvailableTableId(LocalDateTime.now(), DURATION_MIN, numberOfGuests);

			if (tableId != null) {
				InsertReservationResult ins = db.insertNotifiedNow(subscriberId, numberOfGuests, tableId);
				if (ins != null) {
					return new CreateReservationResult(true, "RECEIVE_TABLE_NOW", ins.getReservationId(),
							ins.getConfirmationCode(), List.of());
				}
			}
		}

		InsertReservationResult ins = db.insertWaitlist(subscriberId, numberOfGuests);
		if (ins == null)
			return CreateReservationResult.fail("Insert waitlist failed.");

		return new CreateReservationResult(true, "WAITLIST_JOINED", ins.getReservationId(), ins.getConfirmationCode(),
				List.of());
	}

	/**
	 * Adds a guest to the waitlist.
	 *
	 * @param numberOfGuests number of guests
	 * @param fullName       guest's full name
	 * @param phone          guest's phone number
	 * @param email          guest's email address
	 * @return result with reservation ID and confirmation code
	 * @throws SQLException if database access fails
	 */
	public CreateReservationResult joinWaitlistAsGuest(int numberOfGuests, String fullName, String phone, String email)
			throws SQLException {
		if (numberOfGuests <= 0)
			return CreateReservationResult.fail("Invalid number of guests.");

		int customerId;
		try {
			customerId = getOrCreateGuestCustomerId(fullName, phone, email);
		} catch (IllegalArgumentException e) {
			return CreateReservationResult.fail(e.getMessage());
		}

		return joinWaitlist(customerId, numberOfGuests);
	}

	/**
	 * Checks if there's availability at a given time for the specified guest count.
	 * <p>
	 * Uses the feasibility algorithm to determine if all overlapping reservations
	 * (including the new one) can be assigned to available tables.
	 * </p>
	 *
	 * @param start     the desired start time
	 * @param newGuests number of guests
	 * @return true if there's availability
	 * @throws SQLException if database access fails
	 */
	private boolean isAvailableAt(LocalDateTime start, int newGuests) throws SQLException {
		Map<Integer, Integer> tableCaps = db.getTableIdToCapacity();

		Set<Integer> busy = new HashSet<>(db.getOverlappingPinnedTableIds(start, DURATION_MIN));

		List<Integer> availableCaps = new ArrayList<>();
		for (Map.Entry<Integer, Integer> e : tableCaps.entrySet()) {
			if (!busy.contains(e.getKey())) {
				availableCaps.add(e.getValue());
			}
		}

		List<Integer> overlappingActive = db.getOverlappingActiveGuests(start, DURATION_MIN);
		overlappingActive.add(newGuests);

		return feasible(availableCaps, overlappingActive);
	}

	/**
	 * Notifies the next eligible customer from the waitlist when capacity is freed.
	 *
	 * @param freedCapacity the capacity that was freed (seats)
	 * @return reservation ID of the notified customer, or null if none eligible
	 * @throws SQLException if database access fails
	 */
	public Integer notifyNextFromWaitlist(int freedCapacity) throws SQLException {

		List<WaitingCandidate> candidates = db.getWaitingCandidates(freedCapacity);
		if (candidates.isEmpty())
			return null;

		LocalDateTime now = LocalDateTime.now();

		for (WaitingCandidate c : candidates) {

			if (isAvailableAt(now, c.guests)) {
				Integer tableId = db.findAvailableTableId(now, DURATION_MIN, c.guests);
				if (tableId == null)
					continue;
				boolean ok = db.notifyWaitlistReservation(c.reservationId, tableId);
				if (!ok)
					continue;

				return c.reservationId;
			}
		}

		return null;
	}

	/**
	 * Notifies the next eligible customer from the waitlist (any capacity).
	 *
	 * @return reservation ID of the notified customer, or null if none eligible
	 * @throws SQLException if database access fails
	 */
	public Integer notifyNextFromWaitlist() throws SQLException {
		List<WaitingCandidate> candidates = db.getWaitingCandidates();
		if (candidates.isEmpty())
			return null;

		LocalDateTime now = LocalDateTime.now();

		for (WaitingCandidate c : candidates) {
			if (isAvailableAt(now, c.guests)) {
				Integer tableId = db.findAvailableTableId(now, DURATION_MIN, c.guests);
				if (tableId == null)
					continue;
				boolean ok = db.notifyWaitlistReservation(c.reservationId, tableId);
				if (!ok)
					continue;
				return c.reservationId;
			}
		}
		return null;
	}

	/**
	 * Processes table receiving (check-in) for a reservation.
	 * <p>
	 * For NOTIFIED reservations, keeps the pre-assigned table. For ACTIVE
	 * reservations, assigns an available table now. Updates status to IN_PROGRESS
	 * and records check-in time.
	 * </p>
	 *
	 * @param reservationId the reservation ID
	 * @return result with assigned table number, or failure
	 * @throws SQLException if database access fails
	 */
	public ReceiveTableResult receiveTable(int reservationId) throws SQLException {

		if (reservationId <= 0)
			return ReceiveTableResult.fail("Invalid reservation id.");

		ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);
		if (info == null)
			return ReceiveTableResult.fail("Reservation not found.");

		String statusStr = db.getReservationStatus(reservationId);
		if (statusStr == null)
			return ReceiveTableResult.fail("Reservation not found.");

		ReservationStatus status = ReservationStatus.valueOf(statusStr);

		if (status != ReservationStatus.ACTIVE && status != ReservationStatus.NOTIFIED) {
			return ReceiveTableResult.fail("Reservation status does not allow table receiving.");
		}

		Integer tableId;

		if (status == ReservationStatus.NOTIFIED) {
			tableId = db.getTableIdByReservationId(reservationId);
			if (tableId == null)
				return ReceiveTableResult.fail("No table assigned for this notified reservation.");
		} else {
			tableId = db.findAvailableTableId(LocalDateTime.now(), DURATION_MIN, info.guests);
			if (tableId == null)
				return ReceiveTableResult.fail("No available table right now.");

			if (!db.assignTableNow(reservationId, tableId))
				return ReceiveTableResult.fail("Failed to assign table!");
		}

		if (!db.markSeatedNow(reservationId))
			return ReceiveTableResult.fail("Failed to mark seated");

		return ReceiveTableResult.ok(tableId);
	}

	/**
	 * Generates a random amount between 80 and 150 for billing simulation.
	 *
	 * @return random amount
	 */
	private int rand80to150() {
		return 80 + (int) (Math.random() * 71);
	}

	/**
	 * Computes the total bill amount for a party.
	 *
	 * @param guests number of guests
	 * @return total amount before discounts
	 */
	private double computeAmount(int guests) {
		double sum = 0;
		for (int i = 0; i < guests; i++)
			sum += rand80to150();
		return sum;
	}

	/**
	 * Computes and creates a bill for a reservation.
	 * <p>
	 * Subscribers receive a 10% discount on the final amount.
	 * </p>
	 *
	 * @param reservation the reservation to bill
	 * @return the created bill entity
	 * @throws SQLException if database access fails
	 */
	public Bill computeBill(Reservation reservation) throws SQLException {
		double before = computeAmount(reservation.getNumberOfGuests());
		boolean sub = db.isCustomerSubscribed(reservation.getCustomerId());
		double finalAmount = sub ? before * 0.9 : before;

		return db.insertBill(reservation.getReservationId(), before, finalAmount);
	}

	/**
	 * Processes bill payment.
	 * <p>
	 * Marks the bill as paid, updates reservation to COMPLETED status, and returns
	 * the freed table capacity.
	 * </p>
	 *
	 * @param billId the bill ID to pay
	 * @return result with final amount and freed capacity, or failure
	 * @throws SQLException if database access fails
	 */
	public PayBillResult payBillbyId(int billId) throws SQLException {

		if (billId <= 0) {
			return PayBillResult.fail("Invalid bill id.");
		}

		Bill bill = db.findBillById(billId);
		if (bill == null) {
			return PayBillResult.fail("Bill not found.");
		}

		if (bill.isPaid()) {
			return PayBillResult.fail("Bill already paid.");
		}

		Reservation r = db.findReservationById(bill.getReservationId());
		if (r == null) {
			return PayBillResult.fail("Reservation not found for this bill.");
		}

		if (r.getStatus() != ReservationStatus.IN_PROGRESS) {
			return PayBillResult.fail("Reservation is not in progress.");
		}

		boolean ok = db.markBillPaidById(billId);
		if (!ok) {
			return PayBillResult.fail("Payment failed.");
		}

		db.updateReservationStatus(r.getReservationId(), ReservationStatus.COMPLETED.name());

		return PayBillResult.ok(r.getReservationId(), bill.getFinalAmount(), getFreedCapacity(r));
	}

	/**
	 * Gets the freed table capacity after a reservation completes.
	 *
	 * @param r the completed reservation
	 * @return table capacity, or 0 if no table assigned
	 * @throws SQLException if database access fails
	 */
	private int getFreedCapacity(Reservation r) throws SQLException {
		Integer tableId = r.getTableId();
		if (tableId != null) {
			Integer cap = db.getTableCapacityById(tableId);
			if (cap != null && cap > 0)
				return cap;
		}
		return 0;
	}

	// ======================== RESEND CONFIRMATION CODE ========================

	/**
	 * Finds active reservations by customer phone or email.
	 *
	 * @param phone customer's phone number
	 * @param email customer's email address
	 * @return list of matching reservations
	 * @throws SQLException if database access fails
	 */
	public List<Reservation> findReservationsByPhoneOrEmail(String phone, String email) throws SQLException {
		if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
			return new ArrayList<>();
		}
		return db.findReservationsByPhoneOrEmail(phone, email);
	}

	// ======================== NO-SHOW & REMINDER OPERATIONS
	// ========================

	/**
	 * Gets reservation IDs that are no-shows (past check-in window, not arrived).
	 *
	 * @return list of no-show reservation IDs
	 * @throws SQLException if database access fails
	 */
	public List<Integer> getNoShowReservationIds() throws SQLException {
		return db.getNoShowReservationIds();
	}

	/**
	 * Cancels a no-show reservation.
	 *
	 * @param reservationId the reservation ID to cancel
	 * @return true if cancelled successfully
	 * @throws SQLException if database access fails
	 */
	public boolean cancelNoShowReservation(int reservationId) throws SQLException {
		return db.updateReservationStatus(reservationId, ReservationStatus.CANCELED.name());
	}

	/**
	 * Gets reservation IDs due for reminder notification (2 hours before).
	 *
	 * @return list of reservation IDs needing reminders
	 * @throws SQLException if database access fails
	 */
	public List<Integer> getReservationsForReminder() throws SQLException {
		return db.getReservationsForReminder();
	}

	/**
	 * Marks a reservation as having had its reminder sent.
	 *
	 * @param reservationId the reservation ID
	 * @throws SQLException if database access fails
	 */
	public void markReminderSent(int reservationId) throws SQLException {
		db.markReminderSent(reservationId);
	}

	/**
	 * Gets reservations ready for automatic billing.
	 *
	 * @return list of reservations needing bills
	 * @throws SQLException if database access fails
	 */
	public List<Reservation> getReservationsForBilling() throws SQLException {
		return db.getReservationsForBilling();
	}

	// ======================== MANAGER REPORTS ========================

	/**
	 * Generates a time-based report for a specific month.
	 *
	 * @param year  the report year
	 * @param month the report month (1-12)
	 * @return list of time report entries
	 * @throws SQLException if database access fails
	 */
	public List<TimeReportEntry> getTimeReport(int year, int month) throws SQLException {
		return db.getTimeReportForMonth(year, month);
	}

	/**
	 * Generates a subscriber-based report for a specific month.
	 *
	 * @param year  the report year
	 * @param month the report month (1-12)
	 * @return list of subscriber report entries
	 * @throws SQLException if database access fails
	 */
	public List<SubscriberReportEntry> getSubscriberReport(int year, int month) throws SQLException {
		return db.getSubscriberReportForMonth(year, month);
	}

	// ======================== CONFLICT HANDLING (HOURS/TABLES CHANGES)
	// ========================

	/**
	 * Cancels reservations that fall outside new opening hours for a day of week.
	 *
	 * @param day       the day of week
	 * @param openTime  new opening time
	 * @param closeTime new closing time
	 * @param closed    whether the day is now closed
	 * @return list of cancelled reservation IDs
	 * @throws SQLException if database access fails
	 */
	public List<Integer> cancelReservationsOutsideHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime,
			boolean closed) throws SQLException {
		List<Integer> conflicting = db.getActiveReservationsOutsideHours(day, openTime, closeTime, closed);
		List<Integer> cancelled = new ArrayList<>();
		for (Integer resId : conflicting) {
			if (db.updateReservationStatus(resId, ReservationStatus.CANCELED.name())) {
				cancelled.add(resId);
			}
		}
		return cancelled;
	}

	/**
	 * Cancels reservations that fall outside new hours for a specific date.
	 *
	 * @param date      the date
	 * @param openTime  new opening time
	 * @param closeTime new closing time
	 * @param closed    whether the date is closed
	 * @return list of cancelled reservation IDs
	 * @throws SQLException if database access fails
	 */
	public List<Integer> cancelReservationsOutsideHoursOnDate(LocalDate date, LocalTime openTime, LocalTime closeTime,
			boolean closed) throws SQLException {
		List<Integer> conflicting = db.getActiveReservationsOutsideHoursOnDate(date, openTime, closeTime, closed);
		List<Integer> cancelled = new ArrayList<>();
		for (Integer resId : conflicting) {
			if (db.updateReservationStatus(resId, ReservationStatus.CANCELED.name())) {
				cancelled.add(resId);
			}
		}
		return cancelled;
	}

	/**
	 * Checks if a table has any active reservations.
	 *
	 * @param tableNumber the table number
	 * @return true if there are active reservations on this table
	 * @throws SQLException if database access fails
	 */
	public boolean hasActiveReservationsOnTable(int tableNumber) throws SQLException {
		return db.hasActiveReservationsOnTable(tableNumber);
	}

	/**
	 * Re-validates future reservations after capacity changes.
	 * <p>
	 * Moves reservations to WAITING status if they can no longer be accommodated.
	 * </p>
	 *
	 * @param from start of the time range to check
	 * @param to   end of the time range to check
	 * @return list of reservation IDs moved to waiting
	 * @throws SQLException if database access fails
	 */
	public List<Integer> revalidateFutureActiveReservations(LocalDateTime from, LocalDateTime to) throws SQLException {
		List<Reservation> future = db.getFutureActiveReservations(from, to);
		List<Integer> moved = new ArrayList<>();

		for (Reservation r : future) {
			if (!isAvailableAt(r.getReservationDateTime(), r.getNumberOfGuests())) {
				if (db.moveReservationToWaiting(r.getReservationId())) {
					moved.add(r.getReservationId());
				}
			}
		}
		return moved;
	}

	/**
	 * Gets the seating capacity of a table.
	 *
	 * @param tableNumber the table number
	 * @return number of seats
	 * @throws SQLException if database access fails
	 */
	public int getTableSeats(int tableNumber) throws SQLException {
		return db.getTableCapacity(tableNumber);
	}

}
