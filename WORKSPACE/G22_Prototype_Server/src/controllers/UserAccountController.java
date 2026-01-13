package controllers;

import java.sql.SQLException;

import common.dto.UserAccount.SubscriberLogInResult;
import common.entity.Customer;
import common.enums.EmployeeRole;
import common.dto.UserAccount.EmployeeLogInResult;
import common.dto.UserAccount.RegisterSubscriberResult;
import dbController.DBController;

public class UserAccountController {

	private final DBController db;

	public UserAccountController(DBController db) {
		this.db = db;
	}

	// 1) Subscription customers
	public SubscriberLogInResult LogInBySubscriptionCode(String code) throws SQLException {
		if (code == null || code.isBlank()) {
			return SubscriberLogInResult.fail("Subscription code is required.");
		}

		Integer subscriberId = db.findCustomerIdBySubscriptionCode(code.trim());
		if (subscriberId == null) {
			return SubscriberLogInResult.fail("Invalid subscription code.");
		}

		String fullName = db.getFullNameByCustomerId(subscriberId);

		return SubscriberLogInResult.ok(subscriberId, "Login successful.", fullName);
	}

	public RegisterSubscriberResult registerSubscriber(String fullName, String phone, String email)
			throws SQLException {
		if ((phone == null || phone.isBlank()))
			return RegisterSubscriberResult.fail("Phone number is required.");

		if (email == null || email.isBlank())
			return RegisterSubscriberResult.fail("Email is required.");

		if (fullName == null || fullName.isBlank())
			return RegisterSubscriberResult.fail("Full name is required.");

		if (db.customerExistsByPhoneOrEmail(phone, email)) {
			return RegisterSubscriberResult.fail("Customer already exists (phone/email).");
		}

		String subCode = db.createSubscriber(fullName.trim(), phone.trim(), email.trim());
		if (subCode == null)
			return RegisterSubscriberResult.fail("Failed to create user.");

		return RegisterSubscriberResult.ok(subCode);
	}

	public String getFullNameBySubscriberId(int subscriberId) throws SQLException {
		return db.getFullNameByCustomerId(subscriberId);
	}

	public EmployeeLogInResult employeeLogIn(String username, String password) throws SQLException {
		if (username == null || username.isBlank())
			return EmployeeLogInResult.fail("Username required.");
		if (password == null || password.isBlank())
			return EmployeeLogInResult.fail("Password required.");

		Integer empId = db.findEmployeeIdByCredentials(username.trim(), password);
		if (empId == null)
			return EmployeeLogInResult.fail("Invalid credentials.");

		String fullName = db.getEmployeeNameById(empId);
		EmployeeRole empRole = db.getEmployeeRoleById(empId);

		return EmployeeLogInResult.ok(empId, empRole, fullName, "Login successful.");
	}

	public String getFullNameByEmployeeId(int employeeId) throws SQLException {
		return db.getEmployeeNameById(employeeId);
	}

	public Customer lookupCustomerBySubscriptionCode(String subscriptionCode) throws SQLException {
		if (subscriptionCode == null || subscriptionCode.isBlank()) {
			return null;
		}
		return db.findCustomerBySubscriptionCode(subscriptionCode.trim());
	}

	public Customer lookupCustomerByPhone(String phone) throws SQLException {
		if (phone == null || phone.isBlank()) {
			return null;
		}
		return db.findCustomerByPhone(phone.trim());
	}

	public Customer lookupCustomerByEmail(String email) throws SQLException {
		if (email == null || email.isBlank()) {
			return null;
		}
		return db.findCustomerByEmail(email.trim());
	}
}
