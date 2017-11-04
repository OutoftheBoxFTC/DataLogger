package org.ftc7244.datalogger.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.ftc7244.datalogger.listeners.OnInternalWindowExit;

import java.util.ArrayList;
import java.util.List;

public class InternalWindow {

	private static final int RESIZE_MARGIN = 10;

	@FXML
	public BorderPane node;
	@FXML
	private LineChart<?, ?> lineGraph;
	@FXML
	private Label titleLabel;
	@FXML
	private SplitMenuButton mergeDropdown;

	private double xDragDelta, yDragDelta, mouseX, mouseY;
	private List<OnInternalWindowExit> exitListeners;
	private boolean resizing, resizingX, resizingY;

	@FXML
	void initialize() {
		xDragDelta = 0;
		yDragDelta = 0;
		mouseX = 0;
		mouseY = 0;
		exitListeners = new ArrayList<>();
		resizing = false;
		resizingY = false;
		resizingX = false;

		node.setMinWidth(node.getPrefWidth());
		node.setMinHeight(node.getPrefHeight());
	}

	public void setTitle(String title) {
		this.titleLabel.setText(title);
	}

	public void addWindowExitListener(OnInternalWindowExit listener) {
		exitListeners.add(listener);
	}

	@FXML
	protected void onExit(ActionEvent event) {
		for (OnInternalWindowExit listener : exitListeners) {
			listener.onInternalWindowExit(this, node);
		}
	}

	@FXML
	protected void onClearGraph(ActionEvent event) {

	}

	@FXML
	protected void onExportGraph(ActionEvent event) {

	}

	/******************************************
	 *
	 *                Dragging
	 *
	 ******************************************/

	@FXML
	protected void onBarDragged(MouseEvent event) {
		double offsetX = event.getSceneX() - mouseX;
		double offsetY = event.getSceneY() - mouseY;

		xDragDelta += offsetX;
		yDragDelta += offsetY;

		Pane pane = (Pane) node.getParent();
		node.setLayoutX(constrain(xDragDelta, 0, pane.getWidth() - node.getWidth()));
		node.setLayoutY(constrain(yDragDelta, 0, pane.getHeight() - node.getHeight()));

		// again set current Mouse x AND y position
		mouseX = event.getSceneX();
		mouseY = event.getSceneY();
	}

	@FXML
	protected void onBarPressed(MouseEvent event) {
		mouseX = event.getSceneX();
		mouseY = event.getSceneY();

		xDragDelta = node.getLayoutX();
		yDragDelta = node.getLayoutY();

		node.toFront();
	}

	/******************************************
	 *
	 *                Resizing
	 *
	 ******************************************/

	@FXML
	protected void onResizeReleased(MouseEvent event) {
		resizing = false;
		node.setCursor(Cursor.DEFAULT);
	}

	@FXML
	protected void onResizeOver(MouseEvent event) {
		Cursor cursor = Cursor.DEFAULT;
		if (updateResizeState(event) || resizing) {
			if (resizingX && resizingY) {
				cursor = Cursor.NW_RESIZE;
			} else if (resizingX) {
				cursor = Cursor.E_RESIZE;
			} else if (resizingY) {
				cursor = Cursor.S_RESIZE;
			}
		}

		node.setCursor(cursor);
	}

	@FXML
	protected void onResizeDragged(MouseEvent event) {
		if (!resizing) {
			return;
		}

		Pane pane = (Pane) node.getParent();
		if (resizingX) {
			double width = node.getMinWidth() + (event.getX() - mouseX);
			node.setMinWidth(constrain(width, node.getPrefWidth(), pane.getWidth() - node.getLayoutX()));
			mouseX = event.getX();
		}
		if (resizingY) {
			double height = node.getMinHeight() + (event.getY() - mouseY);
			node.setMinHeight(constrain(height, node.getPrefHeight(), pane.getHeight() - node.getLayoutY()));
			mouseY = event.getY();
		}
	}

	@FXML
	protected void onResizePressed(MouseEvent event) {
		if (!updateResizeState(event)) {
			return;
		}

		resizing = true;

		mouseY = event.getY();
		mouseX = event.getX();
	}

	protected boolean updateResizeState(MouseEvent event) {
		resizingX = event.getX() > node.getWidth() - RESIZE_MARGIN;
		resizingY = event.getY() > node.getHeight() - RESIZE_MARGIN;
		return resizingX || resizingY;
	}

	private double constrain(double value, double min, double max) {
		if (value < min) {
			return min;
		}

		if (value > max) {
			return max;
		}

		return value;
	}

    public void update(double[] values) {

    }
}
