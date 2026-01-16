package controllers;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.dto.Reservation.CreateReservationResult;
import common.dto.Reservation.InsertReservationResult;
import common.dto.Reservation.WaitingCandidate;
import common.entity.DateOverride;
import common.entity.OpeningHours;
import dbController.DBController;

/**
 * Standalone Test Runner for Reservation Logic.
 * Uses a StubDBController to simulate Database behavior without MySQL.
 */
public class ReservationLogicTest {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   Running Reservation Logic Verification");
        System.out.println("==========================================");

        int passed = 0;
        int failed = 0;

        try {
            // Test 1: Simple Reservation - Should Success
            if (testSimpleReservation()) {
                System.out.println("✅ Test 1 Passed: Simple Reservation");
                passed++;
            } else {
                System.out.println("❌ Test 1 Failed: Simple Reservation");
                failed++;
            }

            // Test 2: Capacity Limit - Should Fail (Not enough seats)
            if (testCapacityLimit()) {
                System.out.println("✅ Test 2 Passed: Capacity Limit Checks");
                passed++;
            } else {
                System.out.println("❌ Test 2 Failed: Capacity Limit Checks");
                failed++;
            }

            // Test 3: Overlapping Reservations - Should Fail (Time slot taken)
            if (testOverlappingReservations()) {
                System.out.println("✅ Test 3 Passed: Overlap/Time Slot Logic");
                passed++;
            } else {
                System.out.println("❌ Test 3 Failed: Overlap/Time Slot Logic");
                failed++;
            }

            // Test 4: Waitlist Priority - Should Pick First
            if (testWaitlistPriority()) {
                System.out.println("✅ Test 4 Passed: Waitlist Priority");
                passed++;
            } else {
                System.out.println("❌ Test 4 Failed: Waitlist Priority");
                failed++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        System.out.println("==========================================");
        System.out.println("Total: " + (passed + failed) + " | Passed: " + passed + " | Failed: " + failed);
        System.out.println("==========================================");
    }

    private static boolean testSimpleReservation() throws SQLException {
        StubDBController mockDB = new StubDBController();
        ReservationController controller = new ReservationController(mockDB);

        // Setup: 2 tables of 4 seats
        mockDB.setTableCapacities(List.of(4, 4));
        // Open 10:00 - 22:00
        mockDB.setDefaultOpeningHours(LocalTime.of(10, 0), LocalTime.of(22, 0));

        LocalDateTime bookingTime = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0); // Valid
                                                                                                              // future
                                                                                                              // time
        // Just ensure it's within hours (e.g., if now is midnight, plusHours(2) might
        // be closed)
        // Let's fix the time to a known open time tomorrow at 12:00
        bookingTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);

        CreateReservationResult result = controller.createReservation(101, bookingTime, 4);

        if (!result.isSuccess()) {
            System.out.println("   -> Failed message: " + result.getMessage());
            return false;
        }
        return true;
    }

    private static boolean testCapacityLimit() throws SQLException {
        StubDBController mockDB = new StubDBController();
        ReservationController controller = new ReservationController(mockDB);

        // Setup: Only 1 table of 2 seats
        mockDB.setTableCapacities(List.of(2));
        mockDB.setDefaultOpeningHours(LocalTime.of(10, 0), LocalTime.of(22, 0));

        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0)
                .withNano(0);

        // Try to book 4 people
        CreateReservationResult result = controller.createReservation(101, bookingTime, 4);

        if (result.isSuccess()) {
            System.out.println("   -> Error: Should have failed due to capacity (4 guests vs 2 seats).");
            return false; // Should fail
        }
        return true;
    }

    private static boolean testOverlappingReservations() throws SQLException {
        StubDBController mockDB = new StubDBController();
        ReservationController controller = new ReservationController(mockDB);

        // Setup: 1 table of 4 seats
        mockDB.setTableCapacities(List.of(4));
        mockDB.setDefaultOpeningHours(LocalTime.of(10, 0), LocalTime.of(22, 0));

        LocalDateTime time1 = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);

        // 1. Success booking
        CreateReservationResult res1 = controller.createReservation(101, time1, 4);
        if (!res1.isSuccess())
            return false;

        // 2. Try booking same time (should fail, only 1 table)
        CreateReservationResult res2 = controller.createReservation(102, time1, 4);
        if (res2.isSuccess()) {
            System.out.println("   -> Error: Managed to double book the single table!");
            return false;
        }

        // 3. Try booking 1 hour later (should fail, slot is 2 hours)
        LocalDateTime time2 = time1.plusHours(1);
        CreateReservationResult res3 = controller.createReservation(103, time2, 4);
        if (res3.isSuccess()) {
            System.out.println("   -> Error: Booked overlapping slot!");
            return false;
        }

        // 4. Try booking 2 hours later (should success)
        LocalDateTime time3 = time1.plusHours(2);
        CreateReservationResult res4 = controller.createReservation(104, time3, 4);
        if (!res4.isSuccess()) {
            System.out.println("   -> Error: Should be free after 2 hours. Msg: " + res4.getMessage());
            return false;
        }

        return true;
    }

    private static boolean testWaitlistPriority() throws SQLException {
        StubDBController mockDB = new StubDBController();
        ReservationController controller = new ReservationController(mockDB);

        // Setup: 1 table of 4 seats
        mockDB.setTableCapacities(List.of(4));
        mockDB.setDefaultOpeningHours(LocalTime.of(10, 0), LocalTime.of(22, 0));

        // Add 2 candidates to waiting list
        // Candidate 1: ID 100, 4 guests (fits) - Joined earlier
        // Candidate 2: ID 200, 4 guests (fits) - Joined later

        WaitingCandidate c1 = new WaitingCandidate(1, 100, 4);
        WaitingCandidate c2 = new WaitingCandidate(2, 200, 4);

        mockDB.addWaitlistCandidate(c1);
        mockDB.addWaitlistCandidate(c2);

        // We need to simulate that the table is currently FULL so looking for overlap
        // returns guests
        // But wait, notifyNextFromWaitlist checks if starting NOW is feasible.
        // So we need to ensure "now" is feasible (empty).
        // Since getOverlappingGuests returns empty list by default in stub unless we
        // add reservations,
        // it means the table IS free now.

        Integer notifiedResId = controller.notifyNextFromWaitlist();

        // Should be c1 (ID 1) because it's first in list
        if (notifiedResId == null) {
            System.out.println("   -> Error: No one notified.");
            return false;
        }

        if (notifiedResId != 1) {
            System.out.println("   -> Error: Wrong candidate notified. Expected 1, got " + notifiedResId);
            return false;
        }

        return true;
    }

    // =========================================================================
    // Stub DB Controller
    // =========================================================================
    static class StubDBController extends DBController {

        private List<Integer> tableCapacities = new ArrayList<>();
        private List<InternalReservation> reservations = new ArrayList<>();
        private List<WaitingCandidate> waitlist = new ArrayList<>();

        private LocalTime openTime;
        private LocalTime closeTime;

        public StubDBController() {
            super("dummy", "dummy", "dummy");
        }

        public void setTableCapacities(List<Integer> caps) {
            this.tableCapacities = new ArrayList<>(caps);
        }

        public void setDefaultOpeningHours(LocalTime open, LocalTime close) {
            this.openTime = open;
            this.closeTime = close;
        }

        public void addWaitlistCandidate(WaitingCandidate c) {
            waitlist.add(c);
        }

        @Override
        public List<Integer> getTableCapacities() throws SQLException {
            return tableCapacities;
        }

        @Override
        public boolean testConnection() {
            return true;
        }

        @Override
        public OpeningHours getOpeningHoursForDay(DayOfWeek day) throws SQLException {
            // Assume open every day
            return new OpeningHours(day, openTime, closeTime, false);
        }

        @Override
        public DateOverride getDateOverrideForDate(LocalDate date) throws SQLException {
            return null; // No overrides
        }

        @Override
        public List<Integer> getOverlappingGuests(LocalDateTime start, int durationMin) throws SQLException {
            List<Integer> overlaps = new ArrayList<>();
            LocalDateTime end = start.plusMinutes(durationMin);

            for (InternalReservation r : reservations) {
                LocalDateTime rEnd = r.dateTime.plusMinutes(120); // Assume 2h fixed for stored

                // overlap logic: start < rEnd && end > rStart
                if (start.isBefore(rEnd) && end.isAfter(r.dateTime)) {
                    overlaps.add(r.guests);
                }
            }
            return overlaps;
        }

        @Override
        public InsertReservationResult insertReservation(int customerId, LocalDateTime start, int guests)
                throws SQLException {
            InternalReservation r = new InternalReservation();
            r.id = reservations.size() + 1;
            r.customerId = customerId;
            r.dateTime = start;
            r.guests = guests;
            r.status = "ACTIVE";
            reservations.add(r);

            return new InsertReservationResult(r.id, 123456);
        }

        @Override
        public List<WaitingCandidate> getWaitingCandidates() throws SQLException {
            return new ArrayList<>(waitlist);
        }

        @Override
        public boolean notifyWaitlistReservation(int reservationId) throws SQLException {
            // In a real DB this updates status. Here we just return true.
            return true;
        }

        // Helper class for mock data
        class InternalReservation {
            int id;
            int customerId;
            LocalDateTime dateTime;
            int guests;
            String status;
        }
    }
}
