package org.ftc7244.datalogger.controllers;

import com.sun.deploy.util.FXLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class Window {

    @FXML
    private AnchorPane contentPane;

    @FXML
    protected void onConnect(ActionEvent event) {
        addGraph("Test");
    }

    @FXML
    protected void onHardRest(ActionEvent event) {

    }

    @FXML
    protected void onStartADB(ActionEvent event) {

    }

    protected InternalWindow addGraph(String title) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/internal-window.fxml"));
        try {
            Pane load = loader.load();
            contentPane.getChildren().add(load);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        InternalWindow controller = loader.getController();
        controller.setTitle(title);
        controller.addWindowExitListener((listener, pane) -> contentPane.getChildren().remove(pane));
        return controller;
    }
}
