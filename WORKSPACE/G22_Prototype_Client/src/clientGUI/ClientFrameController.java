package clientGUI;

import client.Client;
import client.ClientUI;
import common.entity.Reservation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ClientFrameController
 *
 * This class is the JavaFX controller for the Client GUI.
 * It acts as the "view controller" that:
 *  - Reads user actions from the GUI
 *  - Sends requests to the Client 
 *  - Updates the interface with server responses
 *
 * It implements ClientUI  meaning the server can communicate
 * with the GUI through displayMessage() and displayReservations().
 *
 * @version 1.0
 */
public class ClientFrameController implements ClientUI {

    private Client client;   //Client object that handles clients activity 
    //UI COMPONENTS linked from FXML
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;

    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, Integer> colNumber;
    @FXML private TableColumn<Reservation, String> colDate;
    @FXML private TableColumn<Reservation, Integer> colGuests;
    @FXML private TableColumn<Reservation, Integer> colConf;
    @FXML private TableColumn<Reservation, Integer> colSub;
    @FXML private TableColumn<Reservation, String> colCreated;

    @FXML private TextField updateReservationField;
    @FXML private TextField updateDateField;
    @FXML private TextField updateGuestsField;

    @FXML private TextArea messageArea;


    /** Initialization
    Called automatically when the GUI loads*/
    @FXML
    public void initialize() {

        // Try auto-fill IP
        try {
            String myIp = java.net.InetAddress.getLocalHost().getHostAddress();
            ipField.setText(myIp);
        } catch (Exception ignored) {}

        // Setup table columns
        /**        setCellValueFactory(...)  tell the column how to extract the value.

        new PropertyValueFactory<>("reservationNumber") Look inside each Reservation object, 
        Call getReservationNumber() and Puts that value in the column cell
        */
        colNumber.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        colDate.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getReservationDateTime();
            String text = (dt == null) ? "" : dt.format(dtf);
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        colConf.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colSub.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        
        colCreated.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getCreatedAt();
            String text = (dt == null) ? "" : dt.format(dtf);
            return new javafx.beans.property.SimpleStringProperty(text);
        });


        statusLabel.setText("DISCONNECTED");//default client status
    }

    // ==========================================================
    // CONNECT
    // ==========================================================
    @FXML
    public void onConnect() {
        try {
            String ip = ipField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());

            //creats client and connects
            client = new Client(ip, port, this);
            client.openConnection();

            statusLabel.setText("CONNECTED");
            messageArea.appendText("Connected to server.\n");

        } catch (Exception e) {
            messageArea.appendText("Connection failed: " + e.getMessage() + "\n");
        }
        
    }

    // ==========================================================
    // DISCONNECT (THIS FIXES THE SERVER GUI ISSUE)
    // ==========================================================
    @FXML
    public void onDisconnect() {

        try {
            if (client != null && client.isConnected()) {

                client.closeConnection();
            }

            statusLabel.setText("DISCONNECTED");
            messageArea.appendText("Disconnected.\n");

        } catch (Exception e) {
            e.printStackTrace();
            messageArea.appendText("Disconnect error: " + e.getMessage() + "\n");
        }
    }


    // ==========================================================
    // LOAD RSERVATIONS
    // ==========================================================
    @FXML
    public void onLoadReservations() {
        if (client == null || !client.isConnected()) {
            messageArea.appendText("Not connected to server.\n");
            return;
        }

        client.requestAllReservations();
    }

    // UPDATE RESERVATION
    // Sends a request to update a specific reservation
    @FXML
    public void onUpdate() {
        if (client == null || !client.isConnected()) {
            messageArea.appendText("Not connected to server.\n");
            return;
        }

        try {
            int reservationNum = Integer.parseInt(updateReservationField.getText().trim());
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            LocalDateTime newDateTime =
                    LocalDateTime.parse(updateDateField.getText().trim(), formatter);
            int guests = Integer.parseInt(updateGuestsField.getText().trim());

            client.requestUpdateReservation(reservationNum, newDateTime, guests);

        } catch (Exception e) {
            messageArea.appendText("Invalid input: " + e.getMessage() + "\n");
        }
    }

    //Shows a text message in the GUI log area 
    @Override
    public void displayMessage(String msg) {
        Platform.runLater(() ->
            messageArea.appendText("[SERVER] " + msg + "\n")
        );
    }
    // Replaces table contents with the list of reservations from the server 
    @Override
    public void displayReservations(List<Reservation> reservations) {
        Platform.runLater(() -> {
            reservationsTable.getItems().clear();
            reservationsTable.getItems().addAll(reservations);
        });
    }



	
}
