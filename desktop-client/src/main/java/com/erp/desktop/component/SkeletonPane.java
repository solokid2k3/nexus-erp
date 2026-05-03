package com.erp.desktop.component;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SkeletonPane extends VBox {

    public SkeletonPane(int rows) {
        this(rows, 300);
    }

    public SkeletonPane(int rows, double width) {
        setSpacing(12);
        setPadding(new Insets(8));

        for (int i = 0; i < rows; i++) {
            var bar = new Region();
            bar.setMinHeight(16);
            bar.setPrefHeight(16);
            bar.setPrefWidth(i == 0 ? width * 0.6 : (i % 2 == 0 ? width * 0.8 : width));
            bar.setMaxWidth(i == 0 ? width * 0.6 : (i % 2 == 0 ? width * 0.8 : width));
            bar.getStyleClass().add("skeleton");

            var pulse = new FadeTransition(Duration.millis(800), bar);
            pulse.setFromValue(0.4);
            pulse.setToValue(1.0);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setDelay(Duration.millis(i * 100));
            pulse.play();

            getChildren().add(bar);
        }
    }
}
