import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Created by OOTB on 11/28/2016.
 */
public class DataLogger extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start(Stage stage) throws Exception {
        URL resource = getClass().getResource("/window.fxml");
        Parent root = FXMLLoader.load(resource);
        stage.setTitle("Data Logger");
        stage.getIcons().addAll(new Image("./icon.png"));
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.show();
    }
}
