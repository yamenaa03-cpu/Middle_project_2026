package controllers;

import java.sql.SQLException;

import common.dto.Authentication.CustomerAuthResult;
import common.entity.Customer;
import dbController.DBController;

public class AuthenticationController {

    private final DBController db;

    public AuthenticationController(DBController db) {
        this.db = db;
    }

    // 1) Subscription customers
    public CustomerAuthResult authenticateBySubscriptionCode(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return CustomerAuthResult.fail("Subscription code is required.");
        }

        Integer customerId = db.findCustomerIdBySubscriptionCode(code.trim());
        if (customerId == null) {
            return CustomerAuthResult.fail("Invalid subscription code.");
        }

        return CustomerAuthResult.ok(customerId, "Login successful.");
    }
    
    public Customer getProfile(int customerId) throws SQLException {
        return db.getCustomerById(customerId);
    }

    public boolean updateProfile(int customerId, String fullName, String phone, String email) throws SQLException {
        return db.updateCustomerProfile(customerId, fullName, phone, email);
    }

}
