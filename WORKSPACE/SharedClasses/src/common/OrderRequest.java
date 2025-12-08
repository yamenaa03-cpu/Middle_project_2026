package common;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A message sent from the client to the server containing
 * the requested operation and relevant parameters.
 * Shared between both projects (client & server).
 * @author: Yamen Abu Ahmad
 * @version 1.0
 */

/*This class */
public class OrderRequest implements Serializable{
	
	 private static final long serialVersionUID = 1L;
	
    private OrderOperation operation;
	
    private int orderNumber;
    private LocalDate newOrderDate;
    private int newNumberOfGuests;
    
    /*identify the request as an instance of Order request class and saves the GET_ALL_ORDERS operation
     * as a field in the class
    */
    public static OrderRequest createGetAllOrdersRequest() {
        OrderRequest req = new OrderRequest();
        req.operation = OrderOperation.GET_ALL_ORDERS;
        return req;
    }
    
    /*identify the request as an instance of Order request class and saves the UPDATE_ORDER_FIELDS operation
      as a field in the class*/
    public static OrderRequest createUpdateOrderRequest(int orderNumber,
            LocalDate newDate,
            int newGuests) {
    	
		OrderRequest req = new OrderRequest();
		req.operation = OrderOperation.UPDATE_ORDER_FIELDS;
		req.orderNumber = orderNumber;
		req.newOrderDate = newDate;
		req.newNumberOfGuests = newGuests;
		return req;
    }
    
   // private OrderRequest() {}
    //Getters for the class fields
    public OrderOperation getOperation() { return operation; }
    public int getOrderNumber() { return orderNumber; }
    public LocalDate getNewOrderDate() { return newOrderDate; }
    public int getNewNumberOfGuests() { return newNumberOfGuests; }
    
}
