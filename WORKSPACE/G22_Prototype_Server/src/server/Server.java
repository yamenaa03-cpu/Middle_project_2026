package server;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.dto.Authentication.SubscriberAuthResult;
import common.dto.Reservation.CancelReservationResult;
import common.dto.Reservation.CreateReservationResult;
import common.dto.Reservation.PayBillResult;
import common.dto.Reservation.ReceiveTableResult;
import common.dto.Reservation.ReservationRequest;
import common.dto.Reservation.ReservationResponse;
import common.dto.Authentication.SubscriberAuthRequest;
import common.dto.Authentication.SubscriberAuthResponse;
import common.entity.Bill;
import common.entity.Reservation;
import common.enums.AuthOperation;
import common.enums.LoggedInStatus;
import common.enums.ReservationOperation;
import common.enums.ReservationStatus;
import dbController.DBController;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGUI.ServerFrameController;
import controllers.AuthenticationController;
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

	private AuthenticationController authController;

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

			if (msg instanceof SubscriberAuthRequest) {

				SubscriberAuthRequest authReq = (SubscriberAuthRequest) msg;

				SubscriberAuthResponse authResp;
				switch (authReq.getOperation()) {

				case LOGOUT:
					if (client.getInfo("subscriberId") == null)
						client.sendToClient(new SubscriberAuthResponse(false, "Already logged out", null,
								LoggedInStatus.NOT_LOGGED_IN));
					client.setInfo("subscriberId", null);

					authResp = new SubscriberAuthResponse(true, "Logged out successfully.", null,
							LoggedInStatus.NOT_LOGGED_IN);

					break;

				case SUBSCRIPTION_CODE:
					SubscriberAuthResult r;
					r = authController.authenticateBySubscriptionCode(authReq.getSubscriptionCode());

					if (r.isSuccess())
						client.setInfo("subscriberId", r.getSubscriberId());

					authResp = new SubscriberAuthResponse(r.isSuccess(), r.getMessage(), r.getFullName(),
							LoggedInStatus.SUBSCRIBER);

					break;

				case LOGGED_IN_STATUS:
					if (client.getInfo("subscriberId") == null)
						authResp = new SubscriberAuthResponse(true, "Not logged in", null,
								LoggedInStatus.NOT_LOGGED_IN);
					else
						authResp = new SubscriberAuthResponse(true, "Subscriber is logged in", null,
								LoggedInStatus.SUBSCRIBER);

					break;

				default:
					authResp = new SubscriberAuthResponse(false, "Invalid Operation!", null, null);
					client.sendToClient(authResp);// returns response to the client
					return;
				}
				client.sendToClient(authResp);// returns response to the client
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
					for (Reservation res : resList) {
						if (notificationController.resendReservationConfirmation(res.getReservationId()))
							sentCount++;
					}
					if (sentCount > 0)
						resResp = new ReservationResponse(true, "Sent " + sentCount + "code/s.", null, null, null);
					else
						resResp = new ReservationResponse(false, "No reservations found.", null, null, null);
					
					break;
					
				case GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION:
					List<Reservation> canList = reservationController.loadReservationsForCancellation(sessionSubscriberId);

					if (canList == null || canList.isEmpty()) {
						resResp = new ReservationResponse(true, "No reservations found.", List.of());
					} else {
						resResp = new ReservationResponse(true, "Your reservations loaded.", canList);
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_CANCELLATION:
					Reservation canReservation = reservationController
							.getReservationForCancellationByCode(resReq.getConfirmationCode());

					if (canReservation != null)
						resResp = new ReservationResponse(true, "Your reservation loaded.",
								List.of(canReservation));
					else
						resResp = new ReservationResponse(true, "No reservations found.", List.of());
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

					if (cr.isSuccess()) {
						if (sessionSubscriberId == null)
							notificationController.sendReservationCanceledByCode(resReq.getConfirmationCode());
						else
							notificationController.sendReservationCanceled(resReq.getReservationId());
					}

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
					List<Reservation> recList = reservationController.loadReservationsForCancellation(sessionSubscriberId);

					if (recList == null || recList.isEmpty()) {
						resResp = new ReservationResponse(true, "No reservations found.", List.of());
					} else {
						resResp = new ReservationResponse(true, "Your reservations loaded.", recList);
					}
					break;

				case GET_RESERVATION_By_CONFIRMATION_CODE_FOR_RECEIVING:
					Reservation reservation = reservationController
							.getReservationForCancellationByCode(resReq.getConfirmationCode());

					if (reservation != null)
						resResp = new ReservationResponse(true, "Your reservation loaded.",
								List.of(reservation));
					else
						resResp = new ReservationResponse(true, "No reservations found.", List.of());
					
					break;

				case RECEIVE_TABLE:
					ReceiveTableResult rtr = reservationController.receiveTable(resReq.getReservationId());
					resResp = new ReservationResponse(rtr.isSuccess(), rtr.getMessage(), List.of());

					if (rtr.isSuccess()) {
						if (sessionSubscriberId == null)
							notificationController.sendTableReceivedByCode(resReq.getConfirmationCode());
						else
							notificationController.sendTableReceived(resReq.getReservationId());
					}
					break;
					

				case CHECKOUT:
					PayBillResult chr = reservationController.payBillByCode(resReq.getConfirmationCode());

					resResp = new ReservationResponse(chr.isSuccess(), chr.getMessage(), List.of());

					if (chr.isSuccess() && chr.getFreedCapacity() > 0)
						runNotifyCheck(chr.getFreedCapacity());

					if (chr.isSuccess()) {
						if (sessionSubscriberId == null)
							notificationController.sendPaymentSuccessByCode(resReq.getConfirmationCode());
						else
							notificationController.sendPaymentSuccess(resReq.getReservationId());
					}
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
		authController = new AuthenticationController(db);
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
