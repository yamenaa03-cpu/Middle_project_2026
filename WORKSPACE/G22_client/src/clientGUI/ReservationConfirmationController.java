package clientGUI;

import java.time.LocalDateTime;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * ReservationConfirmationController displays the confirmation screen after a successful reservation.
 * 
 * <p>This controller shows the customer their reservation details including:
 * <ul>
 *   <li>Confirmation code (for future reference)</li>
 *   <li>Reservation date and time</li>
 *   <li>Number of guests</li>
 *   <li>Success message</li>
 * </ul>
 * 
 * @author G22 Team
 * @version 1.0
 * @see ReservationController
 */
public class ReservationConfirmationController {

    // ==========================================================
    // FXML UI COMPONENTS
    // ==========================================================
    
    @FXML private Label dateTimeLabel;
    @FXML private Label guestsLabel;
    @FXML private Label confirmationCodeLabel;
    @FXML private Label messageLabel;

    // ==========================================================
    // DATA METHODS
    // ==========================================================
    
    /**
     * Sets the confirmation data to display.
     * 
     * @param code the confirmation code for the reservation
     * @param dt the reservation date and time (may be null for waitlist)
     * @param guests the number of guests
     */
    public void setData(int code, LocalDateTime dt, int guests) {
                if(dt!=null) {
        dateTimeLabel.setText(dt.toString());
                }else {dateTimeLabel.setText(null);}
        guestsLabel.setText(String.valueOf(guests));
        confirmationCodeLabel.setText(String.valueOf(code));
        messageLabel.setText("Reservation confirmed!");
    }
}
