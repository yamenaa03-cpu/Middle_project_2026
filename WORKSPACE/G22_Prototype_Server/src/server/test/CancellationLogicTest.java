//package server.test;
//
//import java.sql.SQLException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//import common.dto.Authentication.CustomerAuthResult;
//import common.dto.Reservation.CancelReservationResult;
//import common.dto.Reservation.CreateReservationResult;
//import common.enums.ReservationStatus;
//import controllers.AuthenticationController;
//import controllers.ReservationController;
//import dbController.DBController;
//
///**
// * Manual tests (NO JUnit) for Cancel rules with IN_PROGRESS status.
// * Style: PASS/FAIL prints.
// */
//public class CancellationLogicTest {
//
//    // ====== DB CONFIG (EDIT) ======
//    private static final String DB_NAME = "bistrodb";
//    private static final String DB_USER = "root";
//    private static final String DB_PASS = "Aa123456";
//
//    public static void main(String[] args) {
//        System.out.println("=== CancelReservationStatusTest (Manual) ===");
//
//        try {
//            DBController db = new DBController(DB_NAME, DB_USER, DB_PASS);
//            AuthenticationController auth = new AuthenticationController(db);
//            ReservationController res = new ReservationController(db);
//
//            // Create 2 customers (guest) to test ownership
//            CustomerAuthResult a1 = auth.authenticateGuest("Cancel Tester 1", "0597000001", "ct1@test.com");
//            assertTrue(a1.isSuccess(), "Auth1 failed");
//            int c1 = a1.getCustomerId();
//
//            CustomerAuthResult a2 = auth.authenticateGuest("Cancel Tester 2", "0597000002", "ct2@test.com");
//            assertTrue(a2.isSuccess(), "Auth2 failed");
//            int c2 = a2.getCustomerId();
//
//            // Create reservation for customer1
//            LocalDateTime dt = pickValidReservationTime();
//            CreateReservationResult cr = res.createReservation(c1, dt, 2);
//            assertTrue(cr.isSuccess(), "Create reservation failed: " + cr.getMessage());
//            int reservationId = cr.getReservationId();
//            pass("Created reservation id=" + reservationId);
//
//            // -------- Test 1: cancel should succeed when ACTIVE --------
//            // ensure ACTIVE (in case default isn't)
//            CancelReservationResult r1 = res.cancelReservation(reservationId, c1);
//            
//            assertTrue(r1.isSuccess(), "Expected cancel success for ACTIVE. Got: " + r1.getMessage());
//            pass("Cancel ACTIVE succeeds");
//
//            // -------- Test 2: cancel again should fail (already canceled) --------
//            CancelReservationResult r2 = res.cancelReservation(reservationId, c1);
//            assertFalse(r2.isSuccess(), "Expected cancel fail (already canceled)");
//            pass("Cancel twice fails");
//
//            // Create a second reservation for further tests
//            LocalDateTime dt2 = pickValidReservationTime().plusDays(1);
//            CreateReservationResult cr2 = res.createReservation(c1, dt2, 2);
//            assertTrue(cr2.isSuccess(), "Create reservation #2 failed: " + cr2.getMessage());
//            int reservationId2 = cr2.getReservationId();
//            pass("Created reservation2 id=" + reservationId2);
//
//            // -------- Test 3: cancel should fail if IN_PROGRESS --------
//            db.updateReservationStatus(reservationId2, ReservationStatus.IN_PROGRESS.name());
//            CancelReservationResult r3 = res.cancelReservation(reservationId2, c1);
//            assertFalse(r3.isSuccess(), "Expected cancel fail for IN_PROGRESS");
//            pass("Cancel IN_PROGRESS fails");
//
//            // -------- Test 4: cancel should fail if COMPLETED --------
//            db.updateReservationStatus(reservationId2, ReservationStatus.COMPLETED.name());
//            CancelReservationResult r4 = res.cancelReservation(reservationId2, c1);
//            assertFalse(r4.isSuccess(), "Expected cancel fail for COMPLETED");
//            pass("Cancel COMPLETED fails");
//
//            // -------- Test 5: cancel should fail if not owner --------
//            // set back ACTIVE then try cancel by customer2
//            db.updateReservationStatus(reservationId2, ReservationStatus.ACTIVE.name());
//            CancelReservationResult r5 = res.cancelReservation(reservationId2, c2);
//            assertFalse(r5.isSuccess(), "Expected cancel fail when customer is not owner");
//            pass("Cancel not-owner fails");
//
//            // -------- Test 6: cancel should succeed again when ACTIVE + correct owner --------
//            CancelReservationResult r6 = res.cancelReservation(reservationId2, c1);
//            assertTrue(r6.isSuccess(), "Expected cancel success for ACTIVE by owner");
//            pass("Cancel ACTIVE by owner succeeds");
//
//            System.out.println("\n=== DONE ===");
//
//        } catch (Exception e) {
//            System.out.println("❌ FATAL: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    // Pick a datetime that usually respects your rules:
//    // - >= 1 hour from now
//    // - minutes = 00 or 30
//    // - within 10:00 -> 02:00 (next day)
//    private static LocalDateTime pickValidReservationTime() {
//        LocalDate today = LocalDate.now();
//
//        // choose today 12:00 (safe inside business hours)
//        LocalDateTime candidate = today.atTime(12, 0);
//
//        // if now already past 11:00, shift to next day 12:00
//        if (LocalDateTime.now().plusHours(1).isAfter(candidate)) {
//            candidate = today.plusDays(1).atTime(12, 0);
//        }
//        return candidate;
//    }
//
//    // ------------------ ASSERT HELPERS ------------------
//    private static void pass(String msg) {
//        System.out.println("✅ PASS: " + msg);
//    }
//
//    private static void fail(String msg) {
//        throw new RuntimeException("❌ FAIL: " + msg);
//    }
//
//    private static void assertTrue(boolean cond, String msg) {
//        if (!cond) fail(msg);
//    }
//
//    private static void assertFalse(boolean cond, String msg) {
//        if (cond) fail(msg);
//    }
//}
