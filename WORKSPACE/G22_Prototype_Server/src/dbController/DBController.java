package dbController;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        this.url = "jdbc:mysql://127.0.0.1:3306/" + dbName + "?serverTimezone=UTC";
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

		String sql = "SELECT reservation_id, reservation_date, number_of_guests, "
				+ "confirmation_code, subscriber_id, created_at " + "FROM `reservation`";

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			//get data from DataBase
			while (rs.next()) {
				int reservationNumber = rs.getInt("reservation_id");

				Date reservationDateSql = rs.getDate("reservation_date");
				LocalDate reservationDate;
				if (reservationDateSql != null) {
					reservationDate = reservationDateSql.toLocalDate();
				} else {
					reservationDate = null;
				}

				int guests = rs.getInt("number_of_guests");
				int conf = rs.getInt("confirmation_code");
				int subId = rs.getInt("subscriber_id");

				Date placingSql = rs.getDate("created_at");
				LocalDate placing;
				if (placingSql != null) {
					placing = placingSql.toLocalDate();
				} else {
					placing = null;
				}

				// adds reservation to the arraylist<reservation>
				Reservation o = new Reservation(reservationNumber, reservationDate, guests, conf, subId, placing);
				result.add(o);

			}
		}

		return result;
	}

	public boolean updateReservationFields(int reservationNumber, LocalDate newDate, int newGuests) throws SQLException {

		String sql = "UPDATE reservation " + "SET reservation_date = ?, number_of_guests = ? " + "WHERE reservation_id = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setDate(1, Date.valueOf(newDate));
			ps.setInt(2, newGuests);
			ps.setInt(3, reservationNumber);

			int updated = ps.executeUpdate();
			return updated == 1;//check if the reservation was updated in the DB
		}
	}

	public int insertReservation(int customerId, LocalDate reservationDate, int numberOfGuests) throws SQLException {

	    String sql =
	        "INSERT INTO reservation (reservation_date, number_of_guests, confirmation_code, subscriber_id, created_at) " +
	        "VALUES (?, ?, ?, ?, ?)";

	    LocalDate createdAt = LocalDate.now();

	    for (int attempt = 1; attempt <= 5; attempt++) {

	        int confirmationCode = (int)(Math.random() * 900000) + 100000; // 6 digits

	        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	            ps.setDate(1, Date.valueOf(reservationDate));
	            ps.setInt(2, numberOfGuests);
	            ps.setInt(3, confirmationCode);
	            ps.setInt(4, customerId);
	            ps.setDate(5, Date.valueOf(createdAt));

	            int inserted = ps.executeUpdate();
	            if (inserted != 1) return -1;

	            // generated key = reservation_id (AUTO_INCREMENT)
	            try (ResultSet keys = ps.getGeneratedKeys()) {
	                if (keys.next()) return keys.getInt(1);
	            }

	            return -1;
	        } catch (SQLException e) {
	            // MySQL duplicate entry error (UNIQUE violation)
	            if (e.getErrorCode() == 1062) {
	                continue; // try another confirmationCode
	            }
	            throw e;
	        }
	    }

	    return -1; // very rare: failed after retries
	}
}
