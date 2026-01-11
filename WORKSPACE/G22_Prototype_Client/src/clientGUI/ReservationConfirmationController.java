package clientGUI;

import java.time.LocalDateTime;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReservationConfirmationController {

    @FXML private Label dateTimeLabel;
    @FXML private Label guestsLabel;
    @FXML private Label confirmationCodeLabel;
    @FXML private Label messageLabel;

    public void setData(int code, LocalDateTime dt, int guests) {
        dateTimeLabel.setText(dt.toString());
        guestsLabel.setText(String.valueOf(guests));
        confirmationCodeLabel.setText(String.valueOf(code));
        messageLabel.setText("Reservation confirmed!");
    }
}
