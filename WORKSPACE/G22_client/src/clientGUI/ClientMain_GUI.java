package clientGUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * ClientMain_GUI is the main entry point for the JavaFX client application.
 * 
 * <p>This class extends {@link javafx.application.Application} and is responsible for:
 * <ul>
 *   <li>Loading the main FXML layout (BistroClientInterface.fxml)</li>
 *   <li>Applying the bistro CSS stylesheet</li>
 *   <li>Configuring and displaying the primary stage</li>
 *   <li>Handling application shutdown</li>
 * </ul>
 * 
 * <p>To launch the client application, run the {@link #main(String[])} method.
 * 
 * @author G22 Team
 * @version 1.0
 * @see javafx.application.Application
 * @see ClientController
 */
public class ClientMain_GUI extends Application {

    // ==========================================================
    // APPLICATION LIFECYCLE
    // ==========================================================

        /**
         * Starts the JavaFX application by loading the main interface.
         * 
         * <p>This method is called by the JavaFX runtime after the application
         * is initialized. It performs the following tasks:
         * <ol>
         *   <li>Loads the BistroClientInterface.fxml layout</li>
         *   <li>Applies the bistro.css stylesheet</li>
         *   <li>Configures the stage (title, style, maximized)</li>
         *   <li>Sets up the close request handler for clean shutdown</li>
         * </ol>
         * 
         * @param stage the primary stage provided by the JavaFX runtime
         * @throws Exception if FXML loading fails or resources are not found
         */
        @Override
        public void start(Stage stage) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/BistroClientInterface.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/clientGUI/bistro.css").toExternalForm());

            stage.initStyle(StageStyle.DECORATED); // <-- ensures OS title bar + exit button
            stage.setTitle("Bistro Client");
            stage.setScene(scene);
            stage.show();
            stage.setMaximized(true);
        stage.setOnCloseRequest(event -> {
            System.out.println("Closing GUI → stopping client...");

            Platform.exit();
            System.exit(0);
        });

        }

    // ==========================================================
    // MAIN ENTRY POINT
    // ==========================================================

    /**
     * Main entry point for the client application.
     * 
     * <p>Launches the JavaFX application by calling {@link Application#launch(String[])}.
     * 
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
