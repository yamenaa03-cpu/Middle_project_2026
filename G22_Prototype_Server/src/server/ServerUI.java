
package server;
/**
 * Interface for sending messages and client connection updates to the server GUI.
 * @author Yamen Abu Ahmad
 * @version: 1.0
 */
public interface ServerUI {
    void display(String msg);
    void updateClientStatus(String id, String host, String ip, String status);
}
