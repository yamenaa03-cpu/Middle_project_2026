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

/**
 * Controller responsible for user account management operations.
 * <p>
 * This controller handles:
 * <ul>
 * <li>Subscriber login via subscription code</li>
 * <li>Employee login via username/password</li>
 * <li>Subscriber registration</li>
 * <li>Profile management (view and update)</li>
 * <li>Customer lookup operations for employees</li>
 * </ul>
 * All operations interact with the database through the {@link DBController}.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class UserAccountController {

	/**
	 * Database controller for data persistence operations.
	 */
	private final DBController db;

	/**
	 * Constructs a UserAccountController with the specified database controller.
	 *
	 * @param db the database controller for data access
	 */
	public UserAccountController(DBController db) {
		this.db = db;
	}

	// ======================== LOGIN OPERATIONS ========================

	/**
	 * Authenticates a subscriber using their subscription code.
	 *
	 * @param code the subscription code to validate
	 * @return the login result containing subscriber ID and name on success
	 * @throws SQLException if a database error occurs
	 */
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

	/**
	 * Authenticates an employee using their username and password.
	 *
	 * @param username the employee's username
	 * @param password the employee's password
	 * @return the login result containing employee ID, role, and name on success
	 * @throws SQLException if a database error occurs
	 */
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

	/**
	 * Registers a new subscriber with the provided information.
	 * <p>
	 * Creates a new customer record with subscriber status and generates a unique
	 * subscription code.
	 * </p>
	 *
	 * @param fullName the subscriber's full name
	 * @param phone    the subscriber's phone number (required)
	 * @param email    the subscriber's email address (required)
	 * @return the registration result containing the subscription code on success
	 * @throws SQLException if a database error occurs
	 */
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

	/**
	 * Retrieves the profile information for a subscriber.
	 *
	 * @param subscriberId the ID of the subscriber
	 * @return the lookup result containing customer data on success
	 * @throws SQLException if a database error occurs
	 */
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

	/**
	 * Retrieves the full name of a subscriber by their ID.
	 *
	 * @param subscriberId the subscriber's ID
	 * @return the subscriber's full name, or null if not found
	 * @throws SQLException if a database error occurs
	 */
	public String getFullNameBySubscriberId(int subscriberId) throws SQLException {
		return db.getFullNameByCustomerId(subscriberId);
	}

	/**
	 * Retrieves the full name of an employee by their ID.
	 *
	 * @param employeeId the employee's ID
	 * @return the employee's full name, or null if not found
	 * @throws SQLException if a database error occurs
	 */
	public String getFullNameByEmployeeId(int employeeId) throws SQLException {
		return db.getEmployeeNameById(employeeId);
	}

	// ======================== CUSTOMER LOOKUP (FOR EMPLOYEES)
	// ========================

	/**
	 * Looks up a customer by their subscription code.
	 * <p>
	 * This operation is typically performed by employees to find customer records.
	 * </p>
	 *
	 * @param subscriptionCode the subscription code to search for
	 * @return the lookup result containing customer data if found
	 * @throws SQLException if a database error occurs
	 */
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

	/**
	 * Looks up a customer by their phone number.
	 * <p>
	 * This operation is typically performed by employees to find customer records.
	 * </p>
	 *
	 * @param phone the phone number to search for
	 * @return the lookup result containing customer data if found
	 * @throws SQLException if a database error occurs
	 */
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

	/**
	 * Looks up a customer by their email address.
	 * <p>
	 * This operation is typically performed by employees to find customer records.
	 * </p>
	 *
	 * @param email the email address to search for
	 * @return the lookup result containing customer data if found
	 * @throws SQLException if a database error occurs
	 */
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

	/**
	 * Updates a subscriber's profile information.
	 * <p>
	 * Only the fields provided will be updated; null or blank values will retain
	 * the existing data.
	 * </p>
	 *
	 * @param subscriberId the ID of the subscriber to update
	 * @param fullName     the new full name (optional)
	 * @param phone        the new phone number (optional)
	 * @param email        the new email address (optional)
	 * @return the lookup result containing the updated customer data
	 * @throws SQLException if a database error occurs
	 */
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

	/**
	 * Retrieves all registered subscribers.
	 * <p>
	 * This operation is restricted to employees.
	 * </p>
	 *
	 * @return a list of all subscriber customer records
	 * @throws SQLException if a database error occurs
	 */
	public List<Customer> getAllSubscribers() throws SQLException {
		return db.getAllSubscribers();
	}

	/**
	 * Retrieves all customers currently dining (with in-progress reservations).
	 * <p>
	 * This operation is restricted to employees.
	 * </p>
	 *
	 * @return a list of customers with IN_PROGRESS status reservations
	 * @throws SQLException if a database error occurs
	 */
	public List<Customer> getCurrentDiners() throws SQLException {
		return db.getCurrentDiners();
	}
}
