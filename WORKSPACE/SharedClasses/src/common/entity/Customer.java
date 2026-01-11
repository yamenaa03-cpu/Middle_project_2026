package common.entity;

import java.io.Serializable;

/**
 * Represents a customer record containing personal and subscription information.
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int customerId;
    private boolean subscriber;
    private String fullName;
    private String phone;
    private String email;
    private String subscriptionCode;    

    /**
     * Construct a Customer with subscription code.
     *
     * @param customerId unique id for the customer
     * @param subscriber whether the customer is a subscriber
     * @param fullName full name of the customer
     * @param phone phone number for contact
     * @param email email address for contact
     * @param subscriptionCode subscription code when applicable
     */
    public Customer(int customerId, boolean subscriber, String fullName, String phone, String email, String subscriptionCode) {
        this.customerId = customerId;
        this.subscriber = subscriber;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.subscriptionCode = subscriptionCode;
    }//for personal information in the personal space class

    /**
     * Construct a Customer without a subscription code.
     *
     * @param customerId unique id for the customer
     * @param subscriber whether the customer is a subscriber
     * @param fullName full name of the customer
     * @param phone phone number for contact
     * @param email email address for contact
     */
    public Customer(int customerId, boolean subscriber, String fullName, String phone, String email) {
        this.customerId = customerId;
        this.subscriber = subscriber;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Returns the customer's id.
     *
     * @return customer id
     */
    public int getCustomerId() { return customerId; }

    /**
     * Returns whether the customer is a subscriber.
     *
     * @return true if subscriber, false otherwise
     */
    public boolean isSubscriber() { return subscriber; }

    /**
     * Returns the customer's full name.
     *
     * @return full name
     */
    public String getFullName() { return fullName; }

    /**
     * Returns the customer's phone number.
     *
     * @return phone number
     */
    public String getPhone() { return phone; }

    /**
     * Returns the customer's email.
     *
     * @return email address
     */
    public String getEmail() { return email; }
    
    /**
     * Returns the subscription code or null if none.
     *
     * @return subscription code
     */
    public String getSubscriptionCode() { return subscriptionCode; }

    /**
     * Set or update the subscription code for this customer.
     *
     * @param subscriptionCode new subscription code
     */
    public void setSubscriptionCode(String subscriptionCode) { this.subscriptionCode = subscriptionCode; }


    /**
     * Update the full name of the customer.
     *
     * @param fullName new full name
     */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * Update the customer's phone number.
     *
     * @param phone new phone number
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Update the customer's email address.
     *
     * @param email new email address
     */
    public void setEmail(String email) { this.email = email; }
}
