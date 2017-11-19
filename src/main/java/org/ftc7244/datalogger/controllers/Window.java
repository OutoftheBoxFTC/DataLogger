package org.ftc7244.datalogger.controllers;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.ftc7244.datalogger.DataLogger;
import org.ftc7244.datalogger.listeners.OnConnectionUpdate;
import org.ftc7244.datalogger.listeners.OnReceiveData;
import org.ftc7244.datalogger.misc.ADBUtils;
import org.ftc7244.datalogger.misc.DataStreamer;
import org.ftc7244.datalogger.misc.DeviceUtils;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Window implements OnReceiveData, OnConnectionUpdate {

	private static final int GRAPH_OFFSET = 20;

	@FXML
	public Button adbButton;
	@FXML
	private Label adbStatus, connectionStatus;
	@FXML
	private MenuButton devices;
	@FXML
	private VBox variables;
	@FXML
	private AnchorPane contentPane;
	@FXML
	private DataStreamer streamer;

	private JadbConnection connection;
	private Map<String, InternalWindow> windows;
	private int devicesHashCode;
	private double xGraphOffset, yGraphOffset;

	@FXML
	protected void initialize() {
		this.xGraphOffset = 0;
		this.yGraphOffset = 0;
		this.devicesHashCode = -1;
		this.windows = new HashMap<>();
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

			setADBStatus(connected);

		}, 0, 100, TimeUnit.MILLISECONDS);
	}

	@FXML
	protected void onHardRest(ActionEvent event) {

	}

	@FXML
	public void onRefreshADB(ActionEvent event) {
		DataLogger.getService().execute(() -> {
			if (connection == null) {
				ADBUtils.start();
			} else {
				ADBUtils.stop();
				ADBUtils.start();
			}
		});
	}

	private void connect(JadbDevice device) {
		Optional<String> optionalAddress = DeviceUtils.getIPAddress(device);
		devices.setDisable(true);
		if (optionalAddress.isPresent()) {
			streamer = new DataStreamer(optionalAddress.get())
					.addConnectionListener(this)
					.addDataListener(this)
					.start();
		} else {
			devices.setDisable(false);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Invalid IP!");
			alert.setHeaderText("App Not sSarted!");
			alert.setContentText("We were unable to parse a valid IP address from the device. Please check the console for more info!");
			alert.show();
		}
	}

	protected InternalWindow createGraph(String title) {
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

	private void setADBStatus(boolean running) {
		Platform.runLater(() -> {
			adbButton.setText((running ? "Refresh" : "Start") + " ADB");
			adbButton.setDisable(false);
			updateStatus(running, adbStatus);
		});
	}

	private void updateStatus(boolean status, Label label) {
		label.setText(status ? "Yes" : "No");
		label.setTextFill(status ? Color.GREEN : Color.RED);
	}

	@Override
	public void onConnectionUpdate(boolean connected, Exception exception) {
		Platform.runLater(() -> {
			if (exception != null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Device Connection Error!");
				alert.setHeaderText("We are unable to connect to the device!");
				alert.setContentText(exception.getMessage());
				alert.show();
				exception.printStackTrace();
			}
			devices.setDisable(connected);
			updateStatus(connected, connectionStatus);
		});
		System.out.println("We are connected: " + connected);
	}

	@Override
	public void onReceiveData(String graph, double[] values) {
		Platform.runLater(() -> {
			if(!windows.containsKey(graph))windows.put(graph, createGraph(graph));
			windows.get(graph).update(values, graph);
		});
	}
}
