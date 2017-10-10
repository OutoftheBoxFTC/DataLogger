package org.ftc7244.datalogger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
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
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(event -> Platform.exit());
        setAppIcon(stage, "/icon.png");
        stage.show();

	}

	private static void setAppIcon(Stage stage, String icon) {
    	try {
    		String path = DataLogger.class.getResource(icon).getPath();
			com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
			application.setDockIconImage(Toolkit.getDefaultToolkit().getImage(path));
		} catch (Exception ignore) {
    		//not a mac
		}
		stage.getIcons().addAll(new Image(icon));
	}
}
