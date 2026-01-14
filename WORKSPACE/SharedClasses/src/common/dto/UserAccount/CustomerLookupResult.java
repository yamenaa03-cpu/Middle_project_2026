package common.dto.UserAccount;

import java.io.Serializable;
import common.entity.Customer;

public class CustomerLookupResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean success;
	private String message;
	private Customer customer;

	public boolean isSuccess() { return success; }
	public String getMessage() { return message; }
	public Customer getCustomer() { return customer; }

	public static CustomerLookupResult found(Customer customer) {
		CustomerLookupResult r = new CustomerLookupResult();
		r.success = true;
		r.message = "Customer found.";
		r.customer = customer;
		return r;
	}

	public static CustomerLookupResult notFound(String message) {
		CustomerLookupResult r = new CustomerLookupResult();
		r.success = false;
		r.message = message;
		r.customer = null;
		return r;
	}

	public static CustomerLookupResult fail(String message) {
		CustomerLookupResult r = new CustomerLookupResult();
		r.success = false;
		r.message = message;
		r.customer = null;
		return r;
	}
}
