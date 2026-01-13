package server;

import java.sql.SQLException;
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
import common.dto.UserAccount.UserAccountRequest;
import common.dto.UserAccount.UserAccountResponse;
import common.dto.UserAccount.SubscriberLogInResult;
import common.dto.UserAccount.EmployeeLogInResult;
import common.dto.UserAccount.RegisterSubscriberResult;
import common.entity.Bill;
import common.entity.Customer;
import common.entity.Reservation;
import common.enums.UserAccountOperation;
import common.enums.EmployeeRole;
import common.enums.LoggedInStatus;
import common.enums.ReservationOperation;
import common.enums.ReservationStatus;
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

/**
 * Main class that extends AbstractServer OCSF server class that handles client
 * communication, interacts with the database controller, and updates the JavaFX
 * GUI via ServerUI.
 * 
 * @author Yamen Abu Ahmad
 * @version 1.0
 */

public class Server extends AbstractServer {

	final public static int DEFAULT_PORT = 5555;

	private ServerUI ui; // server user interface

	private DBController db;// Data Base

	private String dbName;// DB name

	private String dbUser;// User of the DB

	private String dbPassword;// DB password

	private int clientCounter = 0;// to give special id to each client

	private ReservationController reservationController;

	private UserAccountController userAccountController;

	private ScheduledExecutorService noShowScheduler;

	private ScheduledExecutorService reminderScheduler;

	private ScheduledExecutorService billingScheduler;

	private NotificationController notificationController;

	private RestaurantManagementController restaurantManagementController;

	private static final String SESSION_SUBSCRIBER_ID = "subscriberId";
	private static final String SESSION_EMPLOYEE_ID = "employeeId";
	private static final String SESSION_EMPLOYEE_ROLE = "employeeRole";

	/* server constructor */
	public Server(int port, ServerFrameController ui) {
		super(port);
		this.ui = ui;

		setTimeout(500); // check every 0.5 sec if clients are alive
	}

	// sets DataBase info inputed from the user
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

	/*
	 * HANDLE MESSAGES FROM CLIENTS, it gets messages as instances of the class
	 * ReservationRequest it checks whats the specific operation that the client
	 * whants in return to the client a response in an instance of
	 * ReservationResponse class
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

		try {
			// if DB not initialized
			if (db == null) {
				client.sendToClient(ReservationResponse.fail("Database not configured!"));
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
						userResp = UserAccountResponse.fail("Not logged in.");
					} else {
						Customer c = db.getSubscribedCustomerById(sessionSubscriberId);
						userResp = (c != null) ? UserAccountResponse.subscriberProfileOk(c)
								: UserAccountResponse.subscriberProfileFail("Subscriber not found.");
					}
					break;

				case LOOKUP_CUSTOMER_BY_SUBSCRIPTION_CODE:
					if (!isEmployeeLoggedIn) {
						userResp = UserAccountResponse.fail("Not authorized. Employee login required.");
					} else {
						Customer cByCode = userAccountController
								.lookupCustomerBySubscriptionCode(userReq.getSubscriptionCode());
						userResp = (cByCode != null) ? UserAccountResponse.customerFound(cByCode)
								: UserAccountResponse.customerNotFound("Customer not found by subscription code.");
					}
					break;

				case LOOKUP_CUSTOMER_BY_PHONE:
					if (!isEmployeeLoggedIn) {
						userResp = UserAccountResponse.fail("Not authorized. Employee login required.");
					} else {
						Customer cByPhone = userAccountController.lookupCustomerByPhone(userReq.getPhone());
						userResp = (cByPhone != null) ? UserAccountResponse.customerFound(cByPhone)
								: UserAccountResponse.customerNotFound("Customer not found by phone.");
					}
					break;

				case LOOKUP_CUSTOMER_BY_EMAIL:
					if (!isEmployeeLoggedIn) {
						userResp = UserAccountResponse.fail("Not authorized. Employee login required.");
					} else {
						Customer cByEmail = userAccountController.lookupCustomerByEmail(userReq.getEmail());
						userResp = (cByEmail != null) ? UserAccountResponse.customerFound(cByEmail)
								: UserAccountResponse.customerNotFound("Customer not found by email.");
					}
					break;

				default:
					userResp = UserAccountResponse.fail("Invalid Operation!");
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

				if (isEmployeeLoggedIn && resReq.getTargetCustomerId() > 0) {
					// Employee acting on behalf of customer
					effectiveCustomerId = resReq.getTargetCustomerId();
					isOnBehalf = true;
				} else if (sessionSubscriberId != null) {
					// Regular logged-in subscriber
					effectiveCustomerId = sessionSubscriberId;
				}

				switch (resReq.getOperation()) {

				case GET_ALL_RESERVATIONS:
					if (!isEmployeeLoggedIn) {
						resResp = ReservationResponse.fail("Not authorized.");
						break;
					}
					resResp = ReservationResponse.withReservations(true, "Reservations loaded.",
							reservationController.getAllReservations());
					break;

				case GET_WAITLIST:
					if (!isEmployeeLoggedIn) {
						resResp = ReservationResponse.fail("Not authorized.");
						break;
					}
					resResp = ReservationResponse.withReservations(true, "Waitlist loaded.",
							reservationController.getWaitlistReservations());
					break;

				case UPDATE_RESERVATION_FIELDS:
					boolean ok = reservationController.updateReservation(resReq.getReservationId(),
							resReq.getReservationDateTime(), resReq.getNumberOfGuests());

					resResp = ReservationResponse.updated(ok, "Reservation updated.", "Reservation not found.",
							reservationController.getAllReservations());

					// checks if the Reservation was updated correctly and returns a response
					// according to the result
					break;

				case CREATE_RESERVATION:
					CreateReservationResult r;

					if (effectiveCustomerId != null) {
						r = reservationController.createReservation(sessionSubscriberId,
								resReq.getReservationDateTime(), resReq.getNumberOfGuests());
					} else {
						r = reservationController.createGuestReservation(resReq.getReservationDateTime(),
								resReq.getNumberOfGuests(), resReq.getFullName(), resReq.getPhone(), resReq.getEmail());
					}

					if (r.isSuccess()) {
						resResp = ReservationResponse.created(r.getReservationId(), r.getConfirmationCode(),
								r.getMessage());

						notificationController.sendReservationConfirmation(r.getReservationId());
					} else
						resResp = ReservationResponse.createFailedWithSuggestions(r.getMessage(), r.getSuggestions());

					break;

				case RESEND_CONFIRMATION_CODE:
					List<Reservation> resList = db.findReservationsByPhoneOrEmail(resReq.getPhone(), resReq.getEmail());
					int sentCount = 0;

					if (resList != null) {
						for (Reservation res : resList) {
							if (notificationController.resendReservationConfirmation(res.getReservationId()))
								sentCount++;
						}
					}

					resResp = ReservationResponse.resendResult(sentCount);
					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION:
					if (effectiveCustomerId == null) {
						resResp = ReservationResponse.emptyListFail("Please enter confirmation code.");
					} else {
						List<Reservation> canList = reservationController
								.loadReservationsForCancellation(effectiveCustomerId);
						resResp = ReservationResponse.loadedOrEmpty(canList, "Your reservations loaded.",
								"No reservations found.");
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CANCELLATION:
					Reservation canReservation = reservationController
							.getReservationForCancellationByCode(resReq.getConfirmationCode());

					resResp = (canReservation != null)
							? ReservationResponse.withReservations(true, "Your reservation loaded.",
									List.of(canReservation))
							: ReservationResponse.withReservations(false, "No reservations found.", List.of());
					break;

				case CANCEL_RESERVATION:
					CancelReservationResult cr;

					cr = reservationController.cancelReservation(resReq.getReservationId());

					if (effectiveCustomerId != null) {
						resResp = ReservationResponse.withReservations(cr.isSuccess(), cr.getMessage(),
								reservationController.loadReservationsForCancellation(effectiveCustomerId));
					} else {
						resResp = ReservationResponse.withReservations(cr.isSuccess(), cr.getMessage(), List.of());
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
						resResp = ReservationResponse.created(jwr.getReservationId(), jwr.getConfirmationCode(),
								jwr.getMessage());
					} else {
						resResp = ReservationResponse.fail(jwr.getMessage());
					}
					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING:
					if (effectiveCustomerId == null) {
						resResp = ReservationResponse.emptyListFail("Please enter confirmation code.");
					} else {
						List<Reservation> recList = reservationController
								.loadReservationsForReceiving(effectiveCustomerId);
						resResp = ReservationResponse.loadedOrEmpty(recList, "Your reservations loaded.",
								"No reservations found.");
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_RECEIVING:
					Reservation recReservation = reservationController
							.getReservationForReceivingByCode(resReq.getConfirmationCode());

					resResp = (recReservation != null)
							? ReservationResponse.withReservations(true, "Your reservation loaded.",
									List.of(recReservation))
							: ReservationResponse.withReservations(false, "No reservations found.", List.of());
					break;

				case RECEIVE_TABLE:
					ReceiveTableResult rtr = reservationController.receiveTable(resReq.getReservationId());
					resResp = rtr.isSuccess() ? ReservationResponse.ok(rtr.getMessage())
							: ReservationResponse.fail(rtr.getMessage());

					if (rtr.isSuccess())
						notificationController.sendTableReceived(resReq.getReservationId());
					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT:
					if (effectiveCustomerId == null) {
						resResp = ReservationResponse.emptyListFail("Please enter confirmation code.");
					} else {
						List<Reservation> payList = reservationController
								.loadReservationsForCheckout(effectiveCustomerId);
						resResp = ReservationResponse.loadedOrEmpty(payList, "Your reservations loaded.",
								"No reservations found.");
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CHECKOUT:
					Reservation payReservation = reservationController
							.getReservationForCheckoutByCode(resReq.getConfirmationCode());

					resResp = (payReservation != null)
							? ReservationResponse.withReservations(true, "Your reservation loaded.",
									List.of(payReservation))
							: ReservationResponse.withReservations(false, "No reservations found.", List.of());
					break;

				case GET_BILL_FOR_PAYING:
					Bill bill = reservationController.getOrCreateBillForPaying(resReq.getReservationId());
					resResp = ReservationResponse.billLoaded(bill, "Bill loaded.");
					break;

				case PAY_BILL:
					PayBillResult pbr = reservationController.payBillbyId(resReq.getBillId());

					resResp = pbr.isSuccess() ? ReservationResponse.ok(pbr.getMessage())
							: ReservationResponse.fail(pbr.getMessage());

					if (pbr.isSuccess() && pbr.getFreedCapacity() > 0)
						runNotifyCheck(pbr.getFreedCapacity());

					if (pbr.isSuccess())
						notificationController.sendPaymentSuccess(pbr.getReservationId());

					break;

				default:
					resResp = ReservationResponse.fail("Unknown operation");

				}
				client.sendToClient(resResp);// returns response to the client
				return;
			}

			if (msg instanceof RestaurantManagementRequest) {
				RestaurantManagementRequest mgrReq = (RestaurantManagementRequest) msg;
				RestaurantManagementResponse mgrResp;

				if (!isEmployeeLoggedIn) {
					mgrResp = RestaurantManagementResponse.fail("Not authorized. Employee login required.");
					client.sendToClient(mgrResp);
					return;
				}

				switch (mgrReq.getOperation()) {
				// Table operations
				case GET_ALL_TABLES:
					mgrResp = RestaurantManagementResponse.tablesLoaded(restaurantManagementController.getAllTables());
					break;

				case ADD_TABLE:
					int newTableNum = restaurantManagementController.addTable(mgrReq.getSeats());
					mgrResp = newTableNum > 0
							? RestaurantManagementResponse.tableAdded(newTableNum,
									restaurantManagementController.getAllTables())
							: RestaurantManagementResponse.fail("Failed to add table.");
					break;

				case UPDATE_TABLE:
					boolean tableUpdated = restaurantManagementController.updateTable(mgrReq.getTableNumber(),
							mgrReq.getSeats());
					mgrResp = tableUpdated
							? RestaurantManagementResponse.tableUpdated(restaurantManagementController.getAllTables())
							: RestaurantManagementResponse.fail("Table not found.");
					break;

				case DELETE_TABLE:
					boolean tableDeleted = restaurantManagementController.deleteTable(mgrReq.getTableNumber());
					mgrResp = tableDeleted
							? RestaurantManagementResponse.tableDeleted(restaurantManagementController.getAllTables())
							: RestaurantManagementResponse.fail("Table not found or in use.");
					break;

				// Hours operations
				case GET_OPENING_HOURS:
					mgrResp = RestaurantManagementResponse
							.hoursLoaded(restaurantManagementController.getOpeningHours());
					break;

				case UPDATE_OPENING_HOURS:
					boolean hoursUpdated = restaurantManagementController.updateOpeningHours(mgrReq.getDayOfWeek(),
							mgrReq.getOpenTime(), mgrReq.getCloseTime(), mgrReq.isClosed());
					mgrResp = hoursUpdated
							? RestaurantManagementResponse
									.hoursUpdated(restaurantManagementController.getOpeningHours())
							: RestaurantManagementResponse.fail("Failed to update hours.");
					break;

				// Date override operations
				case GET_DATE_OVERRIDES:
					mgrResp = RestaurantManagementResponse
							.overridesLoaded(restaurantManagementController.getDateOverrides());
					break;

				case ADD_DATE_OVERRIDE:
					int ovId = restaurantManagementController.addDateOverride(mgrReq.getOverrideDate(),
							mgrReq.getOpenTime(), mgrReq.getCloseTime(), mgrReq.isClosed(), mgrReq.getReason());
					mgrResp = ovId > 0
							? RestaurantManagementResponse
									.overrideAdded(restaurantManagementController.getDateOverrides())
							: RestaurantManagementResponse.fail("Failed to add override.");
					break;

				case UPDATE_DATE_OVERRIDE:
					boolean ovUpdated = restaurantManagementController.updateDateOverride(mgrReq.getOverrideId(),
							mgrReq.getOverrideDate(), mgrReq.getOpenTime(), mgrReq.getCloseTime(), mgrReq.isClosed(),
							mgrReq.getReason());
					mgrResp = ovUpdated
							? RestaurantManagementResponse
									.overrideUpdated(restaurantManagementController.getDateOverrides())
							: RestaurantManagementResponse.fail("Override not found.");
					break;

				case DELETE_DATE_OVERRIDE:
					boolean ovDeleted = restaurantManagementController.deleteDateOverride(mgrReq.getOverrideId());
					mgrResp = ovDeleted
							? RestaurantManagementResponse
									.overrideDeleted(restaurantManagementController.getDateOverrides())
							: RestaurantManagementResponse.fail("Override not found.");
					break;

				default:
					mgrResp = RestaurantManagementResponse.fail("Unknown operation.");
				}
				client.sendToClient(mgrResp);
				return;
			}

		} catch (SQLException e) {
			ui.display("SQL Error: " + e.getMessage());
			e.printStackTrace();
			try {
				client.sendToClient(ReservationResponse.fail("Database error occurred"));
			} catch (Exception ignored) {
			}
		} catch (Exception e) {
			ui.display("Unexpected error: " + e.getMessage());
		}
	}

	@Override
	protected void clientConnected(ConnectionToClient client) {
		clientCounter++;
		client.setInfo("id", clientCounter); // assign unique ID

		String host = client.getInetAddress().getHostName();
		String ip = client.getInetAddress().getHostAddress();

		// saves host and ip in the client object
		client.setInfo("host", host);
		client.setInfo("ip", ip);

		ui.updateClientStatus(String.valueOf(clientCounter), host, ip, "CONNECTED");
	}

	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		Integer id = (Integer) client.getInfo("id");
		if (id == null)
			return;

		ui.updateClientStatus(String.valueOf(id), (String) client.getInfo("host"), (String) client.getInfo("ip"),
				"DISCONNECTED");
	}

	@Override
	protected void listeningException(Throwable exception) {
		ui.display("STATUS: SERVER ERROR, STOPPED LISTENING: " + exception.getMessage());
		stopListening();
	}

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
		notificationController = new NotificationController(ui, db);

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

	}

	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {
		Integer id = (Integer) client.getInfo("id");
		if (id == null)
			return;

		ui.updateClientStatus(String.valueOf(id), (String) client.getInfo("host"), (String) client.getInfo("ip"),
				"DISCONNECTED");
	}

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

	}

	private void runNoShowCheck() throws SQLException {
		List<Integer> ids = db.getNoShowReservationIds();
		if (ids.isEmpty())
			return;

		int canceledCount = 0;
		for (Integer id : ids) {
			boolean ok = db.updateReservationStatus(id, ReservationStatus.CANCELED.name());
			if (ok) {
				canceledCount++;
				notificationController.sendReservationCanceled(id);
			}
		}

		if (canceledCount > 0) {
			ui.display("No-Show: auto-canceled " + canceledCount + " reservations.");
			runNotifyCheck();
		}

	}

	private void runReminderCheck() throws SQLException {
		List<Integer> ids = db.getReservationsForReminder();
		if (ids.isEmpty())
			return;

		for (Integer id : ids) {
			notificationController.sendReservationReminder(id);
			db.markReminderSent(id);
		}
	}

	private void runBillingCheck() throws SQLException {
		List<Reservation> res = db.getReservationsForBilling();
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

}
