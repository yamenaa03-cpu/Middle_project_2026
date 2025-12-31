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

        return CustomerAuthResult.ok(customerId, false, "Login successful.");
    }

    // 2) Guest: find-or-create
    public CustomerAuthResult authenticateGuest(String fullName, String phone, String email) throws SQLException {
        if (fullName == null || fullName.isBlank()) {
            return CustomerAuthResult.fail("Full name is required.");
        }

        boolean phoneEmpty = (phone == null || phone.isBlank());
        boolean emailEmpty = (email == null || email.isBlank());

        if (phoneEmpty && emailEmpty) {
            return CustomerAuthResult.fail("Phone or Email is required.");
        }

        String normPhone = phoneEmpty ? null : phone.trim();
        String normEmail = emailEmpty ? null : email.trim().toLowerCase();

        Integer customerId = db.findCustomerIdByPhoneOrEmail(normPhone, normEmail);

        if (customerId != null) {
            return CustomerAuthResult.ok(customerId, false, "Login successful.");
        }

        int newId = db.createGuestCustomer(fullName.trim(), normPhone, normEmail);
        return CustomerAuthResult.ok(newId, true, "Guest created and logged in.");
    }
}
