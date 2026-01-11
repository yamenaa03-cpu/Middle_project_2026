package common.dto.Notification;

/**
 * Contact information for a customer used by notification flows.
 */
public class CustomerContactInfo {

    private final int customerId;
    private final String fullName;
    private final String phone;
    private final String email;

    /**
     * Create a contact info record.
     *
     * @param customerId customer identifier
     * @param fullName full name of the customer
     * @param phone phone number
     * @param email email address
     */
    public CustomerContactInfo(int customerId, String fullName, String phone, String email) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Returns the customer id.
     *
     * @return customer id
     */
    public int getCustomerId() { return customerId; }

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
     * Returns the customer's email address.
     *
     * @return email address
     */
    public String getEmail() { return email; }
}
