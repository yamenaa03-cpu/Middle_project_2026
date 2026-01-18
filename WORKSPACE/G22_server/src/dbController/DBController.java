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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.dto.Notification.CustomerContactInfo;
import common.dto.Reservation.InsertReservationResult;
import common.dto.Reservation.ReservationBasicInfo;
import common.dto.Reservation.WaitingCandidate;
import common.entity.Bill;
import common.entity.Customer;
import common.entity.DateOverride;
import common.entity.OpeningHours;
import common.entity.Reservation;
import common.entity.SubscriberReportEntry;
import common.entity.Table;
import common.entity.TimeReportEntry;
import common.enums.EmployeeRole;
import common.enums.ReservationStatus;
import common.enums.ReservationType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Handles all direct access to the database. Only this class talks to JDBC.
 * This controller manages all database operations including reservation CRUD
 * operations, customer management, table management, opening hours, date
 * overrides, billing, report generation, waitlist management, and
 * no-show/reminder operations.
 * 
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class DBController {

//      private static final String URL = "jdbc:mysql://127.0.0.1:3306/bestrodb?user=root";
//      private static final String USER = "root";
//      private static final String PASSWORD = "Yabuahmad_782003";

	/**
	 * The JDBC URL for the database connection.
	 */
	private String url;

	/**
	 * The username for database authentication.
	 */
	private String user;

	/**
	 * The password for database authentication.
	 */
	private String password;

	/**
	 * Constructs a new DBController with the specified database connection
	 * parameters.
	 *
	 * @param dbName     the name of the database to connect to
	 * @param dbUser     the username for database authentication
	 * @param dbPassword the password for database authentication
	 */
	public DBController(String dbName, String dbUser, String dbPassword) {
		this.url = "jdbc:mysql://127.0.0.1:3306/" + dbName + "?serverTimezone=Asia/Jerusalem";
		this.user = dbUser;
		this.password = dbPassword;
	}

	/**
	 * Establishes and returns a connection to the database.
	 *
	 * @return a Connection object to the database
	 * @throws SQLException if a database access error occurs
	 */
	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);

	}

	/**
	 * Tests whether a connection to the database can be successfully established.
	 *
	 * @return true if the connection is successful, false otherwise
	 */
	public boolean testConnection() {
		try (Connection conn = getConnection()) {
			return true; // success
		} catch (Exception e) {
			return false; // failed
		}
	}

	/**
	 * Converts a Java DayOfWeek to MySQL day number format. Java: MON=1..SUN=7 ->
	 * MySQL: SUN=1..SAT=7
	 *
	 * @param day the Java DayOfWeek to convert
	 * @return the MySQL day number
	 */
	private int toDbDay(DayOfWeek day) {
		// Java: MON=1..SUN=7 -> DB(MySQL): SUN=1..SAT=7
		return (day.getValue() % 7) + 1;
	}

	/**
	 * Converts a MySQL day number to Java DayOfWeek format. MySQL: SUN=1..SAT=7 ->
	 * Java: MON=1..SUN=7
	 *
	 * @param dbDay the MySQL day number to convert
	 * @return the Java DayOfWeek
	 */
	private DayOfWeek fromDbDay(int dbDay) {
		// DB(MySQL): SUN=1..SAT=7 -> Java: MON=1..SUN=7
		int javaVal = (dbDay == 1) ? 7 : (dbDay - 1);
		return DayOfWeek.of(javaVal);
	}

	/**
	 * Retrieves all active reservations from the database. Active reservations
	 * include those with status ACTIVE, NOTIFIED, or IN_PROGRESS.
	 *
	 * @return a list of active Reservation objects ordered by reservation datetime
	 *         ascending
	 * @throws SQLException if a database access error occurs
	 */
	public List<Reservation> getActiveReservations() throws SQLException {
		List<Reservation> result = new ArrayList<>();// array list to insert the Reservations in it

		String sql = "SELECT * FROM reservation WHERE status IN ('ACTIVE','NOTIFIED','IN_PROGRESS')  ORDER BY reservation_datetime ASC";

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

	/**
	 * Retrieves all reservations currently on the waitlist.
	 *
	 * @return a list of Reservation objects with WAITING status ordered by creation
	 *         time ascending
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Updates the datetime and number of guests for an existing reservation.
	 *
	 * @param reservationNumber the ID of the reservation to update
	 * @param newDateTime       the new date and time for the reservation
	 * @param newGuests         the new number of guests
	 * @return true if the reservation was successfully updated, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Inserts a new advance reservation into the database. Generates a unique
	 * 6-digit confirmation code and retries up to 5 times if duplicates occur.
	 *
	 * @param customerId          the ID of the customer making the reservation
	 * @param reservationDateTime the date and time of the reservation
	 * @param numberOfGuests      the number of guests for the reservation
	 * @return an InsertReservationResult containing the reservation ID and
	 *         confirmation code, or null if insertion failed
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Inserts a new walk-in reservation with NOTIFIED status and assigns a table
	 * immediately.
	 *
	 * @param customerId     the ID of the customer
	 * @param numberOfGuests the number of guests
	 * @param tableId        the ID of the table to assign
	 * @return an InsertReservationResult containing the reservation ID and
	 *         confirmation code, or null if insertion failed
	 * @throws SQLException if a database access error occurs
	 */
	public InsertReservationResult insertNotifiedNow(int customerId, int numberOfGuests, int tableId)
			throws SQLException {
		String sql = """
				                        INSERT INTO reservation (reservation_datetime, number_of_guests, confirmation_code,
				                customer_id, created_at, status, type, table_id)
				VALUES (?, ?, ?, ?, ?, 'NOTIFIED', 'WALKIN', ?)
				""";

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
				ps.setInt(6, tableId);

				int inserted = ps.executeUpdate();
				if (inserted != 1) {
					return null;
				}

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

	/**
	 * Inserts a new waitlist entry for a walk-in customer.
	 *
	 * @param customerId     the ID of the customer
	 * @param numberOfGuests the number of guests
	 * @return an InsertReservationResult containing the reservation ID and
	 *         confirmation code, or null if insertion failed
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Retrieves a mapping of table IDs to their capacities.
	 *
	 * @return a Map with table ID as key and capacity as value
	 * @throws SQLException if a database access error occurs
	 */
	public Map<Integer, Integer> getTableIdToCapacity() throws SQLException {
		Map<Integer, Integer> map = new HashMap<>();
		String sql = "SELECT table_id, capacity FROM restaurant_table";

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				int tableId = rs.getInt("table_id");
				int cap = rs.getInt("capacity");
				map.put(tableId, cap);
			}
		}
		return map;
	}

	/**
	 * Returns table IDs of reservations that are pinned to a table right now
	 * (IN_PROGRESS or NOTIFIED) and overlap the given time window.
	 *
	 * @param start       the start time of the time window
	 * @param durationMin the duration of the time window in minutes
	 * @return a list of table IDs that are occupied during the specified time
	 *         window
	 * @throws SQLException if a database access error occurs
	 */
	public List<Integer> getOverlappingPinnedTableIds(LocalDateTime start, int durationMin) throws SQLException {
		List<Integer> tableIds = new ArrayList<>();

		String sql = "SELECT DISTINCT table_id " + "FROM reservation " + "WHERE status IN ('IN_PROGRESS', 'NOTIFIED') "
				+ "  AND table_id IS NOT NULL " + "  AND reservation_datetime < ? "
				+ "  AND DATE_ADD(reservation_datetime, INTERVAL ? MINUTE) > ?";

		LocalDateTime end = start.plusMinutes(durationMin);

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(end)); // existingStart < newEnd
			ps.setInt(2, durationMin); // existingEnd = start + duration
			ps.setTimestamp(3, Timestamp.valueOf(start)); // existingEnd > newStart

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					tableIds.add(rs.getInt("table_id"));
				}
			}
		}

		return tableIds;
	}

	/**
	 * Returns the number of guests for ACTIVE (unassigned) reservations that
	 * overlap the given time window. ACTIVE is a confirmed advance reservation but
	 * not seated yet (table_id is NULL).
	 *
	 * @param start       the start time of the time window
	 * @param durationMin the duration of the time window in minutes
	 * @return a list of guest counts for overlapping active reservations
	 * @throws SQLException if a database access error occurs
	 */
	public List<Integer> getOverlappingActiveGuests(LocalDateTime start, int durationMin) throws SQLException {
		List<Integer> guests = new ArrayList<>();

		String sql = "SELECT number_of_guests " + "FROM reservation " + "WHERE status = 'ACTIVE' "
				+ "  AND table_id IS NULL " + "  AND reservation_datetime < ? "
				+ "  AND DATE_ADD(reservation_datetime, INTERVAL ? MINUTE) > ?";

		LocalDateTime end = start.plusMinutes(durationMin);

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(end));
			ps.setInt(2, durationMin);
			ps.setTimestamp(3, Timestamp.valueOf(start));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					guests.add(rs.getInt("number_of_guests"));
				}
			}
		}

		return guests;
	}

	/**
	 * Finds an available table that can accommodate the specified number of guests
	 * during the given time window.
	 *
	 * @param start       the start time of the reservation
	 * @param durationMin the expected duration of the reservation in minutes
	 * @param guests      the number of guests to accommodate
	 * @return the table ID of an available table, or null if no table is available
	 * @throws SQLException if a database access error occurs
	 */
	public Integer findAvailableTableId(LocalDateTime start, int durationMin, int guests) throws SQLException {

		String sql = """
				    SELECT t.table_id
				    FROM restaurant_table t
				    WHERE t.capacity >= ?
				      AND NOT EXISTS (
				          SELECT 1
				          FROM reservation r
				          WHERE r.table_id = t.table_id
				            AND r.status IN ('NOTIFIED','IN_PROGRESS')
				            AND r.reservation_datetime IS NOT NULL
				            AND r.reservation_datetime < ?
				            AND DATE_ADD(r.reservation_datetime, INTERVAL ? MINUTE) > ?
				      )
				    ORDER BY t.capacity ASC
				    LIMIT 1
				""";

		LocalDateTime end = start.plusMinutes(durationMin);

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, guests);
			ps.setTimestamp(2, Timestamp.valueOf(end)); // existingStart < newEnd
			ps.setInt(3, durationMin); // interval
			ps.setTimestamp(4, Timestamp.valueOf(start)); // existingEnd > newStart

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		}

		return null;
	}

	/**
	 * Finds a customer ID by their subscription code.
	 *
	 * @param code the subscription code to search for
	 * @return the customer ID if found and subscribed, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public Integer findCustomerIdBySubscriptionCode(String code) throws SQLException {
		String sql = "SELECT customer_id FROM customer WHERE subscription_code = ? AND is_subscribed = 1";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, code);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt("customer_id") : null;
			}
		}
	}

	/**
	 * Finds a customer ID by their phone number or email address. Phone is checked
	 * first, then email if phone doesn't match.
	 *
	 * @param phone the phone number to search for (can be null or blank)
	 * @param email the email address to search for (can be null or blank)
	 * @return the customer ID if found, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Finds all non-completed and non-cancelled reservations for a customer
	 * identified by phone or email.
	 *
	 * @param phone the phone number to search for
	 * @param email the email address to search for
	 * @return a list of reservations for the customer, or null if customer not
	 *         found
	 * @throws SQLException if a database access error occurs
	 */
	public List<Reservation> findReservationsByPhoneOrEmail(String phone, String email) throws SQLException {
		Integer customerId = findCustomerIdByPhoneOrEmail(phone, email);
		if (customerId == null)
			return null;

		String sql = "SELECT * FROM reservation WHERE customer_id = ? AND status NOT IN ('COMPLETED', 'CANCELED')";

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

	/**
	 * Finds a reservation by its confirmation code.
	 *
	 * @param code the confirmation code to search for
	 * @return the Reservation object if found, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public Reservation findReservationByConfirmationCode(int code) throws SQLException {
		String sql = """
				    SELECT *
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

	/**
	 * Finds a reservation by its ID.
	 *
	 * @param reservationId the ID of the reservation to find
	 * @return the Reservation object if found, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public Reservation findReservationById(int reservationId) throws SQLException {
		String sql = """
				    SELECT *
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

	/**
	 * Creates a new guest (non-subscribed) customer in the database.
	 *
	 * @param fullName the full name of the customer
	 * @param phone    the phone number (can be null or blank)
	 * @param email    the email address (can be null or blank)
	 * @return the generated customer ID
	 * @throws SQLException if a database access error occurs or no key is generated
	 */
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

	/**
	 * Updates the status of a reservation. For COMPLETED status, also sets
	 * checked_out_at timestamp. For IN_PROGRESS status, also sets checked_in_at
	 * timestamp.
	 *
	 * @param reservationId the ID of the reservation to update
	 * @param newStatus     the new status to set
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean updateReservationStatus(int reservationId, String newStatus) throws SQLException {
		String sql;
		if ("COMPLETED".equals(newStatus)) {
			sql = "UPDATE reservation SET status=?, checked_out_at=NOW() WHERE reservation_id=?";
		} else if ("IN_PROGRESS".equals(newStatus)) {
			sql = "UPDATE reservation SET status=?, checked_in_at=NOW() WHERE reservation_id=?";
		} else {
			sql = "UPDATE reservation SET status=? WHERE reservation_id=?";
		}

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, newStatus);
			ps.setInt(2, reservationId);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Gets the customer ID associated with a reservation.
	 *
	 * @param reservationId the ID of the reservation
	 * @return the customer ID, or null if reservation not found
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets the status of a reservation.
	 *
	 * @param reservationId the ID of the reservation
	 * @return the status string, or null if reservation not found
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets all cancellable reservations for a customer. Cancellable statuses
	 * include ACTIVE, NOTIFIED, and WAITING.
	 *
	 * @param customerId the ID of the customer
	 * @return a list of cancellable reservations ordered by datetime descending
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets a cancellable reservation by its confirmation code.
	 *
	 * @param confirmationCode the confirmation code to search for
	 * @return the Reservation if found and cancellable, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets all receivable reservations for a customer. Receivable means ACTIVE or
	 * NOTIFIED status with datetime at or before now.
	 *
	 * @param customerId the ID of the customer
	 * @return a list of receivable reservations ordered by datetime descending
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets a receivable reservation by its confirmation code.
	 *
	 * @param confirmationCode the confirmation code to search for
	 * @return the Reservation if found and receivable, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets all payable reservations for a customer. Payable means IN_PROGRESS
	 * status.
	 *
	 * @param customerId the ID of the customer
	 * @return a list of payable reservations ordered by datetime descending
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets a payable reservation by its confirmation code.
	 *
	 * @param confirmationCode the confirmation code to search for
	 * @return the Reservation if found and payable, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets the full name of a customer by their ID.
	 *
	 * @param customerId the ID of the customer
	 * @return the full name of the customer, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Maps a ResultSet row to a Reservation object.
	 *
	 * @param rs the ResultSet positioned at the row to map
	 * @return a Reservation object populated with data from the ResultSet
	 * @throws SQLException if a database access error occurs
	 */
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

		String statusStr = rs.getString("status");
		ReservationStatus status = (statusStr == null) ? null : ReservationStatus.valueOf(statusStr);

		String typeStr = rs.getString("type");
		ReservationType type = (typeStr == null) ? null : ReservationType.valueOf(typeStr);

		boolean reminderSent = rs.getBoolean("reminder_sent");

		LocalDateTime checkedInAt = null;
		Timestamp ciTs = rs.getTimestamp("checked_in_at");
		if (ciTs != null) {
			checkedInAt = ciTs.toLocalDateTime();
		}

		LocalDateTime checkedOutAt = null;
		Timestamp coTs = rs.getTimestamp("checked_out_at");
		if (coTs != null) {
			checkedOutAt = coTs.toLocalDateTime();
		}

		return new Reservation(reservationId, reservationDateTime, guests, confirmationCode, customerId, tableId,
				createdAt, status, reminderSent, type, checkedInAt, checkedOutAt);
	}

	/**
	 * Gets the IDs of reservations that should be marked as no-shows. A no-show is
	 * an ACTIVE or NOTIFIED reservation that is 15+ minutes past its scheduled
	 * time.
	 *
	 * @return a list of reservation IDs that are no-shows
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets the IDs of reservations that need a reminder notification. These are
	 * ACTIVE reservations scheduled for approximately 2 hours from now that haven't
	 * had a reminder sent yet.
	 *
	 * @return a list of reservation IDs needing reminders
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Marks a reservation as having had its reminder sent.
	 *
	 * @param reservationId the ID of the reservation
	 * @throws SQLException if a database access error occurs
	 */
	public void markReminderSent(int reservationId) throws SQLException {
		String sql = "UPDATE reservation SET reminder_sent=TRUE WHERE reservation_id=?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, reservationId);
			ps.executeUpdate();
		}
	}

	/**
	 * Gets waitlist candidates that can be seated at a table with the given
	 * capacity.
	 *
	 * @param maxCapacity the maximum capacity to filter by
	 * @return a list of WaitingCandidate objects ordered by creation time ascending
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets all waitlist candidates regardless of capacity.
	 *
	 * @return a list of all WaitingCandidate objects ordered by creation time
	 *         ascending
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Notifies a waitlist reservation by setting its status to NOTIFIED, assigning
	 * a table, and setting the reservation datetime to now.
	 *
	 * @param reservationId the ID of the reservation to notify
	 * @param tableId       the ID of the table to assign
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean notifyWaitlistReservation(int reservationId, int tableId) throws SQLException {
		String sql = """
				    UPDATE reservation
				    SET status = 'NOTIFIED',
				        reservation_datetime = NOW(),
				        table_id = ?
				    WHERE reservation_id = ?
				      AND status = 'WAITING'
				""";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, tableId);
			ps.setInt(2, reservationId);
			return ps.executeUpdate() == 1;
		}
	}

	/**
	 * Marks a reservation as seated (IN_PROGRESS) and sets the check-in time to
	 * now.
	 *
	 * @param reservationId the ID of the reservation to mark as seated
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean markSeatedNow(int reservationId) throws SQLException {
		String sql = """
				    UPDATE reservation
				    SET status='IN_PROGRESS',
				        reservation_datetime = NOW(),
				        checked_in_at = NOW()
				    WHERE reservation_id = ?
				      AND status IN ('ACTIVE','NOTIFIED')
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, reservationId);
			return ps.executeUpdate() == 1;
		}
	}

	/**
	 * Gets reservations that are ready for billing. These are IN_PROGRESS
	 * reservations that started 2+ hours ago and don't have a bill yet.
	 *
	 * @return a list of reservations ready for billing
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Checks if a customer is subscribed.
	 *
	 * @param customerId the ID of the customer to check
	 * @return true if the customer is subscribed, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean isCustomerSubscribed(int customerId) throws SQLException {
		String sql = "SELECT is_subscribed FROM customer WHERE customer_id=?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, customerId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) == 1;
			}
		}
	}

	/**
	 * Finds a bill by its associated reservation ID.
	 *
	 * @param reservationId the ID of the reservation
	 * @return the Bill object if found, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Inserts a new bill for a reservation.
	 *
	 * @param reservationId        the ID of the reservation
	 * @param amountBeforeDiscount the amount before any discount
	 * @param finalAmount          the final amount after discount
	 * @return the created Bill object, or null if insertion failed
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Finds a bill by its ID.
	 *
	 * @param billId the ID of the bill to find
	 * @return the Bill object if found, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Marks a bill as paid by its ID.
	 *
	 * @param billId the ID of the bill to mark as paid
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Checks if a customer exists with the given phone or email.
	 *
	 * @param phone the phone number to check
	 * @param email the email address to check
	 * @return true if a customer exists with the given phone or email, false
	 *         otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, p);
			ps.setString(2, p);
			ps.setString(3, e);
			ps.setString(4, e);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Gets the capacity of a table by its ID.
	 *
	 * @param tableId the ID of the table
	 * @return the capacity of the table, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Checks if a table has any active reservations (IN_PROGRESS or NOTIFIED
	 * status).
	 *
	 * @param tableNumber the table number to check
	 * @return true if the table has active reservations, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean hasActiveReservationsOnTable(int tableNumber) throws SQLException {
		String sql = """
				    SELECT 1
				    FROM reservation
				    WHERE table_id = ?
				      AND status IN ('IN_PROGRESS', 'NOTIFIED')
				    LIMIT 1
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, tableNumber);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves all future active reservations within a time range.
	 *
	 * @param from the start of the time range
	 * @param to   the end of the time range
	 * @return list of active reservations in the specified range
	 * @throws SQLException if a database access error occurs
	 */
	public List<Reservation> getFutureActiveReservations(LocalDateTime from, LocalDateTime to) throws SQLException {
		String sql = """
				    SELECT
				        reservation_id,
				        reservation_datetime,
				        number_of_guests,
				        confirmation_code,
				        customer_id,
				        created_at,
				        table_id,
				        status,
				        type,
				        reminder_sent,
				        checked_in_at,
				        checked_out_at
				    FROM reservation
				    WHERE status = ?
				      AND reservation_datetime >= ?
				      AND reservation_datetime <= ?
				    ORDER BY reservation_datetime ASC, reservation_id ASC
				""";

		List<Reservation> list = new ArrayList<>();

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, ReservationStatus.ACTIVE.name());
			ps.setTimestamp(2, Timestamp.valueOf(from));
			ps.setTimestamp(3, Timestamp.valueOf(to));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapReservation(rs));
				}
			}
		}

		return list;
	}

	/**
	 * Moves a reservation to WAITING status and clears its table assignment.
	 *
	 * @param reservationId the ID of the reservation to move
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean moveReservationToWaiting(int reservationId) throws SQLException {
		String sql = """
				    UPDATE reservation
				    SET status = ?, table_id = NULL
				    WHERE reservation_id = ?
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, ReservationStatus.WAITING.name());
			ps.setInt(2, reservationId);

			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Gets the seating capacity of a table by its table number.
	 *
	 * @param tableNumber the table number
	 * @return the capacity of the table, or -1 if not found
	 * @throws SQLException if a database access error occurs
	 */
	public int getTableCapacity(int tableNumber) throws SQLException {
		String sql = "SELECT capacity FROM restaurant_table WHERE table_id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, tableNumber);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("capacity");
				}
			}
		}
		return -1;
	}

	/**
	 * Gets the table ID assigned to a reservation.
	 *
	 * @param reservationId the ID of the reservation
	 * @return the table ID, or null if not found or not assigned
	 * @throws SQLException if a database access error occurs
	 */
	public Integer getTableIdByReservationId(int reservationId) throws SQLException {
		String sql = "SELECT table_id FROM reservation WHERE reservation_id = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, reservationId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				int tid = rs.getInt("table_id");
				return rs.wasNull() ? null : tid;
			}
		}
	}

	/**
	 * Gets the contact information for a customer by their ID.
	 *
	 * @param customerId the ID of the customer
	 * @return a CustomerContactInfo object, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
	public CustomerContactInfo getContactInfoByCustomerId(int customerId) throws SQLException {
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

	/**
	 * Gets the contact information for a customer by their reservation ID.
	 *
	 * @param reservationId the ID of the reservation
	 * @return a CustomerContactInfo object, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Gets basic information about a reservation including customer name.
	 *
	 * @param reservationId the ID of the reservation
	 * @return a ReservationBasicInfo object, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Assigns a table to a reservation.
	 *
	 * @param reservationId the ID of the reservation
	 * @param newTableId    the ID of the table to assign
	 * @return true if the assignment was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean assignTableNow(int reservationId, int newTableId) throws SQLException {
		String sql = """
				    UPDATE reservation
				    SET table_id = ?
				    WHERE reservation_id = ?
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, newTableId);
			ps.setInt(2, reservationId);

			return ps.executeUpdate() == 1;
		}
	}

	/**
	 * Creates a new subscriber customer with a generated subscription code.
	 *
	 * @param fullName the full name of the customer
	 * @param phone    the phone number of the customer
	 * @param email    the email address of the customer
	 * @return the generated subscription code, or null if creation failed
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Generates a random 8-character subscription code using alphanumeric
	 * characters.
	 *
	 * @return a random subscription code
	 */
	private String generateSubscriptionCode() {
		String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 8; i++)
			sb.append(chars.charAt((int) (Math.random() * chars.length())));
		return sb.toString();
	}

	/**
	 * Gets a subscribed customer by their ID.
	 *
	 * @param customerId the ID of the customer
	 * @return the Customer object if found and subscribed, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
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

				return mapCustomer(rs);
			}
		}
	}

	/**
	 * Checks if a customer exists with the given phone or email, excluding a
	 * specific customer.
	 *
	 * @param excludeCustomerId the customer ID to exclude from the check
	 * @param phone             the phone number to check
	 * @param email             the email address to check
	 * @return true if another customer exists with the given phone or email, false
	 *         otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean customerExistsByPhoneOrEmailExcept(int excludeCustomerId, String phone, String email)
			throws SQLException {
		String sql = """
				    SELECT 1
				    FROM customer
				    WHERE customer_id != ?
				      AND ((phone = ? AND ? IS NOT NULL)
				           OR (LOWER(email) = LOWER(?) AND ? IS NOT NULL))
				    LIMIT 1
				""";

		String p = (phone == null || phone.isBlank()) ? null : phone.trim();
		String e = (email == null || email.isBlank()) ? null : email.trim();

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, excludeCustomerId);
			ps.setString(2, p);
			ps.setString(3, p);
			ps.setString(4, e);
			ps.setString(5, e);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Updates a customer's profile information.
	 *
	 * @param customerId the ID of the customer to update
	 * @param fullName   the new full name
	 * @param phone      the new phone number
	 * @param email      the new email address
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean updateCustomerProfile(int customerId, String fullName, String phone, String email)
			throws SQLException {
		String sql = """
				    UPDATE customer
				    SET full_name = ?, phone = ?, email = ?
				    WHERE customer_id = ?
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, fullName);
			ps.setString(2, phone);
			ps.setString(3, email);
			ps.setInt(4, customerId);

			return ps.executeUpdate() == 1;
		}
	}

	/**
	 * Gets the reservation history for a customer.
	 *
	 * @param customerId the ID of the customer
	 * @return a list of all reservations for the customer ordered by datetime
	 *         descending
	 * @throws SQLException if a database access error occurs
	 */
	public List<Reservation> getReservationHistoryByCustomerId(int customerId) throws SQLException {
		String sql = """
				    SELECT *
				    FROM reservation
				    WHERE customer_id = ?
				    ORDER BY reservation_datetime DESC
				""";

		List<Reservation> list = new ArrayList<>();

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, customerId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapReservation(rs));
				}
			}
		}
		return list;
	}

	/**
	 * Maps a ResultSet row to a Customer object.
	 *
	 * @param rs the ResultSet positioned at the row to map
	 * @return a Customer object populated with data from the ResultSet
	 * @throws SQLException if a database access error occurs
	 */
	private Customer mapCustomer(ResultSet rs) throws SQLException {
		return new Customer(rs.getInt("customer_id"), rs.getString("full_name"), rs.getString("phone"),
				rs.getString("email"), rs.getInt("is_subscribed") == 1, rs.getString("subscription_code"));
	}

	/**
	 * Gets all subscribed customers.
	 *
	 * @return a list of all subscribed Customer objects ordered by name
	 * @throws SQLException if a database access error occurs
	 */
	public List<Customer> getAllSubscribers() throws SQLException {
		String sql = """
				    SELECT customer_id, full_name, phone, email, is_subscribed, subscription_code
				    FROM customer
				    WHERE is_subscribed = 1
				    ORDER BY full_name
				""";

		List<Customer> list = new ArrayList<>();

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapCustomer(rs));
			}
		}
		return list;
	}

	/**
	 * Gets all customers who are currently dining (have IN_PROGRESS reservations).
	 *
	 * @return a list of Customer objects currently dining, ordered by name
	 * @throws SQLException if a database access error occurs
	 */
	public List<Customer> getCurrentDiners() throws SQLException {
		String sql = """
				    SELECT DISTINCT c.customer_id, c.full_name, c.phone, c.email,
				           c.is_subscribed, c.subscription_code
				    FROM customer c
				    JOIN reservation r ON c.customer_id = r.customer_id
				    WHERE r.status = 'IN_PROGRESS'
				    ORDER BY c.full_name
				""";

		List<Customer> list = new ArrayList<>();

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapCustomer(rs));
			}
		}
		return list;
	}

	/**
	 * Finds an employee ID by their login credentials.
	 *
	 * @param username the employee's username
	 * @param password the employee's password
	 * @return the employee ID if credentials match, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public Integer findEmployeeIdByCredentials(String username, String password) throws SQLException {
		String sql = """
				    SELECT employee_id
				    FROM employee
				    WHERE username = ? AND password = ?
				    LIMIT 1
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, username);
			ps.setString(2, password);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt("employee_id") : null;
			}
		}
	}

	/**
	 * Gets the role of an employee by their ID.
	 *
	 * @param employeeId the ID of the employee
	 * @return the EmployeeRole, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
	public EmployeeRole getEmployeeRoleById(int employeeId) throws SQLException {
		String sql = """
				    SELECT role
				    FROM employee
				    WHERE employee_id = ?
				    LIMIT 1
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, employeeId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				return EmployeeRole.valueOf(rs.getString("role"));
			}
		}
	}

	/**
	 * Gets the name of an employee by their ID.
	 *
	 * @param employeeId the ID of the employee
	 * @return the employee's full name, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
	public String getEmployeeNameById(int employeeId) throws SQLException {
		String sql = """
				    SELECT full_name
				    FROM employee
				    WHERE employee_id = ?
				    LIMIT 1
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, employeeId);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getString("full_name") : null;
			}
		}
	}

	/**
	 * Finds a customer by their subscription code.
	 *
	 * @param subscriptionCode the subscription code to search for
	 * @return the Customer object if found, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public Customer findCustomerBySubscriptionCode(String subscriptionCode) throws SQLException {
		String sql = """
				    SELECT customer_id, full_name, phone, email, is_subscribed, subscription_code
				    FROM customer
				    WHERE subscription_code = ?
				    LIMIT 1
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, subscriptionCode);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				return new Customer(rs.getInt("customer_id"), rs.getString("full_name"), rs.getString("phone"),
						rs.getString("email"), rs.getInt("is_subscribed") == 1, rs.getString("subscription_code"));
			}
		}
	}

	/**
	 * Finds a customer by their phone number.
	 *
	 * @param phone the phone number to search for
	 * @return the Customer object if found, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public Customer findCustomerByPhone(String phone) throws SQLException {
		String sql = """
				    SELECT customer_id, full_name, phone, email, is_subscribed, subscription_code
				    FROM customer
				    WHERE phone = ?
				    LIMIT 1
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, phone);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				return new Customer(rs.getInt("customer_id"), rs.getString("full_name"), rs.getString("phone"),
						rs.getString("email"), rs.getInt("is_subscribed") == 1, rs.getString("subscription_code"));
			}
		}
	}

	/**
	 * Finds a customer by their email address.
	 *
	 * @param email the email address to search for
	 * @return the Customer object if found, null otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public Customer findCustomerByEmail(String email) throws SQLException {
		String sql = """
				    SELECT customer_id, full_name, phone, email, is_subscribed, subscription_code
				    FROM customer
				    WHERE email = ?
				    LIMIT 1
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email.toLowerCase().trim());

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				return new Customer(rs.getInt("customer_id"), rs.getString("full_name"), rs.getString("phone"),
						rs.getString("email"), rs.getInt("is_subscribed") == 1, rs.getString("subscription_code"));
			}
		}
	}

	// ======================== TABLE MANAGEMENT ========================

	/**
	 * Gets all tables in the restaurant.
	 *
	 * @return a list of all Table objects ordered by table ID
	 * @throws SQLException if a database access error occurs
	 */
	public List<Table> getAllTables() throws SQLException {
		List<Table> tables = new ArrayList<>();
		String sql = "SELECT table_id, capacity FROM restaurant_table ORDER BY table_id";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				tables.add(new Table(rs.getInt("table_id"), rs.getInt("capacity")));
			}
		}
		return tables;
	}

	/**
	 * Adds a new table to the restaurant.
	 *
	 * @param capacity the seating capacity of the new table
	 * @return the generated table ID, or -1 if insertion failed
	 * @throws SQLException if a database access error occurs
	 */
	public int addTable(int capacity) throws SQLException {
		String sql = "INSERT INTO restaurant_table (capacity) VALUES (?)";

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			ps.setInt(1, capacity);
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getInt(1);
				}
			}
		}
		return -1;
	}

	/**
	 * Updates the capacity of an existing table.
	 *
	 * @param tableNumber the ID of the table to update
	 * @param newcapacity the new capacity for the table
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean updateTableCapacity(int tableNumber, int newcapacity) throws SQLException {
		String sql = "UPDATE restaurant_table SET capacity = ? WHERE table_id = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, newcapacity);
			ps.setInt(2, tableNumber);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Deletes a table from the restaurant. First detaches any reservations from the
	 * table, then deletes the table.
	 *
	 * @param tableNumber the ID of the table to delete
	 * @return true if the deletion was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean deleteTable(int tableNumber) throws SQLException {

		String detachSql = """
				    UPDATE reservation
				    SET table_id = NULL
				    WHERE table_id = ?
				""";

		String deleteSql = "DELETE FROM restaurant_table WHERE table_id = ?";

		try (Connection conn = getConnection()) {
			try {
				// 1) detach any remaining references (should be none if you block
				// IN_PROGRESS/NOTIFIED)
				try (PreparedStatement ps = conn.prepareStatement(detachSql)) {
					ps.setInt(1, tableNumber);
					ps.executeUpdate();
				}

				// 2) delete table
				int affected;
				try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
					ps.setInt(1, tableNumber);
					affected = ps.executeUpdate();
				}
				System.out.println("DELETE affected rows = " + affected);

				return affected > 0;
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	// ======================== OPENING HOURS MANAGEMENT ========================

	/**
	 * Gets the opening hours for all days of the week.
	 *
	 * @return a list of OpeningHours objects for each day
	 * @throws SQLException if a database access error occurs
	 */
	public List<OpeningHours> getOpeningHours() throws SQLException {
		List<OpeningHours> hours = new ArrayList<>();
		String sql = "SELECT day_of_week, open_time, close_time, is_closed FROM opening_hours ORDER BY day_of_week";

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				int dayNum = rs.getInt("day_of_week");
				DayOfWeek day = fromDbDay(dayNum);
				LocalTime openTime = rs.getTime("open_time") != null ? rs.getTime("open_time").toLocalTime() : null;
				LocalTime closeTime = rs.getTime("close_time") != null ? rs.getTime("close_time").toLocalTime() : null;
				boolean closed = rs.getBoolean("is_closed");

				hours.add(new OpeningHours(day, openTime, closeTime, closed));
			}
		}
		return hours;
	}

	/**
	 * Updates the opening hours for a specific day of the week.
	 *
	 * @param day       the day of the week to update
	 * @param openTime  the opening time (null if closed)
	 * @param closeTime the closing time (null if closed)
	 * @param closed    whether the restaurant is closed on this day
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean updateOpeningHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime, boolean closed)
			throws SQLException {
		int dayNum = toDbDay(day);

		String sql = """
				        INSERT INTO opening_hours (day_of_week, open_time, close_time, is_closed)
				        VALUES (?, ?, ?, ?)
				        ON DUPLICATE KEY UPDATE open_time = VALUES(open_time), close_time = VALUES(close_time), is_closed = VALUES(is_closed)
				""";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

	/**
	 * Gets all date overrides for special opening hours.
	 *
	 * @return a list of DateOverride objects ordered by date
	 * @throws SQLException if a database access error occurs
	 */
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

				overrides.add(new DateOverride(rs.getInt("override_id"), date, openTime, closeTime,
						rs.getBoolean("is_closed"), rs.getString("reason")));
			}
		}
		return overrides;
	}

	/**
	 * Adds a new date override for special opening hours.
	 *
	 * @param date      the date for the override
	 * @param openTime  the opening time (null if closed)
	 * @param closeTime the closing time (null if closed)
	 * @param closed    whether the restaurant is closed on this date
	 * @param reason    the reason for the override
	 * @return the generated override ID, or -1 if insertion failed
	 * @throws SQLException if a database access error occurs
	 */
	public int addDateOverride(LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed, String reason)
			throws SQLException {
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

	/**
	 * Updates an existing date override.
	 *
	 * @param id        the ID of the override to update
	 * @param date      the new date for the override
	 * @param openTime  the new opening time (null if closed)
	 * @param closeTime the new closing time (null if closed)
	 * @param closed    whether the restaurant is closed on this date
	 * @param reason    the new reason for the override
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean updateDateOverride(int id, LocalDate date, LocalTime openTime, LocalTime closeTime, boolean closed,
			String reason) throws SQLException {
		String sql = "UPDATE date_override SET override_date = ?, open_time = ?, close_time = ?, is_closed = ?, reason = ? WHERE override_id = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

	/**
	 * Deletes a date override.
	 *
	 * @param id the ID of the override to delete
	 * @return true if the deletion was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean deleteDateOverride(int id) throws SQLException {
		String sql = "DELETE FROM date_override WHERE override_id = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}

	// ======================== SPECIFIC DAY/DATE LOOKUPS ========================

	/**
	 * Gets the opening hours for a specific day of the week.
	 *
	 * @param day the day of the week
	 * @return the OpeningHours for that day, or null if not found
	 * @throws SQLException if a database access error occurs
	 */
	public OpeningHours getOpeningHoursForDay(DayOfWeek day) throws SQLException {
		int dayNum = toDbDay(day);
		String sql = "SELECT day_of_week, open_time, close_time, is_closed FROM opening_hours WHERE day_of_week = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, dayNum);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					LocalTime openTime = rs.getTime("open_time") != null ? rs.getTime("open_time").toLocalTime() : null;
					LocalTime closeTime = rs.getTime("close_time") != null ? rs.getTime("close_time").toLocalTime()
							: null;
					boolean closed = rs.getBoolean("is_closed");
					return new OpeningHours(day, openTime, closeTime, closed);
				}
			}
		}
		return null;
	}

	/**
	 * Gets the date override for a specific date.
	 *
	 * @param date the date to check
	 * @return the DateOverride for that date, or null if no override exists
	 * @throws SQLException if a database access error occurs
	 */
	public DateOverride getDateOverrideForDate(LocalDate date) throws SQLException {
		String sql = "SELECT override_id, override_date, open_time, close_time, is_closed, reason FROM date_override WHERE override_date = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setDate(1, Date.valueOf(date));
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					LocalTime openTime = rs.getTime("open_time") != null ? rs.getTime("open_time").toLocalTime() : null;
					LocalTime closeTime = rs.getTime("close_time") != null ? rs.getTime("close_time").toLocalTime()
							: null;
					return new DateOverride(rs.getInt("override_id"), date, openTime, closeTime,
							rs.getBoolean("is_closed"), rs.getString("reason"));
				}
			}
		}
		return null;
	}

	// ======================== REPORTS ========================

	/**
	 * Gets time report data for a specific month. Includes completed reservations
	 * with check-in and check-out times.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @return a list of TimeReportEntry objects for the specified month
	 * @throws SQLException if a database access error occurs
	 */
	public List<TimeReportEntry> getTimeReportForMonth(int year, int month) throws SQLException {
		String sql = """
				    SELECT r.reservation_id, r.reservation_datetime, r.checked_in_at, r.checked_out_at,
				           r.number_of_guests, c.full_name, c.is_subscribed
				    FROM reservation r
				    JOIN customer c ON r.customer_id = c.customer_id
				    WHERE r.status = 'COMPLETED'
				      AND r.checked_in_at IS NOT NULL
				      AND YEAR(r.checked_in_at) = ?
				      AND MONTH(r.checked_in_at) = ?
				    ORDER BY r.checked_in_at
				""";

		List<TimeReportEntry> list = new ArrayList<>();

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LocalDateTime scheduled = rs.getTimestamp("reservation_datetime") != null
							? rs.getTimestamp("reservation_datetime").toLocalDateTime()
							: null;
					LocalDateTime checkedIn = rs.getTimestamp("checked_in_at") != null
							? rs.getTimestamp("checked_in_at").toLocalDateTime()
							: null;
					LocalDateTime checkedOut = rs.getTimestamp("checked_out_at") != null
							? rs.getTimestamp("checked_out_at").toLocalDateTime()
							: null;

					list.add(new TimeReportEntry(rs.getInt("reservation_id"), scheduled, checkedIn,
							checkedOut, rs.getInt("number_of_guests"), rs.getString("full_name"),
							rs.getInt("is_subscribed") == 1));
				}
			}
		}
		return list;
	}

	/**
	 * Gets subscriber report data for a specific month. Includes reservation
	 * statistics for each subscribed customer.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @return a list of SubscriberReportEntry objects for the specified month
	 * @throws SQLException if a database access error occurs
	 */
	public List<SubscriberReportEntry> getSubscriberReportForMonth(int year, int month)
			throws SQLException {
		String sql = """
				    SELECT c.customer_id, c.full_name, c.subscription_code,
				           COUNT(r.reservation_id) AS total_reservations,
				           SUM(CASE WHEN r.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
				           SUM(CASE WHEN r.status = 'CANCELED' THEN 1 ELSE 0 END) AS cancelled,
				           SUM(CASE WHEN r.type = 'WALKIN' THEN 1 ELSE 0 END) AS waitlist_entries
				    FROM customer c
				    LEFT JOIN reservation r ON c.customer_id = r.customer_id
				         AND YEAR(r.created_at) = ? AND MONTH(r.created_at) = ?
				    WHERE c.is_subscribed = 1
				    GROUP BY c.customer_id, c.full_name, c.subscription_code
				    ORDER BY total_reservations DESC
				""";

		List<SubscriberReportEntry> list = new ArrayList<>();

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(new SubscriberReportEntry(rs.getInt("customer_id"),
							rs.getString("full_name"), rs.getString("subscription_code"),
							rs.getInt("total_reservations"), rs.getInt("completed"), rs.getInt("cancelled"),
							rs.getInt("waitlist_entries")));
				}
			}
		}
		return list;
	}

	// ======================== REPORT STORAGE ========================

	/**
	 * Clears stored time report data for a specific month.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @throws SQLException if a database access error occurs
	 */
	public void clearTimeReport(int year, int month) throws SQLException {
		String sql = "DELETE FROM time_report WHERE report_year = ? AND report_month = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.executeUpdate();
		}
	}

	/**
	 * Inserts a time report entry into storage.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @param entry the TimeReportEntry to store
	 * @throws SQLException if a database access error occurs
	 */
	public void insertTimeReportEntry(int year, int month, TimeReportEntry entry)
			throws SQLException {
		String sql = """
				    INSERT INTO time_report (report_year, report_month, reservation_id, scheduled_time,
				        checked_in_at, checked_out_at, arrival_delay_minutes, session_duration_minutes,
				        number_of_guests, customer_name, is_subscriber)
				    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.setInt(3, entry.getReservationId());
			ps.setTimestamp(4, entry.getScheduledTime() != null ? Timestamp.valueOf(entry.getScheduledTime()) : null);
			ps.setTimestamp(5, entry.getCheckedInAt() != null ? Timestamp.valueOf(entry.getCheckedInAt()) : null);
			ps.setTimestamp(6, entry.getCheckedOutAt() != null ? Timestamp.valueOf(entry.getCheckedOutAt()) : null);
			ps.setLong(7, entry.getArrivalDelayMinutes());
			ps.setLong(8, entry.getSessionDurationMinutes());
			ps.setInt(9, entry.getNumberOfGuests());
			ps.setString(10, entry.getCustomerName());
			ps.setBoolean(11, entry.isSubscriber());
			ps.executeUpdate();
		}
	}

	/**
	 * Clears stored subscriber report data for a specific month.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @throws SQLException if a database access error occurs
	 */
	public void clearSubscriberReport(int year, int month) throws SQLException {
		String sql = "DELETE FROM subscriber_report WHERE report_year = ? AND report_month = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.executeUpdate();
		}
	}

	/**
	 * Inserts a subscriber report entry into storage.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @param entry the SubscriberReportEntry to store
	 * @throws SQLException if a database access error occurs
	 */
	public void insertSubscriberReportEntry(int year, int month, SubscriberReportEntry entry)
			throws SQLException {
		String sql = """
				    INSERT INTO subscriber_report (report_year, report_month, customer_id, customer_name,
				        subscription_code, total_reservations, completed_reservations, cancelled_reservations, waitlist_entries)
				    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			ps.setInt(3, entry.getCustomerId());
			ps.setString(4, entry.getCustomerName());
			ps.setString(5, entry.getSubscriptionCode());
			ps.setInt(6, entry.getTotalReservations());
			ps.setInt(7, entry.getCompletedReservations());
			ps.setInt(8, entry.getCancelledReservations());
			ps.setInt(9, entry.getWaitlistEntries());
			ps.executeUpdate();
		}
	}

	/**
	 * Retrieves stored time report data for a specific month.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @return a list of TimeReportEntry objects from storage
	 * @throws SQLException if a database access error occurs
	 */
	public List<TimeReportEntry> getStoredTimeReport(int year, int month) throws SQLException {
		String sql = """
				    SELECT tr.reservation_id,
				           tr.scheduled_time,
				           tr.checked_in_at,
				           tr.checked_out_at,
				           r.number_of_guests,
				           tr.customer_name,
				           tr.is_subscriber
				    FROM time_report tr
				    JOIN reservation r ON r.reservation_id = tr.reservation_id
				    WHERE tr.report_year = ? AND tr.report_month = ?
				    ORDER BY tr.checked_in_at
				""";

		List<TimeReportEntry> list = new ArrayList<>();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, year);
			ps.setInt(2, month);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LocalDateTime scheduled = rs.getTimestamp("scheduled_time") != null
							? rs.getTimestamp("scheduled_time").toLocalDateTime()
							: null;
					LocalDateTime checkedIn = rs.getTimestamp("checked_in_at") != null
							? rs.getTimestamp("checked_in_at").toLocalDateTime()
							: null;
					LocalDateTime checkedOut = rs.getTimestamp("checked_out_at") != null
							? rs.getTimestamp("checked_out_at").toLocalDateTime()
							: null;

					list.add(new TimeReportEntry(rs.getInt("reservation_id"), scheduled, checkedIn, checkedOut,
							rs.getInt("number_of_guests"), rs.getString("customer_name"),
							rs.getBoolean("is_subscriber")));
				}
			}
		}
		return list;
	}

	/**
	 * Retrieves stored subscriber report data for a specific month.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @return a list of SubscriberReportEntry objects from storage
	 * @throws SQLException if a database access error occurs
	 */
	public List<SubscriberReportEntry> getStoredSubscriberReport(int year, int month)
			throws SQLException {
		String sql = """
				    SELECT customer_id, customer_name, subscription_code,
				           total_reservations, completed_reservations, cancelled_reservations, waitlist_entries
				    FROM subscriber_report
				    WHERE report_year = ? AND report_month = ?
				    ORDER BY total_reservations DESC
				""";

		List<SubscriberReportEntry> list = new ArrayList<>();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(new SubscriberReportEntry(rs.getInt("customer_id"),
							rs.getString("customer_name"), rs.getString("subscription_code"),
							rs.getInt("total_reservations"), rs.getInt("completed_reservations"),
							rs.getInt("cancelled_reservations"), rs.getInt("waitlist_entries")));
				}
			}
		}
		return list;
	}

	/**
	 * Checks if a stored time report exists for a specific month.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @return true if a stored time report exists, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean hasStoredTimeReport(int year, int month) throws SQLException {
		String sql = "SELECT COUNT(*) FROM time_report WHERE report_year = ? AND report_month = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

	/**
	 * Checks if a stored subscriber report exists for a specific month.
	 *
	 * @param year  the year
	 * @param month the month (1-12)
	 * @return true if a stored subscriber report exists, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean hasStoredSubscriberReport(int year, int month) throws SQLException {
		String sql = "SELECT COUNT(*) FROM subscriber_report WHERE report_year = ? AND report_month = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, year);
			ps.setInt(2, month);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

	// ======================== CONFLICT DETECTION METHODS ========================

	/**
	 * Gets IDs of active reservations on a specific day of the week.
	 *
	 * @param day the day of the week
	 * @return a list of reservation IDs scheduled for that day
	 * @throws SQLException if a database access error occurs
	 */
	public List<Integer> getActiveReservationsOnDay(DayOfWeek day) throws SQLException {
		int dayNum = toDbDay(day);
		String sql = """
				        SELECT reservation_id FROM reservation
				        WHERE status IN ('ACTIVE', 'NOTIFIED')
				        AND DAYOFWEEK(reservation_datetime) = ?
				""";
		List<Integer> ids = new ArrayList<>();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, dayNum == 7 ? 1 : dayNum + 1);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ids.add(rs.getInt("reservation_id"));
				}
			}
		}
		return ids;
	}

	/**
	 * Gets IDs of active reservations on a specific date.
	 *
	 * @param date the date to check
	 * @return a list of reservation IDs scheduled for that date
	 * @throws SQLException if a database access error occurs
	 */
	public List<Integer> getActiveReservationsOnDate(LocalDate date) throws SQLException {
		String sql = """
				        SELECT reservation_id FROM reservation
				        WHERE status IN ('ACTIVE', 'NOTIFIED')
				        AND DATE(reservation_datetime) = ?
				""";
		List<Integer> ids = new ArrayList<>();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(date));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ids.add(rs.getInt("reservation_id"));
				}
			}
		}
		return ids;
	}

	/**
	 * Gets IDs of active reservations that fall outside the specified opening
	 * hours.
	 *
	 * @param day       the day of the week
	 * @param openTime  the opening time
	 * @param closeTime the closing time
	 * @param closed    whether the restaurant is closed
	 * @return a list of reservation IDs that conflict with the hours
	 * @throws SQLException if a database access error occurs
	 */
	public List<Integer> getActiveReservationsOutsideHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime,
			boolean closed) throws SQLException {
		int dayNum = day.getValue();
		int mysqlDayNum = dayNum == 7 ? 1 : dayNum + 1;

		if (closed) {
			return getActiveReservationsOnDay(day);
		}

		String sql = """
				        SELECT reservation_id, reservation_datetime FROM reservation
				        WHERE status IN ('ACTIVE', 'NOTIFIED')
				        AND DAYOFWEEK(reservation_datetime) = ?
				""";

		List<Integer> conflicting = new ArrayList<>();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, mysqlDayNum);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LocalDateTime resDateTime = rs.getTimestamp("reservation_datetime").toLocalDateTime();

					if (!isReservationWithinHours(resDateTime, 120, openTime, closeTime)) {
						conflicting.add(rs.getInt("reservation_id"));
					}
				}
			}
		}
		return conflicting;
	}

	/**
	 * Gets IDs of active reservations on a specific date that fall outside the
	 * specified hours.
	 *
	 * @param date      the date to check
	 * @param openTime  the opening time
	 * @param closeTime the closing time
	 * @param closed    whether the restaurant is closed
	 * @return a list of reservation IDs that conflict with the hours
	 * @throws SQLException if a database access error occurs
	 */
	public List<Integer> getActiveReservationsOutsideHoursOnDate(LocalDate date, LocalTime openTime,
			LocalTime closeTime, boolean closed) throws SQLException {
		if (closed) {
			return getActiveReservationsOnDate(date);
		}

		String sql = """
				        SELECT reservation_id, reservation_datetime FROM reservation
				        WHERE status IN ('ACTIVE', 'NOTIFIED')
				        AND DATE(reservation_datetime) = ?
				""";

		List<Integer> conflicting = new ArrayList<>();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(date));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LocalDateTime resDateTime = rs.getTimestamp("reservation_datetime").toLocalDateTime();

					if (!isReservationWithinHours(resDateTime, 120, openTime, closeTime)) {
						conflicting.add(rs.getInt("reservation_id"));
					}
				}
			}
		}
		return conflicting;
	}

	/**
	 * Checks if a reservation time falls within the specified opening hours.
	 *
	 * @param reservationDateTime the reservation date and time
	 * @param durationMinutes     the expected duration in minutes
	 * @param openTime            the opening time
	 * @param closeTime           the closing time
	 * @return true if the reservation fits within the hours, false otherwise
	 */
	private boolean isReservationWithinHours(LocalDateTime reservationDateTime, int durationMinutes, LocalTime openTime,
			LocalTime closeTime) {
		LocalTime resTime = reservationDateTime.toLocalTime();
		LocalTime resEndTime = resTime.plusMinutes(durationMinutes);

		return !resTime.isBefore(openTime) && !resEndTime.isAfter(closeTime);
	}

	/**
	 * Checks if a table is currently in use (has IN_PROGRESS or NOTIFIED
	 * reservations).
	 *
	 * @param tableId the ID of the table to check
	 * @return true if the table is in use, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean isTableInUse(int tableId) throws SQLException {
		String sql = """
				        SELECT COUNT(*) FROM reservation
				        WHERE table_id = ?
				        AND status IN ('IN_PROGRESS', 'NOTIFIED')
				""";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, tableId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

	/**
	 * Gets all reservation IDs that are using a specific table and are IN_PROGRESS
	 * or NOTIFIED.
	 *
	 * @param tableId the ID of the table
	 * @return a list of reservation IDs using that table
	 * @throws SQLException if a database access error occurs
	 */
	public List<Integer> getInUseReservationsForTable(int tableId) throws SQLException {
		String sql = """
				        SELECT reservation_id FROM reservation
				        WHERE table_id = ?
				        AND status IN ('IN_PROGRESS', 'NOTIFIED')
				""";
		List<Integer> ids = new ArrayList<>();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, tableId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ids.add(rs.getInt("reservation_id"));
				}
			}
		}
		return ids;
	}
}
