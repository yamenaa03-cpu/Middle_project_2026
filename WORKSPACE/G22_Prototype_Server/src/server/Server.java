package server;

import java.io.IOException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import common.dto.CreateReservationResult;
import common.dto.ReservationRequest;
import common.dto.ReservationResponse;
import common.entity.Reservation;
import common.enums.ReservationOperation;
import dbController.DBController;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGUI.ServerFrameController;
import controllers.ReservationController;
/**
 * Main class that extends AbstractServer OCSF server class that handles client communication,
 * interacts with the database controller,
 * and updates the JavaFX GUI via ServerUI.
 * @author Yamen Abu Ahmad
 * @version 1.0
 */

public class Server extends AbstractServer {
	
	  final public static int DEFAULT_PORT = 5555;
	  
	    private ServerUI ui; // server user interface
	    
	    private DBController db;//Data Base
	   
	    private String dbName;//DB name
	    
	    private String dbUser;//User of the DB
	    
	    private String dbPassword;//DB password
	    
	    private int clientCounter = 0;//to give special id to each client
	    
	    private ReservationController reservationController;


	    
/*server constructor*/
	public Server(int port, ServerFrameController ui) {
		super(port);
        this.ui = ui;

        setTimeout(500); // check every 0.5 sec if clients are alive
	}
	//sets DataBase info inputed from the user
	public void setDatabaseConfig(String dbName, String dbUser, String dbPassword) {
	    this.dbName = dbName;
	    this.dbUser = dbUser;
	    this.dbPassword = dbPassword;
	}

	
/*HANDLE MESSAGES FROM CLIENTS, it gets messages as instances of the class ReservationRequest it checks whats the specific operation that the client whants
 * in return to the client a response in an instance of ReservationResponse class*/
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        try {
            // if DB not initialized
            if (db == null ) {
                client.sendToClient(
                        new ReservationResponse(false, "Database not configured!", null)
                );
                ui.display("Client attempted request but DB not configured.");
                return;
            }

            // if Wrong message type
            if (!(msg instanceof ReservationRequest)) {
                client.sendToClient(
                        new ReservationResponse(false, "Unknown request type", null)
                );
                return;
            }


            ReservationRequest req = (ReservationRequest) msg;
            ReservationResponse resp;

            switch (req.getOperation()) {

                case GET_ALL_RESERVATIONS:
                    resp = new ReservationResponse(true,
                            "Reservations loaded.",
                            reservationController.getAllReservations());
                    break;

                case UPDATE_RESERVATION_FIELDS:
                    boolean ok = reservationController.updateReservation(
                            req.getReservationId(),
                            req.getReservationDateTime(),
                            req.getNumberOfGuests()
                    );

                    resp = new ReservationResponse(
                            ok,
                            ok ? "Reservation updated." : "Reservation not found.",
                            reservationController.getAllReservations()
                    );
                    //checks if the Reservation was updated correctly and returns a response according to the result
                    break;
                    
                case CREATE_RESERVATION:
                    CreateReservationResult r = reservationController.createReservation(
                        req.getCustomerId(),
                        req.getReservationDateTime(),
                        req.getNumberOfGuests()
                    );
                    if (r.isSuccess()) {
                        resp = new ReservationResponse(
                            true,
                            r.getMessage(),
                            r.getReservationId(),
                            r.getConfirmationCode(),
                            List.of()
                        );
                    } else {
                        resp = new ReservationResponse(
                            false,
                            r.getMessage(),
                            null,
                            null,
                            r.getSuggestions()
                        );
                    }
                    break;

                default:
                    resp = new ReservationResponse(false,
                            "Unknown operation",
                            null);
            }

            client.sendToClient(resp);//returns response to the client

        } catch (SQLException e) {
            ui.display("SQL Error: " + e.getMessage());
            e.printStackTrace();
            try {
                client.sendToClient(
                        new ReservationResponse(false, "Database error occurred", null)
                );
            } catch (Exception ignored) {}
        } catch (Exception e) {
            ui.display("Unexpected error: " + e.getMessage());
        }
    }
	
	
    @Override
    protected void clientConnected(ConnectionToClient client) {
        clientCounter++;
        client.setInfo("id", clientCounter);  // assign unique ID

        String host = client.getInetAddress().getHostName();
        String ip = client.getInetAddress().getHostAddress();
        
        //saves host and ip in the client object
        client.setInfo("host", host);
        client.setInfo("ip", ip);

        ui.updateClientStatus(String.valueOf(clientCounter),host,ip,"CONNECTED");
    }

	
	

    @Override
     protected void clientDisconnected(ConnectionToClient client) {
        Integer id = (Integer) client.getInfo("id");
        if (id == null) return;



        ui.updateClientStatus(String.valueOf(id),(String) client.getInfo("host"),(String) client.getInfo("ip"),"DISCONNECTED");
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
	        ui.display("Database connection initialized.");
	    } catch (Exception e) {
	        ui.display("Database initialization failed: " + e.getMessage());
	    }
	}

	
	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {
	    Integer id = (Integer) client.getInfo("id");
	    if (id == null) return;

	    

	    ui.updateClientStatus(String.valueOf(id),(String) client.getInfo("host"),(String) client.getInfo("ip"),"DISCONNECTED");
	}


	

	@Override
	protected void serverStopped() {
	    ui.display("Server stopped.");

	    // Remove all clients
	    ui.updateClientStatus("ALL", "", "", "DISCONNECTED");
	}
	

	


}
