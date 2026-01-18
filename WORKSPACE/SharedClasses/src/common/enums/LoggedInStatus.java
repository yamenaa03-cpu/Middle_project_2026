package common.enums;

/**
 * Enumeration representing the current login state of a client session.
 * <p>
 * This enum is used to communicate the authentication status back to clients
 * and to determine what operations are available to the current session.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum LoggedInStatus {
    /**
     * No user is currently logged in to this session.
     * Only guest operations are available.
     */
    NOT_LOGGED_IN,

    /**
     * A subscriber (registered customer) is logged in.
     * Subscriber-specific operations and discounts are available.
     */
    SUBSCRIBER,

    /**
     * A customer service representative is logged in.
     * Standard employee operations are available.
     */
    REPRESENTATIVE,

    /**
     * A manager is logged in.
     * Full administrative and operational access is available.
     */
    MANAGER
}