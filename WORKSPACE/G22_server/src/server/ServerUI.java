
package server;

/**
 * Interface for sending messages and client connection updates to the server
 * GUI.
 * <p>
 * This interface defines the contract between the server logic and the
 * graphical user interface, enabling the server to communicate status updates,
 * log messages, and client connection information to the UI layer without tight
 * coupling.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public interface ServerUI {

	/**
	 * Displays a log message in the server console or log area.
	 * <p>
	 * This method is typically used for status updates and general server activity
	 * logging that doesn't require user acknowledgment.
	 * </p>
	 *
	 * @param msg the message to display in the log area
	 */
	void display(String msg);

	/**
	 * Displays a message to the user, typically in a dialog or alert box.
	 * <p>
	 * This method is used for important notifications that should be brought to the
	 * user's attention, such as notification delivery confirmations or error
	 * alerts.
	 * </p>
	 *
	 * @param msg the message to display to the user
	 */
	void displayMessage(String msg);

	/**
	 * Updates the status of a connected client in the client tracking table.
	 * <p>
	 * This method is called when a client connects, disconnects, or when their
	 * connection status changes. The UI should update the corresponding row in the
	 * client status table or add a new row if the client is new.
	 * </p>
	 *
	 * @param id     the unique identifier assigned to the client connection
	 * @param host   the hostname of the connected client
	 * @param ip     the IP address of the connected client
	 * @param status the current status of the client (e.g., "Connected",
	 *               "Disconnected")
	 */
	void updateClientStatus(String id, String host, String ip, String status);
}
