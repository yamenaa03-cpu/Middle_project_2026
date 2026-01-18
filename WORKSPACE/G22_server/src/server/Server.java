package server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.dto.Reservation.CancelReservationResult;
import common.dto.Reservation.CreateReservationResult;
import common.dto.Reservation.PayBillResult;
import common.dto.Reservation.ReceiveTableResult;
import common.dto.Reservation.ReservationRequest;
import common.dto.Reservation.ReservationResponse;
import common.dto.Reservation.UpdateReservationResult;
import common.dto.UserAccount.UserAccountRequest;
import common.dto.UserAccount.UserAccountResponse;
import common.dto.UserAccount.SubscriberLogInResult;
import common.dto.UserAccount.EmployeeLogInResult;
import common.dto.UserAccount.RegisterSubscriberResult;
import common.dto.UserAccount.CustomerLookupResult;
import common.entity.Bill;
import common.entity.Customer;
import common.entity.Reservation;
import common.entity.SubscriberReportEntry;
import common.entity.Table;
import common.entity.TimeReportEntry;
import common.enums.UserAccountOperation;
import common.enums.EmployeeRole;
import common.enums.LoggedInStatus;
import common.enums.ReservationOperation;
import common.enums.ReservationStatus;
import common.enums.RestaurantManagementOperation;
import dbController.DBController;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGUI.ServerFrameController;
import controllers.UserAccountController;
import controllers.NotificationController;
import controllers.ReservationController;
import controllers.RestaurantManagementController;
import common.dto.RestaurantManagement.RestaurantManagementRequest;
import common.dto.RestaurantManagement.RestaurantManagementResponse;
import common.dto.RestaurantManagement.RestaurantManagementResult;
import common.dto.Report.ReportRequest;
import common.dto.Report.ReportResponse;
import common.enums.ReportOperation;

/**
 * Main class that extends AbstractServer OCSF server class that handles client
 * communication, interacts with the database controller, and updates the JavaFX
 * GUI via ServerUI.
 * 
 * @author Yamen Abu Ahmad
 * @version 1.0
 */

public class Server extends AbstractServer {

	/**
	 * Default port number for server connections.
	 */
	final public static int DEFAULT_PORT = 5555;

	/**
	 * Server user interface for displaying messages and client status.
	 */
	private ServerUI ui;

	/**
	 * Database controller for all database operations.
	 */
	private DBController db;

	/**
	 * Name of the database to connect to.
	 */
	private String dbName;

	/**
	 * Username for database authentication.
	 */
	private String dbUser;

	/**
	 * Password for database authentication.
	 */
	private String dbPassword;

	/**
	 * Counter to assign unique IDs to each connected client.
	 */
	private int clientCounter = 0;

	/**
	 * Controller handling all reservation-related business logic.
	 */
	private ReservationController reservationController;

	/**
	 * Controller handling user account operations including login and registration.
	 */
	private UserAccountController userAccountController;

	/**
	 * Scheduled executor for periodic no-show detection tasks.
	 */
	private ScheduledExecutorService noShowScheduler;

	/**
	 * Scheduled executor for sending reservation reminder notifications.
	 */
	private ScheduledExecutorService reminderScheduler;

	/**
	 * Scheduled executor for automatic billing operations.
	 */
	private ScheduledExecutorService billingScheduler;

	/**
	 * Controller handling notification delivery (email, SMS).
	 */
	private NotificationController notificationController;

	/**
	 * Controller handling restaurant management operations (tables, hours).
	 */
	private RestaurantManagementController restaurantManagementController;

	/**
	 * Controller handling report generation and retrieval.
	 */
	private controllers.ReportController reportController;

	/**
	 * Scheduled executor for monthly report generation tasks.
	 */
	private ScheduledExecutorService reportScheduler;

	/**
	 * Session key for storing subscriber ID in client connection info.
	 */
	private static final String SESSION_SUBSCRIBER_ID = "subscriberId";

	/**
	 * Session key for storing employee ID in client connection info.
	 */
	private static final String SESSION_EMPLOYEE_ID = "employeeId";

	/**
	 * Session key for storing employee role in client connection info.
	 */
	private static final String SESSION_EMPLOYEE_ROLE = "employeeRole";

	/**
	 * Constructs a new Server instance with the specified port and UI.
	 *
	 * @param port the port number to listen on for client connections
	 * @param ui   the server user interface for displaying messages
	 */
	public Server(int port, ServerFrameController ui) {
		super(port);
		this.ui = ui;

		setTimeout(500);
	}

	/**
	 * Configures the database connection parameters.
	 * <p>
	 * This method must be called before starting the server to set up the database
	 * connection information.
	 * </p>
	 *
	 * @param dbName     the name of the database
	 * @param dbUser     the username for database authentication
	 * @param dbPassword the password for database authentication
	 */
	public void setDatabaseConfig(String dbName, String dbUser, String dbPassword) {
		this.dbName = dbName;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
	}

	// Returns Logged In Subscriber ID
	private Integer getSessionSubscriberId(ConnectionToClient client) {
		return (Integer) client.getInfo(SESSION_SUBSCRIBER_ID);
	}

	private void setSessionSubscriberId(ConnectionToClient client, Integer sid) {
		client.setInfo(SESSION_SUBSCRIBER_ID, sid);
	}

	private Integer getSessionEmployeeId(ConnectionToClient client) {
		return (Integer) client.getInfo(SESSION_EMPLOYEE_ID);
	}

	private void setSessionEmployeeId(ConnectionToClient client, Integer empid) {
		client.setInfo(SESSION_EMPLOYEE_ID, empid);
	}

	private EmployeeRole getSessionEmployeeRole(ConnectionToClient client) {
		return (EmployeeRole) client.getInfo(SESSION_EMPLOYEE_ROLE);
	}

	private void setSessionEmployeeRole(ConnectionToClient client, EmployeeRole empRole) {
		client.setInfo(SESSION_EMPLOYEE_ROLE, empRole);
	}

	private void clearSession(ConnectionToClient client) {
		client.setInfo(SESSION_SUBSCRIBER_ID, null);
		client.setInfo(SESSION_EMPLOYEE_ID, null);
		client.setInfo(SESSION_EMPLOYEE_ROLE, null);
	}

	/**
	 * Handles incoming messages from connected clients.
	 * <p>
	 * This method processes request DTOs (ReservationRequest, UserAccountRequest,
	 * RestaurantManagementRequest, ReportRequest) and dispatches them to the
	 * appropriate controller. It manages session state for subscriber and employee
	 * authentication, enforces authorization rules, and sends appropriate responses
	 * back to the client.
	 * </p>
	 *
	 * @param msg    the message object received from the client
	 * @param client the connection to the client that sent the message
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

		try {
			// if DB not initialized
			if (db == null) {
				client.sendToClient(ReservationResponse.fail("Database not configured!", null));
				ui.display("Client attempted Request but DB not configured.");
				return;
			}

			Integer sessionSubscriberId = getSessionSubscriberId(client);
			Integer sessionEmployeeId = getSessionEmployeeId(client);
			EmployeeRole sessionEmployeeRole = getSessionEmployeeRole(client);

			boolean isSubscriberLoggedIn = (sessionSubscriberId != null);
			boolean isEmployeeLoggedIn = (sessionEmployeeId != null && sessionEmployeeRole != null);
			boolean isRepresentativeLoggedIn = isEmployeeLoggedIn && sessionEmployeeRole == EmployeeRole.REPRESENTATIVE;
			boolean isManagerLoggedIn = isEmployeeLoggedIn && sessionEmployeeRole == EmployeeRole.MANAGER;

			if (msg instanceof UserAccountRequest) {

				UserAccountRequest userReq = (UserAccountRequest) msg;

				UserAccountResponse userResp;

				switch (userReq.getOperation()) {

				case LOGOUT:
					if (sessionSubscriberId == null && sessionEmployeeId == null) {
						userResp = UserAccountResponse.alreadyLoggedOut();
					} else {
						clearSession(client);
						userResp = UserAccountResponse.logoutOk();
					}
					break;

				case SUBSCRIBER_LOG_IN:
					clearSession(client);

					SubscriberLogInResult slir = userAccountController
							.LogInBySubscriptionCode(userReq.getSubscriptionCode());

					if (slir.isSuccess()) {
						setSessionSubscriberId(client, slir.getSubscriberId());
						userResp = UserAccountResponse.loginOk(slir.getSubscriberId(), slir.getFullName());
					} else {
						userResp = UserAccountResponse.loginFail(slir.getMessage());
					}
					break;

				case EMPLOYEE_LOG_IN:
					clearSession(client);

					EmployeeLogInResult elir = userAccountController.employeeLogIn(userReq.getUsername(),
							userReq.getPassword());
					if (elir.isSuccess()) {
						setSessionEmployeeId(client, elir.getEmployeeId());
						setSessionEmployeeRole(client, elir.getRole());
						userResp = UserAccountResponse.employeeLoginOk(elir.getEmployeeId(), elir.getRole(),
								elir.getFullName());
					} else {
						userResp = UserAccountResponse.employeeLoginFail(elir.getMessage());
					}
					break;

				case REGISTER_SUBSCRIBER:
					RegisterSubscriberResult rsr = userAccountController.registerSubscriber(userReq.getFullName(),
							userReq.getPhone(), userReq.getEmail());

					userResp = rsr.isSuccess() ? UserAccountResponse.registerOk(rsr.getSubscriptionCode())
							: UserAccountResponse.registerFail(rsr.getMessage());
					break;

				case LOGGED_IN_STATUS:
					if (sessionSubscriberId != null) {
						userResp = UserAccountResponse.statusSubscriber(sessionSubscriberId,
								userAccountController.getFullNameBySubscriberId(sessionSubscriberId));

					} else if (sessionEmployeeId != null && sessionEmployeeRole != null) {

						if (sessionEmployeeRole == EmployeeRole.MANAGER)
							userResp = UserAccountResponse.statusManager(sessionEmployeeId,
									userAccountController.getFullNameByEmployeeId(sessionEmployeeId));
						else
							userResp = UserAccountResponse.statusRep(sessionEmployeeId,
									userAccountController.getFullNameByEmployeeId(sessionEmployeeId));

					} else
						userResp = UserAccountResponse.statusNotLoggedIn();
					break;

				case GET_SUBSCRIBER_PROFILE:
					if (sessionSubscriberId == null) {
						userResp = UserAccountResponse.fail("Not logged in.", userReq.getOperation());
					} else {
						CustomerLookupResult profileResult = userAccountController
								.getSubscriberProfile(sessionSubscriberId);
						userResp = profileResult.isSuccess()
								? UserAccountResponse.subscriberProfileOk(profileResult.getCustomer())
								: UserAccountResponse.subscriberProfileFail(profileResult.getMessage());
					}
					break;

				case UPDATE_SUBSCRIBER_PROFILE:
					if (sessionSubscriberId == null) {
						userResp = UserAccountResponse.updateProfileFail("Not logged in.");
					} else {
						CustomerLookupResult updateResult = userAccountController.updateSubscriberProfile(
								sessionSubscriberId, userReq.getFullName(), userReq.getPhone(), userReq.getEmail());
						userResp = updateResult.isSuccess()
								? UserAccountResponse.updateProfileOk(updateResult.getCustomer())
								: UserAccountResponse.updateProfileFail(updateResult.getMessage());
					}
					break;

				case LOOKUP_CUSTOMER_BY_SUBSCRIPTION_CODE:
					if (!isEmployeeLoggedIn) {
						userResp = UserAccountResponse.fail("Not authorized. Employee login required.",
								userReq.getOperation());
					} else {
						CustomerLookupResult codeResult = userAccountController
								.lookupCustomerBySubscriptionCode(userReq.getSubscriptionCode());
						userResp = codeResult.isSuccess()
								? UserAccountResponse.customerFound(codeResult.getCustomer(), userReq.getOperation())
								: UserAccountResponse.customerNotFound(codeResult.getMessage(), userReq.getOperation());
					}
					break;

				case LOOKUP_CUSTOMER_BY_PHONE:
					if (!isEmployeeLoggedIn) {
						userResp = UserAccountResponse.fail("Not authorized. Employee login required.",
								userReq.getOperation());
					} else {
						CustomerLookupResult phoneResult = userAccountController
								.lookupCustomerByPhone(userReq.getPhone());
						userResp = phoneResult.isSuccess()
								? UserAccountResponse.customerFound(phoneResult.getCustomer(), userReq.getOperation())
								: UserAccountResponse.customerNotFound(phoneResult.getMessage(),
										userReq.getOperation());
					}
					break;

				case LOOKUP_CUSTOMER_BY_EMAIL:
					if (!isEmployeeLoggedIn) {
						userResp = UserAccountResponse.fail("Not authorized. Employee login required.",
								userReq.getOperation());
					} else {
						CustomerLookupResult emailResult = userAccountController
								.lookupCustomerByEmail(userReq.getEmail());
						userResp = emailResult.isSuccess()
								? UserAccountResponse.customerFound(emailResult.getCustomer(), userReq.getOperation())
								: UserAccountResponse.customerNotFound(emailResult.getMessage(),
										userReq.getOperation());
					}
					break;

				case GET_ALL_SUBSCRIBERS:
					if (!isEmployeeLoggedIn) {
						userResp = UserAccountResponse.fail("Not authorized. Employee login required.",
								userReq.getOperation());
					} else {
						try {
							List<Customer> subscribers = userAccountController.getAllSubscribers();
							userResp = UserAccountResponse.subscribersLoaded(subscribers);
						} catch (SQLException e) {
							e.printStackTrace();
							userResp = UserAccountResponse.subscribersLoadFail("Failed to load subscribers.");
						}
					}
					break;

				case GET_CURRENT_DINERS:
					if (!isEmployeeLoggedIn) {
						userResp = UserAccountResponse.fail("Not authorized. Employee login required.",
								userReq.getOperation());
					} else {
						try {
							List<Customer> diners = userAccountController.getCurrentDiners();
							userResp = UserAccountResponse.dinersLoaded(diners);
						} catch (SQLException e) {
							e.printStackTrace();
							userResp = UserAccountResponse.dinersLoadFail("Failed to load current diners.");
						}
					}
					break;

				default:
					userResp = UserAccountResponse.fail("Invalid Operation!", userReq.getOperation());
				}
				client.sendToClient(userResp);// returns response to the client
				return;
			}

			if (msg instanceof ReservationRequest) {
				ReservationRequest resReq = (ReservationRequest) msg;

				ReservationResponse resResp;

				// Determine effective customer ID: employee on-behalf or logged-in subscriber
				Integer effectiveCustomerId = null;
				boolean isOnBehalf = false;

				if (isEmployeeLoggedIn && resReq.getTargetCustomerId() != null) {
					// Employee acting on behalf of customer
					effectiveCustomerId = resReq.getTargetCustomerId();
					isOnBehalf = true;
				} else if (sessionSubscriberId != null) {
					// Regular logged-in subscriber
					effectiveCustomerId = sessionSubscriberId;
				}

				switch (resReq.getOperation()) {

				case GET_ACTIVE_RESERVATIONS:
					if (!isEmployeeLoggedIn) {
						resResp = ReservationResponse.fail("Not authorized.", resReq.getOperation());
						break;
					}
					resResp = ReservationResponse.withReservations(true, "Reservations loaded.",
							reservationController.getAllActiveReservations(), resReq.getOperation());
					break;

				case GET_WAITLIST:
					if (!isEmployeeLoggedIn) {
						resResp = ReservationResponse.fail("Not authorized.", resReq.getOperation());
						break;
					}
					resResp = ReservationResponse.withReservations(true, "Waitlist loaded.",
							reservationController.getWaitlistReservations(), resReq.getOperation());
					break;

				case UPDATE_RESERVATION_FIELDS:
					UpdateReservationResult updateResult = reservationController.updateReservation(
							resReq.getReservationId(), resReq.getReservationDateTime(), resReq.getNumberOfGuests());

					resResp = ReservationResponse.updated(updateResult.isSuccess(), updateResult.getMessage(),
							updateResult.getMessage(), reservationController.getAllActiveReservations(),
							resReq.getOperation());
					break;

				case CREATE_RESERVATION:
					CreateReservationResult r;

					if (effectiveCustomerId != null) {
						r = reservationController.createReservation(effectiveCustomerId,
								resReq.getReservationDateTime(), resReq.getNumberOfGuests());
					} else {
						r = reservationController.createGuestReservation(resReq.getReservationDateTime(),
								resReq.getNumberOfGuests(), resReq.getFullName(), resReq.getPhone(), resReq.getEmail());
					}

					if (r.isSuccess()) {
						resResp = ReservationResponse.created(r.getReservationId(), r.getConfirmationCode(),
								r.getMessage(), resReq.getOperation());

						notificationController.sendReservationConfirmation(r.getReservationId());
					} else
						resResp = ReservationResponse.createFailedWithSuggestions(r.getMessage(), r.getSuggestions(),
								resReq.getOperation());

					break;

				case RESEND_CONFIRMATION_CODE:
					List<Reservation> resList = reservationController.findReservationsByPhoneOrEmail(resReq.getPhone(),
							resReq.getEmail());
					int sentCount = 0;

					if (resList != null) {
						for (Reservation res : resList) {
							if (notificationController.resendReservationConfirmation(res.getReservationId())
									.isSuccess())
								sentCount++;
						}
					}

					resResp = ReservationResponse.resendResult(sentCount, resReq.getOperation());
					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION:
					if (effectiveCustomerId == null) {
						resResp = ReservationResponse.emptyListFail("Please enter confirmation code.",
								resReq.getOperation());
					} else {
						List<Reservation> canList = reservationController
								.loadReservationsForCancellation(effectiveCustomerId);
						resResp = ReservationResponse.loadedOrEmpty(canList, "Your reservations loaded.",
								"No reservations found.", resReq.getOperation());
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CANCELLATION:
					Reservation canReservation = reservationController
							.getReservationForCancellationByCode(resReq.getConfirmationCode());

					resResp = (canReservation != null)
							? ReservationResponse.withReservations(true, "Your reservation loaded.",
									List.of(canReservation), resReq.getOperation())
							: ReservationResponse.withReservations(false, "No reservations found.", List.of(),
									resReq.getOperation());
					break;

				case CANCEL_RESERVATION:
					CancelReservationResult cr;

					cr = reservationController.cancelReservation(resReq.getReservationId());

					if (effectiveCustomerId != null) {
						resResp = ReservationResponse.withReservations(cr.isSuccess(), cr.getMessage(),
								reservationController.loadReservationsForCancellation(effectiveCustomerId),
								resReq.getOperation());
					} else {
						resResp = ReservationResponse.withReservations(cr.isSuccess(), cr.getMessage(), List.of(),
								resReq.getOperation());
					}

					if (cr.isSuccess() && (cr.getReservationStatusBefore() == ReservationStatus.ACTIVE
							|| cr.getReservationStatusBefore() == ReservationStatus.NOTIFIED)) {
						runNotifyCheck();
					}

					if (cr.isSuccess())
						notificationController.sendReservationCanceled(resReq.getReservationId());

					break;

				case JOIN_WAITLIST:
					CreateReservationResult jwr;

					if (effectiveCustomerId != null) {
						jwr = reservationController.joinWaitlist(effectiveCustomerId, resReq.getNumberOfGuests());
					} else {
						jwr = reservationController.joinWaitlistAsGuest(resReq.getNumberOfGuests(),
								resReq.getFullName(), resReq.getPhone(), resReq.getEmail());
					}

					if (jwr.isSuccess()) {
						if (jwr.getMessage() == "RECEIVE_TABLE_NOW")
							notificationController.sendTableAvailable(jwr.getReservationId());

						resResp = ReservationResponse.created(jwr.getReservationId(), jwr.getConfirmationCode(),
								jwr.getMessage(), resReq.getOperation());
					} else {
						resResp = ReservationResponse.fail(jwr.getMessage(), resReq.getOperation());
					}
					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING:
					if (effectiveCustomerId == null) {
						resResp = ReservationResponse.emptyListFail("Please enter confirmation code.",
								resReq.getOperation());
					} else {
						List<Reservation> recList = reservationController
								.loadReservationsForReceiving(effectiveCustomerId);
						resResp = ReservationResponse.loadedOrEmpty(recList, "Your reservations loaded.",
								"No reservations found.", resReq.getOperation());
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_RECEIVING:
					Reservation recReservation = reservationController
							.getReservationForReceivingByCode(resReq.getConfirmationCode());

					resResp = (recReservation != null)
							? ReservationResponse.withReservations(true, "Your reservation loaded.",
									List.of(recReservation), resReq.getOperation())
							: ReservationResponse.withReservations(false, "No reservations found.", List.of(),
									resReq.getOperation());
					break;

				case RECEIVE_TABLE:
					ReceiveTableResult rtr = reservationController.receiveTable(resReq.getReservationId());
					resResp = rtr.isSuccess()
							? ReservationResponse.tableAssigned(rtr.getTableNumber(), rtr.getMessage())
							: ReservationResponse.fail(rtr.getMessage(), resReq.getOperation());

					if (rtr.isSuccess())
						notificationController.sendTableReceived(resReq.getReservationId());
					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT:
					if (effectiveCustomerId == null) {
						resResp = ReservationResponse.emptyListFail("Please enter confirmation code.",
								resReq.getOperation());
					} else {
						List<Reservation> payList = reservationController
								.loadReservationsForCheckout(effectiveCustomerId);
						resResp = ReservationResponse.loadedOrEmpty(payList, "Your reservations loaded.",
								"No reservations found.", resReq.getOperation());
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CHECKOUT:
					Reservation payReservation = reservationController
							.getReservationForCheckoutByCode(resReq.getConfirmationCode());

					resResp = (payReservation != null)
							? ReservationResponse.withReservations(true, "Your reservation loaded.",
									List.of(payReservation), resReq.getOperation())
							: ReservationResponse.withReservations(false, "No reservations found.", List.of(),
									resReq.getOperation());
					break;

				case GET_BILL_FOR_PAYING:
					Bill bill = reservationController.getOrCreateBillForPaying(resReq.getReservationId());
					resResp = ReservationResponse.billLoaded(bill, "Bill loaded.", resReq.getOperation());
					break;

				case PAY_BILL:
					PayBillResult pbr = reservationController.payBillbyId(resReq.getBillId());

					resResp = pbr.isSuccess() ? ReservationResponse.ok(pbr.getMessage(), resReq.getOperation())
							: ReservationResponse.fail(pbr.getMessage(), resReq.getOperation());

					if (pbr.isSuccess() && pbr.getFreedCapacity() > 0)
						runNotifyCheck(pbr.getFreedCapacity());

					if (pbr.isSuccess())
						notificationController.sendPaymentSuccess(pbr.getReservationId());

					break;

				case GET_SUBSCRIBER_HISTORY:
					if (sessionSubscriberId == null) {
						resResp = ReservationResponse.emptyListFail("Please log in to view history.",
								resReq.getOperation());
					} else {
						List<Reservation> historyList = reservationController.getSubscriberHistory(sessionSubscriberId);
						resResp = ReservationResponse.loadedOrEmpty(historyList, "Your reservation history loaded.",
								"No reservations found.", resReq.getOperation());
					}
					break;

				default:
					resResp = ReservationResponse.fail("Unknown operation", resReq.getOperation());

				}
				client.sendToClient(resResp);// returns response to the client
				return;
			}

			if (msg instanceof ReportRequest) {
				ReportRequest repReq = (ReportRequest) msg;
				ReportResponse repResp;

				if (!isManagerLoggedIn) {
					repResp = ReportResponse.fail("Manager login required.", repReq.getOperation());
					client.sendToClient(repResp);
					return;
				}

				switch (repReq.getOperation()) {
				case GET_TIME_REPORT:
					List<TimeReportEntry> timeEntries = reportController.getStoredTimeReport(repReq.getYear(),
							repReq.getMonth());
					repResp = ReportResponse.timeReport(timeEntries, repReq.getYear(), repReq.getMonth());
					break;

				case GET_SUBSCRIBER_REPORT:
					List<SubscriberReportEntry> subEntries = reportController
							.getStoredSubscriberReport(repReq.getYear(), repReq.getMonth());
					repResp = ReportResponse.subscriberReport(subEntries, repReq.getYear(), repReq.getMonth());
					break;

				default:
					repResp = ReportResponse.fail("Unknown report operation", repReq.getOperation());
				}
				client.sendToClient(repResp);
				return;
			}

			if (msg instanceof RestaurantManagementRequest) {
				RestaurantManagementRequest mgrReq = (RestaurantManagementRequest) msg;
				RestaurantManagementResponse mgrResp;

				boolean isReadOnly = mgrReq.getOperation() == RestaurantManagementOperation.GET_OPENING_HOURS
						|| mgrReq.getOperation() == RestaurantManagementOperation.GET_DATE_OVERRIDES;

				if (!isEmployeeLoggedIn && !isReadOnly) {
					mgrResp = RestaurantManagementResponse.fail("Not authorized. Employee login required.",
							mgrReq.getOperation());
					client.sendToClient(mgrResp);
					return;
				}

				switch (mgrReq.getOperation()) {

				// Table operations
				case GET_ALL_TABLES:
					mgrResp = RestaurantManagementResponse.tablesLoaded(restaurantManagementController.getAllTables());
					break;

				case ADD_TABLE:
					RestaurantManagementResult addResult = restaurantManagementController.addTable(mgrReq.getSeats());
					mgrResp = addResult.isSuccess()
							? RestaurantManagementResponse.tableAdded(addResult.getNewTableNumber(),
									restaurantManagementController.getAllTables())
							: RestaurantManagementResponse.fail(addResult.getMessage(), mgrReq.getOperation());
					if (addResult.isSuccess()) {
						runNotifyCheck();
					}
					break;

				case UPDATE_TABLE:
					int tableNum = mgrReq.getTableNumber();
					int newCap = mgrReq.getSeats();

					// 1) conflict check BEFORE update
					RestaurantManagementResult conflict = runConflictCheckForReducedCapacity(tableNum);
					if (!conflict.isSuccess()) {
						mgrResp = RestaurantManagementResponse.fail(conflict.getMessage(), mgrReq.getOperation());
						break;
					}

					// 2) update capacity
					RestaurantManagementResult updateResult = restaurantManagementController.updateTable(tableNum,
							newCap);

					if (!updateResult.isSuccess()) {
						mgrResp = RestaurantManagementResponse.fail(updateResult.getMessage(), mgrReq.getOperation());
						break;
					}

					// 3) revalidate AFTER update
					runPostCapacityReductionRevalidation(tableNum, newCap);

					// 4) response
					mgrResp = RestaurantManagementResponse.tableUpdated(restaurantManagementController.getAllTables());
					break;

				case DELETE_TABLE:
					int tableId = mgrReq.getTableNumber();

					// 1) conflict check BEFORE deletion
					RestaurantManagementResult deleteConflict = runConflictCheckForTableDeletion(tableId);
					if (!deleteConflict.isSuccess()) {
						mgrResp = RestaurantManagementResponse.fail(deleteConflict.getMessage(), mgrReq.getOperation());
						break;
					}

					// 2) delete table
					RestaurantManagementResult deleteResult = restaurantManagementController.deleteTable(tableId);
					if (!deleteResult.isSuccess()) {
						mgrResp = RestaurantManagementResponse.fail(deleteResult.getMessage(), mgrReq.getOperation());
						break;
					}

					// 3) revalidate AFTER deletion
					runPostDeletionCapacityRevalidation();

					// 4) response
					mgrResp = RestaurantManagementResponse.tableDeleted(restaurantManagementController.getAllTables());

					break;

				// Hours operations
				case GET_OPENING_HOURS:

					System.out.println(mgrReq.getOperation() + " before response");

					mgrResp = RestaurantManagementResponse
							.hoursLoaded(restaurantManagementController.getOpeningHours());

					System.out.println(mgrReq.getOperation() + " after response");

					break;

				case UPDATE_OPENING_HOURS:
					RestaurantManagementResult hoursResult = restaurantManagementController.updateOpeningHours(
							mgrReq.getDayOfWeek(), mgrReq.getOpenTime(), mgrReq.getCloseTime(), mgrReq.isClosed());
					mgrResp = hoursResult.isSuccess()
							? RestaurantManagementResponse
									.hoursUpdated(restaurantManagementController.getOpeningHours())
							: RestaurantManagementResponse.fail(hoursResult.getMessage(), mgrReq.getOperation());
					if (hoursResult.isSuccess()) {
						runConflictCheckForHoursChange(mgrReq.getDayOfWeek(), mgrReq.getOpenTime(),
								mgrReq.getCloseTime(), mgrReq.isClosed());
					}
					break;

				// Date override operations
				case GET_DATE_OVERRIDES:
					mgrResp = RestaurantManagementResponse
							.overridesLoaded(restaurantManagementController.getDateOverrides());
					break;

				case ADD_DATE_OVERRIDE:
					RestaurantManagementResult addOvResult = restaurantManagementController.addDateOverride(
							mgrReq.getOverrideDate(), mgrReq.getOpenTime(), mgrReq.getCloseTime(), mgrReq.isClosed(),
							mgrReq.getReason());
					mgrResp = addOvResult.isSuccess()
							? RestaurantManagementResponse
									.overrideAdded(restaurantManagementController.getDateOverrides())
							: RestaurantManagementResponse.fail(addOvResult.getMessage(), mgrReq.getOperation());
					if (addOvResult.isSuccess()) {
						runConflictCheckForDateOverride(mgrReq.getOverrideDate(), mgrReq.getOpenTime(),
								mgrReq.getCloseTime(), mgrReq.isClosed());
					}
					break;

				case UPDATE_DATE_OVERRIDE:
					RestaurantManagementResult updateOvResult = restaurantManagementController.updateDateOverride(
							mgrReq.getOverrideId(), mgrReq.getOverrideDate(), mgrReq.getOpenTime(),
							mgrReq.getCloseTime(), mgrReq.isClosed(), mgrReq.getReason());
					mgrResp = updateOvResult.isSuccess()
							? RestaurantManagementResponse
									.overrideUpdated(restaurantManagementController.getDateOverrides())
							: RestaurantManagementResponse.fail(updateOvResult.getMessage(), mgrReq.getOperation());
					if (updateOvResult.isSuccess()) {
						runConflictCheckForDateOverride(mgrReq.getOverrideDate(), mgrReq.getOpenTime(),
								mgrReq.getCloseTime(), mgrReq.isClosed());
					}
					break;

				case DELETE_DATE_OVERRIDE:
					RestaurantManagementResult deleteOvResult = restaurantManagementController
							.deleteDateOverride(mgrReq.getOverrideId());
					mgrResp = deleteOvResult.isSuccess()
							? RestaurantManagementResponse
									.overrideDeleted(restaurantManagementController.getDateOverrides())
							: RestaurantManagementResponse.fail(deleteOvResult.getMessage(), mgrReq.getOperation());
					break;

				default:
					mgrResp = RestaurantManagementResponse.fail("Unknown operation.", mgrReq.getOperation());
				}
				System.out.println("res sending " + mgrReq.getOperation());
				System.out.println(mgrResp.getMessage() + mgrResp.getOperation());
				client.sendToClient(mgrResp);
				return;
			}

		} catch (SQLException e) {
			ui.display("SQL Error: " + e.getMessage());
			e.printStackTrace();
			try {
				client.sendToClient(ReservationResponse.fail("Database error occurred", null));
			} catch (Exception ignored) {
			}
		} catch (Exception e) {
			ui.display("Unexpected error: " + e.getMessage());
		}
	}

	/**
	 * Called when a new client connects to the server.
	 * <p>
	 * Assigns a unique ID to the client, stores connection information, and updates
	 * the UI with the new client status.
	 * </p>
	 *
	 * @param client the connection to the newly connected client
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		clientCounter++;
		client.setInfo("id", clientCounter);

		String host = client.getInetAddress().getHostName();
		String ip = client.getInetAddress().getHostAddress();

		client.setInfo("host", host);
		client.setInfo("ip", ip);

		ui.updateClientStatus(String.valueOf(clientCounter), host, ip, "CONNECTED");
	}

	/**
	 * Called when a client disconnects from the server.
	 * <p>
	 * Updates the UI to reflect the client's disconnected status.
	 * </p>
	 *
	 * @param client the connection to the disconnected client
	 */
	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		Integer id = (Integer) client.getInfo("id");
		if (id == null)
			return;

		ui.updateClientStatus(String.valueOf(id), (String) client.getInfo("host"), (String) client.getInfo("ip"),
				"DISCONNECTED");
	}

	/**
	 * Called when an exception occurs while listening for connections.
	 * <p>
	 * Displays an error message and stops listening on the port.
	 * </p>
	 *
	 * @param exception the exception that occurred
	 */
	@Override
	protected void listeningException(Throwable exception) {
		ui.display("STATUS: SERVER ERROR, STOPPED LISTENING: " + exception.getMessage());
		stopListening();
	}

	/**
	 * Called when the server has successfully started listening for connections.
	 * <p>
	 * Initializes the database connection, creates all business logic controllers,
	 * and starts scheduled tasks for no-show detection, reminders, billing, and
	 * monthly report generation.
	 * </p>
	 */
	@Override
	protected void serverStarted() {
		System.out.println("Server started on port: " + getPort());

		try {
			db = new DBController(dbName, dbUser, dbPassword);
			ui.display("Database connection initialized.");
		} catch (Exception e) {
			ui.display("Database initialization failed: " + e.getMessage());
		}

		reservationController = new ReservationController(db);
		userAccountController = new UserAccountController(db);
		restaurantManagementController = new RestaurantManagementController(db);
		notificationController = new NotificationController(ui, db);

		reportController = new controllers.ReportController(db);

		noShowScheduler = Executors.newSingleThreadScheduledExecutor();
		noShowScheduler.scheduleAtFixedRate(() -> {
			try {
				runNoShowCheck();
			} catch (Exception e) {
				ui.display("No-Show check error: " + e.getMessage());
			}
		}, 10, 60, TimeUnit.SECONDS); // start after 10s, then every 60s

		reminderScheduler = Executors.newSingleThreadScheduledExecutor();
		reminderScheduler.scheduleAtFixedRate(() -> {
			try {
				runReminderCheck();
			} catch (Exception e) {
				ui.display("Reminder error: " + e.getMessage());
			}
		}, 10, 60, TimeUnit.SECONDS);

		billingScheduler = Executors.newSingleThreadScheduledExecutor();
		billingScheduler.scheduleAtFixedRate(() -> {
			try {
				runBillingCheck();
			} catch (Exception e) {
				ui.display("Billing check error: " + e.getMessage());
			}
		}, 10, 60, TimeUnit.SECONDS);

		reportScheduler = Executors.newSingleThreadScheduledExecutor();
		reportScheduler.scheduleAtFixedRate(() -> {
			try {
				runMonthlyReportCheck();
			} catch (Exception e) {
				ui.display("Report generation error: " + e.getMessage());
			}
		}, 30, 3600, TimeUnit.SECONDS); // start after 30s, check every hour

	}

	/**
	 * Called when an exception occurs in a client connection.
	 * <p>
	 * Updates the client status to disconnected in the UI.
	 * </p>
	 *
	 * @param client    the connection where the exception occurred
	 * @param exception the exception that was thrown
	 */
	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {
		Integer id = (Integer) client.getInfo("id");
		if (id == null)
			return;

		ui.updateClientStatus(String.valueOf(id), (String) client.getInfo("host"), (String) client.getInfo("ip"),
				"DISCONNECTED");
	}

	/**
	 * Called when the server stops listening for connections.
	 * <p>
	 * Shuts down all scheduled tasks (no-show, reminder, billing, report) and
	 * updates the UI to reflect the stopped state.
	 * </p>
	 */
	@Override
	protected void serverStopped() {
		ui.display("Server stopped.");

		// Remove all clients
		ui.updateClientStatus("ALL", "", "", "DISCONNECTED");

		if (noShowScheduler != null) {
			noShowScheduler.shutdownNow();
			noShowScheduler = null;
		}

		if (reminderScheduler != null) {
			reminderScheduler.shutdownNow();
			reminderScheduler = null;
		}

		if (billingScheduler != null) {
			billingScheduler.shutdownNow();
			billingScheduler = null;
		}

		if (reportScheduler != null) {
			reportScheduler.shutdownNow();
			reportScheduler = null;
		}

	}

	private void runNoShowCheck() throws SQLException {
		List<Integer> ids = reservationController.getNoShowReservationIds();
		if (ids.isEmpty())
			return;

		int canceledCount = 0;
		for (Integer id : ids) {
			boolean ok = reservationController.cancelNoShowReservation(id);
			if (ok) {
				canceledCount++;
				notificationController.sendReservationCanceledDueToNoShow(id);
			}
		}

		if (canceledCount > 0) {
			ui.display("No-Show: auto-canceled " + canceledCount + " reservations.");
			runNotifyCheck();
		}

	}

	private void runReminderCheck() throws SQLException {
		List<Integer> ids = reservationController.getReservationsForReminder();
		if (ids.isEmpty())
			return;

		for (Integer id : ids) {
			notificationController.sendReservationReminder(id);
			reservationController.markReminderSent(id);
		}
	}

	private void runBillingCheck() throws SQLException {
		List<Reservation> res = reservationController.getReservationsForBilling();
		if (res.isEmpty())
			return;

		int sent = 0;

		for (Reservation r : res) {
			Bill bill = reservationController.computeBill(r);
			if (bill != null) {
				sent++;
				notificationController.sendBillSent(r.getReservationId(), bill);
			}
		}
		if (sent > 0)
			ui.display("💳 Bills sent: " + sent);
	}

	private void runNotifyCheck() throws SQLException {
		Integer notifiedReservationId = null;
		while ((notifiedReservationId = reservationController.notifyNextFromWaitlist()) != null)
			notificationController.sendTableAvailable(notifiedReservationId);
	}

	private void runNotifyCheck(int freedCapacity) throws SQLException {
		Integer notifiedReservationId = null;
		while ((notifiedReservationId = reservationController.notifyNextFromWaitlist(freedCapacity)) != null)
			notificationController.sendTableAvailable(notifiedReservationId);
	}

	private void runConflictCheckForHoursChange(java.time.DayOfWeek day, java.time.LocalTime openTime,
			java.time.LocalTime closeTime, boolean closed) throws SQLException {
		List<Integer> cancelled = reservationController.cancelReservationsOutsideHours(day, openTime, closeTime,
				closed);
		for (Integer resId : cancelled) {
			notificationController.sendReservationCanceledDueToHoursChange(resId);
		}
		if (!cancelled.isEmpty()) {
			ui.display("Hours change: auto-canceled " + cancelled.size() + " reservations outside new hours.");
			runNotifyCheck();
		}
	}

	private void runConflictCheckForDateOverride(java.time.LocalDate date, java.time.LocalTime openTime,
			java.time.LocalTime closeTime, boolean closed) throws SQLException {
		List<Integer> cancelled = reservationController.cancelReservationsOutsideHoursOnDate(date, openTime, closeTime,
				closed);
		for (Integer resId : cancelled) {
			notificationController.sendReservationCanceledDueToDateOverride(resId);
		}
		if (!cancelled.isEmpty()) {
			ui.display("Date override: auto-canceled " + cancelled.size() + " reservations on " + date + ".");
			runNotifyCheck();
		}
	}

	private RestaurantManagementResult runConflictCheckForTableDeletion(int tableNumber) throws SQLException {
		if (reservationController.hasActiveReservationsOnTable(tableNumber)) {
			return RestaurantManagementResult
					.fail("Cannot delete table: there are reservations currently in progress or notified.");
		}
		return RestaurantManagementResult.ok("No conflict detected");
	}

	private void runPostDeletionCapacityRevalidation() throws SQLException {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime to = now.plusMonths(1);

		List<Integer> moved = reservationController.revalidateFutureActiveReservations(now, to);

		for (Integer resId : moved) {
			notificationController.sendReservationMovedToWaiting(resId);
		}

		if (!moved.isEmpty()) {
			ui.display("Table deletion: moved " + moved.size()
					+ " future reservations to WAITING due to capacity change.");
			runNotifyCheck();
		}
	}

	private RestaurantManagementResult runConflictCheckForReducedCapacity(int tableNumber) throws SQLException {
		if (reservationController.hasActiveReservationsOnTable(tableNumber)) {
			return RestaurantManagementResult.fail(
					"Cannot reduce capacity: there are reservations currently in progress or notified on this table.");
		}
		return RestaurantManagementResult.ok("No conflict detected");
	}

	private void runPostCapacityReductionRevalidation(int tableNumber, int newCapacity) throws SQLException {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime to = now.plusMonths(1);

		List<Integer> moved = reservationController.revalidateFutureActiveReservations(now, to);

		for (Integer resId : moved) {
			notificationController.sendReservationMovedToWaiting(resId);
		}

		if (!moved.isEmpty()) {
			ui.display("Capacity reduced (table " + tableNumber + " -> " + newCapacity + "): moved " + moved.size()
					+ " future reservations to WAITING due to capacity change.");
			runNotifyCheck();
		}
	}

	private void runMonthlyReportCheck() throws SQLException {
		LocalDate today = LocalDate.now();
		if (today.getDayOfMonth() != 1) {
			return;
		}

		LocalDate lastMonth = today.minusMonths(1);
		int year = lastMonth.getYear();
		int month = lastMonth.getMonthValue();

		boolean timeExists = reportController.hasStoredTimeReport(year, month);
		boolean subExists = reportController.hasStoredSubscriberReport(year, month);

		if (!timeExists) {
			ui.display("Generating time report for " + month + "/" + year + "...");
			reportController.generateAndStoreTimeReport(year, month);
			ui.display("Time report generated for " + month + "/" + year);
		}

		if (!subExists) {
			ui.display("Generating subscriber report for " + month + "/" + year + "...");
			reportController.generateAndStoreSubscriberReport(year, month);
			ui.display("Subscriber report generated for " + month + "/" + year);
		}
	}

}