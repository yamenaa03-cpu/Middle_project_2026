package controllers;

import java.sql.SQLException;

import common.dto.Authentication.SubscriberAuthResult;
import dbController.DBController;

public class AuthenticationController {

    private final DBController db;

    public AuthenticationController(DBController db) {
        this.db = db;
    }

    // 1) Subscription customers
    public SubscriberAuthResult authenticateBySubscriptionCode(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return SubscriberAuthResult.fail("Subscription code is required.");
        }

        Integer subscriberId = db.findCustomerIdBySubscriptionCode(code.trim());
        if (subscriberId == null) {
            return SubscriberAuthResult.fail("Invalid subscription code.");
        }
        
        String fullName = db.getFullNameByCustomerId(subscriberId);
        
        return SubscriberAuthResult.ok(subscriberId, "Login successful.", fullName);
    }

}
