package client;

import ocsf.client.AbstractClient;

import common.OrderResponse;
import common.OrderRequest;
import common.Order;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
/**
 * The Client class extends the OCSF AbstractClient framework.
 * It is responsible for:
 *  - Connecting to the server
 *  - Sending requests (OrderRequest objects)
 *  - Receiving responses from the server (OrderResponse)
 *  - Forwarding results/messages to the GUI through ClientUI
 *@version 1.0
 */

public class Client extends AbstractClient {

    private ClientUI ui;

    public Client(String host, int port, ClientUI ui) {
        super(host, port);
        this.ui = ui;
    }
    /**
     * This method automatically runs whenever the server sends a message.
     * It handles responses from the server as an instance of OrderResponse object
     * 
     *
     * @param msg The object received from the server
     * 
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (!(msg instanceof OrderResponse)) {
            ui.displayMessage("Unknown message from server: " + msg);
            return;
        }

        OrderResponse resp = (OrderResponse) msg;
        ui.displayMessage(resp.getMessage());

        List<Order> orders = resp.getOrders();
        if (orders != null) {
            ui.displayOrders(orders);
        }
    }
    
    /**
     * Sends a request to retrieve all orders from the database.
     */
    public void requestAllOrders() {
        OrderRequest req = OrderRequest.createGetAllOrdersRequest();
        sendRequest(req);
    }
    /**
     * Sends a request (to the sever) to update an order in the database.
     *
     * @param orderNumber The order ID to update
     * @param newDate     The new date to set
     * @param newGuests   The new guest count
     */
    
    public void requestUpdateOrder(int orderNumber, LocalDate newDate, int newGuests) {
        OrderRequest req = OrderRequest.createUpdateOrderRequest(orderNumber, newDate, newGuests);
        sendRequest(req);
    }
    
    /**
     * sends a request object to the server.
     * catches any Exception while doing so
     *
     * @param req The OrderRequest object to send
     */
    
    private void sendRequest(OrderRequest req) {
        try {
            sendToServer(req);
        } catch (IOException e) {
            ui.displayMessage("Error sending request: " + e.getMessage());
        }
    }

}
