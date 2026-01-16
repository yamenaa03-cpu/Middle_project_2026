	package clientGUI;

	import client.Client;
	import common.dto.Reservation.ReservationResponse;
	import common.entity.Reservation;
	import javafx.application.Platform;
	import javafx.fxml.FXML;
	import javafx.fxml.FXMLLoader;
	import javafx.scene.Parent;
	import javafx.scene.Scene;
	import javafx.scene.control.*;
	import javafx.stage.Modality;
	import javafx.stage.Stage;

	import java.io.IOException;
	import java.util.List;

public class ReceiveTableController {


	

	    private Client client;
	    private BistroKioskController KioskController;

	    public void setClient(Client client) { this.client = client; }
	    public void setBistroKioskController(BistroKioskController mainController) { this.KioskController = mainController; }

	    @FXML private TextField codeField;
	    @FXML private Label statusLabel;
	    @FXML private Button continueBtn;
	    @FXML private Button forgotCodeBtn;

	    private boolean waitingForReservationByCode = false;

		public void setActiveBistroKioskController(BistroKioskController bistroKioskController) {
			this.KioskController = bistroKioskController;
		}
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

	        waitingForReservationByCode = true;
	        continueBtn.setDisable(true);
	        codeField.setDisable(true);
	        statusLabel.setText("Loading reservation...");

	        client.requestGetReceivableReservationByConfirmationCode(code);
	    }

	    @FXML
	    private void onForgotCode() {
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/ForgotConfirmationCode.fxml"));
	            Parent root = loader.load();

	            ForgotConfirmationCodeController ctrl = loader.getController();
	            ctrl.setClient(client);
	            ctrl.setMainController(KioskController.getMainController());

	            Stage popup = new Stage();
	            popup.initModality(Modality.APPLICATION_MODAL);
	            popup.setTitle("Forgot Code");
	            popup.setScene(new Scene(root));
	            popup.setResizable(false);
	            popup.showAndWait();

	        } catch (IOException e) {
	            e.printStackTrace();
	            statusLabel.setText("Failed to open forgot code screen.");
	        }
	    }

	    
	    public void onReservationResponse(ReservationResponse resp) {
	        Platform.runLater(() -> {
	            if (!waitingForReservationByCode) return;

	            waitingForReservationByCode = false;
	            continueBtn.setDisable(false);
	            codeField.setDisable(false);

	            List<Reservation> list = resp.getReservations();
	            if (list == null || list.isEmpty()) {
	                statusLabel.setText(resp.getMessage() == null ? "No reservations found." : resp.getMessage());
	                return;
	            }

	            Reservation r = list.get(0);
	            openConfirmScreen(r);
	        });
	    }

	    private void openConfirmScreen(Reservation r) {
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/ReceiveTableConfirmation.fxml"));
	            Parent root = loader.load();

	            ReceiveTableConfirmationController RTCC = loader.getController();
	            RTCC.setClient(client);
	            RTCC.setMainController(KioskController.getMainController());
	            RTCC.setReservation(r);

	            KioskController.setReceiveTableConfController(RTCC);
	            Stage stage = (Stage) continueBtn.getScene().getWindow();
	            stage.setScene(new Scene(root));

	        } catch (IOException e) {
	            e.printStackTrace();
	            statusLabel.setText("Failed to open confirmation screen.");
	        }
	    }

	    private void closeThisWindow() {
	        Stage stage = (Stage) continueBtn.getScene().getWindow();
	        stage.close();
	    }

	}



