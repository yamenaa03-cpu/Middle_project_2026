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
import common.dto.UserAccount.LogInResult;
import common.dto.UserAccount.RegisterSubscriberResult;
import common.entity.Bill;
import common.entity.Reservation;
import common.enums.UserAccountOperation;
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
	private Integer getSessionsubscriberId(ConnectionToClient client) {
		return (Integer) client.getInfo("subscriberId");
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
				client.sendToClient(new ReservationResponse(false, "Database not configured!"));
				ui.display("Client attempted Request but DB not configured.");
				return;
			}

			if (msg instanceof UserAccountRequest) {

				UserAccountRequest userReq = (UserAccountRequest) msg;

				UserAccountResponse userResp;

				switch (userReq.getOperation()) {

				case LOGOUT:
					if (client.getInfo("subscriberId") == null)
						userResp = new UserAccountResponse(false, "Already logged out", null,
								LoggedInStatus.NOT_LOGGED_IN);
					else {
						client.setInfo("subscriberId", null);

						userResp = new UserAccountResponse(true, "Logged out successfully.", null,
								LoggedInStatus.NOT_LOGGED_IN);
					}

					break;

				case SUBSCRIPTION_CODE:
					LogInResult r;
					r = userAccountController.LogInBySubscriptionCode(userReq.getSubscriptionCode());

					if (r.isSuccess())
						client.setInfo("subscriberId", r.getSubscriberId());

					userResp = new UserAccountResponse(r.isSuccess(), r.getMessage(), r.getFullName(),
							LoggedInStatus.SUBSCRIBER);

					break;

				case REGISTER_SUBSCRIBER:
					RegisterSubscriberResult rsr = userAccountController.registerSubscriber(userReq.getFullName(),
							userReq.getPhone(), userReq.getEmail());

					if (rsr.isSuccess())
						userResp = new UserAccountResponse(true, rsr.getMessage(), rsr.getSubscriptionCode());
					else
						userResp = new UserAccountResponse(false, rsr.getMessage(), null);
					break;

				case LOGGED_IN_STATUS:
					if (client.getInfo("subscriberId") == null)
						userResp = new UserAccountResponse(true, "Not logged in", null, LoggedInStatus.NOT_LOGGED_IN);
					else
						userResp = new UserAccountResponse(true, "Subscriber is logged in", null,
								LoggedInStatus.SUBSCRIBER);

					break;

				default:
					userResp = new UserAccountResponse(false, "Invalid Operation!", null, null);
				}
				client.sendToClient(userResp);// returns response to the client
				return;
			}

			if (msg instanceof ReservationRequest) {
				ReservationRequest resReq = (ReservationRequest) msg;

				Integer sessionSubscriberId = (Integer) client.getInfo("subscriberId");

				ReservationResponse resResp;

				switch (resReq.getOperation()) {

				case GET_ALL_RESERVATIONS:
					resResp = new ReservationResponse(true, "Reservations loaded.",
							reservationController.getAllReservations());
					break;

				case UPDATE_RESERVATION_FIELDS:
					boolean ok = reservationController.updateReservation(resReq.getReservationId(),
							resReq.getReservationDateTime(), resReq.getNumberOfGuests());

					resResp = new ReservationResponse(ok, ok ? "Reservation updated." : "Reservation not found.",
							reservationController.getAllReservations());
					// checks if the Reservation was updated correctly and returns a response
					// according to the result
					break;

				case CREATE_RESERVATION:
					CreateReservationResult r;

					if (sessionSubscriberId != null) {
						r = reservationController.createReservation(sessionSubscriberId,
								resReq.getReservationDateTime(), resReq.getNumberOfGuests());
					} else {
						r = reservationController.createGuestReservation(resReq.getReservationDateTime(),
								resReq.getNumberOfGuests(), resReq.getFullName(), resReq.getPhone(), resReq.getEmail());
					}

					if (r.isSuccess()) {
						resResp = new ReservationResponse(true, r.getMessage(), r.getReservationId(),
								r.getConfirmationCode(), List.of());

						notificationController.sendReservationConfirmation(r.getReservationId());
					} else
						resResp = new ReservationResponse(false, r.getMessage(), null, null, r.getSuggestions());

					break;

				case RESEND_CONFIRMATION_CODE:
					List<Reservation> resList = db.findReservationsByPhoneOrEmail(resReq.getPhone(), resReq.getEmail());
					int sentCount = 0;
					if (resList == null || resList.isEmpty())
						resResp = new ReservationResponse(false, "No reservations found.", null, null, null);
					else {
						for (Reservation res : resList) {
							if (notificationController.resendReservationConfirmation(res.getReservationId()))
								sentCount++;
						}
						if (sentCount > 0)
							resResp = new ReservationResponse(true, "Sent " + sentCount + "code/s.", null, null, null);
						else
							resResp = new ReservationResponse(false, "No reservations found.", null, null, null);
					}
					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION:
					if (sessionSubscriberId == null)
						resResp = new ReservationResponse(false, "Please enter confirmation code.", List.of());
					else {
						List<Reservation> canList = reservationController
								.loadReservationsForCancellation(sessionSubscriberId);

						if (canList == null || canList.isEmpty()) {
							resResp = new ReservationResponse(false, "No reservations found.", List.of());
						} else {
							resResp = new ReservationResponse(true, "Your reservations loaded.", canList);
						}
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CANCELLATION:
					Reservation canReservation = reservationController
							.getReservationForCancellationByCode(resReq.getConfirmationCode());

					if (canReservation != null)
						resResp = new ReservationResponse(true, "Your reservation loaded.", List.of(canReservation));
					else
						resResp = new ReservationResponse(false, "No reservations found.", List.of());
					break;

				case CANCEL_RESERVATION:
					CancelReservationResult cr;

					cr = reservationController.cancelReservation(resReq.getReservationId());

					if (sessionSubscriberId != null) {
						resResp = new ReservationResponse(cr.isSuccess(), cr.getMessage(),
								reservationController.loadReservationsForCancellation(sessionSubscriberId));
					} else {
						resResp = new ReservationResponse(cr.isSuccess(), cr.getMessage(), List.of());
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

					if (sessionSubscriberId != null) {
						// Subscriber path
						jwr = reservationController.joinWaitlist(sessionSubscriberId, resReq.getNumberOfGuests());
					} else {
						// Guest path (no session saved)
						jwr = reservationController.joinWaitlistAsGuest(resReq.getNumberOfGuests(),
								resReq.getFullName(), resReq.getPhone(), resReq.getEmail());
					}

					if (jwr.isSuccess()) {
						resResp = new ReservationResponse(true, jwr.getMessage(), jwr.getReservationId(),
								jwr.getConfirmationCode(), List.of());

					} else {
						resResp = new ReservationResponse(false, jwr.getMessage(), null, null, List.of());
					}
					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING:
					if (sessionSubscriberId == null)
						resResp = new ReservationResponse(false, "Please enter confirmation code.", List.of());
					else {
						List<Reservation> recList = reservationController
								.loadReservationsForReceiving(sessionSubscriberId);

						if (recList == null || recList.isEmpty()) {
							resResp = new ReservationResponse(false, "No reservations found.", List.of());
						} else {
							resResp = new ReservationResponse(true, "Your reservations loaded.", recList);
						}
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_RECEIVING:
					Reservation recReservation = reservationController
							.getReservationForReceivingByCode(resReq.getConfirmationCode());

					if (recReservation != null)
						resResp = new ReservationResponse(true, "Your reservation loaded.", List.of(recReservation));
					else
						resResp = new ReservationResponse(false, "No reservations found.", List.of());

					break;

				case RECEIVE_TABLE:
					ReceiveTableResult rtr = reservationController.receiveTable(resReq.getReservationId());
					resResp = new ReservationResponse(rtr.isSuccess(), rtr.getMessage(), List.of());

					if (rtr.isSuccess())
						notificationController.sendTableReceived(resReq.getReservationId());

					break;

				case GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT:
					if (sessionSubscriberId == null)
						resResp = new ReservationResponse(false, "Please enter confirmation code.", List.of());
					else {
						List<Reservation> payList = reservationController
								.loadReservationsForCheckout(sessionSubscriberId);

						if (payList == null || payList.isEmpty()) {
							resResp = new ReservationResponse(false, "No reservations found.", List.of());
						} else {
							resResp = new ReservationResponse(true, "Your reservations loaded.", payList);
						}
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CHECKOUT:
					Reservation payReservation = reservationController
							.getReservationForCheckoutByCode(resReq.getConfirmationCode());

					if (payReservation != null)
						resResp = new ReservationResponse(true, "Your reservation loaded.", List.of(payReservation));
					else
						resResp = new ReservationResponse(false, "No reservations found.", List.of());

					break;

				case GET_BILL_FOR_PAYING:
					Bill bill = reservationController.getOrCreateBillForPaying(resReq.getReservationId());

					if (bill != null) {
						resResp = new ReservationResponse(true, "Bill loaded.", bill);
					} else {
						resResp = new ReservationResponse(false, "No bill found.", (Bill) null);
					}
					break;

				case PAY_BILL:
					PayBillResult pbr = reservationController.payBillbyId(resReq.getBillId());

					resResp = new ReservationResponse(pbr.isSuccess(), pbr.getMessage(), List.of());

					if (pbr.isSuccess() && pbr.getFreedCapacity() > 0)
						runNotifyCheck(pbr.getFreedCapacity());

					if (pbr.isSuccess())
						notificationController.sendPaymentSuccess(pbr.getReservationId());

					break;

				default:
					resResp = new ReservationResponse(false, "Unknown operation");

				}
				client.sendToClient(resResp);// returns response to the client
				return;
			}

		} catch (SQLException e) {
			ui.display("SQL Error: " + e.getMessage());
			e.printStackTrace();
			try {
				client.sendToClient(new ReservationResponse(false, "Database error occurred"));
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
