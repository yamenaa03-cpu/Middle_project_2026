package common.dto.Authentication;

import java.io.Serializable;

import common.entity.Customer;

/**
 * Response object for customer authentication requests. Contains success
 * indicator, message, subscriber id and optional customer profile.
 */
public class CustomerAuthResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private int subscriberId;
    private Customer customer;

    /**
     * Create a response without a customer profile.
     *
     * @param success whether the operation succeeded
     * @param message descriptive message
     * @param subscriberId subscriber id when applicable
     */
    public CustomerAuthResponse(boolean success, String message, Integer subscriberId) {
        this.success = success;
        this.message = message;
        this.subscriberId = subscriberId;
    }
    
    /**
     * Create a response including the customer's profile.
     *
     * @param success whether the operation succeeded
     * @param message descriptive message
     * @param subscriberId subscriber id when applicable
     * @param c the customer's profile
     */
    public CustomerAuthResponse(boolean success, String message, Integer subscriberId, Customer c) {
        this.success = success;
        this.message = message;
        this.subscriberId = subscriberId;
        this.customer = c;
    }

    /** @return true if operation succeeded */
    public boolean isSuccess() { return success; }
    /** @return descriptive message */
    public String getMessage() { return message; }
    /** @return subscriber id when available */
    public Integer getSubscriberId() { return subscriberId; }
    /** @return customer profile when present */
    public Customer getCustomer() {return customer;}
}
