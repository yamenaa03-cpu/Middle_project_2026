package common;

import java.io.Serializable;
import java.util.List;

public class OrderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<Order> orders;  

    public OrderResponse(boolean success, String message, List<Order> orders) {
        this.success = success;
        this.message = message;
        this.orders = orders;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Order> getOrders() { return orders; }
}