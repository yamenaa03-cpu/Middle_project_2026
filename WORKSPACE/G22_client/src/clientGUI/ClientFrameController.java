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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import common.entity.OpeningHours;
import common.dto.RestaurantManagement.RestaurantManagementResponse;

/**
 * ReservationController manages the reservation creation popup.
 * 
 * <p>This controller handles the complete reservation workflow for both
 * subscribers (members) and guests, including date/time selection, party
 * size, and availability checking.
 * 
 * <p>Key Features:
 * <ul>
 *   <li><b>Dynamic Time Slots</b> - Populates available times based on restaurant opening hours</li>
 *   <li><b>Overnight Hours Support</b> - Handles restaurants open past midnight with "(+1 day)" suffix</li>
 *   <li><b>Member/Guest Toggle</b> - Switches between subscriber login and guest details entry</li>
 *   <li><b>Availability Check</b> - Validates table availability before confirming</li>
 *   <li><b>Guest Count Management</b> - +/- buttons for party size selection</li>
 * </ul>
 * 
 * <p>Time Slot Logic:
 * <ul>
 *   <li>Time slots are generated from opening time to (closing time - 2 hours)</li>
 *   <li>Minimum 2-hour window required for reservations</li>
 *   <li>After-midnight slots display with "(+1 day)" suffix</li>
 *   <li>Closed days show "Restaurant Closed" message</li>
 * </ul>
 * 
 * @author G22 Team
 * @version 1.0
 * @see ClientController
 * @see OpeningHours
 * @see ReservationConfirmationController
 */
public class ReservationController {

    // ==========================================================
    // SERVER CONNECTION
    // ==========================================================
    private Client client;
    private ClientController mainController;
    private LocalDateTime lastRequestedDateTime;
    private int lastRequestedGuests;
    
    /** Cached list of opening hours from server */
    private List<OpeningHours> cachedOpeningHours;

    public void setClient(Client client) {
        this.client = client;
        // Request opening hours when client is set
        if (client != null && client.isConnected()) {
            client.LoadOpeningHoursRequest();
        }
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
    @FXML private ComboBox<String> timeComboBox;

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
        setupDefaultTimeOptions(); // Show placeholder until opening hours are loaded
        updateGuestCountLabel();
        statusLabel.setText("");
        
        // Add listener to datePicker to update time options when date changes
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && cachedOpeningHours != null) {
                updateTimeOptionsForDate(newDate);
            }
        });
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
    
    /**
     * Shows default time options (placeholder) until opening hours are loaded from server.
     */
    private void setupDefaultTimeOptions() {
        timeComboBox.getItems().clear();
        timeComboBox.setPromptText("Select date first");
        timeComboBox.setDisable(true);
    }
    
    /**
     * Updates the time ComboBox based on the selected date's opening hours.
     * Last selectable time is 2 hours before closing (reservation duration is 2 hours).
     * Handles overnight hours (when close time is after midnight, e.g., 10:00 to 02:00).
     * For overnight hours, times after midnight are displayed with "(+1 day)" suffix.
     * 
     * @param selectedDate the date selected by the user
     */
    private void updateTimeOptionsForDate(LocalDate selectedDate) {
        timeComboBox.getItems().clear();
        
        if (cachedOpeningHours == null || cachedOpeningHours.isEmpty()) {
            timeComboBox.setPromptText("Loading hours...");
            timeComboBox.setDisable(true);
            return;
        }
        
        DayOfWeek dayOfWeek = selectedDate.getDayOfWeek();
        
        // Find opening hours for this day
        OpeningHours hoursForDay = null;
        for (OpeningHours oh : cachedOpeningHours) {
            if (oh.getDayOfWeek() == dayOfWeek) {
                hoursForDay = oh;
                break;
            }
        }
        
        // If no hours found or restaurant is closed
        if (hoursForDay == null || hoursForDay.isClosed()) {
            timeComboBox.setPromptText("Closed on this day");
            timeComboBox.setDisable(true);
            statusLabel.setText("The restaurant is closed on " + dayOfWeek + ".");
            return;
        }
        
        LocalTime openTime = hoursForDay.getOpenTime();
        LocalTime closeTime = hoursForDay.getCloseTime();
        
        // Use LocalDateTime to handle overnight hours correctly
        LocalDateTime openDateTime = LocalDateTime.of(selectedDate, openTime);
        LocalDateTime closeDateTime;
        boolean isOvernight = false;
        
        // Check if close time is on the next day (overnight hours)
        // closeTime < openTime means close is after midnight
        if (closeTime.isBefore(openTime)) {
            closeDateTime = LocalDateTime.of(selectedDate.plusDays(1), closeTime);
            isOvernight = true;
        } else if (closeTime.equals(openTime)) {
            // If open == close, treat as closed (0 hours open)
            timeComboBox.setPromptText("Closed on this day");
            timeComboBox.setDisable(true);
            statusLabel.setText("The restaurant is closed on " + dayOfWeek + ".");
            return;
        } else {
            closeDateTime = LocalDateTime.of(selectedDate, closeTime);
        }
        
        // Calculate last reservable time (2 hours before closing)
        LocalDateTime lastReservableDateTime = closeDateTime.minusHours(2);
        
        // Handle edge case: if lastReservableDateTime is before openDateTime, no slots
        if (lastReservableDateTime.isBefore(openDateTime)) {
            timeComboBox.setPromptText("No available slots");
            timeComboBox.setDisable(true);
            statusLabel.setText("No reservation slots available (restaurant open less than 2 hours).");
            return;
        }
        
        // Generate time slots in 30-minute increments from open to lastReservable
        // For overnight hours, append "(+1 day)" to times after midnight
        LocalDateTime current = openDateTime;
        while (!current.isAfter(lastReservableDateTime)) {
            LocalTime slotTime = current.toLocalTime();
            
            // Check if this slot is after midnight (next day)
            if (isOvernight && slotTime.isBefore(openTime)) {
                // Display with next day indicator
                String displayText = slotTime.toString() + " (+1 day)";
                timeComboBox.getItems().add(displayText);
            } else {
                timeComboBox.getItems().add(slotTime.toString());
            }
            current = current.plusMinutes(30);
        }
        
        if (timeComboBox.getItems().isEmpty()) {
            timeComboBox.setPromptText("No available slots");
            timeComboBox.setDisable(true);
            return;
        }
        
        timeComboBox.setDisable(false);
        timeComboBox.setPromptText("Select time");
        statusLabel.setText("");
    }
    
    /**
     * Gets the actual reservation LocalDateTime based on selected date and time.
     * Handles overnight hours by adding a day if the time is after midnight.
     * 
     * @return the correct LocalDateTime for the reservation
     */
    public LocalDateTime getReservationDateTime() {
        LocalDate selectedDate = datePicker.getValue();
        Object selectedItem = timeComboBox.getValue();
        
        if (selectedDate == null || selectedItem == null) {
            return null;
        }
        
        String timeStr = selectedItem.toString();
        boolean isNextDay = timeStr.contains("(+1 day)");
        
        // Parse the time, removing the next day indicator if present
        String cleanTimeStr = timeStr.replace(" (+1 day)", "");
        LocalTime selectedTime = LocalTime.parse(cleanTimeStr);
        
        // Add a day if this is an after-midnight slot
        if (isNextDay) {
            return LocalDateTime.of(selectedDate.plusDays(1), selectedTime);
        }
        
        return LocalDateTime.of(selectedDate, selectedTime);
    }
    
    /**
     * Handles the opening hours response from the server.
     * Caches the hours and updates time options if a date is already selected.
     * 
     * @param resp the restaurant management response containing opening hours
     */
    public void onOpeningHoursResponse(RestaurantManagementResponse resp) {
        Platform.runLater(() -> {
            if (resp.isSuccess() && resp.getOpeningHours() != null) {
                this.cachedOpeningHours = resp.getOpeningHours();
                System.out.println("Opening hours loaded: " + cachedOpeningHours.size() + " days");
                
                // If a date is already selected, update the time options
                if (datePicker.getValue() != null) {
                    updateTimeOptionsForDate(datePicker.getValue());
                }
            } else {
                statusLabel.setText("Could not load opening hours.");
            }
        });
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

        // Use getReservationDateTime() which handles overnight hours correctly
        LocalDateTime reservationDateTime = getReservationDateTime();
        if (reservationDateTime == null) {
            statusLabel.setText("Invalid date/time selection.");
            return;
        }

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
