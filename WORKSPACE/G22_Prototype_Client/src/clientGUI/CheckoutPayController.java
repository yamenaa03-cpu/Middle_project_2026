package clientGUI;

import client.Client;
import common.dto.Reservation.ReservationResponse;
import common.entity.Reservation;
import common.enums.ReservationOperation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CheckoutPayController {

    private Client client;
    private ClientController mainController;

    private List<Reservation> reservations;
    private Reservation selectedReservation;

    private Object currentBill;   // keep generic: Bill class still comes in resp.getBill()
    private boolean waitingList = false;
    private boolean waitingBill = false;
    private boolean waitingPay = false;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, LocalDateTime> colDateTime;
    @FXML private TableColumn<Reservation, Integer> colGuests;
    @FXML private TableColumn<Reservation, Object> colStatus;
    @FXML private TableColumn<Reservation, Integer> colCode;

    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label guestsLabel;
    @FXML private Label codeLabel;

    @FXML private Label billIdLabel;
    @FXML private Label billAmountLabel;
    @FXML private Label billStatusLabel;

    @FXML private Label statusLabel;
    @FXML private Button loadBillBtn;
    @FXML private Button payBtn;

    public void setClient(Client client) { this.client = client; }
    public void setMainController(ClientController mainController) { this.mainController = mainController; }

    @FXML
    public void initialize() {
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("reservationDateTime"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));

        colDateTime.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
        });

        reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedReservation = n;
                fillDetails(n);
                clearBillUI();
            }
        });

        statusLabel.setText("");
        billStatusLabel.setText("");
        payBtn.setDisable(true);
    }

    /** Call this when subscriber opens the checkout screen */
    public void loadSubscriberPayableReservations() {
        if (client == null || !client.isConnected()) {
            statusLabel.setText("Not connected to server.");
            return;
        }
        waitingList = true;
        statusLabel.setText("Loading reservations...");

        client.requestGetCustomerReservationsForCheckout();
    }

    public void setReservations(List<Reservation> list) {
        this.reservations = list;

        Platform.runLater(() -> {
            reservationTable.getItems().setAll(list == null ? List.of() : list);

            if (list == null || list.isEmpty()) {
                selectedReservation = null;
                statusLabel.setText("No reservations found.");
                clearDetails();
                clearBillUI();
                return;
            }

            reservationTable.getSelectionModel().select(0);
            selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
            fillDetails(selectedReservation);
            statusLabel.setText("");
        });
    }

    @FXML
    private void onLoadBill(ActionEvent e) {
        if (client == null || !client.isConnected()) {
            statusLabel.setText("Not connected to server.");
            return;
        }
        if (selectedReservation == null) {
            statusLabel.setText("Please select a reservation first.");
            return;
        }

        waitingBill = true;
        loadBillBtn.setDisable(true);
        payBtn.setDisable(true);
        billStatusLabel.setText("Loading bill...");

        client.requestGetBillForPaying(selectedReservation.getReservationId());
    }

    @FXML
    private void onPay(ActionEvent e) {
        if (client == null || !client.isConnected()) {
            statusLabel.setText("Not connected to server.");
            return;
        }
        Integer billId = extractBillId(currentBill);
        if (billId == null) {
            statusLabel.setText("Bill not loaded.");
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Payment");
        a.setHeaderText(null);
        a.setContentText("Pay bill #" + billId + "?");
        if (a.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        waitingPay = true;
        payBtn.setDisable(true);
        billStatusLabel.setText("Paying...");

        client.requestPayBill(billId);
    }

    @FXML
    private void onBack(ActionEvent e) {
        closeWindow();
    }

    public void onReservationResponse(ReservationResponse resp) {
        Platform.runLater(() -> {
            if (resp == null || resp.getOperation() == null) return;

            ReservationOperation op = resp.getOperation();

            if (waitingList && op == ReservationOperation.GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT) {
                waitingList = false;
                setReservations(resp.getReservations());
                return;
            }

            if (op == ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CHECKOUT) {
                // guest flow (if you decide to forward here too)
                setReservations(resp.getReservations());
                return;
            }

            if (waitingBill && op == ReservationOperation.GET_BILL_FOR_PAYING) {
                waitingBill = false;
                loadBillBtn.setDisable(false);

                currentBill = resp.getBill(); // ✅ assumes ReservationResponse has getBill()
                Integer billId = extractBillId(currentBill);

                billIdLabel.setText(billId == null ? "" : String.valueOf(billId));
                billAmountLabel.setText(extractBillAmount(currentBill));
                billStatusLabel.setText(resp.getMessage() == null ? "Bill loaded." : resp.getMessage());

                payBtn.setDisable(billId == null);
                return;
            }

            if (waitingPay && op == ReservationOperation.PAY_BILL) {
                waitingPay = false;
                payBtn.setDisable(false);

                String msg = resp.getMessage() == null ? "Done." : resp.getMessage();
                billStatusLabel.setText(msg);

                if (resp.isSuccess()) {
                    closeWindow();
                }
            }
        });
    }

    private void fillDetails(Reservation r) {
        if (r == null) { clearDetails(); return; }
        LocalDateTime dt = r.getReservationDateTime();
        dateLabel.setText(dt == null ? "" : dt.format(DATE_FMT));
        timeLabel.setText(dt == null ? "" : dt.format(TIME_FMT));
        guestsLabel.setText(r.getNumberOfGuests() + " guests");
        codeLabel.setText(String.valueOf(r.getConfirmationCode()));
    }

    private void clearDetails() {
        dateLabel.setText("");
        timeLabel.setText("");
        guestsLabel.setText("");
        codeLabel.setText("");
    }

    private void clearBillUI() {
        currentBill = null;
        billIdLabel.setText("");
        billAmountLabel.setText("");
        billStatusLabel.setText("");
        payBtn.setDisable(true);
        loadBillBtn.setDisable(false);
    }

    private Integer extractBillId(Object bill) {
        if (bill == null) return null;
        for (String m : List.of("getBillId", "getId")) {
            try {
                Method method = bill.getClass().getMethod(m);
                Object val = method.invoke(bill);
                if (val instanceof Number) return ((Number) val).intValue();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String extractBillAmount(Object bill) {
        if (bill == null) return "";
        for (String m : List.of("getTotalAmount", "getTotalPrice", "getAmount", "getTotal")) {
            try {
                Method method = bill.getClass().getMethod(m);
                Object val = method.invoke(bill);
                return String.valueOf(val);
            } catch (Exception ignored) {}
        }
        return bill.toString(); // fallback
    }

    private void closeWindow() {
        Stage stage = (Stage) reservationTable.getScene().getWindow();
        stage.close();
    }
}
