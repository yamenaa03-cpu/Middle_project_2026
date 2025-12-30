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

import common.dto.InsertReservationResult;
import common.entity.Reservation;

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

    //constructor for ServerController
    public DBController(String dbName, String dbUser, String dbPassword) {
        this.url = "jdbc:mysql://127.0.0.1:3306/" + dbName + "?serverTimezone=Asia/Jerusalem";
        this.user = dbUser;
        this.password = dbPassword;
    }
    //makes connection with the DB accourding to the input of the USER
    private Connection getConnection() throws SQLException  {
		return DriverManager.getConnection(url, user, password);
	
    }
    
    //check success when user connects to DB
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return true;  // success
        } catch (Exception e) {
            return false; // failed
        }
    }

	public List<Reservation> getAllReservations() throws SQLException {
		// !! check if it is possible to change to Hashmap for faster results !!
		List<Reservation> result = new ArrayList<>();// array list to insert the Reservations in it 

		String sql = "SELECT reservation_id, reservation_datetime, number_of_guests, "
				+ "confirmation_code, customer_id, created_at " + "FROM `reservation`";

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			//get data from DataBase
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

				// adds reservation to the arraylist<reservation>
				Reservation o = new Reservation(reservationNumber, reservationDate, guests, conf, cusId, placing);
				result.add(o);

			}
		}

		return result;
	}

	public boolean updateReservationFields(int reservationNumber, LocalDateTime newDateTime, int newGuests) throws SQLException {

		String sql = "UPDATE reservation " + "SET reservation_datetime = ?, number_of_guests = ? " + "WHERE reservation_id = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(newDateTime));
			ps.setInt(2, newGuests);
			ps.setInt(3, reservationNumber);

			int updated = ps.executeUpdate();
			return updated == 1;//check if the reservation was updated in the DB
		}
	}

	public InsertReservationResult insertReservation(int customerId, LocalDateTime reservationDateTime, int numberOfGuests) throws SQLException {

	    String sql =
	        "INSERT INTO reservation (reservation_datetime, number_of_guests, confirmation_code, customer_id, created_at) " +
	        "VALUES (?, ?, ?, ?, ?)";

	    LocalDateTime createdAt = LocalDateTime.now();

	    for (int attempt = 1; attempt <= 5; attempt++) {

	        int confirmationCode = (int)(Math.random() * 900000) + 100000; // 6 digits

	        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	            ps.setTimestamp(1, Timestamp.valueOf(reservationDateTime));
	            ps.setInt(2, numberOfGuests);
	            ps.setInt(3, confirmationCode);
	            ps.setInt(4, customerId);
	            ps.setTimestamp(5, Timestamp.valueOf(createdAt));

	            int inserted = ps.executeUpdate();
	            if (inserted != 1) return null;

	            // generated key = reservation_id (AUTO_INCREMENT)
	            try (ResultSet keys = ps.getGeneratedKeys()) {
	                if (keys.next()) return new InsertReservationResult(keys.getInt(1), confirmationCode);
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
	        while (rs.next()) caps.add(rs.getInt(1));
	    }
	    return caps;
	}

	public List<Integer> getOverlappingGuests(LocalDateTime start, int durationMin) throws SQLException {
	    List<Integer> guests = new ArrayList<>();
	    String sql =
	        "SELECT number_of_guests FROM reservation " +
	        "WHERE status = 'ACTIVE' " +
	        "AND reservation_datetime < ? " +
	        "AND DATE_ADD(reservation_datetime, INTERVAL ? MINUTE) > ?";

	    LocalDateTime end = start.plusMinutes(durationMin);

	    try (Connection conn = getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setTimestamp(1, Timestamp.valueOf(end));    // existingStart < newEnd
	        ps.setInt(2, durationMin);                     // existingEnd = start + duration
	        ps.setTimestamp(3, Timestamp.valueOf(start));  // existingEnd > newStart

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) guests.add(rs.getInt(1));
	        }
	    }
	    return guests;
	}

}
