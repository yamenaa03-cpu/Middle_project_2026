package clientGUI;

import client.Client;
import client.ClientUI;
import common.Order;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.LocalDate;
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
 * with the GUI through displayMessage() and displayOrders().
 *
 * @version 1.0
 */
public class ClientFrameController implements ClientUI {

    private Client client;   //Client object that handles clients activity 
    //UI COMPONENTS linked from FXML
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colNumber;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, Integer> colGuests;
    @FXML private TableColumn<Order, Integer> colConf;
    @FXML private TableColumn<Order, Integer> colSub;
    @FXML private TableColumn<Order, String> colPlaced;

    @FXML private TextField updateOrderField;
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

        new PropertyValueFactory<>("orderNumber") Look inside each Order object, 
        Call getOrderNumber() and Puts that value in the column cell
        */
        colNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        colConf.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colSub.setCellValueFactory(new PropertyValueFactory<>("subscriberId"));
        colPlaced.setCellValueFactory(new PropertyValueFactory<>("dateOfPlacing"));

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

                // First notify server cleanly
                client.sendToServer("CLIENT_EXITING");

                Thread.sleep(50); // tiny delay to allow clean close
                // thread sleep was used here to allow the socket of the client to close prepertly
                // Now close socket
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
    // LOAD ORDERS
    // ==========================================================
    @FXML
    public void onLoadOrders() {
        if (client == null || !client.isConnected()) {
            messageArea.appendText("Not connected to server.\n");
            return;
        }

        client.requestAllOrders();
    }

    // UPDATE ORDER
    // Sends a request to update a specific order
    @FXML
    public void onUpdate() {
        if (client == null || !client.isConnected()) {
            messageArea.appendText("Not connected to server.\n");
            return;
        }

        try {
            int orderNum = Integer.parseInt(updateOrderField.getText().trim());
            LocalDate newDate = LocalDate.parse(updateDateField.getText().trim());
            int guests = Integer.parseInt(updateGuestsField.getText().trim());

            client.requestUpdateOrder(orderNum, newDate, guests);

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
    // Replaces table contents with the list of orders from the server 
    @Override
    public void displayOrders(List<Order> orders) {
        Platform.runLater(() -> {
            ordersTable.getItems().clear();
            ordersTable.getItems().addAll(orders);
        });
    }



	
}
