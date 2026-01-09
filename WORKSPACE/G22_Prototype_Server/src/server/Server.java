package server;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.dto.Authentication.CustomerAuthResult;
import common.dto.Reservation.CancelReservationResult;
import common.dto.Reservation.CreateReservationResult;
import common.dto.Reservation.PayBillResult;
import common.dto.Reservation.ReceiveTableResult;
import common.dto.Reservation.ReservationRequest;
import common.dto.Reservation.ReservationResponse;
import common.dto.Authentication.CustomerAuthRequest;
import common.dto.Authentication.CustomerAuthResponse;
import common.entity.Bill;
import common.entity.Reservation;
import common.enums.AuthOperation;
import common.enums.ReservationOperation;
import common.enums.ReservationStatus;
import dbController.DBController;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGUI.ServerFrameController;
import controllers.AuthenticationController;
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
				ui.display("Client attempted request but DB not configured.");
				return;
			}

			if (msg instanceof CustomerAuthRequest) {
				CustomerAuthRequest authReq = (CustomerAuthRequest) msg;

				if (authReq.getOperation() == AuthOperation.LOGOUT) {
					if (client.getInfo("subscriberId") == null)
						client.sendToClient(new CustomerAuthResponse(false, "Already logged out", null));
					client.setInfo("subscriberId", null);

					client.sendToClient(new CustomerAuthResponse(true, "Logged out successfully.", null));
					return;
				}

				CustomerAuthResult r;
				if (authReq.getOperation() == AuthOperation.SUBSCRIPTION_CODE) {
					r = authController.authenticateBySubscriptionCode(authReq.getSubscriptionCode());
				} else {
					r = CustomerAuthResult.fail("Invalid Operation!");
				}

				if (r.isSuccess()) {
					client.setInfo("subscriberId", r.getSubscriberId());
				}

				CustomerAuthResponse resp = new CustomerAuthResponse(r.isSuccess(), r.getMessage(),
						r.getSubscriberId());

				client.sendToClient(resp);// returns response to the client
				return;
			}

			if (msg instanceof ReservationRequest) {
				ReservationRequest req = (ReservationRequest) msg;

				Integer sessionSubscriberId = (Integer) client.getInfo("subscriberId");

				ReservationResponse resp;

				switch (req.getOperation()) {

				case GET_ALL_RESERVATIONS:
					resp = new ReservationResponse(true, "Reservations loaded.",
							reservationController.getAllReservations());
					break;

				case UPDATE_RESERVATION_FIELDS:
					boolean ok = reservationController.updateReservation(req.getReservationId(),
							req.getReservationDateTime(), req.getNumberOfGuests());

					resp = new ReservationResponse(ok, ok ? "Reservation updated." : "Reservation not found.",
							reservationController.getAllReservations());
					// checks if the Reservation was updated correctly and returns a response
					// according to the result
					break;

				case CREATE_RESERVATION: {
					CreateReservationResult r;

					if (sessionSubscriberId != null) {
						r = reservationController.createReservation(sessionSubscriberId, req.getReservationDateTime(),
								req.getNumberOfGuests());
					} else {
						r = reservationController.createGuestReservation(req.getReservationDateTime(),
								req.getNumberOfGuests(), req.getFullName(), req.getPhone(), req.getEmail());
					}

					if (r.isSuccess()) {
						resp = new ReservationResponse(true, r.getMessage(), r.getReservationId(),
								r.getConfirmationCode(), List.of());
					} else {
						resp = new ReservationResponse(false, r.getMessage(), null, null, r.getSuggestions());
					}
					break;
				}

				case GET_CUSTOMER_RESERVATIONS:
					List<Reservation> list = reservationController.getReservationsForCustomer(sessionSubscriberId);

					if (list == null || list.isEmpty()) {
						resp = new ReservationResponse(true, "No reservations found.", List.of());
					} else {
						resp = new ReservationResponse(true, "Your reservations loaded.", list);
					}
					break;

				case CANCEL_RESERVATION: {
					CancelReservationResult cr;
					
					if (sessionSubscriberId != null) {
						cr = reservationController.cancelReservation(req.getReservationId(), sessionSubscriberId);
					} else {
						cr = reservationController.cancelGuestReservation(req.getConfirmationCode());
					}

					if (sessionSubscriberId != null) {
						resp = new ReservationResponse(cr.isSuccess(), cr.getMessage(),
								reservationController.getReservationsForCustomer(sessionSubscriberId));
					} else {
						resp = new ReservationResponse(cr.isSuccess(), cr.getMessage(), List.of());
					}
					
					if(cr.isSuccess())
						reservationController.notifyNextFromWaitlist();
					
					break;
				}

				case JOIN_WAITLIST: {
					CreateReservationResult r;

					if (sessionSubscriberId != null) {
						// Subscriber path
						r = reservationController.joinWaitlist(sessionSubscriberId, req.getNumberOfGuests());
					} else {
						// Guest path (no session saved)
						r = reservationController.joinWaitlistAsGuest(req.getNumberOfGuests(), req.getFullName(),
								req.getPhone(), req.getEmail());
					}

					if (r.isSuccess()) {
						resp = new ReservationResponse(true, r.getMessage(), r.getReservationId(),
								r.getConfirmationCode(), List.of());
					} else {
						resp = new ReservationResponse(false, r.getMessage(), null, null, List.of());
					}
					break;
				}

				case RECEIVE_TABLE: {
					ReceiveTableResult r = reservationController.receiveTable(req.getConfirmationCode());
					resp = new ReservationResponse(r.isSuccess(), r.getMessage(), List.of());
					break;
				}

				case CHECKOUT: {
					PayBillResult r = reservationController.payBillByCode(req.getConfirmationCode());

					resp = new ReservationResponse(r.isSuccess(), r.getMessage(), List.of());
					
					if(r.isSuccess())
						reservationController.notifyNextFromWaitlist(r.getFreedCapacity());
					break;
				}

				default:
					resp = new ReservationResponse(false, "Unknown operation");
				}

				client.sendToClient(resp);// returns response to the client
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
			reservationController = new ReservationController(db);
			authController = new AuthenticationController(db);
			ui.display("Database connection initialized.");
		} catch (Exception e) {
			ui.display("Database initialization failed: " + e.getMessage());
		}

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
			if (ok)
				canceledCount++;
		}

		if (canceledCount > 0) {
			ui.display("No-Show: auto-canceled " + canceledCount + " reservations.");
		}
	}

	private void runReminderCheck() throws SQLException {
		List<Integer> ids = db.getReservationsForReminder();
		if (ids.isEmpty())
			return;

		for (Integer id : ids) {
			// MOCK reminder
			ui.display("🔔 Reminder: Reservation #" + id + " is scheduled in 2 hours.");
			db.markReminderSent(id);
		}
	}

	private void runBillingCheck() throws SQLException {
	    List<Reservation> res = db.getReservationsForBilling();
	    if (res.isEmpty()) return;

	    int sent = 0;

	    for (Reservation r : res) {
	        Bill bill = reservationController.computeBill(r);
	        if (bill != null) {

	            sent++;
	            ui.display("💳 Bill sent for reservation # " + r.getReservationId() + "Initial amount: "
	            		+ bill.getAmountBeforeDiscount() + "Final amount: " + bill.getFinalAmount());
	        }
	    }
	    if (sent > 0) ui.display("💳 Bills sent: " + sent);
	}

}
