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

import common.dto.Reservation.InsertReservationResult;
import common.entity.Reservation;
import common.enums.ReservationStatus;

/**
 * Handles all direct access to the database. Only this class talks to JDBC.
 * 
 * @author Yamen_abu_ahmad
 * @version 1.0
 */
public class DBController {

//	private static final String URL = "jdbc:mysql://127.0.0.1:3306/bestrodb?user=root";
//	private static final String USER = "root";
//	private static final String PASSWORD = "Yabuahmad_782003";

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

		String sql = "SELECT reservation_id, reservation_datetime, number_of_guests, "
				+ "confirmation_code, customer_id, created_at, table_id " + "FROM `reservation`";

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			// get data from DataBase
			while (rs.next()) {
				int reservationNumber = rs.getInt("reservation_id");

				Timestamp ts_reservation = rs.getTimestamp("reservation_datetime");
				LocalDateTime reservationDate;
				if (ts_reservation != null) {
					reservationDate = ts_reservation.toLocalDateTime();
				} else {
					reservationDate = null;
				}

				int guests = rs.getInt("number_of_guests");
				int conf = rs.getInt("confirmation_code");
				int cusId = rs.getInt("customer_id");

				Timestamp ts_created = rs.getTimestamp("created_at");
				LocalDateTime placing;
				if (ts_created != null) {
					placing = ts_created.toLocalDateTime();
				} else {
					placing = null;
				}

				Integer tableId = null;
				int tid = rs.getInt("table_id");
				if (!rs.wasNull())
					tableId = tid;

				// adds reservation to the arraylist<reservation>
				Reservation o = new Reservation(reservationNumber, reservationDate, guests, conf, cusId, placing,
						tableId);
				result.add(o);

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
				+ " customer_id, created_at) VALUES (?, ?, ?, ?, ?)";

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
		String sql = "SELECT number_of_guests FROM reservation " + "WHERE status = 'ACTIVE' "
				+ "AND reservation_datetime < ? " + "AND DATE_ADD(reservation_datetime, INTERVAL ? MINUTE) > ?";

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

	public List<Reservation> getReservationsByCustomerId(int customerId) throws SQLException {
		String sql = "SELECT * FROM reservation WHERE customer_id=? AND status='ACTIVE' ORDER BY reservation_datetime DESC";
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

	private Reservation mapReservation(ResultSet rs) throws SQLException {

		int reservationId = rs.getInt("reservation_id");

		LocalDateTime reservationDateTime = rs.getTimestamp("reservation_datetime").toLocalDateTime();

		int guests = rs.getInt("number_of_guests");
		int confirmationCode = rs.getInt("confirmation_code");
		int customerId = rs.getInt("customer_id");

		LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

		Integer tableId = null;
		int tid = rs.getInt("table_id");
		if (!rs.wasNull())
			tableId = tid;

		return new Reservation(reservationId, reservationDateTime, guests, confirmationCode, customerId, createdAt,
				tableId);
	}

	public List<Integer> getNoShowReservationIds() throws SQLException {
	    String sql =
	        "SELECT reservation_id " +
	        "FROM reservation " +
	        "WHERE status='ACTIVE' " +
	        "AND reservation_datetime <= (NOW() - INTERVAL 15 MINUTE)";

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

	public List<Integer> getReservationsForReminder() throws SQLException {
	    String sql =
	        "SELECT reservation_id " +
	        "FROM reservation " +
	        "WHERE status='ACTIVE' " +
	        "AND reminder_sent = FALSE " +
	        "AND reservation_datetime BETWEEN (NOW() + INTERVAL 2 HOUR - INTERVAL 1 MINUTE) " +
	        "AND (NOW() + INTERVAL 2 HOUR + INTERVAL 1 MINUTE)";

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
	    try (Connection conn = getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, reservationId);
	        ps.executeUpdate();
	    }
	}

}
