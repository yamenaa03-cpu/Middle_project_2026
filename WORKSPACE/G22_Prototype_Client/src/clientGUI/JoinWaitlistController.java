package clientGUI;

import java.io.IOException;
import java.time.LocalDateTime;

import client.Client;
import common.dto.Reservation.ReservationResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JoinWaitlistController {
	private BistroKioskController KC;
	private Client client;
	
	public void setActiveBistroKioskController(BistroKioskController KC) {
		this.KC = KC;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	
	@FXML TextField fullNameField;
	@FXML TextField phoneField;
	@FXML TextField emailField;
	@FXML private Spinner<Integer> guestsSpinner;
	@FXML Label Status;
	
	

	@FXML
	public void initialize() {
	    guestsSpinner.setValueFactory(
	        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1)
	    );
	    guestsSpinner.setEditable(false);
	}

    
    @FXML
    private void onClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onClear(ActionEvent event) {
        // TODO implement later
    }

    @FXML
    private void onJoinWaitlist(ActionEvent event) {
    	 if (KC == null || client == null) return;

    	    Integer guestsVal = (Integer) guestsSpinner.getValue();
    	    int guestNum = (guestsVal == null ? 1 : guestsVal);

    	    if (KC.getMainController().isLoggedIn()) {
    	        client.requestWaitingListForSub(guestNum);
    	        return;
    	    }

    	    // Guest only:
    	    if (fullNameField == null || phoneField == null || emailField == null) {
    	        Status.setText("Guest fields are missing in this FXML.");
    	        return;
    	    }

    	    String name = fullNameField.getText().trim();
    	    String phone = phoneField.getText().trim();
    	    String email = emailField.getText().trim();
    			client.requestWaitingListForGuest(guestNum, name, phone, email);;
    		}
    	

    public void onReservationResponse(ReservationResponse resp) {
        if (resp == null || !resp.isSuccess()) return;

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientGUI/ReservationPageConfirmation.fxml")
                );

                Parent root = loader.load(); // ✅ root هون

                ReservationConfirmationController controller = loader.getController(); // ✅ controller من الـFXML
                controller.setData(resp.getConfirmationCode(), null, 0);

                Stage st = new Stage();
                st.setTitle("Reservation Confirmation");
                st.setScene(new Scene(root));
                st.setResizable(false);
                st.show(); // أو showAndWait()

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
