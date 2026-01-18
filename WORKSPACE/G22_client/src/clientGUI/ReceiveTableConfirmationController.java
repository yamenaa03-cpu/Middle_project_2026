        package clientGUI;

        import client.Client;
        import common.dto.Reservation.ReservationResponse;
        import common.entity.Reservation;
        import common.enums.ReservationStatus;
        import javafx.application.Platform;
        import javafx.event.ActionEvent;
        import javafx.fxml.FXML;
        import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;
        import javafx.scene.input.MouseEvent;
        import javafx.scene.layout.VBox;
        import javafx.stage.Stage;

        import java.time.LocalDateTime;
        import java.time.format.DateTimeFormatter;
        import java.util.EnumSet;
        import java.util.List;
        import java.util.stream.Collectors;

/**
 * ReceiveTableConfirmationController displays reservation details before table check-in.
 * 
 * <p>This controller shows the customer their reservation information and
 * allows them to confirm they are ready to be seated at their table.
 * 
 * <p>Features:
 * <ul>
 *   <li>Display reservation date, time, and guest count</li>
 *   <li>Show multiple receivable reservations if applicable</li>
 *   <li>Filter to only show NOTIFIED status reservations</li>
 *   <li>Complete the receive table process</li>
 * </ul>
 * 
 * @author G22 Team
 * @version 1.0
 * @see ReceiveTableController
 * @see Reservation
 */
public class ReceiveTableConfirmationController {



            private Client client;
            private ClientController mainController;

            private List<Reservation> reservations;
            private Reservation selectedReservation;
            private boolean waitingCancelResponse = false;

            private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

            private static final EnumSet<ReservationStatus> RECEIVABLE =
                    EnumSet.of(ReservationStatus.NOTIFIED);

            @FXML private Label dateLabel;
            @FXML private Label timeLabel;
            @FXML private Label guestsLabel;

            @FXML private Button goBackBtn;
            @FXML private Button receiveTbllBtn;
            @FXML private Label statusLabel;
            @FXML private VBox tableBox;
            @FXML private TableView<Reservation> reservationTable;
            @FXML private TableColumn<Reservation, LocalDateTime> colDateTime;
            @FXML private TableColumn<Reservation, Integer> colGuests;
            @FXML private TableColumn<Reservation, ReservationStatus> colStatus;
            @FXML private TableColumn<Reservation, Integer> colCode;

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

                reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null) {
                        selectedReservation = newV;
                        fillDetails(newV);
                    }
                });

                statusLabel.setText("");
                receiveTbllBtn.setDisable(true);
            }

            public void setReservation(Reservation r) {
                setReservations(r == null ? List.of() : List.of(r));
            }

            public void setReservations(List<Reservation> list) {
                List<Reservation> filtered = (list == null ? List.<Reservation>of() : list).stream()
                        .filter(this::isReceivable)
                        .collect(Collectors.toList());

                this.reservations = filtered;

                Platform.runLater(() -> {
                    reservationTable.getItems().setAll(filtered);

                    boolean showTable = filtered.size() > 1;
                    tableBox.setManaged(showTable);
                    tableBox.setVisible(showTable);

                    if (filtered.isEmpty()) {
                        selectedReservation = null;
                        dateLabel.setText("");
                        timeLabel.setText("");
                        guestsLabel.setText("");
                        receiveTbllBtn.setDisable(true);
                        statusLabel.setText("No cancellable reservations found.");
                        return;
                    }

                    receiveTbllBtn.setDisable(false);

                    reservationTable.getSelectionModel().select(0);
                    selectedReservation = filtered.get(0);
                    fillDetails(selectedReservation);
                    statusLabel.setText("");
                });
            }

            private boolean isReceivable(Reservation r) {
                return r != null && r.getStatus() != null && RECEIVABLE.contains(r.getStatus());
            }

            private void fillDetails(Reservation r) {
                LocalDateTime dt = r.getReservationDateTime();
                if (dt == null) {
                    dateLabel.setText("");
                    timeLabel.setText("");
                } else {
                    dateLabel.setText(dt.format(DATE_FMT));
                    timeLabel.setText(dt.format(TIME_FMT));
                }
                guestsLabel.setText(r.getNumberOfGuests() + " guests");
            }

            @FXML
            private void onBackToMenu(MouseEvent event) {
                closeWindow();
            }

            @FXML
            private void onGoBack(ActionEvent event) {
                closeWindow();
            }

            @FXML
            private void onReceiveTable(ActionEvent event) {
                if (client == null || !client.isConnected()) {
                    statusLabel.setText("Not connected to server.");
                    return;
                }
                if (selectedReservation == null) {
                    statusLabel.setText("Please select a reservation first.");
                    return;
                }

                waitingCancelResponse = true;
                receiveTbllBtn.setDisable(true);
                statusLabel.setText("Canceling reservation...");

                // send both reservationId 
                client.requestReceiveTable(
                        selectedReservation.getReservationId()
                );
                closeWindow();
            }

            public void onReservationResponse(ReservationResponse resp) {
                Platform.runLater(() -> {
                    if (!waitingCancelResponse) return;

                    waitingCancelResponse = false;
                    receiveTbllBtn.setDisable(false);

                    statusLabel.setText(resp.getMessage() == null ? "Done." : resp.getMessage());

                    if (resp.isSuccess()) {
                        closeWindow();
                    }
                });
            }

            private void closeWindow() {
                Stage stage = (Stage) receiveTbllBtn.getScene().getWindow();
                stage.close();
            }

        }


