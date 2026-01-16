package controllers;

import java.sql.SQLException;
import java.util.List;

import common.dto.UserAccount.SubscriberLogInResult;
import common.dto.UserAccount.CustomerLookupResult;
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

	// ======================== LOGIN OPERATIONS ========================

	public SubscriberLogInResult LogInBySubscriptionCode(String code) throws SQLException {
		if (code == null || code.isBlank()) {
			return SubscriberLogInResult.fail("Subscription code is required.");
		}

		Integer subscriberId = db.findCustomerIdBySubscriptionCode(code.trim());
		if (subscriberId == null) {
			return SubscriberLogInResult.fail("Invalid subscription code.");
		}

		String fullName = db.getFullNameByCustomerId(subscriberId);

		return SubscriberLogInResult.ok(subscriberId, fullName, "Subscriber login successful.");
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

		return EmployeeLogInResult.ok(empId, empRole, fullName, "Employee login successful.");
	}

	// ======================== REGISTRATION ========================

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

	// ======================== PROFILE OPERATIONS ========================

	public CustomerLookupResult getSubscriberProfile(int subscriberId) throws SQLException {
		if (subscriberId <= 0) {
			return CustomerLookupResult.fail("Invalid subscriber ID.");
		}
		Customer c = db.getSubscribedCustomerById(subscriberId);
		if (c == null) {
			return CustomerLookupResult.notFound("Subscriber not found.");
		}
		return CustomerLookupResult.found(c);
	}

	public String getFullNameBySubscriberId(int subscriberId) throws SQLException {
		return db.getFullNameByCustomerId(subscriberId);
	}

	public String getFullNameByEmployeeId(int employeeId) throws SQLException {
		return db.getEmployeeNameById(employeeId);
	}

	// ======================== CUSTOMER LOOKUP (FOR EMPLOYEES)
	// ========================

	public CustomerLookupResult lookupCustomerBySubscriptionCode(String subscriptionCode) throws SQLException {
		if (subscriptionCode == null || subscriptionCode.isBlank()) {
			return CustomerLookupResult.fail("Subscription code is required.");
		}
		Customer c = db.findCustomerBySubscriptionCode(subscriptionCode.trim());
		if (c == null) {
			return CustomerLookupResult.notFound("Customer not found by subscription code.");
		}
		return CustomerLookupResult.found(c);
	}

	public CustomerLookupResult lookupCustomerByPhone(String phone) throws SQLException {
		if (phone == null || phone.isBlank()) {
			return CustomerLookupResult.fail("Phone number is required.");
		}
		Customer c = db.findCustomerByPhone(phone.trim());
		if (c == null) {
			return CustomerLookupResult.notFound("Customer not found by phone.");
		}
		return CustomerLookupResult.found(c);
	}

	public CustomerLookupResult lookupCustomerByEmail(String email) throws SQLException {
		if (email == null || email.isBlank()) {
			return CustomerLookupResult.fail("Email is required.");
		}
		Customer c = db.findCustomerByEmail(email.trim());
		if (c == null) {
			return CustomerLookupResult.notFound("Customer not found by email.");
		}
		return CustomerLookupResult.found(c);
	}

	// ======================== UPDATE PROFILE ========================

	public CustomerLookupResult updateSubscriberProfile(int subscriberId, String fullName, String phone, String email)
			throws SQLException {
		if (subscriberId <= 0) {
			return CustomerLookupResult.fail("Invalid subscriber ID.");
		}

		Customer existing = db.getSubscribedCustomerById(subscriberId);
		if (existing == null) {
			return CustomerLookupResult.notFound("Subscriber not found.");
		}

		String newFullName = (fullName != null && !fullName.isBlank()) ? fullName.trim() : existing.getFullName();
		String newPhone = (phone != null && !phone.isBlank()) ? phone.trim() : existing.getPhone();
		String newEmail = (email != null && !email.isBlank()) ? email.trim() : existing.getEmail();

		if (db.customerExistsByPhoneOrEmailExcept(subscriberId, newPhone, newEmail)) {
			return CustomerLookupResult.fail("Phone or email already in use by another customer.");
		}

		boolean updated = db.updateCustomerProfile(subscriberId, newFullName, newPhone, newEmail);
		if (!updated) {
			return CustomerLookupResult.fail("Failed to update profile.");
		}

		Customer updatedCustomer = db.getSubscribedCustomerById(subscriberId);
		return CustomerLookupResult.found(updatedCustomer);
	}

	// ======================== EMPLOYEE QUERIES ========================

	public List<Customer> getAllSubscribers() throws SQLException {
		return db.getAllSubscribers();
	}

	public List<Customer> getCurrentDiners() throws SQLException {
		return db.getCurrentDiners();
	}
}
