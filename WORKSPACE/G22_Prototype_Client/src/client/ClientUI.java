package client;

import java.util.List;

import common.dto.Reservation.ReservationResponse;
import common.dto.UserAccount.UserAccountResponse;
import common.entity.Reservation;

/**
 * ClientUI is an interface that defines the communication layer
 * between the Client and the user interface
 * (JavaFX GUI ).
 *
 * By using this interface, the Client class becomes reusable,
 * testable, and completely independent of GUI code.
 *
 *   @version 1.1
 */ 
public interface ClientUI {
    /**
     * Displays a text message to the user.
     *
     * The Client calls this method when:
     *   - the server sends back status messages
     *   - errors occur
     *   - confirmation messages appear 
     *
     * @param msg The text to display.
     */
    void displayMessage(String msg);
    
    /**
     * Displays a list of Reservation objects.
     *
     * The Client calls this method when the server responds
     * with a list of reservations (for example, after GET_ALL_RESERVATIONS).
     *
     * A UI implementation might:
     *   - Fill a JavaFX TableView
     *  
     *
     * @param reservations The list of reservations sent from the server.
     */
    void displayReservations(List<Reservation> reservations);
    
    void handleAuthResponse(UserAccountResponse resp);
    
    
    void handleReservationResponse(ReservationResponse resp);
    
    


}

