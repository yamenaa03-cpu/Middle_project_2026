package client;

import ocsf.client.AbstractClient;

import common.OrderResponse;
import common.OrderRequest;
import common.Order;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class Client extends AbstractClient {

    private ClientUI ui;

    public Client(String host, int port, ClientUI ui) {
        super(host, port);
        this.ui = ui;
    }

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

    public void requestAllOrders() {
        OrderRequest req = OrderRequest.createGetAllOrdersRequest();
        sendRequest(req);
    }

    public void requestUpdateOrder(int orderNumber, LocalDate newDate, int newGuests) {
        OrderRequest req = OrderRequest.createUpdateOrderRequest(orderNumber, newDate, newGuests);
        sendRequest(req);
    }

    private void sendRequest(OrderRequest req) {
        try {
            sendToServer(req);
        } catch (IOException e) {
            ui.displayMessage("Error sending request: " + e.getMessage());
        }
    }

}
