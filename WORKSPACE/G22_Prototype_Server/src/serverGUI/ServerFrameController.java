package serverGUI;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import server.Server;
import server.ServerUI;
import serverDbController.ServerController;
import server.ClientStatusInfo;

public class ServerFrameController implements ServerUI {

    public static Server activeServer; // For closing when GUI exits

    private Server server;

    @FXML private TextField portField;
    @FXML private TextField dbNameField;
    @FXML private TextField dbUserField;
    @FXML private TextField dbPasswordField;


    @FXML private Label serverStatusLabel;
    @FXML private Label dbStatusLabel;
    
    @FXML private TableView<ObservableList<String>> clientTable;

    @FXML private TableColumn<ObservableList<String>, String> idColumn;
    @FXML private TableColumn<ObservableList<String>, String> hostColumn;
    @FXML private TableColumn<ObservableList<String>, String> ipColumn;
    @FXML private TableColumn<ObservableList<String>, String> statusColumn;


    @FXML
    public void initialize() {
        Platform.setImplicitExit(true);

        serverStatusLabel.setText("STOPPED");
        dbStatusLabel.setText("NOT CONNECTED");
        /*For each column, read the correct property from each row.
         * for example :For the ID column
						Take each row c
						Get the row's object with c.getValue()
						Then get the row's idProperty()
						And display its value in that cell*/
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(0)));
        hostColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(1)));
        ipColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(2)));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(3)));

        clientTable.setItems(FXCollections.observableArrayList());

    }

    @FXML
    public void onStartServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());

            // set server
            server = new Server(port, this);
            activeServer = server;
            //set DB
            server.setDatabaseConfig(
                dbNameField.getText().trim(),
                dbUserField.getText().trim(),
                dbPasswordField.getText().trim()
            );

            server.listen();
            //validates that the server is intelized and its watinig for client 
            if (server.isListening()) {
                System.out.println("Server is now listening.");
            } else {
                System.out.println("Server failed to start listening.");
            }
            serverStatusLabel.setText("RUNNING");
            dbStatusLabel.setText("CONNECTED");

        } catch (Exception e) {
            serverStatusLabel.setText("ERROR");
            dbStatusLabel.setText("FAILED");
        }
    }


    @FXML
    public void onStopServer() {
        if (getServer() == null) return;

        try {
            getServer().close();
            activeServer = null;
            serverStatusLabel.setText("STOPPED");

        } catch (Exception e) {
            serverStatusLabel.setText("ERROR STOPPING");
        }
    }

    @Override
    public void display(String message) {
        // Log removed intentionally
    }

    @Override
    public void updateClientStatus(String id, String host, String ip, String status) {
        Platform.runLater(() -> {

            // if this host+ip already exists in the table
            for (ObservableList<String> row : clientTable.getItems()) {

                boolean sameClient =
                    row.get(1).equals(host) &&
                    row.get(2).equals(ip);  // match based on machine

                if (sameClient) {

                    // Update the ID 
                    row.set(0, id);

                    // Update status
                    row.set(3, status);

                    clientTable.refresh();
                    return;
                }
            }

            // If client didn't exist add as new row
            clientTable.getItems().add(
                    FXCollections.observableArrayList(id, host, ip, status)
            );
            clientTable.refresh();
        });
    }



	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}


}
