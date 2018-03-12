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
import javafx.stage.FileChooser;
import org.ftc7244.datalogger.DataLogger;
import org.ftc7244.datalogger.listeners.OnConnectionUpdate;
import org.ftc7244.datalogger.listeners.OnInternalWindowExit;
import org.ftc7244.datalogger.listeners.OnMergeChart;
import org.ftc7244.datalogger.listeners.OnReceiveData;
import org.ftc7244.datalogger.misc.ADBUtils;
import org.ftc7244.datalogger.misc.DataStorage;
import org.ftc7244.datalogger.misc.DataStreamer;
import org.ftc7244.datalogger.misc.DeviceUtils;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Window implements OnReceiveData, OnConnectionUpdate, OnMergeChart, OnInternalWindowExit {

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

	private boolean updated;

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
		updated = false;
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
			Platform.runLater(()->{
				devices.setDisable(false);
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Invalid IP!");
				alert.setHeaderText("App Not Started!");
				alert.setContentText("We were unable to parse a valid IP address from the device. Please check the console for more info!");
				alert.show();
			});
		}
	}

	private InternalWindow createGraph(String title) {
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

		return loader.<InternalWindow>getController()
				.setTitle(title)
				.addWindowExitListener(this)
				.addMergeListener(this);
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
			if (!windows.containsKey(graph)) {
				windows.put(graph, createGraph(graph));
				updateMergables();
			}
			windows.get(graph).update(graph, values);
		});
	}

	@Override
	public void onMergeChart(InternalWindow targetWindow, Pane targetPane, InternalWindow mergeWindow, Pane mergePane) {
		mergeWindow.getData().forEach((tag, values) -> {
			windows.put(tag, targetWindow);
			targetWindow.update(tag, values);
		});

		targetWindow.setTitle(targetWindow.getKeys().stream().collect(Collectors.joining(", ")));
		contentPane.getChildren().remove(mergePane);
		updateMergables();
	}

	@Override
	public void onInternalWindowExit(InternalWindow window, Pane pane) {
		contentPane.getChildren().remove(pane);
		windows.values().removeAll(Collections.singleton(window));
		updateMergables();
	}

	private void updateMergables() {
		Collection<InternalWindow> windows = this.windows.values();
		windows.forEach(x -> x.setMergeable(new HashSet<>(windows)));
	}
	@FXML
	private void onClearAll(){
		if(updated) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Session Unsaved");
			alert.setContentText("Are you sure you want to clear your session without saving?");
			Optional<ButtonType> selection = alert.showAndWait();
			if (selection.equals(ButtonType.YES)) {
				windows.entrySet().forEach((x)->x.getValue().onClearGraph(null));
			}
		}
		else windows.entrySet().forEach((x)->x.getValue().onClearGraph(null));
	}

	@FXML
	private void onDeleteAll(){

	}

	@FXML
	private void onDistribute(){

	}

	@FXML
	private void onExportSession(){
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Save Session To directory");
		chooser.showSaveDialog(null);
		File file = chooser.showSaveDialog(null);
		if(file != null){
			DataStorage storage = new DataStorage();
			windows.entrySet().forEach((x) -> {
				DataStorage subStorage = new DataStorage();
				x.getValue().saveTo(subStorage);
				storage.addSubStorage(subStorage);
			});
		}
	}

	@FXML
	private void onOpenSession(){
		FileChooser chooser = new FileChooser();
		chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("txt"));
		chooser.setTitle("Open Session from Directory");
		File file = chooser.showOpenDialog(null);
		if(file!=null){
			if(updated){
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Session Unsaved");
				alert.setContentText("Are you sure you want to clear your session without saving?");
				Optional<ButtonType> selection = alert.showAndWait();
				if (selection.equals(ButtonType.YES)) {
					try {
						contentPane.getChildren().clear();
						String input = new Scanner(file).nextLine();
						String[] windows = input.substring(0, input.indexOf(')')).split("/");
						DataStorage storage = new DataStorage(input.substring(input.indexOf(')')+1));
						for (int i = 0; i < windows.length; i++) {
							String[] subSections = windows[i].split(",");
							InternalWindow window = createGraph(windows[i]);
							window.init(subSections, storage.getSubStorage(i));
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
