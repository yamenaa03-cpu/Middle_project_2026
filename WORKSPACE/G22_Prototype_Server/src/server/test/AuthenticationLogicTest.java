package server.test;

import java.sql.SQLException;

import common.dto.AuthenticationResult;
import controllers.AuthenticationController;
import dbController.DBController;

/**
 * Manual Integration tests (NO JUnit)
 * Style: PASS/FAIL prints.
 *
 * Prerequisites:
 * - DB is reachable
 * - customer table exists with columns:
 *   customer_id, full_name, phone, email, is_subscribed, subscription_code
 *
 * Update DB credentials + existing data constants below.
 */
public class AuthenticationLogicTest {

    // ====== DB CONFIG (EDIT) ======
    private static final String DB_NAME = "Bistrodb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Aa123456";

    // ====== TEST DATA (EDIT to match your DB) ======
    private static final String VALID_SUB_CODE = "123456"; // must exist, is_subscribed=1
    private static final String INVALID_SUB_CODE = "1";

    private static final String EXISTING_GUEST_PHONE = "0501234567"; // if exists -> should login as existing
    private static final String EXISTING_GUEST_EMAIL = "cristiano@cr7.com";          

    // For "new guest" test, use unique phone/email not in DB
    private static final String NEW_GUEST_PHONE = "0599999999";
    private static final String NEW_GUEST_EMAIL = "new_guest_test_999@example.com";

    public static void main(String[] args) {
        System.out.println("=== AuthenticationLogicTest (Manual) ===");

        DBController db = null;
        try {
            db = new DBController(DB_NAME, DB_USER, DB_PASS);
            AuthenticationController auth = new AuthenticationController(db);

            test_Subscription_valid(auth);
            test_Subscription_invalid(auth);
            test_Subscription_empty(auth);

            test_Guest_missingName(auth);
            test_Guest_missingContact(auth);

            test_Guest_existingByPhone(auth);
            test_Guest_existingByEmail(auth);

            test_Guest_createNew(auth);

            System.out.println("\n=== DONE ===");

        } catch (Exception e) {
            System.out.println("❌ FATAL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // If your DBController has close(), call it here
        }
    }

    // ------------------ TESTS ------------------

    private static void test_Subscription_valid(AuthenticationController auth) throws SQLException {
        System.out.println("\n[Test] Subscription code - VALID");
        AuthenticationResult r = auth.authenticateBySubscriptionCode(VALID_SUB_CODE);

        assertTrue(r.isSuccess(), "Expected success=true");
        assertNotNull(r.getCustomerId(), "Expected customerId != null");
        assertFalse(r.isNewCustomer(), "Expected newCustomer=false");
        pass();
    }

    private static void test_Subscription_invalid(AuthenticationController auth) throws SQLException {
        System.out.println("\n[Test] Subscription code - INVALID");
        AuthenticationResult r = auth.authenticateBySubscriptionCode(INVALID_SUB_CODE);

        assertFalse(r.isSuccess(), "Expected success=false");
        assertNull(r.getCustomerId(), "Expected customerId == null");
        pass();
    }

    private static void test_Subscription_empty(AuthenticationController auth) throws SQLException {
        System.out.println("\n[Test] Subscription code - EMPTY");
        AuthenticationResult r = auth.authenticateBySubscriptionCode("   ");

        assertFalse(r.isSuccess(), "Expected success=false");
        assertNull(r.getCustomerId(), "Expected customerId == null");
        pass();
    }

    private static void test_Guest_missingName(AuthenticationController auth) throws SQLException {
        System.out.println("\n[Test] Guest - Missing name");
        AuthenticationResult r = auth.authenticateGuest("   ", "0500000000", "");

        assertFalse(r.isSuccess(), "Expected success=false");
        assertNull(r.getCustomerId(), "Expected customerId == null");
        pass();
    }

    private static void test_Guest_missingContact(AuthenticationController auth) throws SQLException {
        System.out.println("\n[Test] Guest - Missing phone & email");
        AuthenticationResult r = auth.authenticateGuest("Test Guest", "   ", "   ");

        assertFalse(r.isSuccess(), "Expected success=false");
        assertNull(r.getCustomerId(), "Expected customerId == null");
        pass();
    }

    private static void debug_findByPhone(DBController db) throws SQLException {
        System.out.println("\n[Debug] find by phone = " + EXISTING_GUEST_PHONE);

        Integer id = db.findCustomerIdByPhoneOrEmail(EXISTING_GUEST_PHONE, null);
        System.out.println("Result customerId = " + id);
        }

    private static void test_Guest_existingByPhone(AuthenticationController auth) throws SQLException {
        System.out.println("\n[Test] Guest - Existing by PHONE (should NOT create new)");
        AuthenticationResult r = auth.authenticateGuest("Some Name", EXISTING_GUEST_PHONE, "");

        assertTrue(r.isSuccess(), "Expected success=true");
        assertNotNull(r.getCustomerId(), "Expected customerId != null");
        assertFalse(r.isNewCustomer(), "Expected newCustomer=false");
        pass();
    }

    private static void test_Guest_existingByEmail(AuthenticationController auth) throws SQLException {
        System.out.println("\n[Test] Guest - Existing by EMAIL (should NOT create new)");
        if (EXISTING_GUEST_EMAIL == null || EXISTING_GUEST_EMAIL.isBlank()) {
            System.out.println("⚠️ Skipped: EXISTING_GUEST_EMAIL not provided.");
            return;
        }

        AuthenticationResult r = auth.authenticateGuest("Some Name", "", EXISTING_GUEST_EMAIL);

        assertTrue(r.isSuccess(), "Expected success=true");
        assertNotNull(r.getCustomerId(), "Expected customerId != null");
        assertFalse(r.isNewCustomer(), "Expected newCustomer=false");
        pass();
    }

    private static void test_Guest_createNew(AuthenticationController auth) throws SQLException {
        System.out.println("\n[Test] Guest - Create NEW (find-or-create)");
        AuthenticationResult r = auth.authenticateGuest("New Guest", NEW_GUEST_PHONE, NEW_GUEST_EMAIL);

        assertTrue(r.isSuccess(), "Expected success=true");
        assertNotNull(r.getCustomerId(), "Expected customerId != null");
        assertTrue(r.isNewCustomer(), "Expected newCustomer=true");
        pass();

        // OPTIONAL: call again to ensure it now finds existing
        System.out.println("[Test] Guest - Same contact again (should be existing now)");
        AuthenticationResult r2 = auth.authenticateGuest("New Guest X", NEW_GUEST_PHONE, NEW_GUEST_EMAIL);
        assertTrue(r2.isSuccess(), "Expected success=true");
        assertNotNull(r2.getCustomerId(), "Expected customerId != null");
        assertFalse(r2.isNewCustomer(), "Expected newCustomer=false");
        pass();
    }

    // ------------------ ASSERT HELPERS ------------------

    private static void pass() {
        System.out.println("✅ PASS");
    }

    private static void fail(String msg) {
        throw new RuntimeException("❌ FAIL: " + msg);
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) fail(msg);
    }

    private static void assertFalse(boolean cond, String msg) {
        if (cond) fail(msg);
    }

    private static void assertNotNull(Object o, String msg) {
        if (o == null) fail(msg);
    }

    private static void assertNull(Object o, String msg) {
        if (o != null) fail(msg);
    }
}
