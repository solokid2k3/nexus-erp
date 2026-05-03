package com.erp.core.component;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class LoadingSpinner extends VBox {

    public LoadingSpinner() {
        this("Loading...");
    }

    public LoadingSpinner(String message) {
        setAlignment(Pos.CENTER);
        setSpacing(16);
        getStyleClass().add("loading-container");

        var dots = new HBox(8);
        dots.setAlignment(Pos.CENTER);

        for (int i = 0; i < 3; i++) {
            var dot = new Circle(5);
            dot.setStyle("-fx-fill: #D4D4D8;");
            dots.getChildren().add(dot);

            var pulse = new ScaleTransition(Duration.millis(600), dot);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.5);
            pulse.setToY(1.5);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setDelay(Duration.millis(i * 200));

            var fade = new FadeTransition(Duration.millis(600), dot);
            fade.setFromValue(0.4);
            fade.setToValue(1.0);
            fade.setAutoReverse(true);
            fade.setCycleCount(Animation.INDEFINITE);
            fade.setDelay(Duration.millis(i * 200));

            pulse.play();
            fade.play();
        }

        var label = new Label(message);
        label.getStyleClass().add("caption-text");

        getChildren().addAll(dots, label);
    }
}
