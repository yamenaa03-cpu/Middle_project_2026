package dbController;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.dto.Notification.CustomerContactInfo;
import common.dto.Reservation.InsertReservationResult;
import common.dto.Reservation.ReservationBasicInfo;
import common.dto.Reservation.WaitingCandidate;
import common.entity.Bill;
import common.entity.Customer;
import common.entity.DateOverride;
import common.entity.OpeningHours;
import common.entity.Reservation;
import common.entity.Table;
import common.enums.EmployeeRole;
import common.enums.ReservationStatus;
import common.enums.ReservationType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Handles all direct access to the database. Only this class talks to JDBC.
 * 
 * @author Yamen_abu_ahmad
 * @version 1.0
 */
public class DBController {

//      private static final String URL = "jdbc:mysql://127.0.0.1:3306/bestrodb?user=root";
//      private static final String USER = "root";
//      private static final String PASSWORD = "Yabuahmad_782003";

        private String url;

        private String user;

        private String password;

        // constructor for ServerController
        public DBController(String dbName, String dbUser, String dbPassword) {
                this.url = "jdbc:mysql://127.0.0.1:3306/" + dbName + "?serverTimezone=Asia/Jerusalem";
                this.user = dbUser;
                this.password = dbPassword;
        }

        // makes connection with the DB accourding to the input of the USER
        private Connection getConnection() throws SQLException {
                return DriverManager.getConnection(url, user, password);

        }

        // check success when user connects to DB
        public boolean testConnection() {
                try (Connection conn = getConnection()) {
                        return true; // success
                } catch (Exception e) {
                        return false; // failed
                }
        }

        public List<Reservation> getAllReservations() throws SQLException {
                // !! check if it is possible to change to Hashmap for faster results !!
                List<Reservation> result = new ArrayList<>();// array list to insert the Reservations in it

                String sql = "SELECT * FROM reservation";

                try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {
                        // get data from DataBase
                        while (rs.next()) {
                                // adds reservation to the arraylist<reservation>
                                Reservation o = mapReservation(rs);
                                result.add(o);

                        }
                }

                return result;
        }

        public List<Reservation> getWaitlistReservations() throws SQLException {
                List<Reservation> result = new ArrayList<>();

                String sql = "SELECT * FROM reservation WHERE status = 'WAITING' ORDER BY created_at ASC";

                try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                                result.add(mapReservation(rs));
                        }
                }

                return result;
        }

        public boolean updateReservationFields(int reservationNumber, LocalDateTime newDateTime, int newGuests)
                        throws SQLException {

                String sql = "UPDATE reservation " + "SET reservation_datetime = ?, number_of_guests = ? "
                                + "WHERE reservation_id = ?";

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setTimestamp(1, Timestamp.valueOf(newDateTime));
                        ps.setInt(2, newGuests);
                        ps.setInt(3, reservationNumber);

                        int updated = ps.executeUpdate();
                        return updated == 1;// check if the reservation was updated in the DB
                }
        }

        public InsertReservationResult insertReservation(int customerId, LocalDateTime reservationDateTime,
                        int numberOfGuests) throws SQLException {

                String sql = "INSERT INTO reservation (reservation_datetime, number_of_guests, confirmation_code,"
                                + " customer_id, created_at, type) VALUES (?, ?, ?, ?, ?, ?)";

                LocalDateTime createdAt = LocalDateTime.now();

                for (int attempt = 1; attempt <= 5; attempt++) {

                        int confirmationCode = (int) (Math.random() * 900000) + 100000; // 6 digits

                        try (Connection conn = getConnection();
                                        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                                ps.setTimestamp(1, Timestamp.valueOf(reservationDateTime));
                                ps.setInt(2, numberOfGuests);
                                ps.setInt(3, confirmationCode);
                                ps.setInt(4, customerId);
                                ps.setTimestamp(5, Timestamp.valueOf(createdAt));
                                ps.setString(6, "ADVANCE"); // Advance reservation

                                int inserted = ps.executeUpdate();
                                if (inserted != 1)
                                        return null;

                                // generated key = reservation_id (AUTO_INCREMENT)
                                try (ResultSet keys = ps.getGeneratedKeys()) {
                                        if (keys.next())
                                                return new InsertReservationResult(keys.getInt(1), confirmationCode);
                                }

                                return null;
                        } catch (SQLException e) {
                                // MySQL duplicate entry error (UNIQUE violation)
                                if (e.getErrorCode() == 1062) {
                                        continue; // try another confirmationCode
                                }
                                throw e;
                        }
                }

                return null; // very rare: failed after retries
        }

        public InsertReservationResult insertNotifiedNow(int customerId, int numberOfGuests) throws SQLException {
                String sql = "INSERT INTO reservation (reservation_datetime, number_of_guests, confirmation_code, "
                                + "customer_id, created_at, status, type) VALUES (?, ?, ?, ?, ?, ?, ?)";

                LocalDateTime now = LocalDateTime.now();

                for (int attempt = 1; attempt <= 5; attempt++) {
                        int confirmationCode = (int) (Math.random() * 900000) + 100000;

                        try (Connection conn = getConnection();
                                        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                                ps.setTimestamp(1, Timestamp.valueOf(now));
                                ps.setInt(2, numberOfGuests);
                                ps.setInt(3, confirmationCode);
                                ps.setInt(4, customerId);
                                ps.setTimestamp(5, Timestamp.valueOf(now));
                                ps.setString(6, "NOTIFIED");
                                ps.setString(7, "WALKIN"); // From waitlist

                                int inserted = ps.executeUpdate();
                                if (inserted != 1)
                                        return null;

                                try (ResultSet keys = ps.getGeneratedKeys()) {
                                        if (keys.next())
                                                return new InsertReservationResult(keys.getInt(1), confirmationCode);
                                }
                                return null;

                        } catch (SQLException e) {
                                if (e.getErrorCode() == 1062)
                                        continue;
                                throw e;
                        }
                }
                return null;
        }

        public InsertReservationResult insertWaitlist(int customerId, int numberOfGuests) throws SQLException {

                String sql = "INSERT INTO reservation (reservation_datetime, number_of_guests, confirmation_code, "
                                + "customer_id, created_at, status, type) VALUES (?, ?, ?, ?, ?, ?, ?)";

                LocalDateTime createdAt = LocalDateTime.now();

                for (int attempt = 1; attempt <= 5; attempt++) {
                        int confirmationCode = (int) (Math.random() * 900000) + 100000; // 6 digits

                        try (Connection conn = getConnection();
                                        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                                ps.setTimestamp(1, null); // reservation_datetime = NULL for WAITING
                                ps.setInt(2, numberOfGuests);
                                ps.setInt(3, confirmationCode);
                                ps.setInt(4, customerId);
                                ps.setTimestamp(5, Timestamp.valueOf(createdAt));
                                ps.setString(6, "WAITING");
                                ps.setString(7, "WALKIN"); // Walk-in / waitlist entry

                                int inserted = ps.executeUpdate();
                                if (inserted != 1)
                                        return null;

                                try (ResultSet keys = ps.getGeneratedKeys()) {
                                        if (keys.next())
                                                return new InsertReservationResult(keys.getInt(1), confirmationCode);
                                }

                                return null;

                        } catch (SQLException e) {
                                if (e.getErrorCode() == 1062)
                                        continue; // duplicate confirmation_code
                                throw e;
                        }
                }

                return null;
        }

        public List<Integer> getTableCapacities() throws SQLException {
                List<Integer> caps = new ArrayList<>();
                String sql = "SELECT capacity FROM restaurant_table";
                try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {
                        while (rs.next())
                                caps.add(rs.getInt(1));
                }
                return caps;
        }

        public List<Integer> getOverlappingGuests(LocalDateTime start, int durationMin) throws SQLException {
                List<Integer> guests = new ArrayList<>();
                String sql = "SELECT number_of_guests FROM reservation "
                                + "WHERE status IN ('ACTIVE','NOTIFIED','IN_PROGRESS') " + "AND reservation_datetime < ? "
                                + "AND DATE_ADD(reservation_datetime, INTERVAL ? MINUTE) > ?";

                LocalDateTime end = start.plusMinutes(durationMin);

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setTimestamp(1, Timestamp.valueOf(end)); // existingStart < newEnd
                        ps.setInt(2, durationMin); // existingEnd = start + duration
                        ps.setTimestamp(3, Timestamp.valueOf(start)); // existingEnd > newStart

                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next())
                                        guests.add(rs.getInt(1));
                        }
                }
                return guests;
        }

        public Integer findCustomerIdBySubscriptionCode(String code) throws SQLException {
                String sql = "SELECT customer_id FROM customer WHERE subscription_code = ? AND is_subscribed = 1";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, code);
                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next() ? rs.getInt("customer_id") : null;
                        }
                }
        }

        public Integer findCustomerIdByPhoneOrEmail(String phone, String email) throws SQLException {
                if (phone != null && !phone.isBlank()) {
                        String sql = "SELECT customer_id FROM customer WHERE phone = ?";
                        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                                ps.setString(1, phone);
                                try (ResultSet rs = ps.executeQuery()) {
                                        if (rs.next())
                                                return rs.getInt("customer_id");
                                }
                        }
                }

                if (email != null && !email.isBlank()) {
                        String sql = "SELECT customer_id FROM customer WHERE email = ?";
                        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                                ps.setString(1, email.toLowerCase().trim());
                                try (ResultSet rs = ps.executeQuery()) {
                                        if (rs.next())
                                                return rs.getInt("customer_id");
                                }
                        }
                }

                return null;
        }

        public List<Reservation> findReservationsByPhoneOrEmail(String phone, String email) throws SQLException {
                Integer customerId = findCustomerIdByPhoneOrEmail(phone, email);
                if (customerId == null)
                        return null;

                String sql = "SELECT * FROM reservation WHERE customer_id = ?";

                List<Reservation> list = new ArrayList<>();
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, customerId);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        Reservation r = mapReservation(rs);
                                        list.add(r);
                                }
                        }
                }
                return list;
        }

        public Reservation findReservationByConfirmationCode(int code) throws SQLException {
                String sql = """
                                    SELECT reservation_id, reservation_datetime, number_of_guests,
                                           confirmation_code, customer_id, created_at, table_id, status
                                    FROM reservation
                                    WHERE confirmation_code = ?
                                    LIMIT 1
                                """;

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, code);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next())
                                        return null;
                                return mapReservation(rs);
                        }
                }
        }

        public Reservation findReservationById(int reservationId) throws SQLException {
                String sql = """
                                    SELECT reservation_id, reservation_datetime, number_of_guests,
                                           confirmation_code, customer_id, created_at, table_id, status
                                    FROM reservation
                                    WHERE reservation_id = ?
                                    LIMIT 1
                                """;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, reservationId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next())
                                        return null;
                                return mapReservation(rs);
                        }
                }
        }

        public int createGuestCustomer(String fullName, String phone, String email) throws SQLException {
                String sql = "INSERT INTO customer(full_name, phone, email, is_subscribed, subscription_code) VALUES (?, ?, ?, 0, NULL)";
                try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, fullName);
                        ps.setString(2, (phone == null || phone.isBlank()) ? null : phone);
                        ps.setString(3, (email == null || email.isBlank()) ? null : email.toLowerCase().trim());
                        ps.executeUpdate();

                        try (ResultSet keys = ps.getGeneratedKeys()) {
                                if (keys.next())
                                        return keys.getInt(1);
                        }
                }
                throw new SQLException("Failed to create guest customer (no generated key).");
        }

        public boolean updateReservationStatus(int reservationId, String newStatus) throws SQLException {
                String sql = "UPDATE reservation SET status=? " + "WHERE reservation_id=? ";

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setString(1, newStatus);
                        ps.setInt(2, reservationId);

                        return ps.executeUpdate() > 0;
                }
        }

        public Integer getReservationCustomerId(int reservationId) throws SQLException {
                String sql = "SELECT customer_id FROM reservation WHERE reservation_id=?";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, reservationId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next())
                                        return rs.getInt("customer_id");
                                return null; // reservation not found
                        }
                }
        }

        public String getReservationStatus(int reservationId) throws SQLException {
                String sql = "SELECT status FROM reservation WHERE reservation_id=?";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, reservationId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next())
                                        return rs.getString("status");
                                return null; // reservation not found
                        }
                }
        }

        public List<Reservation> getCancellableReservationsByCustomerId(int customerId) throws SQLException {
                String sql = "SELECT * FROM reservation WHERE customer_id=? AND status IN ('ACTIVE','NOTIFIED', 'WAITING')"
                                + " ORDER BY reservation_datetime DESC";
                List<Reservation> list = new ArrayList<>();

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, customerId);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        Reservation r = mapReservation(rs);
                                        list.add(r);
                                }
                        }
                }
                return list;
        }

        public Reservation getCancellableReservationByCode(int confirmationCode) throws SQLException {
                String sql = "SELECT * FROM reservation WHERE confirmation_code = ? AND status IN"
                                + " ('ACTIVE','NOTIFIED', 'WAITING') LIMIT 1";

                Reservation reservation = null;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, confirmationCode);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        reservation = mapReservation(rs);
                                }
                        }
                }
                return reservation;
        }

        public List<Reservation> getReceivableReservationsByCustomerId(int customerId) throws SQLException {
                String sql = "SELECT * FROM reservation WHERE customer_id=? AND status IN ('ACTIVE','NOTIFIED')"
                                + " And reservation_datetime <= NOW()" + " ORDER BY reservation_datetime DESC";
                List<Reservation> list = new ArrayList<>();

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, customerId);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        Reservation r = mapReservation(rs);
                                        list.add(r);
                                }
                        }
                }
                return list;
        }

        public Reservation getReceivableReservationByCode(int confirmationCode) throws SQLException {
                String sql = "SELECT * FROM reservation WHERE confirmation_code = ? AND status IN"
                                + " ('ACTIVE','NOTIFIED') And reservation_datetime <= NOW() LIMIT 1";

                Reservation reservation = null;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, confirmationCode);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        reservation = mapReservation(rs);
                                }
                        }
                }
                return reservation;
        }

        public List<Reservation> getPayableReservationsByCustomerId(int customerId) throws SQLException {
                String sql = "SELECT * FROM reservation WHERE customer_id=? AND status = 'IN_PROGRESS'"
                                + " ORDER BY reservation_datetime DESC";
                List<Reservation> list = new ArrayList<>();

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, customerId);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        Reservation r = mapReservation(rs);
                                        list.add(r);
                                }
                        }
                }
                return list;
        }

        public Reservation getPayableReservationByCode(int confirmationCode) throws SQLException {
                String sql = "SELECT * FROM reservation WHERE confirmation_code = ? AND status = 'IN_PROGRESS' LIMIT 1";

                Reservation reservation = null;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, confirmationCode);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        reservation = mapReservation(rs);
                                }
                        }
                }
                return reservation;
        }

        public String getFullNameByCustomerId(int customerId) throws SQLException {
                String sql = "SELECT full_name FROM customer WHERE customer_id=?";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, customerId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next())
                                        return rs.getString("full_name");
                                return null; // reservation not found
                        }
                }
        }

        private Reservation mapReservation(ResultSet rs) throws SQLException {

                int reservationId = rs.getInt("reservation_id");

                LocalDateTime reservationDateTime = null;
                Timestamp ts = rs.getTimestamp("reservation_datetime");
                if (ts != null) {
                        reservationDateTime = ts.toLocalDateTime();
                }

                int guests = rs.getInt("number_of_guests");
                int confirmationCode = rs.getInt("confirmation_code");
                int customerId = rs.getInt("customer_id");

                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                Integer tableId = null;
                int tid = rs.getInt("table_id");
                if (!rs.wasNull()) {
                        tableId = tid;
                }

                ReservationStatus status = null;
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                        status = ReservationStatus.valueOf(statusStr);
                }

                ReservationType type = ReservationType.ADVANCE; // default
                String typeStr = rs.getString("type");
                if (typeStr != null) {
                        type = ReservationType.valueOf(typeStr);
                }

                return new Reservation(reservationId, reservationDateTime, guests, confirmationCode, customerId, createdAt,
                                tableId, status, type);
        }

        public List<Integer> getNoShowReservationIds() throws SQLException {
                String sql = "SELECT reservation_id " + "FROM reservation " + "WHERE status IN ('ACTIVE','NOTIFIED') "
                                + "AND reservation_datetime <= (NOW() - INTERVAL 15 MINUTE)";

                List<Integer> ids = new ArrayList<>();

                try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next())
                                ids.add(rs.getInt("reservation_id"));
                }
                return ids;
        }

        public List<Integer> getReservationsForReminder() throws SQLException {
                String sql = "SELECT reservation_id " + "FROM reservation " + "WHERE status='ACTIVE' "
                                + "AND reminder_sent = FALSE "
                                + "AND reservation_datetime BETWEEN (NOW() + INTERVAL 2 HOUR - INTERVAL 1 MINUTE) "
                                + "AND (NOW() + INTERVAL 2 HOUR + INTERVAL 1 MINUTE)";

                List<Integer> ids = new ArrayList<>();

                try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                                ids.add(rs.getInt("reservation_id"));
                        }
                }
                return ids;
        }

        public void markReminderSent(int reservationId) throws SQLException {
                String sql = "UPDATE reservation SET reminder_sent=TRUE WHERE reservation_id=?";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, reservationId);
                        ps.executeUpdate();
                }
        }

        public List<WaitingCandidate> getWaitingCandidates(int maxCapacity) throws SQLException {
                String sql = """
                                    SELECT reservation_id, customer_id, number_of_guests
                                    FROM reservation
                                    WHERE status = 'WAITING'
                                      AND number_of_guests <= ?
                                    ORDER BY created_at ASC
                                """;

                List<WaitingCandidate> list = new ArrayList<>();

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, maxCapacity);

                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        list.add(new WaitingCandidate(rs.getInt("reservation_id"), rs.getInt("customer_id"),
                                                        rs.getInt("number_of_guests")));
                                }
                        }
                }
                return list;
        }

        public List<WaitingCandidate> getWaitingCandidates() throws SQLException {
                String sql = """
                                    SELECT reservation_id, customer_id, number_of_guests
                                    FROM reservation
                                    WHERE status = 'WAITING'
                                    ORDER BY created_at ASC
                                """;

                List<WaitingCandidate> list = new ArrayList<>();

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        list.add(new WaitingCandidate(rs.getInt("reservation_id"), rs.getInt("customer_id"),
                                                        rs.getInt("number_of_guests")));
                                }
                        }
                }
                return list;
        }

        public boolean notifyWaitlistReservation(int reservationId) throws SQLException {
                String sql = """
                                    UPDATE reservation
                                    SET status = 'NOTIFIED',
                                        reservation_datetime = NOW()
                                    WHERE reservation_id = ?
                                      AND status = 'WAITING'
                                """;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, reservationId);
                        return ps.executeUpdate() == 1;
                }
        }

        public boolean markSeatedNow(int reservationId) throws SQLException {
                String sql = """
                                    UPDATE reservation
                                    SET status='IN_PROGRESS',
                                        reservation_datetime = NOW()
                                    WHERE reservation_id = ?
                                      AND status IN ('ACTIVE','NOTIFIED')
                                """;

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, reservationId);
                        return ps.executeUpdate() == 1;
                }
        }

        public List<Reservation> getReservationsForBilling() throws SQLException {
                String sql = """
                                    SELECT r.*
                                    FROM reservation r
                                    LEFT JOIN bill b ON b.reservation_id = r.reservation_id
                                    WHERE r.status = 'IN_PROGRESS'
                                      AND r.reservation_datetime <= NOW() - INTERVAL 2 HOUR
                                      AND b.bill_id IS NULL
                                """;

                List<Reservation> list = new ArrayList<>();
                try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                                list.add(mapReservation(rs));
                        }
                }
                return list;
        }

        public boolean isCustomerSubscribed(int customerId) throws SQLException {
                String sql = "SELECT is_subscribed FROM customer WHERE customer_id=?";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, customerId);
                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next() && rs.getInt(1) == 1;
                        }
                }
        }

        public Bill findBillByReservationId(int reservationId) throws SQLException {
                String sql = """
                                    SELECT bill_id, reservation_id, amount_before_discount, final_amount, paid
                                    FROM bill
                                    WHERE reservation_id = ?
                                    LIMIT 1
                                """;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, reservationId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        return new Bill(rs.getInt("bill_id"), rs.getInt("reservation_id"),
                                                        rs.getDouble("amount_before_discount"), rs.getDouble("final_amount"),
                                                        rs.getInt("paid") == 1);
                                }
                        }
                }
                return null;
        }

        public Bill insertBill(int reservationId, double amountBeforeDiscount, double finalAmount) throws SQLException {
                String sql = """
                                    INSERT INTO bill (reservation_id, amount_before_discount, final_amount, paid)
                                    VALUES (?, ?, ?, 0)
                                """;
                try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, reservationId);
                        ps.setDouble(2, amountBeforeDiscount);
                        ps.setDouble(3, finalAmount);

                        int inserted = ps.executeUpdate();
                        if (inserted != 1)
                                return null;

                        try (ResultSet keys = ps.getGeneratedKeys()) {
                                if (keys.next()) {
                                        Bill bill = new Bill(keys.getInt(1), reservationId, amountBeforeDiscount, finalAmount, false);
                                        return bill;
                                }
                        }
                }
                return null;
        }

        public Bill findBillById(int billId) throws SQLException {
                String sql = """
                                    SELECT bill_id, reservation_id, amount_before_discount, final_amount, paid
                                    FROM bill
                                    WHERE bill_id = ?
                                    LIMIT 1
                                """;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, billId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        return new Bill(rs.getInt("bill_id"), rs.getInt("reservation_id"),
                                                        rs.getDouble("amount_before_discount"), rs.getDouble("final_amount"),
                                                        rs.getInt("paid") == 1);
                                }
                        }
                }
                return null;
        }

        public boolean markBillPaidById(int billId) throws SQLException {
                String sql = """
                                    UPDATE bill
                                    SET paid = 1, paid_at = NOW()
                                    WHERE bill_id = ? AND paid = 0
                                """;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, billId);
                        return ps.executeUpdate() == 1;
                }
        }
        
        public boolean customerExistsByPhoneOrEmail(String phone, String email) throws SQLException {
            String sql = """
                SELECT 1
                FROM customer
                WHERE (phone = ? AND ? IS NOT NULL)
                   OR (LOWER(email) = LOWER(?) AND ? IS NOT NULL)
                LIMIT 1
            """;

            String p = (phone == null || phone.isBlank()) ? null : phone.trim();
            String e = (email == null || email.isBlank()) ? null : email.trim();

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, p);
                ps.setString(2, p);
                ps.setString(3, e);
                ps.setString(4, e);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }


        public Integer getTableCapacityById(int tableId) throws SQLException {
                String sql = "SELECT capacity FROM restaurant_table WHERE table_id = ? LIMIT 1";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, tableId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next())
                                        return rs.getInt(1);
                        }
                }
                return null;
        }

        public Integer getTableIdByReservationId(int reservationId) throws SQLException {
                String sql = "SELECT table_id FROM reservation WHERE reservation_id = ? LIMIT 1";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, reservationId);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next())
                                        return rs.getInt(1);
                        }
                }
                return null;
        }

        public CustomerContactInfo getCustomerContactInfo(int customerId) throws SQLException {
                String sql = """
                                    SELECT customer_id, full_name, phone, email
                                    FROM customer
                                    WHERE customer_id = ?
                                    LIMIT 1
                                """;

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, customerId);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next())
                                        return null;

                                return new CustomerContactInfo(rs.getInt("customer_id"), rs.getString("full_name"),
                                                rs.getString("phone"), rs.getString("email"));
                        }
                }
        }

        public CustomerContactInfo getContactInfoByReservationId(int reservationId) throws SQLException {
                String sql = """
                                    SELECT c.customer_id, c.full_name, c.phone, c.email
                                    FROM reservation r
                                    JOIN customer c ON c.customer_id = r.customer_id
                                    WHERE r.reservation_id = ?
                                    LIMIT 1
                                """;

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, reservationId);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next())
                                        return null;

                                return new CustomerContactInfo(rs.getInt("customer_id"), rs.getString("full_name"),
                                                rs.getString("phone"), rs.getString("email"));
                        }
                }
        }

        public ReservationBasicInfo getReservationBasicInfo(int reservationId) throws SQLException {
                String sql = """
                                    SELECT c.full_name, r.reservation_datetime, r.number_of_guests, r.confirmation_code
                                    FROM reservation r
                                    JOIN customer c ON c.customer_id = r.customer_id
                                    WHERE r.reservation_id = ?
                                    LIMIT 1
                                """;

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, reservationId);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next())
                                        return null;

                                Timestamp ts = rs.getTimestamp("reservation_datetime");
                                LocalDateTime dt = (ts == null ? null : ts.toLocalDateTime());

                                return new ReservationBasicInfo(rs.getString("full_name"), dt, rs.getInt("number_of_guests"),
                                                rs.getInt("confirmation_code"));
                        }
                }
        }

        public Integer assignTableNow(int reservationId, int guests, int durationMin) throws SQLException {

                String qFindTable = """
                                    SELECT t.table_id
                                    FROM restaurant_table t
                                    WHERE t.capacity >= ?
                                      AND NOT EXISTS (
                                          SELECT 1
                                          FROM reservation r
                                          WHERE r.table_id = t.table_id
                                            AND r.status IN ('ACTIVE','NOTIFIED','IN_PROGRESS')
                                            AND r.reservation_datetime IS NOT NULL
                                            AND r.reservation_datetime < (NOW() + INTERVAL ? MINUTE)
                                            AND DATE_ADD(r.reservation_datetime, INTERVAL ? MINUTE) > NOW()
                                      )
                                    ORDER BY t.capacity ASC
                                    LIMIT 1
                                """;

                String qUpdate = """
                                    UPDATE reservation
                                    SET status='IN_PROGRESS',
                                        reservation_datetime=NOW(),
                                        table_id=?
                                    WHERE reservation_id=?
                                      AND status IN ('ACTIVE','NOTIFIED')
                                """;

                Integer tableId = null;

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(qFindTable)) {
                        ps.setInt(1, guests);
                        ps.setInt(2, durationMin);
                        ps.setInt(3, durationMin);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next())
                                        tableId = rs.getInt(1);
                        }
                }

                if (tableId == null) {
                        return null; // no table available
                }

                int updated;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(qUpdate)) {
                        ps.setInt(1, tableId);
                        ps.setInt(2, reservationId);
                        updated = ps.executeUpdate();
                }

                if (updated != 1) {
                        return null;
                }
                return tableId;
        }

        public String createSubscriber(String fullName, String phone, String email) throws SQLException {
                String sql = """
                                    INSERT INTO customer(full_name, phone, email, is_subscribed, subscription_code)
                                    VALUES (?, ?, ?, 1, ?)
                                """;

                for (int attempt = 1; attempt <= 5; attempt++) {
                        String code = generateSubscriptionCode();

                        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                                ps.setString(1, fullName);
                                ps.setString(2, phone);
                                ps.setString(3, email);
                                ps.setString(4, code);

                                int inserted = ps.executeUpdate();
                                if (inserted == 1)
                                        return code;
                                return null;

                        } catch (SQLException e) {
                                if (e.getErrorCode() == 1062)
                                        continue; // duplicate unique
                                throw e;
                        }
                }
                return null;
        }

        private String generateSubscriptionCode() {
                String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 8; i++)
                        sb.append(chars.charAt((int) (Math.random() * chars.length())));
                return sb.toString();
        }

        public Customer getSubscribedCustomerById(int customerId) throws SQLException {
                String sql = """
                                    SELECT customer_id, full_name, phone, email, is_subscribed, subscription_code
                                    FROM customer
                                    WHERE customer_id = ? AND is_subscribed = 1
                                    LIMIT 1
                                """;

                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, customerId);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next())
                                        return null;

                                return new Customer(rs.getInt("customer_id"), rs.getString("full_name"), rs.getString("phone"),
                                                rs.getString("email"), rs.getInt("is_subscribed") == 1, rs.getString("subscription_code"));
                        }
                }
        }
        
        public Integer findEmployeeIdByCredentials(String username, String password) throws SQLException {
            String sql = """
                SELECT employee_id
                FROM employee
                WHERE username = ? AND password = ?
                LIMIT 1
            """;

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt("employee_id") : null;
                }
            }
        }

        public EmployeeRole getEmployeeRoleById(int employeeId) throws SQLException {
            String sql = """
                SELECT role
                FROM employee
                WHERE employee_id = ?
                LIMIT 1
            """;

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, employeeId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return EmployeeRole.valueOf(rs.getString("role"));
                }
            }
        }

        public String getEmployeeNameById(int employeeId) throws SQLException {
            String sql = """
                SELECT full_name
                FROM employee
                WHERE employee_id = ?
                LIMIT 1
            """;

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, employeeId);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("full_name") : null;
                }
            }
        }

        // Employee lookup customer methods
        public Customer findCustomerBySubscriptionCode(String subscriptionCode) throws SQLException {
            String sql = """
                SELECT customer_id, full_name, phone, email, is_subscribed, subscription_code
                FROM customer
                WHERE subscription_code = ?
                LIMIT 1
            """;

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, subscriptionCode);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getInt("is_subscribed") == 1,
                        rs.getString("subscription_code")
                    );
                }
            }
        }

        public Customer findCustomerByPhone(String phone) throws SQLException {
            String sql = """
                SELECT customer_id, full_name, phone, email, is_subscribed, subscription_code
                FROM customer
                WHERE phone = ?
                LIMIT 1
            """;

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, phone);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getInt("is_subscribed") == 1,
                        rs.getString("subscription_code")
                    );
                }
            }
        }

        public Customer findCustomerByEmail(String email) throws SQLException {
            String sql = """
                SELECT customer_id, full_name, phone, email, is_subscribed, subscription_code
                FROM customer
                WHERE email = ?
                LIMIT 1
            """;

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, email.toLowerCase().trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getInt("is_subscribed") == 1,
                        rs.getString("subscription_code")
                    );
                }
            }
        }

        // ======================== TABLE MANAGEMENT ========================

        public List<Table> getAllTables() throws SQLException {
                List<Table> tables = new ArrayList<>();
                String sql = "SELECT table_number, seats FROM restaurant_table ORDER BY table_number";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql);
                         ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                                tables.add(new Table(
                                        rs.getInt("table_number"),
                                        rs.getInt("seats")
                                ));
                        }
                }
                return tables;
        }

        public int addTable(int seats) throws SQLException {
                String sql = "INSERT INTO restaurant_table (seats) VALUES (?)";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, seats);
                        ps.executeUpdate();

                        try (ResultSet keys = ps.getGeneratedKeys()) {
                                if (keys.next()) {
                                        return keys.getInt(1);
                                }
                        }
                }
                return -1;
        }

        public boolean updateTableSeats(int tableNumber, int newSeats) throws SQLException {
                String sql = "UPDATE restaurant_table SET seats = ? WHERE table_number = ?";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, newSeats);
                        ps.setInt(2, tableNumber);
                        return ps.executeUpdate() > 0;
                }
        }

        public boolean deleteTable(int tableNumber) throws SQLException {
                String sql = "DELETE FROM restaurant_table WHERE table_number = ?";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, tableNumber);
                        return ps.executeUpdate() > 0;
                }
        }

        // ======================== OPENING HOURS MANAGEMENT ========================

        public List<OpeningHours> getOpeningHours() throws SQLException {
                List<OpeningHours> hours = new ArrayList<>();
                String sql = "SELECT day_of_week, open_time, close_time, is_closed FROM opening_hours ORDER BY day_of_week";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql);
                         ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                                int dayNum = rs.getInt("day_of_week");
                                DayOfWeek day = DayOfWeek.of(dayNum == 0 ? 7 : dayNum);
                                LocalTime openTime = rs.getTime("open_time") != null ? rs.getTime("open_time").toLocalTime() : null;
                                LocalTime closeTime = rs.getTime("close_time") != null ? rs.getTime("close_time").toLocalTime() : null;
                                boolean closed = rs.getBoolean("is_closed");

                                hours.add(new OpeningHours(day, openTime, closeTime, closed));
                        }
                }
                return hours;
        }

        public boolean updateOpeningHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime, boolean closed) throws SQLException {
                int dayNum = day.getValue();

                String sql = """
                        INSERT INTO opening_hours (day_of_week, open_time, close_time, is_closed)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE open_time = VALUES(open_time), close_time = VALUES(close_time), is_closed = VALUES(is_closed)
                """;

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, dayNum);
                        if (closed) {
                                ps.setNull(2, java.sql.Types.TIME);
                                ps.setNull(3, java.sql.Types.TIME);
                        } else {
                                ps.setTime(2, java.sql.Time.valueOf(openTime));
                                ps.setTime(3, java.sql.Time.valueOf(closeTime));
                        }
                        ps.setBoolean(4, closed);
                        return ps.executeUpdate() > 0;
                }
        }

        // ======================== DATE OVERRIDE MANAGEMENT ========================

        public List<DateOverride> getDateOverrides() throws SQLException {
                List<DateOverride> overrides = new ArrayList<>();
                String sql = "SELECT override_id, override_date, open_time, close_time, is_closed, reason FROM date_override ORDER BY override_date";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql);
                         ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                                LocalDate date = rs.getDate("override_date").toLocalDate();
                                LocalTime openTime = rs.getTime("open_time") != null ? rs.getTime("open_time").toLocalTime() : null;
                                LocalTime closeTime = rs.getTime("close_time") != null ? rs.getTime("close_time").toLocalTime() : null;

                                overrides.add(new DateOverride(
                                        rs.getInt("override_id"),
                                        date,
                                        openTime,
                                        closeTime,
                                        rs.getBoolean("is_closed"),
                                        rs.getString("reason")
                                ));
                        }
                }
                return overrides;
        }

        public int addDateOverride(LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed, String reason) throws SQLException {
                String sql = "INSERT INTO date_override (override_date, open_time, close_time, is_closed, reason) VALUES (?, ?, ?, ?, ?)";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setDate(1, Date.valueOf(date));
                        if (closed) {
                                ps.setNull(2, java.sql.Types.TIME);
                                ps.setNull(3, java.sql.Types.TIME);
                        } else {
                                ps.setTime(2, java.sql.Time.valueOf(openTime));
                                ps.setTime(3, java.sql.Time.valueOf(closeTime));
                        }
                        ps.setBoolean(4, closed);
                        ps.setString(5, reason);
                        ps.executeUpdate();

                        try (ResultSet keys = ps.getGeneratedKeys()) {
                                if (keys.next()) {
                                        return keys.getInt(1);
                                }
                        }
                }
                return -1;
        }

        public boolean updateDateOverride(int id, LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed, String reason) throws SQLException {
                String sql = "UPDATE date_override SET override_date = ?, open_time = ?, close_time = ?, is_closed = ?, reason = ? WHERE override_id = ?";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setDate(1, Date.valueOf(date));
                        if (closed) {
                                ps.setNull(2, java.sql.Types.TIME);
                                ps.setNull(3, java.sql.Types.TIME);
                        } else {
                                ps.setTime(2, java.sql.Time.valueOf(openTime));
                                ps.setTime(3, java.sql.Time.valueOf(closeTime));
                        }
                        ps.setBoolean(4, closed);
                        ps.setString(5, reason);
                        ps.setInt(6, id);
                        return ps.executeUpdate() > 0;
                }
        }

        public boolean deleteDateOverride(int id) throws SQLException {
                String sql = "DELETE FROM date_override WHERE override_id = ?";

                try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, id);
                        return ps.executeUpdate() > 0;
                }
        }

}
