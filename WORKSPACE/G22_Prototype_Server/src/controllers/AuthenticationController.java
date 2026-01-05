package controllers;

import java.sql.SQLException;

import common.dto.Authentication.CustomerAuthResult;
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

}
