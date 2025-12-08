package client;

import java.util.List;

import common.Order;

public interface ClientUI {
    void displayMessage(String msg);
    void displayOrders(List<Order> orders);
    void setStatus(String status);

}

