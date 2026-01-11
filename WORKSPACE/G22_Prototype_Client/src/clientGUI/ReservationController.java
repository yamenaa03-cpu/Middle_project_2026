package clientGUI;

import client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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
    private ClientController mainController;
    private LocalDateTime lastRequestedDateTime;
    private int lastRequestedGuests;


    public void setClient(Client client) {
        this.client = client;
    }

    
    public void setMainController(ClientController mainController) {
        this.mainController = mainController;
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
    @FXML private TextField fullNameField;
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
    
    @FXML private Label statusLabel1;

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
        timeComboBox.getItems().clear();

        // 10:00 -> 23:30
        for (int hour = 10; hour <= 23; hour++) {
            timeComboBox.getItems().add(LocalTime.of(hour, 0));
            timeComboBox.getItems().add(LocalTime.of(hour, 30));
        }

        // 00:00 -> 02:00
        timeComboBox.getItems().add(LocalTime.of(0, 0));
        timeComboBox.getItems().add(LocalTime.of(0, 30));
        timeComboBox.getItems().add(LocalTime.of(1, 0));
        timeComboBox.getItems().add(LocalTime.of(1, 30));
        timeComboBox.getItems().add(LocalTime.of(2, 0));
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

        statusLabel.setText("");

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

        lastRequestedDateTime = reservationDateTime;
        lastRequestedGuests = guestCount;

        // ================= MEMBER =================
        if (memberToggle.isSelected()) {

            // ✅ require login
            if (mainController == null || !mainController.isLoggedIn()) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setHeaderText(null);
                a.setContentText("You must log in before making a member reservation.");
                a.show();
                statusLabel.setText("Please login first.");
                return;
            }

            // ✅ send CREATE_RESERVATION (server uses session subscriberId)
            client.requestCreateReservation(0, reservationDateTime, guestCount);
            statusLabel.setText("Checking availability...");
            return;
        }

        // ================= GUEST =================
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            statusLabel.setText("Please enter phone number.");
            return;
        }

        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();

        // ✅ You must send guest info in request, so we need a new request type (next step)
        client.requestCreateGuestReservation(reservationDateTime, guestCount, name, phone, email);

        statusLabel.setText("Checking availability...");
    }
    
    public void onReservationResponse(common.dto.Reservation.ReservationResponse resp) {
        Platform.runLater(() -> {

            if (resp.isSuccess()) {
                // open confirmation page
                openConfirmationPage(resp.getConfirmationCode(), lastRequestedDateTime, lastRequestedGuests);
                return;
            }

            // failed
            if (resp.getSuggestedTimes() != null && !resp.getSuggestedTimes().isEmpty()) {
                LocalDateTime closest = resp.getSuggestedTimes().get(0);
                Platform.runLater(() -> statusLabel1.setText(resp.getMessage() + "\nClosest available: " + closest));
            } else {
            		statusLabel1.setText(resp.getMessage());
            }
        });
    }
    
    private void openConfirmationPage(int confirmationCode, LocalDateTime dt, int guests) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/ReservationPageConfirmation.fxml"));
            Parent root = loader.load();

            ReservationConfirmationController c = loader.getController();
            c.setData(confirmationCode, dt, guests);

            Stage stage = new Stage();
            stage.setTitle("Reservation Confirmation");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

            // optionally close current reservation window:
            Stage current = (Stage) statusLabel.getScene().getWindow();
            current.close();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not open confirmation page.");
        }
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
