package org.ftc7244.datalogger.listeners;

import javafx.scene.layout.Pane;
import org.ftc7244.datalogger.controllers.InternalWindow;

public interface OnMergeChart {
	void onMergeChart(InternalWindow targetWindow, Pane targetPane, InternalWindow mergeWindow, Pane mergePane);
}
