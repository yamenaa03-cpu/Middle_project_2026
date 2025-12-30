package controllers;

import java.sql.SQLException;

import common.dto.AuthenticationResult;
import dbController.DBController;

public class AuthenticationController {

    private final DBController db;

    public AuthenticationController(DBController db) {
        this.db = db;
    }

    // 1) Subscription customers
    public AuthenticationResult authenticateBySubscriptionCode(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return AuthenticationResult.fail("Subscription code is required.");
        }

        Integer customerId = db.findCustomerIdBySubscriptionCode(code.trim());
        if (customerId == null) {
            return AuthenticationResult.fail("Invalid subscription code.");
        }

        return AuthenticationResult.ok(customerId, false, "Login successful.");
    }

    // 2) Guest: find-or-create
    public AuthenticationResult authenticateGuest(String fullName, String phone, String email) throws SQLException {
        if (fullName == null || fullName.isBlank()) {
            return AuthenticationResult.fail("Full name is required.");
        }

        boolean phoneEmpty = (phone == null || phone.isBlank());
        boolean emailEmpty = (email == null || email.isBlank());

        if (phoneEmpty && emailEmpty) {
            return AuthenticationResult.fail("Phone or Email is required.");
        }

        String normPhone = phoneEmpty ? null : phone.trim();
        String normEmail = emailEmpty ? null : email.trim().toLowerCase();

        Integer customerId = db.findCustomerIdByPhoneOrEmail(normPhone, normEmail);

        if (customerId != null) {
            return AuthenticationResult.ok(customerId, false, "Login successful.");
        }

        int newId = db.createGuestCustomer(fullName.trim(), normPhone, normEmail);
        return AuthenticationResult.ok(newId, true, "Guest created and logged in.");
    }
}
