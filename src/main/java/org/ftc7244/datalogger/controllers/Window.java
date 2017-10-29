package org.ftc7244.datalogger.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Random;

public class Window {

	private static final int GRAPH_OFFSET = 20;

	@FXML
	public VBox variables;
	@FXML
    private AnchorPane contentPane;

	private double xGraphOffset, yGraphOffset;

	@FXML
	protected void initialize() {
		this.xGraphOffset = 0;
		this.yGraphOffset = 0;
	}

    @FXML
    protected void onConnect(ActionEvent event) {
        addGraph("Test");
    }

    @FXML
    protected void onHardRest(ActionEvent event) {
		addVariable("Test", Variable.Type.values()[new Random().nextInt(Variable.Type.values().length)]);
    }

    @FXML
    protected void onStartADB(ActionEvent event) {

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

    protected void addVariable(String name, Variable.Type type) {
		variables.getChildren().add(new Variable(name, type));
	}
}
