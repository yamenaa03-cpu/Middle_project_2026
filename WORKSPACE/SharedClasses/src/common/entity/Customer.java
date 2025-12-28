package common.entity;

import java.io.Serializable;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int customerId;
    private boolean subscriber;
    private String fullName;
    private String phone;
    private String email;


    public Customer(int customerId, boolean subscriber, String fullName, String phone, String email) {
        this.customerId = customerId;
        this.subscriber = subscriber;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    public int getCustomerId() { return customerId; }
    public boolean isSubscriber() { return subscriber; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
}
