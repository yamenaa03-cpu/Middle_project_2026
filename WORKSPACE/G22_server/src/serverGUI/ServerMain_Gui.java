package serverGUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the server-side JavaFX graphical user interface.
 * <p>
 * This class initializes and launches the server control panel GUI, which
 * allows administrators to start/stop the server, configure database
 * connections, and monitor connected clients. The application ensures proper
 * cleanup of server resources when the window is closed.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public class ServerMain_Gui extends Application {

	/**
	 * Initializes and displays the server control panel window.
	 * <p>
	 * This method loads the ServerConfig.fxml layout, sets up the controller,
	 * configures the window close handler to properly stop the server, and displays
	 * the primary stage.
	 * </p>
	 *
	 * @param primaryStage the primary stage for this JavaFX application
	 * @throws Exception if the FXML file cannot be loaded
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerConfig.fxml"));
		Parent root = loader.load();

		ServerFrameController controller = loader.getController();

		controller.setServer(null);
		ServerFrameController.activeServer = null;

		primaryStage.setScene(new Scene(root));
		primaryStage.setTitle("Server Control Panel");

		primaryStage.setOnCloseRequest(event -> {
			System.out.println("Closing GUI → stopping server...");

			if (ServerFrameController.activeServer != null) {
				try {
					ServerFrameController.activeServer.stopListening();
					ServerFrameController.activeServer.close();
					System.out.println("Server terminated.");
				} catch (Exception e) {
					System.out.println("Error closing server: " + e.getMessage());
				}
			}

			Platform.exit();
			System.exit(0);
		});

		primaryStage.show();
	}

	/**
	 * Main entry point for the server GUI application.
	 * <p>
	 * Launches the JavaFX application by calling the inherited
	 * {@link Application#launch} method.
	 * </p>
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
