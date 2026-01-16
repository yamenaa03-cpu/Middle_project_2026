package controllers;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import common.enums.ReservationStatus;
import dbController.DBController;

public class ReservationController {

        private final DBController db;

        private static final int DURATION_MIN = 120;

        // Default hours if database has no data
        private static final LocalTime DEFAULT_OPEN = LocalTime.of(10, 0);
        private static final LocalTime DEFAULT_CLOSE = LocalTime.of(2, 0); // next day

        public ReservationController(DBController db) {
                this.db = db;
        }

        public List<Reservation> getAllActiveReservations() throws SQLException {
                return db.getActiveReservations();
        }

        public List<Reservation> getWaitlistReservations() throws SQLException {
                return db.getWaitlistReservations();
        }

        public List<Reservation> getSubscriberHistory(int customerId) throws SQLException {
                if (customerId <= 0)
                        return new ArrayList<>();
                return db.getReservationHistoryByCustomerId(customerId);
        }

        public UpdateReservationResult updateReservation(int reservationId, LocalDateTime newDateTime, int newGuests) throws SQLException {

                if (reservationId <= 0)
                        return UpdateReservationResult.fail("Invalid reservation ID.");
                if (newDateTime == null)
                        return UpdateReservationResult.fail("Date and time is required.");
                if (newGuests <= 0)
                        return UpdateReservationResult.fail("Number of guests must be positive.");

                // Validate opening hours for new date/time
                String hoursError = validateOpeningHours(newDateTime);
                if (hoursError != null)
                        return UpdateReservationResult.fail(hoursError);

                // Validate time constraints (30-min slots)
                int m = newDateTime.getMinute();
                if (m != 0 && m != 30)
                        return UpdateReservationResult.fail("Time must be in 30-minute intervals.");

                boolean updated = db.updateReservationFields(reservationId, newDateTime, newGuests);
                if (!updated)
                        return UpdateReservationResult.fail("Reservation not found or could not be updated.");

                return UpdateReservationResult.ok(reservationId);
        }

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

        public CreateReservationResult createReservation(int customerId, LocalDateTime start, int guests)
                        throws SQLException {

                if (customerId <= 0)
                        return CreateReservationResult.fail("Invalid customer id.");

                String err = validateRequest(start, guests); // +1h, +1month, 30-min, 10:00->02:00
                if (err != null)
                        return CreateReservationResult.fail(err);

                List<Integer> caps = db.getTableCapacities();
                List<Integer> overlapping = db.getOverlappingGuests(start, DURATION_MIN);
                overlapping.add(guests);

                if (!feasible(caps, overlapping)) {
                        List<LocalDateTime> sug = suggestTimesWithGuests(start, guests, caps);
                        return CreateReservationResult.noSpace("No space at requested time.", sug);
                }

                InsertReservationResult r = db.insertReservation(customerId, start, guests);
                if (r == null)
                        return CreateReservationResult.fail("Insert failed.");

                return CreateReservationResult.ok(r.getReservationId(), r.getConfirmationCode());
        }

        private String validateRequest(LocalDateTime start, int guests) throws SQLException {
                if (start == null)
                        return "Missing date/time.";
                if (guests <= 0)
                        return "Guests must be positive.";

                LocalDateTime now = LocalDateTime.now();

                // at least 1 hour from now
                if (start.isBefore(now.plusHours(1)))
                        return "Reservation must be at least 1 hour from now.";

                // no more than 1 month
                if (start.isAfter(now.plusMonths(1)))
                        return "Reservation cannot be more than 1 month ahead.";

                // 30-minute slots
                int m = start.getMinute();
                if (m != 0 && m != 30)
                        return "Time must be in 30-minute intervals.";

                // opening hours from database
                String hoursError = validateOpeningHours(start);
                if (hoursError != null)
                        return hoursError;

                return null; // OK
        }

        private String validateOpeningHours(LocalDateTime start) throws SQLException {
                LocalDate date = start.toLocalDate();
                LocalTime time = start.toLocalTime();
                LocalTime endTime = start.plusMinutes(DURATION_MIN).toLocalTime();

                // First check for date override (holidays, special events)
                DateOverride override = db.getDateOverrideForDate(date);
                if (override != null) {
                        if (override.isClosed()) {
                                String reason = override.getReason();
                                return "Restaurant is closed on " + date + (reason != null ? " (" + reason + ")" : "") + ".";
                        }
                        return validateTimeRange(time, endTime, override.getOpenTime(), override.getCloseTime());
                }

                // Otherwise use regular opening hours for this day of week
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                OpeningHours hours = db.getOpeningHoursForDay(dayOfWeek);
                if (hours != null) {
                        if (hours.isClosed()) {
                                return "Restaurant is closed on " + dayOfWeek + "s.";
                        }
                        return validateTimeRange(time, endTime, hours.getOpenTime(), hours.getCloseTime());
                }

                // Fallback to default hours if no database entry
                return validateTimeRange(time, endTime, DEFAULT_OPEN, DEFAULT_CLOSE);
        }

        private String validateTimeRange(LocalTime start, LocalTime end, LocalTime open, LocalTime close) {
                if (open == null || close == null) {
                        return "Restaurant hours not configured.";
                }

                // Handle hours that cross midnight (e.g., 10:00 - 02:00)
                boolean crossesMidnight = close.isBefore(open);

                if (crossesMidnight) {
                        // Valid times: from open until midnight, OR from midnight until close
                        boolean startOk = !start.isBefore(open) || start.isBefore(close);
                        boolean endOk = !end.isBefore(open) || end.isBefore(close) || end.equals(close);

                        // Handle case where reservation itself crosses midnight
                        if (end.isBefore(start)) {
                                // Reservation crosses midnight - end must be before close
                                endOk = end.isBefore(close) || end.equals(close);
                        }

                        if (!startOk || !endOk) {
                                return "Restaurant is closed at that time (open " + open + " - " + close + ").";
                        }
                } else {
                        // Normal hours (e.g., 10:00 - 22:00)
                        if (start.isBefore(open) || end.isAfter(close)) {
                                return "Restaurant is closed at that time (open " + open + " - " + close + ").";
                        }
                }

                return null; // OK
        }

        private boolean feasible(List<Integer> tableCaps, List<Integer> reservationGuests) {
                // largest reservations first
                reservationGuests.sort((a, b) -> Integer.compare(b, a));

                // multiset (capacity -> count)
                java.util.TreeMap<Integer, Integer> capCount = new java.util.TreeMap<>();
                for (int c : tableCaps)
                        capCount.merge(c, 1, Integer::sum);

                for (int g : reservationGuests) {
                        Integer cap = capCount.ceilingKey(g); // smallest table that fits
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

        private List<LocalDateTime> suggestTimesWithGuests(LocalDateTime start, int newGuests, List<Integer> caps)
                        throws SQLException {
                List<LocalDateTime> suggestions = new ArrayList<>();

                for (int i = 1; i <= 12; i++) {
                        LocalDateTime plus = start.plusMinutes(30L * i);
                        LocalDateTime minus = start.minusMinutes(30L * i);

                        if (isCandidateOk(plus, newGuests, caps) && !suggestions.contains(plus)) {
                                suggestions.add(plus);
                                if (suggestions.size() >= 3)
                                        break;
                        }

                        if (isCandidateOk(minus, newGuests, caps) && !suggestions.contains(minus)) {
                                suggestions.add(minus);
                                if (suggestions.size() >= 3)
                                        break;
                        }
                }

                suggestions.sort(LocalDateTime::compareTo);
                return suggestions;
        }

        private boolean isCandidateOk(LocalDateTime cand, int newGuests, List<Integer> caps) throws SQLException {
                // keep the same slot rules
                if (cand.getSecond() != 0 || cand.getNano() != 0)
                        return false;
                int m = cand.getMinute();
                if (m != 0 && m != 30)
                        return false;

                // opening hours from database
                if (validateOpeningHours(cand) != null)
                        return false;

                // time window rule
                LocalDateTime now = LocalDateTime.now();
                if (cand.isBefore(now.plusHours(1)))
                        return false;
                if (cand.isAfter(now.plusMonths(1)))
                        return false;

                List<Integer> overlapping = db.getOverlappingGuests(cand, DURATION_MIN);
                overlapping.add(newGuests);
                return feasible(caps, overlapping);
        }

        public List<Reservation> loadReservationsForCancellation(int customerId) throws SQLException {
                return db.getCancellableReservationsByCustomerId(customerId);
        }

        public Reservation getReservationForCancellationByCode(int confirmationCode) throws SQLException {
                return db.getCancellableReservationByCode(confirmationCode);
        }

        public List<Reservation> loadReservationsForReceiving(int customerId) throws SQLException {
                return db.getReceivableReservationsByCustomerId(customerId);
        }

        public Reservation getReservationForReceivingByCode(int confirmationCode) throws SQLException {
                return db.getReceivableReservationByCode(confirmationCode);
        }

        public List<Reservation> loadReservationsForCheckout(int customerId) throws SQLException {
                return db.getPayableReservationsByCustomerId(customerId);
        }

        public Reservation getReservationForCheckoutByCode(int confirmationCode) throws SQLException {
                return db.getPayableReservationByCode(confirmationCode);
        }

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

        public CreateReservationResult joinWaitlist(Integer subscriberId, int numberOfGuests) throws SQLException {
                if (subscriberId == null || subscriberId <= 0)
                        return CreateReservationResult.fail("Invalid customer id.");
                if (numberOfGuests <= 0)
                        return CreateReservationResult.fail("Invalid number of guests.");

                InsertReservationResult ins;

                if (isAvailableNow(numberOfGuests)) {
                        ins = db.insertNotifiedNow(subscriberId, numberOfGuests);
                        if (ins == null)
                                return CreateReservationResult.fail("Insert failed.");
                        return new CreateReservationResult(true, "GO_NOW", ins.getReservationId(), ins.getConfirmationCode(),
                                        List.of());
                }

                ins = db.insertWaitlist(subscriberId, numberOfGuests);
                if (ins == null)
                        return CreateReservationResult.fail("Insert waitlist failed.");

                return new CreateReservationResult(true, "WAITLIST_JOINED", ins.getReservationId(), ins.getConfirmationCode(),
                                List.of());
        }

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

        private boolean isAvailableNow(int guests) throws SQLException {
                LocalDateTime now = LocalDateTime.now();

                List<Integer> caps = db.getTableCapacities();
                List<Integer> overlapping = db.getOverlappingGuests(now, DURATION_MIN);
                overlapping.add(guests);

                return feasible(caps, overlapping);
        }

        public Integer notifyNextFromWaitlist(int freedCapacity) throws SQLException {
                // 1) Get all WAITING candidates that fit this table
                List<WaitingCandidate> candidates = db.getWaitingCandidates(freedCapacity);
                if (candidates.isEmpty())
                        return null;

                // 2) For each candidate FIFO: check if starting NOW for 2 hours is feasible
                LocalDateTime now = LocalDateTime.now();
                List<Integer> caps = db.getTableCapacities();
                List<Integer> overlapping = db.getOverlappingGuests(now, DURATION_MIN); // 2h window if DURATION_MIN=120

                for (WaitingCandidate c : candidates) {
                        List<Integer> test = new ArrayList<>(overlapping);
                        test.add(c.guests);

                        if (feasible(caps, test)) {
                                boolean ok = db.notifyWaitlistReservation(c.reservationId);
                                if (!ok)
                                        continue; // already notified/canceled
                                return c.reservationId;
                        }
                }
                return null;
        }

        public Integer notifyNextFromWaitlist() throws SQLException {
                // 1) Get all WAITING candidates
                List<WaitingCandidate> candidates = db.getWaitingCandidates();
                if (candidates.isEmpty())
                        return null;

                // 2) For each candidate FIFO: check if starting NOW for 2 hours is feasible
                LocalDateTime now = LocalDateTime.now();
                List<Integer> caps = db.getTableCapacities();
                List<Integer> overlapping = db.getOverlappingGuests(now, DURATION_MIN); // 2h window if DURATION_MIN=120

                for (WaitingCandidate c : candidates) {
                        List<Integer> test = new ArrayList<>(overlapping);
                        test.add(c.guests);

                        if (feasible(caps, test)) {
                                boolean ok = db.notifyWaitlistReservation(c.reservationId);
                                if (!ok)
                                        continue; // already notified/canceled
                                return c.reservationId;
                        }
                }
                return null;
        }

        public ReceiveTableResult receiveTable(int reservationId) throws SQLException {

                if (reservationId <= 0)
                        return ReceiveTableResult.fail("Invalid reservation id.");

                ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);
                if (info == null)
                        return ReceiveTableResult.fail("Reservation not found.");

                // status check
                String statusStr = db.getReservationStatus(reservationId);
                if (statusStr == null)
                        return ReceiveTableResult.fail("Reservation not found.");

                ReservationStatus status = ReservationStatus.valueOf(statusStr);

                if (status != ReservationStatus.ACTIVE && status != ReservationStatus.NOTIFIED) {
                        return ReceiveTableResult.fail("Reservation status does not allow table receiving.");
                }

                Integer tableId = db.assignTableNow(reservationId, info.guests, DURATION_MIN);
                if (tableId == null) {
                        return ReceiveTableResult.fail("No available table right now.");
                }

                if (!db.markSeatedNow(reservationId))
                        return ReceiveTableResult.fail("Failed to mark seated");

                return ReceiveTableResult.ok(tableId);
        }

        private int rand80to150() {
                return 80 + (int) (Math.random() * 71);
        }

        private double computeAmount(int guests) {
                double sum = 0;
                for (int i = 0; i < guests; i++)
                        sum += rand80to150();
                return sum;
        }

        public Bill computeBill(Reservation reservation) throws SQLException {
                double before = computeAmount(reservation.getNumberOfGuests());
                boolean sub = db.isCustomerSubscribed(reservation.getCustomerId());
                double finalAmount = sub ? before * 0.9 : before;

                return db.insertBill(reservation.getReservationId(), before, finalAmount);
        }

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

        public List<Reservation> findReservationsByPhoneOrEmail(String phone, String email) throws SQLException {
                if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
                        return new ArrayList<>();
                }
                return db.findReservationsByPhoneOrEmail(phone, email);
        }

        // ======================== NO-SHOW & REMINDER OPERATIONS ========================

        public List<Integer> getNoShowReservationIds() throws SQLException {
                return db.getNoShowReservationIds();
        }

        public boolean cancelNoShowReservation(int reservationId) throws SQLException {
                return db.updateReservationStatus(reservationId, ReservationStatus.CANCELED.name());
        }

        public List<Integer> getReservationsForReminder() throws SQLException {
                return db.getReservationsForReminder();
        }

        public void markReminderSent(int reservationId) throws SQLException {
                db.markReminderSent(reservationId);
        }

        public List<Reservation> getReservationsForBilling() throws SQLException {
                return db.getReservationsForBilling();
        }

        // ======================== MANAGER REPORTS ========================

        public List<common.dto.Report.TimeReportEntry> getTimeReport(int year, int month) throws SQLException {
                return db.getTimeReportForMonth(year, month);
        }

        public List<common.dto.Report.SubscriberReportEntry> getSubscriberReport(int year, int month) throws SQLException {
                return db.getSubscriberReportForMonth(year, month);
        }

        // ======================== CONFLICT HANDLING (HOURS/TABLES CHANGES) ========================

        public List<Integer> cancelReservationsOutsideHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime, boolean closed) throws SQLException {
                List<Integer> conflicting = db.getActiveReservationsOutsideHours(day, openTime, closeTime, closed);
                List<Integer> cancelled = new ArrayList<>();
                for (Integer resId : conflicting) {
                        if (db.updateReservationStatus(resId, ReservationStatus.CANCELED.name())) {
                                cancelled.add(resId);
                        }
                }
                return cancelled;
        }

        public List<Integer> cancelReservationsOutsideHoursOnDate(LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed) throws SQLException {
                List<Integer> conflicting = db.getActiveReservationsOutsideHoursOnDate(date, openTime, closeTime, closed);
                List<Integer> cancelled = new ArrayList<>();
                for (Integer resId : conflicting) {
                        if (db.updateReservationStatus(resId, ReservationStatus.CANCELED.name())) {
                                cancelled.add(resId);
                        }
                }
                return cancelled;
        }

        public List<Integer> cancelReservationsOnTable(int tableNumber) throws SQLException {
                List<Integer> onTable = db.getActiveReservationsOnTable(tableNumber);
                List<Integer> cancelled = new ArrayList<>();
                for (Integer resId : onTable) {
                        if (db.updateReservationStatus(resId, ReservationStatus.CANCELED.name())) {
                                cancelled.add(resId);
                        }
                }
                return cancelled;
        }

        public List<Integer> cancelReservationsExceedingCapacity(int tableNumber, int newCapacity) throws SQLException {
                List<Integer> exceeding = db.getActiveReservationsExceedingCapacity(tableNumber, newCapacity);
                List<Integer> cancelled = new ArrayList<>();
                for (Integer resId : exceeding) {
                        if (db.updateReservationStatus(resId, ReservationStatus.CANCELED.name())) {
                                cancelled.add(resId);
                        }
                }
                return cancelled;
        }

        public int getTableSeats(int tableNumber) throws SQLException {
                return db.getTableSeats(tableNumber);
        }

}
