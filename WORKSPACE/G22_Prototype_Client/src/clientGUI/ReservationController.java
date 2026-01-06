package clientGUI;

import client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.IntStream;

/**
 * ReservationController
 *
 * Controller for the reservation popup.
 * Responsibilities:
 *  - Collect reservation details from UI
 *  - Validate user input
 *  - Send reservation request to server
 *  - Display server feedback
 *
 * @version 1.0
 */
public class ReservationController {

    // ==========================================================
    // SERVER CONNECTION
    // ==========================================================
    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }

    // ==========================================================
    // FXML FIELDS (MATCH FXML IDs)
    // ==========================================================
    @FXML private ToggleButton memberToggle;
    @FXML private ToggleButton guestToggle;
    @FXML private Button customerTypeTag;

    @FXML private VBox memberPane;
    @FXML private VBox guestPane;

    @FXML private TextField membershipCodeField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeComboBox;

    @FXML private Label guestCountLabel;
    @FXML private Label statusLabel;

    @FXML private Button plusGuestButton;
    @FXML private Button minusGuestButton;

    @FXML private Button checkAvailabilityButton;
    @FXML private Hyperlink cancelReservationLink;

    // ==========================================================
    // INTERNAL STATE
    // ==========================================================
    private int guestCount = 2;

    // ==========================================================
    // INITIALIZATION
    // ==========================================================
    @FXML
    public void initialize() {
        setupToggleGroup();
        setupTimeOptions();
        updateGuestCountLabel();
        statusLabel.setText("");
    }

    // ==========================================================
    // TOGGLE MEMBER / GUEST
    // ==========================================================
    private void setupToggleGroup() {
        ToggleGroup group = new ToggleGroup();
        memberToggle.setToggleGroup(group);
        guestToggle.setToggleGroup(group);

        memberToggle.setSelected(true);
        switchToMember();

        group.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == memberToggle) {
                switchToMember();
            } else {
                switchToGuest();
            }
        });
    }

    private void switchToMember() {
        memberPane.setVisible(true);
        memberPane.setManaged(true);

        guestPane.setVisible(false);
        guestPane.setManaged(false);

        customerTypeTag.setText("Member");
    }

    private void switchToGuest() {
        memberPane.setVisible(false);
        memberPane.setManaged(false);

        guestPane.setVisible(true);
        guestPane.setManaged(true);

        customerTypeTag.setText("Guest");
    }

    // ==========================================================
    // TIME COMBOBOX SETUP
    // ==========================================================
    private void setupTimeOptions() {
        IntStream.rangeClosed(10, 22)
                .mapToObj(hour -> LocalTime.of(hour, 0))
                .forEach(timeComboBox.getItems()::add);
    }

    // ==========================================================
    // GUEST COUNT HANDLING
    // ==========================================================
    @FXML
    private void onIncreaseGuests() {
        if (guestCount < 10) {
            guestCount++;
            updateGuestCountLabel();
        }
    }

    @FXML
    private void onDecreaseGuests() {
        if (guestCount > 1) {
            guestCount--;
            updateGuestCountLabel();
        }
    }

    private void updateGuestCountLabel() {
        guestCountLabel.setText(String.valueOf(guestCount));
    }

    // ==========================================================
    // MAIN ACTION: CHECK AVAILABILITY
    // ==========================================================
    @FXML
    private void onCheckAvailability() {

        if (client == null || !client.isConnected()) {
            statusLabel.setText("Not connected to server.");
            return;
        }

        if (datePicker.getValue() == null || timeComboBox.getValue() == null) {
            statusLabel.setText("Please select date and time.");
            return;
        }

        LocalDate date = datePicker.getValue();
        LocalTime time = timeComboBox.getValue();
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time);

        if (memberToggle.isSelected()) {
            String code = membershipCodeField.getText().trim();
            if (code.isEmpty()) {
                statusLabel.setText("Please enter membership code.");
                return;
            }
            client.requestCreateReservation(guestCount, reservationDateTime, guestCount);
        } else {
            String phone = phoneField.getText().trim();
            if (phone.isEmpty()) {
                statusLabel.setText("Please enter phone number.");
                return;
            }
            client.requestCreateReservation(guestCount, reservationDateTime, guestCount);
        }

        statusLabel.setText("Checking availability...");
    }

    // ==========================================================
    // SERVER FEEDBACK
    // ==========================================================
    public void displayStatus(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }

    // ==========================================================
    // CANCEL / CLOSE POPUP
    // ==========================================================
    @FXML
    private void onCancelReservation() {
        Stage stage = (Stage) cancelReservationLink
                .getScene()
                .getWindow();
        stage.close();
    }
}
