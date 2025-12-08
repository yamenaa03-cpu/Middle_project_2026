package server;

import java.io.IOException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import common.Order;
import common.OrderOperation;
import common.OrderRequest;
import common.OrderResponse;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverDbController.ServerController;
import serverGUI.ServerFrameController;
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
	    
	    private ServerController db;//Data Base
	   
	    private String dbName;//DB name
	    
	    private String dbUser;//User of the DB
	    
	    private String dbPassword;//DB password
	    
	    private int clientCounter = 0;//to give special id to each client


	    
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

	
/*HANDLE MESSAGES FROM CLIENTS, it gets messages as instances of the class OrderRequest it checks whats the specific operation that the client whants
 * in return to the client a response in an instance of OrderResponse class*/
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        try {
            // if DB not initialized
            if (db == null ) {
                client.sendToClient(
                        new OrderResponse(false, "Database not configured!", null)
                );
                ui.display("Client attempted request but DB not configured.");
                return;
            }
            if (msg.equals("CLIENT_EXITING")) {
                // clean disconnect
                clientDisconnected(client);
                return;
            }

            // if Wrong message type
            if (!(msg instanceof OrderRequest)) {
                client.sendToClient(
                        new OrderResponse(false, "Unknown request type", null)
                );
                return;
            }


            OrderRequest req = (OrderRequest) msg;
            OrderResponse resp;

            switch (req.getOperation()) {

                case GET_ALL_ORDERS:
                    resp = new OrderResponse(true,
                            "Orders loaded.",
                            db.getAllOrders());
                    break;

                case UPDATE_ORDER_FIELDS:
                    boolean ok = db.updateOrderFields(
                            req.getOrderNumber(),
                            req.getNewOrderDate(),
                            req.getNewNumberOfGuests()
                    );

                    resp = new OrderResponse(
                            ok,
                            ok ? "Order updated." : "Order not found.",
                            db.getAllOrders()
                    );//checks if the order was updated correctly and returns a response according to the result
                    break;

                default:
                    resp = new OrderResponse(false,
                            "Unknown operation",
                            null);
            }

            client.sendToClient(resp);//returns response to the client

        } catch (SQLException e) {
            ui.display("SQL Error: " + e.getMessage());
            e.printStackTrace();
            try {
                client.sendToClient(
                        new OrderResponse(false, "Database error occurred", null)
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

        ui.updateClientStatus(String.valueOf(clientCounter),host,ip,"CONNECTED");
    }

	
	

    @Override
     protected void clientDisconnected(ConnectionToClient client) {
        Integer id = (Integer) client.getInfo("id");
        if (id == null) return;

        String host = client.getInetAddress().getHostName();
        String ip = client.getInetAddress().getHostAddress();

        ui.updateClientStatus(String.valueOf(id),host,ip,"DISCONNECTED");
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
	        db = new ServerController(dbName, dbUser, dbPassword);
	        ui.display("Database connection initialized.");
	    } catch (Exception e) {
	        ui.display("Database initialization failed: " + e.getMessage());
	    }
	}

	
	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {
	    Integer id = (Integer) client.getInfo("id");
	    if (id == null) return;

	    String host = client.getInetAddress().getHostName();
	    String ip = client.getInetAddress().getHostAddress();

	    ui.updateClientStatus(String.valueOf(id),host,ip,"DISCONNECTED");
	}


	

	@Override
	protected void serverStopped() {
	    ui.display("Server stopped.");

	    // Remove all clients
	    ui.updateClientStatus("ALL", "", "", "DISCONNECTED");
	}
	

	


}
