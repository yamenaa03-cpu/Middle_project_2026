package common;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a single Order entity stored in the database.
 * Contains all fields taken from the Order table and used by both client and server.
 * @author Yamen abu Ahmad
 * @version 1.0
 * 
 * */

public class Order implements Serializable{
	
	//a version number used during object serialization
	private static final long serialVersionUID = 1L;
	
	//Data base table order fields
	private int orderNumber;        // order_number (PK)
    private LocalDate orderDate;    // order_date
    private int numberOfGuests;     // number_of_guests
    private int confirmationCode;   // confirmation_code
    private int subscriberId;       // subscriber_id (FK)
    private LocalDate dateOfPlacing; // date_of_placing_order
    
    //constructor to intelize one order
    public Order(int orderNumber, LocalDate orderDate, int numberOfGuests,
            int confirmationCode, int subscriberId, LocalDate dateOfPlacing) {
   this.orderNumber = orderNumber;
   this.orderDate = orderDate;
   this.numberOfGuests = numberOfGuests;
   this.confirmationCode = confirmationCode;
   this.subscriberId = subscriberId;
   this.dateOfPlacing = dateOfPlacing;
    }
    
    //Getters for the order fileds
    public int getOrderNumber() { return orderNumber; }
    public LocalDate getOrderDate() { return orderDate; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public int getConfirmationCode() { return confirmationCode; }
    public int getSubscriberId() { return subscriberId; }
    public LocalDate getDateOfPlacing() { return dateOfPlacing; }
    
    @Override
    public String toString() {
        return "Order #" + orderNumber +
               " | date=" + orderDate +
               " | guests=" + numberOfGuests +
               " | conf=" + confirmationCode +
               " | sub=" + subscriberId +
               " | placed=" + dateOfPlacing;
    }
    
}
