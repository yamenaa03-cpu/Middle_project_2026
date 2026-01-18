package common.dto.UserAccount;

import common.enums.EmployeeRole;

/**
 * Result object for employee login operations.
 * <p>
 * Contains the outcome of authenticating an employee via username and password,
 * including the employee ID, role, and full name on success.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class EmployeeLogInResult {

	/**
	 * Whether the login succeeded.
	 */
	private final boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private final String message;

	/**
	 * Employee ID on successful login.
	 */
	private final Integer employeeId;

	/**
	 * Employee's role (MANAGER or REPRESENTATIVE) on successful login.
	 */
	private final EmployeeRole role;

	/**
	 * Employee's full name on successful login.
	 */
	private final String fullName;

	/**
	 * Private constructor used by factory methods.
	 */
	private EmployeeLogInResult(boolean success, String message, Integer employeeId, EmployeeRole role,
			String fullName) {
		this.success = success;
		this.message = message;
		this.employeeId = employeeId;
		this.role = role;
		this.fullName = fullName;
	}

	/**
	 * Creates a successful login result.
	 *
	 * @param id       the authenticated employee's ID
	 * @param role     the employee's role
	 * @param fullName the employee's full name
	 * @param msg      success message
	 * @return success result with employee info
	 */
	public static EmployeeLogInResult ok(int id, EmployeeRole role, String fullName, String msg) {
		return new EmployeeLogInResult(true, msg, id, role, fullName);
	}

	/**
	 * Creates a failed login result.
	 *
	 * @param msg failure message
	 * @return failed result
	 */
	public static EmployeeLogInResult fail(String msg) {
		return new EmployeeLogInResult(false, msg, null, null, null);
	}

	/**
	 * Returns whether the login succeeded.
	 *
	 * @return true if successful
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Returns the result message.
	 *
	 * @return descriptive message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the employee ID on success.
	 *
	 * @return employee ID or null if failed
	 */
	public Integer getEmployeeId() {
		return employeeId;
	}

	/**
	 * Returns the employee's role on success.
	 *
	 * @return employee role or null if failed
	 */
	public EmployeeRole getRole() {
		return role;
	}

	/**
	 * Returns the employee's full name on success.
	 *
	 * @return full name or null if failed
	 */
	public String getFullName() {
		return fullName;
	}
}
