package serverGUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain_Gui extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerConfig.fxml"));
        Parent root = loader.load();

        ServerFrameController controller = loader.getController();

        controller.setServer(null);
        ServerFrameController.activeServer = null;

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Server Control Panel");

        // ⭐ Ensure server stops when GUI closes
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
    

    public static void main(String[] args) {
        launch(args);
    }
}
