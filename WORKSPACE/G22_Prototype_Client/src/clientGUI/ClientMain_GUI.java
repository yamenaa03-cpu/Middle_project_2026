package clientGUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class ClientMain_GUI extends Application {



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
        stage.setOnCloseRequest(event -> {
            System.out.println("Closing GUI → stopping client...");

            Platform.exit();
            System.exit(0);
        });

	}


    public static void main(String[] args) {
        launch(args);
    }
}
