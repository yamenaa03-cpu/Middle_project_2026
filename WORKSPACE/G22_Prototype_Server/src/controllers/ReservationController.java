package controllers;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import common.dto.CreateReservationResult;
import common.dto.InsertReservationResult;
import common.entity.Reservation;
import dbController.DBController;

public class ReservationController {

    private final DBController db;
    
    private static final LocalTime OPEN = LocalTime.of(10, 0);
    private static final LocalTime CLOSE = LocalTime.of(2, 0); // next day
    private static final int DURATION_MIN = 120;

    public ReservationController(DBController db) {
        this.db = db;
    }

    public List<Reservation> getAllReservations() throws SQLException {
        return db.getAllReservations();
    }

    public boolean updateReservation(int reservationId, LocalDateTime newDateTime, int newGuests)
            throws SQLException {

    	if (reservationId <= 0) return false;
        if (newDateTime == null) return false;
        if (newGuests <= 0) return false;

        return db.updateReservationFields(reservationId, newDateTime, newGuests);
    }
    
    public CreateReservationResult createReservation(int customerId, LocalDateTime start, int guests) throws SQLException {

        if (customerId <= 0) return CreateReservationResult.fail("Not logged in.");

        String err = validateRequest(start, guests); // +1h, +1month, 30-min, 10:00->02:00
        if (err != null) return CreateReservationResult.fail(err);

        List<Integer> caps = db.getTableCapacities();
        List<Integer> overlapping = db.getOverlappingGuests(start, DURATION_MIN);
        overlapping.add(guests);

        if (!feasible(caps, overlapping)) {
            List<LocalDateTime> sug = suggestTimesWithGuests(start, guests, caps);
            return CreateReservationResult.noSpace("No space at requested time.", sug);
        }

        InsertReservationResult r = db.insertReservation(customerId, start, guests);
        if (r == null) return CreateReservationResult.fail("Insert failed.");

        return CreateReservationResult.ok(r.getReservationId(), r.getConfirmationCode());
    }

    
    private String validateRequest(LocalDateTime start, int guests) {
        if (start == null) return "Missing date/time.";
        if (guests <= 0) return "Guests must be positive.";

        LocalDateTime now = LocalDateTime.now();

        // at least 1 hour from now
        if (start.isBefore(now.plusHours(1))) return "Reservation must be at least 1 hour from now.";

        // no more than 1 month
        if (start.isAfter(now.plusMonths(1))) return "Reservation cannot be more than 1 month ahead.";

        // 30-minute slots
        if (start.getSecond() != 0 || start.getNano() != 0) return "Invalid seconds.";
        int m = start.getMinute();
        if (m != 0 && m != 30) return "Time must be in 30-minute intervals.";

        // opening hours across midnight
        if (!isWithinOpeningHours(start)) return "Restaurant is closed at that time.";

        return null; // OK
    }

    
    private boolean isWithinOpeningHours(LocalDateTime start) {
        LocalDateTime end = start.plusMinutes(DURATION_MIN);

        LocalTime s = start.toLocalTime();
        LocalTime e = end.toLocalTime();

        // Case 1: between 10:00 and midnight
        if (!s.isBefore(OPEN)) {
            return true; // start OK, end will still be <= 02:00 next day
        }

        // Case 2: after midnight until 02:00
        if (s.isBefore(CLOSE)) {
            return e.isBefore(CLOSE) || e.equals(CLOSE);
        }

        return false;
    }
    
    private boolean feasible(List<Integer> tableCaps, List<Integer> reservationGuests) {
        // largest reservations first
        reservationGuests.sort((a,b) -> Integer.compare(b,a));

        // multiset (capacity -> count)
        java.util.TreeMap<Integer,Integer> capCount = new java.util.TreeMap<>();
        for (int c : tableCaps) capCount.merge(c, 1, Integer::sum);

        for (int g : reservationGuests) {
            Integer cap = capCount.ceilingKey(g); // smallest table that fits
            if (cap == null) return false;

            int cnt = capCount.get(cap);
            if (cnt == 1) capCount.remove(cap);
            else capCount.put(cap, cnt - 1);
        }
        return true;
    }

    private List<LocalDateTime> suggestTimesWithGuests(LocalDateTime start, int newGuests, List<Integer> caps) throws SQLException {
        List<LocalDateTime> suggestions = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            LocalDateTime plus = start.plusMinutes(30L * i);
            LocalDateTime minus = start.minusMinutes(30L * i);

            if (isCandidateOk(plus, newGuests, caps) && !suggestions.contains(plus)) {
                suggestions.add(plus);
                if (suggestions.size() >= 3) break;
            }

            if (isCandidateOk(minus, newGuests, caps) && !suggestions.contains(minus)) {
                suggestions.add(minus);
                if (suggestions.size() >= 3) break;
            }
        }

        suggestions.sort(LocalDateTime::compareTo);
        return suggestions;
    }

    private boolean isCandidateOk(LocalDateTime cand, int newGuests, List<Integer> caps) throws SQLException {
        // keep the same slot rules
        if (cand.getSecond() != 0 || cand.getNano() != 0) return false;
        int m = cand.getMinute();
        if (m != 0 && m != 30) return false;

        // opening hours across midnight (the important part)
        if (!isWithinOpeningHours(cand)) return false;

        // keep the time window rule? (usually YES)
        LocalDateTime now = LocalDateTime.now();
        if (cand.isBefore(now.plusHours(1))) return false;
        if (cand.isAfter(now.plusMonths(1))) return false;

        List<Integer> overlapping = db.getOverlappingGuests(cand, DURATION_MIN);
        overlapping.add(newGuests);
        return feasible(caps, overlapping);
    }

    
}
