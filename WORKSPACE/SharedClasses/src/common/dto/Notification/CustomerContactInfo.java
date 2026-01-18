package common.dto.Notification;

/**
 * Contact information for a customer used by notification flows.
 * <p>
 * This DTO contains the essential contact details needed to send
 * notifications to a customer via email or SMS.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class CustomerContactInfo {

    /**
     * Unique identifier for the customer.
     */
    private final int customerId;

    /**
     * Customer's full name for personalized messages.
     */
    private final String fullName;

    /**
     * Customer's phone number for SMS notifications.
     */
    private final String phone;

    /**
     * Customer's email address for email notifications.
     */
    private final String email;

    /**
     * Creates a contact info record with all fields.
     *
     * @param customerId unique customer identifier
     * @param fullName   full name of the customer
     * @param phone      phone number (may be null)
     * @param email      email address (may be null)
     */
    public CustomerContactInfo(int customerId, String fullName, String phone, String email) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Returns the customer's unique identifier.
     *
     * @return customer ID
     */
    public int getCustomerId() { return customerId; }

    /**
     * Returns the customer's full name.
     *
     * @return full name
     */
    public String getFullName() { return fullName; }

    /**
     * Returns the customer's phone number for SMS.
     *
     * @return phone number or null if not available
     */
    public String getPhone() { return phone; }

    /**
     * Returns the customer's email address.
     *
     * @return email address or null if not available
     */
    public String getEmail() { return email; }
}
