package clientGUI;

import client.Client;
import common.dto.Reservation.ReservationResponse;
import common.entity.Reservation;
import common.enums.ReservationOperation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class CheckoutGuestController {

    private Client client;
    private ClientController mainController;

    public void setClient(Client client) { this.client = client; }
    public void setMainController(ClientController mainController) { this.mainController = mainController; }

    @FXML private TextField codeField;
    @FXML private Label statusLabel;
    @FXML private Button continueBtn;

    private boolean waitingByCode = false;

    @FXML
    private void onBackToMenu() {
        closeThisWindow();
    }

    @FXML
    private void onContinue() {
        statusLabel.setText("");

        if (client == null || !client.isConnected()) {
            statusLabel.setText("Not connected to server.");
            return;
        }

        int code;
        try {
            code = Integer.parseInt(codeField.getText().trim());
        } catch (Exception e) {
            statusLabel.setText("Please enter a valid number.");
            return;
        }

        waitingByCode = true;
        continueBtn.setDisable(true);
        codeField.setDisable(true);
        statusLabel.setText("Loading reservation...");

        // ✅ you need this method in Client (or rename to your existing one)
        client.requestGetReservationForCheckoutByConfirmationCode(code);
    }

    public void onReservationResponse(ReservationResponse resp) {
        Platform.runLater(() -> {
            if (!waitingByCode) return;
            if (resp == null || resp.getOperation() == null) return;
            if (resp.getOperation() != ReservationOperation.GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CHECKOUT) return;

            waitingByCode = false;
            continueBtn.setDisable(false);
            codeField.setDisable(false);

            List<Reservation> list = resp.getReservations();
            if (list == null || list.isEmpty()) {
                statusLabel.setText(resp.getMessage() == null ? "No reservations found." : resp.getMessage());
                return;
            }

            openPayScreen(list);
        });
    }

    private void openPayScreen(List<Reservation> reservations) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/CheckoutPay.fxml"));
            Parent root = loader.load();

            CheckoutPayController ctrl = loader.getController();
            ctrl.setClient(client);
            ctrl.setMainController(mainController);
            ctrl.setReservations(reservations); // guest arrives with 1 reservation

            // switch scene in same popup window
            Stage stage = (Stage) continueBtn.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open checkout screen.");
        }
    }

    private void closeThisWindow() {
        Stage stage = (Stage) continueBtn.getScene().getWindow();
        stage.close();
    }
}
