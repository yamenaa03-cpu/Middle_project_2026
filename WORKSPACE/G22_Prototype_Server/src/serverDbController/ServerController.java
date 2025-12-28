package serverDbController;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
public class ServerController {

//	private static final String URL = "jdbc:mysql://127.0.0.1:3306/bestrodb?user=root";
//	private static final String USER = "root";
//	private static final String PASSWORD = "Yabuahmad_782003";

    private String url;
    
    private String user;
    
    private String password;

    //constructor for ServerController
    public ServerController(String dbName, String dbUser, String dbPassword) {
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

		String sql = "SELECT reservation_number, reservation_date, number_of_guests, "
				+ "confirmation_code, subscriber_id, date_of_placing_reservation " + "FROM `reservation`";

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			//get data from DataBase
			while (rs.next()) {
				int reservationNumber = rs.getInt("reservation_number");

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

				Date placingSql = rs.getDate("date_of_placing_reservation");
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

		String sql = "UPDATE `Reservation` " + "SET reservation_date = ?, number_of_guests = ? " + "WHERE reservation_number = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			if (newDate != null) {
				ps.setDate(1, Date.valueOf(newDate));
			} else {
				ps.setNull(1, java.sql.Types.DATE);// if the value of the new date is null
			}

			ps.setInt(2, newGuests);
			ps.setInt(3, reservationNumber);

			int updated = ps.executeUpdate();
			return updated == 1;//check if the reservation was updated in the DB
		}
	}

}
