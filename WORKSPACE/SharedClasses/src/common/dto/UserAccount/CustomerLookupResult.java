package common.dto.UserAccount;

import java.io.Serializable;
import common.entity.Customer;

/**
 * Result object for customer lookup operations.
 * <p>
 * Contains the outcome of searching for a customer by subscription code, phone
 * number, or email address. On success, includes the found customer entity with
 * full details.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class CustomerLookupResult implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Whether the lookup succeeded.
	 */
	private boolean success;

	/**
	 * Descriptive message about the result.
	 */
	private String message;

	/**
	 * The found customer entity (null if not found).
	 */
	private Customer customer;

	/**
	 * Returns whether the lookup succeeded.
	 *
	 * @return true if customer was found
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
	 * Returns the found customer entity.
	 *
	 * @return customer or null if not found
	 */
	public Customer getCustomer() {
		return customer;
	}

	/**
	 * Creates a successful lookup result with the found customer.
	 *
	 * @param customer the found customer entity
	 * @return success result with customer data
	 */
	public static CustomerLookupResult found(Customer customer) {
		CustomerLookupResult r = new CustomerLookupResult();
		r.success = true;
		r.message = "Customer found.";
		r.customer = customer;
		return r;
	}

	/**
	 * Creates a not found result.
	 *
	 * @param message description of why customer was not found
	 * @return failed result
	 */
	public static CustomerLookupResult notFound(String message) {
		CustomerLookupResult r = new CustomerLookupResult();
		r.success = false;
		r.message = message;
		r.customer = null;
		return r;
	}

	/**
	 * Creates a failure result for lookup errors.
	 *
	 * @param message error message
	 * @return failed result
	 */
	public static CustomerLookupResult fail(String message) {
		CustomerLookupResult r = new CustomerLookupResult();
		r.success = false;
		r.message = message;
		r.customer = null;
		return r;
	}
}
