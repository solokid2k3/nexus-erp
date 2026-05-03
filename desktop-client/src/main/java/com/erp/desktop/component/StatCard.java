package com.erp.desktop.component;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class StatCard extends VBox {

    private final Label valueLabel;
    private final Label titleLabel;

    public StatCard(String title, String value) {
        getStyleClass().add("card-stat");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(24));
        setSpacing(8);
        setMinWidth(180);

        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("caption-text");

        valueLabel = new Label(value);
        valueLabel.getStyleClass().add("heading-2");

        if ("—".equals(value)) {
            valueLabel.setStyle("-fx-text-fill: #D4D4D8;");
        }

        getChildren().addAll(titleLabel, valueLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
        valueLabel.setStyle("-fx-text-fill: #111111;");

        var fade = new FadeTransition(Duration.millis(300), valueLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public void setTitle(String title) { titleLabel.setText(title); }
}
