package server;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JavaFX-compatible model class representing the connection status of a client.
 * <p>
 * This class holds observable string properties for client identification and
 * status, making it suitable for use in JavaFX TableView components. Each
 * property can be bound to UI elements for automatic updates when values
 * change.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class ClientStatusInfo {

	/**
	 * Observable property for the client's unique identifier.
	 */
	private final StringProperty id;

	/**
	 * Observable property for the client's hostname.
	 */
	private final StringProperty host;

	/**
	 * Observable property for the client's IP address.
	 */
	private final StringProperty ip;

	/**
	 * Observable property for the client's connection status.
	 */
	private final StringProperty status;

	/**
	 * Constructs a new ClientStatusInfo with the specified connection details.
	 *
	 * @param id     the unique identifier for the client connection
	 * @param host   the hostname of the client machine
	 * @param ip     the IP address of the client
	 * @param status the current connection status (e.g., "Connected",
	 *               "Disconnected")
	 */
	public ClientStatusInfo(String id, String host, String ip, String status) {
		this.id = new SimpleStringProperty(id);
		this.host = new SimpleStringProperty(host);
		this.ip = new SimpleStringProperty(ip);
		this.status = new SimpleStringProperty(status);
	}

	/**
	 * Returns the client's unique identifier.
	 *
	 * @return the client ID string
	 */
	public String getId() {
		return id.get();
	}

	/**
	 * Returns the observable property for the client ID.
	 * <p>
	 * This property can be used for JavaFX bindings and TableView cell value
	 * factories.
	 * </p>
	 *
	 * @return the StringProperty for ID
	 */
	public StringProperty idProperty() {
		return id;
	}

	/**
	 * Returns the client's hostname.
	 *
	 * @return the hostname string
	 */
	public String getHost() {
		return host.get();
	}

	/**
	 * Returns the observable property for the client hostname.
	 * <p>
	 * This property can be used for JavaFX bindings and TableView cell value
	 * factories.
	 * </p>
	 *
	 * @return the StringProperty for host
	 */
	public StringProperty hostProperty() {
		return host;
	}

	/**
	 * Returns the client's IP address.
	 *
	 * @return the IP address string
	 */
	public String getIp() {
		return ip.get();
	}

	/**
	 * Returns the observable property for the client IP address.
	 * <p>
	 * This property can be used for JavaFX bindings and TableView cell value
	 * factories.
	 * </p>
	 *
	 * @return the StringProperty for IP
	 */
	public StringProperty ipProperty() {
		return ip;
	}

	/**
	 * Returns the client's current connection status.
	 *
	 * @return the status string (e.g., "Connected", "Disconnected")
	 */
	public String getStatus() {
		return status.get();
	}

	/**
	 * Returns the observable property for the client connection status.
	 * <p>
	 * This property can be used for JavaFX bindings and TableView cell value
	 * factories.
	 * </p>
	 *
	 * @return the StringProperty for status
	 */
	public StringProperty statusProperty() {
		return status;
	}

	/**
	 * Updates the client's connection status.
	 *
	 * @param value the new status string to set
	 */
	public void setStatus(String value) {
		status.set(value);
	}
}
