package common.enums;

/**
 * Enumeration of employee roles within the restaurant management system.
 * <p>
 * Each role has different permissions and access levels:
 * <ul>
 *   <li><strong>REPRESENTATIVE:</strong> Can perform customer service operations,
 *       manage reservations, and assist customers</li>
 *   <li><strong>MANAGER:</strong> Has all representative permissions plus
 *       administrative access to restaurant settings and reports</li>
 * </ul>
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum EmployeeRole {
	/**
	 * Customer service representative with standard operational permissions.
	 */
	REPRESENTATIVE,

	/**
	 * Manager with full administrative and operational permissions.
	 */
	MANAGER
}