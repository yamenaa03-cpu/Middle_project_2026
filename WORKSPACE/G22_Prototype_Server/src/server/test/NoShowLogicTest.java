//package server.test;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.time.LocalDateTime;
//
//import common.enums.ReservationStatus;
//import dbController.DBController;
//
///**
// * Manual tests (NO JUnit) for No-Show auto-cancel logic:
// * If reservation_datetime <= NOW() - 15min AND status='ACTIVE' -> auto set to CANCELED.
// *
// * This test inserts a reservation with datetime = NOW() - 20min so we don't wait 15 minutes.
// *
// * IMPORTANT:
// * - Update DB credentials below.
// * - Ensure table reservation has status column (DEFAULT 'ACTIVE').
// * - Ensure customer_id exists in customer table (use a real existing id).
// */
//public class NoShowLogicTest {
//
//    // ====== DB CONFIG (EDIT) ======
//    private static final String DB_NAME = "bistrodb";
//    private static final String DB_USER = "root";
//    private static final String DB_PASS = "Aa123456";
//
//    // Use an existing customer_id from your DB
//    private static final int EXISTING_CUSTOMER_ID = 1001;
//
//    public static void main(String[] args) {
//        System.out.println("=== NoShowLogicTest (Manual) ===");
//
//        try {
//            DBController db = new DBController(DB_NAME, DB_USER, DB_PASS);
//
//            // 1) Insert an "old" ACTIVE reservation: NOW() - 20 minutes
//            int reservationId = insertOldActiveReservation(db, EXISTING_CUSTOMER_ID);
//            pass("Inserted old ACTIVE reservation id=" + reservationId);
//
//            // 2) Verify it is ACTIVE before check
//            String before = getStatus(db, reservationId);
//            assertEquals(ReservationStatus.ACTIVE.name(), before, "Expected status ACTIVE before no-show check");
//            pass("Status before no-show is ACTIVE");
//
//            // 3) Run the no-show logic (same SQL criteria as server scheduler)
//            int canceled = runNoShowCheck(db);
//            assertTrue(canceled >= 1, "Expected at least 1 reservation to be auto-canceled");
//            pass("No-show check canceled count=" + canceled);
//
//            // 4) Verify status became CANCELED
//            String after = getStatus(db, reservationId);
//            assertEquals(ReservationStatus.CANCELED.name(), after, "Expected status CANCELED after no-show check");
//            pass("Status after no-show is CANCELED");
//
//            // 5) Safety: if reservation is IN_PROGRESS it should NOT be canceled
//            int res2 = insertOldActiveReservation(db, EXISTING_CUSTOMER_ID);
//            db.updateReservationStatus(res2, ReservationStatus.IN_PROGRESS.name());
//            pass("Inserted old reservation2 id=" + res2 + " and set to IN_PROGRESS");
//
//            int canceled2 = runNoShowCheck(db);
//            // could still cancel others, but must NOT cancel res2
//            String after2 = getStatus(db, res2);
//            assertEquals(ReservationStatus.IN_PROGRESS.name(), after2, "IN_PROGRESS must not be auto-canceled");
//            pass("IN_PROGRESS not canceled (ok)");
//
//            System.out.println("\n=== DONE ===");
//
//        } catch (Exception e) {
//            System.out.println("❌ FATAL: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Inserts reservation with:
//     * - reservation_datetime = NOW() - 20 minutes
//     * - status = 'ACTIVE'
//     * Returns generated reservation_id.
//     */
//    private static int insertOldActiveReservation(DBController db, int customerId) throws SQLException {
//        String sql =
//            "INSERT INTO reservation " +
//            "(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status) " +
//            "VALUES (NOW() - INTERVAL 20 MINUTE, ?, ?, ?, NULL, NOW(), ?)";
//
//        // generate a confirmation_code that is very unlikely to collide
//        int code = (int)(System.currentTimeMillis() % 1_000_000_000);
//
//        try (Connection conn = db.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
//
//            ps.setInt(1, 2); // guests
//            ps.setInt(2, code);
//            ps.setInt(3, customerId);
//            ps.setString(4, ReservationStatus.ACTIVE.name());
//
//            int rows = ps.executeUpdate();
//            assertTrue(rows == 1, "Insert failed (rows != 1)");
//
//            try (ResultSet rs = ps.getGeneratedKeys()) {
//                if (!rs.next()) throw new SQLException("No generated key returned for reservation_id");
//                return rs.getInt(1);
//            }
//        }
//    }
//
//    /**
//     * Implements the same rule the server scheduler will run:
//     * cancel all reservations that are ACTIVE and older than 15 minutes.
//     */
//    private static int runNoShowCheck(DBController db) throws SQLException {
//        String selectSql =
//            "SELECT reservation_id FROM reservation " +
//            "WHERE status='ACTIVE' AND reservation_datetime <= (NOW() - INTERVAL 15 MINUTE)";
//
//        int canceled = 0;
//
//        try (Connection conn = db.getConnection();
//             PreparedStatement ps = conn.prepareStatement(selectSql);
//             ResultSet rs = ps.executeQuery()) {
//
//            while (rs.next()) {
//                int id = rs.getInt("reservation_id");
//                boolean ok = db.updateReservationStatus(id, ReservationStatus.CANCELED.name());
//                if (ok) canceled++;
//            }
//        }
//
//        return canceled;
//    }
//
//    private static String getStatus(DBController db, int reservationId) throws SQLException {
//        String sql = "SELECT status FROM reservation WHERE reservation_id=?";
//        try (Connection conn = db.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setInt(1, reservationId);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (!rs.next()) return null;
//                return rs.getString("status");
//            }
//        }
//    }
//
//    // ------------------ ASSERT HELPERS ------------------
//
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
//    private static void assertEquals(String expected, String actual, String msg) {
//        if (expected == null && actual == null) return;
//        if (expected != null && expected.equals(actual)) return;
//        fail(msg + " | expected=" + expected + " actual=" + actual);
//    }
//}
