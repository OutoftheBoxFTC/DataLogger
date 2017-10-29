package org.ftc7244.datalogger.controllers;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Written By: brandon on 10/10/17
 */
public class Variable<T> extends HBox {

	private Label name;

	public Variable(String label, Class<T> type) {
		super(new Label(label), matchControlType(type));
		name = (Label) getChildren().get(0);
		Control node = (Control) getChildren().get(1);
		name.setPrefWidth(180);
		node.setPrefWidth(180);
	}

	private static Control matchControlType(Class<T> type) {
		if (T. == Type.BOOLEAN) {
			return new ChoiceBox<>(FXCollections.observableArrayList("True", "False"));
		}

		return new TextField();
	}
}
