package org.ftc7244.datalogger.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.ftc7244.datalogger.DataLogger;
import org.ftc7244.datalogger.DeviceUtils;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Window {

	private static final int GRAPH_OFFSET = 20;

	@FXML
	private Label adbStatus, connectionStatus;

	@FXML
	private MenuButton devices;
	private int devicesHashCode;

	@FXML
	private VBox variables;

	@FXML
	private AnchorPane contentPane;

	private JadbConnection connection;

	private ArrayList<InternalWindow> windows;

	private double xGraphOffset, yGraphOffset;

	@FXML
	protected void initialize() {
		this.xGraphOffset = 0;
		this.yGraphOffset = 0;
		this.devicesHashCode = -1;
		this.windows = new ArrayList<>();
		DataLogger.getService().scheduleAtFixedRate(() -> {
			boolean connected = true;
			try {
				if (connection == null)
					connection = new JadbConnection();
				List<JadbDevice> devices = connection.getDevices();
				if (devicesHashCode != devices.hashCode()) {
					devicesHashCode = devices.hashCode();
					updateDeviceList(devices.stream().collect(Collectors.toMap(x -> x, DeviceUtils::getTitle)));
				}
			} catch (Exception e) {
				if (!(e instanceof ConnectException))
					e.printStackTrace();
				devices.setDisable(true);
				connection = null;
				connected = false;
			}
			updateStatus(connected, adbStatus);

		}, 0, 100, TimeUnit.MILLISECONDS);
	}

	@FXML
	protected void onHardRest(ActionEvent event) {

	}

	@FXML
	protected void onStartADB(ActionEvent event) {

	}

	private void connect(JadbDevice device) {

	}

	protected InternalWindow addGraph(String title) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/internal-window.fxml"));
		try {
			Pane load = loader.load();
			load.setLayoutX(xGraphOffset);
			load.setLayoutY(yGraphOffset);
			contentPane.getChildren().add(load);

			xGraphOffset += GRAPH_OFFSET;
			yGraphOffset += GRAPH_OFFSET;

			if (yGraphOffset + load.getPrefHeight() > contentPane.getHeight()) {
				xGraphOffset += GRAPH_OFFSET;
				yGraphOffset = 0;
			}

			if (xGraphOffset + load.getPrefWidth() > contentPane.getWidth()) {
				xGraphOffset = 0;
				yGraphOffset = 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		InternalWindow controller = loader.getController();
		controller.setTitle(title);
		controller.addWindowExitListener((listener, pane) -> contentPane.getChildren().remove(pane));
		return controller;
	}

	private void updateDeviceList(Map<JadbDevice, String> devices) {
		Platform.runLater(() -> {
			ObservableList<MenuItem> menuItems = this.devices.getItems();
			menuItems.clear();
			for (JadbDevice device : devices.keySet()) {
				MenuItem item = new MenuItem(devices.get(device));
				item.setOnAction(e -> connect(device));
				menuItems.add(item);
			}
			this.devices.setDisable(devices.size() <= 0);
		});
	}

	private void updateStatus(boolean status, Label label) {
		Platform.runLater(() -> {
			label.setText(status ? "Yes" : "No");
			label.setTextFill(status ? Color.GREEN : Color.RED);
		});
	}
}
