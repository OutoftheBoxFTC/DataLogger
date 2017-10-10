package org.ftc7244.datalogger.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class InternalWindow {

    @FXML
    public BorderPane node;
    @FXML
    private LineChart<?, ?> lineGraph;
    @FXML
    private Label titleLabel;
    @FXML
    private SplitMenuButton mergeDropdown;

    private double xDragDelta, yDragDelta, mouseX, mouseY;

    public InternalWindow() {
        xDragDelta = 0;
        yDragDelta = 0;
        mouseX = 0;
        mouseY = 0;
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    @FXML
    protected void onBarDragged(MouseEvent event) {
        double offsetX = event.getSceneX() - mouseX;
        double offsetY = event.getSceneY() - mouseY;

        xDragDelta += offsetX;
        yDragDelta += offsetY;

        Pane pane = (Pane) node.getParent();
        node.setLayoutX(constrict(xDragDelta, 0, pane.getWidth() - node.getWidth()));
        node.setLayoutY(constrict(yDragDelta, 0, pane.getHeight() - node.getHeight()));

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

    @FXML
    protected void onClearGraph(ActionEvent event) {

    }

    @FXML
    protected void onExit(ActionEvent event) {

    }

    @FXML
    protected void onExportGraph(ActionEvent event) {

    }

    private double constrict(double value, double min, double max) {
        if (value < min) {
            return min;
        }

        if (value > max) {
            return max;
        }

        return value;
    }

}
