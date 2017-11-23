package org.ftc7244.datalogger.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.ftc7244.datalogger.listeners.OnInternalWindowExit;
import org.ftc7244.datalogger.listeners.OnMergeChart;

import java.util.*;

public class InternalWindow {

    private static final int RESIZE_MARGIN = 10;

    @FXML
    public BorderPane node;
    @FXML
    private LineChart<Number, Number> lineGraph;
    @FXML
    private Label titleLabel;
    @FXML
    private MenuButton mergeDropdown;

    private Map<String, XYChart.Series<Number, Number>> series;

    private double xDragDelta, yDragDelta, mouseX, mouseY;
    private List<OnInternalWindowExit> exitListeners;
    private List<OnMergeChart> mergeListeners;
    private boolean resizing, resizingX, resizingY;

    @FXML
    void initialize() {
        xDragDelta = 0;
        yDragDelta = 0;
        mouseX = 0;
        mouseY = 0;
        exitListeners = new ArrayList<>();
        mergeListeners = new ArrayList<>();
        resizing = false;
        resizingY = false;
        resizingX = false;

        node.setMinWidth(node.getPrefWidth());
        node.setMinHeight(node.getPrefHeight());

        series = new HashMap<>();
    }

    public InternalWindow setTitle(String title) {
        this.titleLabel.setText(title);
        return this;
    }

    public InternalWindow addWindowExitListener(OnInternalWindowExit listener) {
        exitListeners.add(listener);
        return this;
    }

    public InternalWindow addMergeListener(OnMergeChart listener) {
        mergeListeners.add(listener);
        return this;
    }

    @FXML
    protected void onExit(ActionEvent event) {
        for (OnInternalWindowExit listener : exitListeners) {
            listener.onInternalWindowExit(this, node);
        }
    }

    @FXML
    protected void onClearGraph(ActionEvent event) {
        this.series.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .map(XYChart.Series::getData)
                .forEach(List::clear);
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

    public void update(String tag, double[] values) {
        if (!series.containsKey(tag)) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(tag);
            lineGraph.getData().add(series);
            this.series.put(tag, series);
        }
        List<XYChart.Data<Number, Number>> addedData = new ArrayList<>();
        int size = series.get(tag).getData().size();
        for (int i = 0; i < values.length; i++)
            addedData.add(new XYChart.Data<>(i + size, values[i]));
        Platform.runLater(() -> {
            series.get(tag).getData().addAll(addedData);
            lineGraph.setLegendVisible(series.size() > 1);
        });
    }

    public String getTitle() {
        return titleLabel.textProperty().get();
    }

    public void setMergeable(Collection<InternalWindow> windows) {
        List<MenuItem> items = mergeDropdown.getItems();
        items.clear();
        for (InternalWindow window : windows) {
            if (window.equals(this))
                continue;
            MenuItem item = new MenuItem(window.getTitle());
            item.setOnAction(event -> {
                Optional<InternalWindow> mergable = windows.stream().filter(x -> x.getTitle().equals(item.getText())).findAny();
                if (!mergable.isPresent()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Unable to Merge!");
                    alert.setHeaderText("There was no instance of " + item.getText());
                    alert.setContentText("There was an error finding the item specified!");
                    alert.show();
                    return;
                }
                InternalWindow mergeWindow = mergable.get();
                mergeListeners.forEach(x -> x.onMergeChart(this, node, mergeWindow, mergeWindow.node));
            });
            items.add(item);
        }
        mergeDropdown.setDisable(windows.size() <= 1);
    }

    public List<String> getKeys() {
        return new ArrayList<>(this.series.keySet());
    }

    public Map<String, double[]> getData() {
        Map<String, double[]> data = new HashMap<>();
        this.series.forEach((tag, values) -> {
            double[] numbers = values.getData()
                    .stream()
                    .mapToDouble(x -> (Double) x.YValueProperty().get())
                    .toArray();
            data.put(tag, numbers);
        });
        return data;
    }
}
