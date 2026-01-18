package serverGUI;

import dbController.DBController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import server.Server;
import server.ServerUI;
import server.ClientStatusInfo;

/**
 * JavaFX controller for the server configuration and monitoring GUI.
 * <p>
 * This controller manages the server control panel interface, allowing users
 * to:
 * <ul>
 * <li>Configure and establish database connections</li>
 * <li>Start and stop the OCSF server</li>
 * <li>Monitor connected clients in real-time</li>
 * <li>View server status and log messages</li>
 * </ul>
 * Implements {@link ServerUI} to receive callbacks from the server layer.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class ServerFrameController implements ServerUI {

	/**
	 * Static reference to the currently active server instance. Used for cleanup
	 * when the GUI is closed.
	 */
	public static Server activeServer;

	/**
	 * The server instance managed by this controller.
	 */
	private Server server;

	/**
	 * Text field for entering the server port number.
	 */
	@FXML
	private TextField portField;

	/**
	 * Text field for entering the database name.
	 */
	@FXML
	private TextField dbNameField;

	/**
	 * Text field for entering the database username.
	 */
	@FXML
	private TextField dbUserField;

	/**
	 * Text field for entering the database password.
	 */
	@FXML
	private TextField dbPasswordField;

	/**
	 * Label displaying the current server status (RUNNING, STOPPED, ERROR).
	 */
	@FXML
	private Label serverStatusLabel;

	/**
	 * Label displaying the current database connection status.
	 */
	@FXML
	private Label dbStatusLabel;

	/**
	 * Label for displaying log messages and server activity.
	 */
	@FXML
	private Label logLabel;

	/**
	 * TableView displaying connected clients and their status.
	 */
	@FXML
	private TableView<ObservableList<String>> clientTable;

	/**
	 * Table column for client ID.
	 */
	@FXML
	private TableColumn<ObservableList<String>, String> idColumn;

	/**
	 * Table column for client hostname.
	 */
	@FXML
	private TableColumn<ObservableList<String>, String> hostColumn;

	/**
	 * Table column for client IP address.
	 */
	@FXML
	private TableColumn<ObservableList<String>, String> ipColumn;

	/**
	 * Table column for client connection status.
	 */
	@FXML
	private TableColumn<ObservableList<String>, String> statusColumn;

	/**
	 * Initializes the controller after FXML loading.
	 * <p>
	 * Sets up the initial UI state, configures table column cell value factories,
	 * and prepares the client table for displaying connection information.
	 * </p>
	 */
	@FXML
	public void initialize() {
		Platform.setImplicitExit(true);

		serverStatusLabel.setText("STOPPED");
		dbStatusLabel.setText("NOT CONNECTED");

		idColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(0)));
		hostColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(1)));
		ipColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(2)));
		statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(3)));

		clientTable.setItems(FXCollections.observableArrayList());

	}

	/**
	 * Handles the "Start Server" button click event.
	 * <p>
	 * Creates a new server instance with the configured port and database settings,
	 * then starts listening for client connections. Updates status labels to
	 * reflect the current state.
	 * </p>
	 */
	@FXML
	public void onStartServer() {
		try {
			int port = Integer.parseInt(portField.getText().trim());

			server = new Server(port, this);
			activeServer = server;

			server.setDatabaseConfig(dbNameField.getText().trim(), dbUserField.getText().trim(),
					dbPasswordField.getText().trim());

			server.listen();

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

	/**
	 * Handles the "Stop Server" button click event.
	 * <p>
	 * Stops the currently running server and updates the status labels. Does
	 * nothing if no server is currently running.
	 * </p>
	 */
	@FXML
	public void onStopServer() {
		if (getServer() == null)
			return;

		try {
			getServer().close();
			activeServer = null;
			serverStatusLabel.setText("STOPPED");

		} catch (Exception e) {
			serverStatusLabel.setText("ERROR STOPPING");
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Displays the message in the log label on the JavaFX Application Thread.
	 * </p>
	 */
	@Override
	public void display(String message) {
		Platform.runLater(() -> {
			logLabel.setText(message);
		});
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Updates or adds a client row in the client table. If a client with the same
	 * host and IP already exists, updates their ID and status; otherwise, adds a
	 * new row.
	 * </p>
	 */
	@Override
	public void updateClientStatus(String id, String host, String ip, String status) {
		Platform.runLater(() -> {

			for (ObservableList<String> row : clientTable.getItems()) {

				boolean sameClient = row.get(1).equals(host) && row.get(2).equals(ip);

				if (sameClient) {

					row.set(0, id);

					row.set(3, status);

					clientTable.refresh();
					return;
				}
			}

			clientTable.getItems().add(FXCollections.observableArrayList(id, host, ip, status));
			clientTable.refresh();
		});
	}

	/**
	 * Returns the currently managed server instance.
	 *
	 * @return the Server instance, or null if not started
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Sets the server instance for this controller.
	 *
	 * @param server the Server instance to manage
	 */
	public void setServer(Server server) {
		this.server = server;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Displays an informational alert dialog with the specified message. The dialog
	 * is shown asynchronously to avoid blocking the server thread.
	 * </p>
	 */
	@Override
	public void displayMessage(String msg) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Message");
			alert.setHeaderText(null);
			alert.setContentText(msg);
			alert.show();
		});
	}

}
