package controllers;

import java.sql.SQLException;

import common.dto.UserAccount.LogInResult;
import common.dto.UserAccount.RegisterSubscriberResult;
import dbController.DBController;

public class UserAccountController {

    private final DBController db;

    public UserAccountController(DBController db) {
        this.db = db;
    }

    // 1) Subscription customers
    public LogInResult LogInBySubscriptionCode(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return LogInResult.fail("Subscription code is required.");
        }

        Integer subscriberId = db.findCustomerIdBySubscriptionCode(code.trim());
        if (subscriberId == null) {
            return LogInResult.fail("Invalid subscription code.");
        }
        
        String fullName = db.getFullNameByCustomerId(subscriberId);
        
        return LogInResult.ok(subscriberId, "Login successful.", fullName);
    }
    
    public RegisterSubscriberResult registerSubscriber(String fullName, String phone, String email) throws SQLException {
        if ((phone == null || phone.isBlank()))
            return RegisterSubscriberResult.fail("Phone number is required.");
        
        if (email == null || email.isBlank())
            return RegisterSubscriberResult.fail("Email is required.");
        
        if (fullName == null || fullName.isBlank())
            return RegisterSubscriberResult.fail("Full name is required.");

        String subCode = db.createSubscriber(fullName.trim(), phone.trim(), email.trim());
        if (subCode == null) return RegisterSubscriberResult.fail("Failed to create user.");

        return RegisterSubscriberResult.ok(subCode);
    }

}
