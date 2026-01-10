package common.dto.Notification;

public class CustomerContactInfo {

    private final int customerId;
    private final String fullName;
    private final String phone;
    private final String email;

    public CustomerContactInfo(int customerId, String fullName, String phone, String email) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    public int getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
