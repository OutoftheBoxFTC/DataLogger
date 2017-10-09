package org.ftc7244.datalogger.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.input.MouseEvent;
import org.ftc7244.datalogger.DataLogger;

import java.awt.event.ActionEvent;

public class InternalWindow {

    private boolean dragging;

    public InternalWindow() {

    }

    @FXML
    private LineChart<?, ?> lineGraph;

    @FXML
    private SplitMenuButton mergeDropdown;

    @FXML
    void onBarDrag(MouseEvent event) {

    }

    @FXML
    void onBarPressed(MouseEvent event) {
    }

    @FXML
    void onBarReleased(MouseEvent event) {

    }

    @FXML
    void onClearGraph(ActionEvent event) {

    }

    @FXML
    void onCloseGraph(ActionEvent event) {

    }

    @FXML
    void onExportGraph(ActionEvent event) {

    }

    @FXML
    void onMouseEnterBar(MouseEvent event) {

    }

    @FXML
    void onMouseExitBar(MouseEvent event) {

    }

}
