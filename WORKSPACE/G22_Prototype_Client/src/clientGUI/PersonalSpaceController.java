package clientGUI;

import client.Client;
import common.dto.Reservation.ReservationResponse;
import common.dto.UserAccount.UserAccountResponse;
import common.entity.Customer;
import common.entity.Reservation;
import common.enums.ReservationOperation;
import common.enums.ReservationStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class PersonalSpaceController {

    private Client client;
    private ClientController mainController;

    public void setClient(Client client) { this.client = client; }
    public void setMainController(ClientController mainController) { this.mainController = mainController; }

    // ====== Personal Info (match FXML fx:id) ======
    @FXML private TextField membershipNumberField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private CheckBox subscribedCheckBox;
    @FXML private Label infoStatusLabel;

    @FXML private Button refreshBtn;
    @FXML private Button logoutBtn;
    @FXML private Button saveBtn;

    // ====== Reservations (match FXML fx:id) ======
    @FXML private TableView<Reservation> activeReservationsTable;
    @FXML private TableColumn<Reservation, LocalDateTime> colA_datetime;
    @FXML private TableColumn<Reservation, Integer> colA_guests;
    @FXML private TableColumn<Reservation, Integer> colA_tableId;
    @FXML private TableColumn<Reservation, LocalDateTime> colA_code;   // createdAt
    @FXML private TableColumn<Reservation, Integer> colA_code1;        // confirmationCode

    @FXML private TableView<Reservation> reservationHistoryTable;
    @FXML private TableColumn<Reservation, LocalDateTime> colH_datetime;
    @FXML private TableColumn<Reservation, Integer> colH_guests;
    @FXML private TableColumn<Reservation, Integer> colH_tableId;
    @FXML private TableColumn<Reservation, LocalDateTime> colH_createdAt;
    @FXML private TableColumn<Reservation, Integer> colH_createdAt1;   // confirmationCode

    @FXML private Button cancelActiveButton;
    @FXML private Button leaveWaitingListButton;
    @FXML private Label reservationStatusLabel;
    @FXML private Label activeCountLabel;

    // ====== Bills (exists in FXML) ======
    @FXML private TableView<?> billsTable;

    private static final EnumSet<ReservationStatus> ACTIVE_STATUSES =
            EnumSet.of(ReservationStatus.ACTIVE, ReservationStatus.NOTIFIED,
                       ReservationStatus.IN_PROGRESS, ReservationStatus.WAITING);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        membershipNumberField.setEditable(false);

        // ---- Active table columns ----
        colA_datetime.setCellValueFactory(new PropertyValueFactory<>("reservationDateTime"));
        colA_guests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        colA_tableId.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        colA_code.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colA_code1.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));

        // ---- History table columns ----
        colH_datetime.setCellValueFactory(new PropertyValueFactory<>("reservationDateTime"));
        colH_guests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        colH_tableId.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        colH_createdAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colH_createdAt1.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));

        // nicer formatting for LocalDateTime columns
        setupDateTimeColumn(colA_datetime);
        setupDateTimeColumn(colA_code);
        setupDateTimeColumn(colH_datetime);
        setupDateTimeColumn(colH_createdAt);

        // tableId can be null -> show "-"
        setupNullableIntColumn(colA_tableId);
        setupNullableIntColumn(colH_tableId);

        // buttons actions (because your FXML didn't set onAction for these)
        cancelActiveButton.setOnAction(this::onCancelActiveReservation);
        leaveWaitingListButton.setOnAction(this::onLeaveWaitingList);

        // disable cancel until a row selected
        cancelActiveButton.setDisable(true);
        activeReservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            cancelActiveButton.setDisable(newV == null);
        });

        infoStatusLabel.setText("");
        reservationStatusLabel.setText("");

    }
    public void onOpen() {
        loadPersonalSpaceData();
    }


    private void loadPersonalSpaceData() {
        if (client == null || !client.isConnected()) {
            showAlert("Not connected to server.");
            return;
        }
        if (mainController == null || !mainController.isLoggedIn()) {
            showAlert("Please login first.");
            return;
        }

        infoStatusLabel.setText("Loading...");
        reservationStatusLabel.setText("Loading...");

        client.requestCustomerProfile();
        // إذا بعدك مش جاهز بهي، علّقها مؤقتاً:
        client.requestCustomerReservations();
    }

    // ===================== RESPONSES FROM MAIN CONTROLLER =====================

    // Profile comes via CustomerAuthResponse (GET_PROFILE / UPDATE_PROFILE)
    public void onAuthResponse(UserAccountResponse resp) {
        Platform.runLater(() -> {
            if (!resp.isSuccess()) {
                infoStatusLabel.setText(resp.getMessage());
                return;
            }
            if (resp.getCustomer() != null) {
                fillCustomer(resp.getCustomer());
                infoStatusLabel.setText(resp.getMessage());
            } else {
                infoStatusLabel.setText(resp.getMessage());
            }
            
        });
    }

    // Reservations come via ReservationResponse
    public void onReservationResponse(ReservationResponse resp) {
        Platform.runLater(() -> {
            reservationStatusLabel.setText(resp.getMessage());

            if (resp.getOperation() == ReservationOperation.GET_SUBSCRIBER_HISTORY) {
                fillReservations(resp.getReservations());
                return;
            }

            if (resp.getOperation() == ReservationOperation.CANCEL_RESERVATION && resp.isSuccess()) {
                client.requestCustomerReservations(); // reload history after cancel
            }
        });
    }

    // ===================== FILL UI =====================

    private void fillCustomer(Customer c) {
        membershipNumberField.setText(String.valueOf(c.getSubscriptionCode()));
        fullNameField.setText(c.getFullName());
        phoneField.setText(c.getPhone());
        emailField.setText(c.getEmail());

        subscribedCheckBox.setSelected(c.isSubscriber());
    }
    
    
    private void fillReservations(List<Reservation> all) {
        if (all == null) all = List.of();

        List<Reservation> active = all.stream()
                .filter(this::isActive)
                .sorted(Comparator.comparing(Reservation::getReservationDateTime))
                .collect(Collectors.toList());

        List<Reservation> history = all.stream()
                .filter(r -> !isActive(r))
                .sorted(Comparator.comparing(Reservation::getReservationDateTime).reversed())
                .collect(Collectors.toList());
        activeCountLabel.setText(String.valueOf(active.size()));
        activeReservationsTable.setItems(FXCollections.observableArrayList(active));
        reservationHistoryTable.setItems(FXCollections.observableArrayList(history));
    }
    private boolean isActive(Reservation r) {
        if (r.getStatus() == null) return false;
        return r.getStatus() == common.enums.ReservationStatus.ACTIVE
            || r.getStatus() == common.enums.ReservationStatus.NOTIFIED
            || r.getStatus() == common.enums.ReservationStatus.IN_PROGRESS
            || r.getStatus() == common.enums.ReservationStatus.WAITING;
    }



    // ===================== BUTTONS (these MUST match FXML onAction) =====================

    @FXML
    private void onRefresh(ActionEvent e) {
        loadPersonalSpaceData();
    }

    @FXML
    private void onSaveChanges(ActionEvent e) {
        if (client == null || !client.isConnected()) return;

        String name = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            showAlert("Full name and phone are required.");
            return;
        }

        infoStatusLabel.setText("Saving...");
        client.requestUpdateCustomerProfile(name, phone, email);
        infoStatusLabel.setText("Personal information Updated 🔄");

    }

    private void onCancelActiveReservation(ActionEvent e) {
        Reservation selected = activeReservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select an active reservation first.");
            return;
        }
        reservationStatusLabel.setText("Canceling reservation...");
        client.requestCancelReservation(selected.getReservationId());
    }

    private void onLeaveWaitingList(ActionEvent e) {
        Reservation waiting = activeReservationsTable.getItems().stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst().orElse(null);

        if (waiting == null) {
            showAlert("You are not in the waiting list.");
            return;
        }

        reservationStatusLabel.setText("Leaving waiting list...");
        client.requestCancelReservation(waiting.getReservationId());
    }

    @FXML
    private void onLogout(ActionEvent e) {
       // if (mainController != null) mainController.logout();
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        stage.close();
    }

    // ===================== HELPERS =====================

    private void setupDateTimeColumn(TableColumn<Reservation, LocalDateTime> col) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : DT_FMT.format(item));
            }
        });
    }

    private void setupNullableIntColumn(TableColumn<Reservation, Integer> col) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : (item == null ? "-" : String.valueOf(item)));
            }
        });
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}
