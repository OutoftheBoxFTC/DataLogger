package org.ftc7244.datalogger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.ftc7244.datalogger.misc.ADBUtils;

import java.awt.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by OOTB on 11/28/2016.
 */
public class DataLogger extends Application {

	private static ScheduledExecutorService service;

	public static ScheduledExecutorService getService() {
		return service;
	}

	public static void main(String[] args) {
		service = Executors.newScheduledThreadPool(4);
		ADBUtils.stop();
		Application.launch(args);
	}

	private static void setAppIcon(Stage stage, String icon) {
		try {
			String path = DataLogger.class.getResource(icon).getPath();
			Class<?> application = Class.forName("com.apple.eawt.Application");
			Object instance = application.getMethod("getApplication").invoke(null);
			Method setDockIconImage = application.getMethod("setDockIconImage", java.awt.Image.class);
			setDockIconImage.invoke(instance, Toolkit.getDefaultToolkit().getImage(path));
		} catch (Exception ignore) {
			//not a mac
		}
		stage.getIcons().addAll(new Image(icon));
	}

	public void start(Stage stage) throws Exception {
		URL resource = getClass().getResource("/window.fxml");
		SplitPane root = FXMLLoader.load(resource);
		stage.setTitle("Data Logger");
		stage.setScene(new Scene(root));
		stage.setMinHeight(root.getMinHeight());
		stage.setMinWidth(root.getMinWidth());
		stage.setOnCloseRequest(event -> {
			Platform.exit();
			service.shutdown();
		});
		setAppIcon(stage, "/icon.png");
		stage.show();

	}
}
