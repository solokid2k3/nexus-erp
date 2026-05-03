package com.erp.core.component;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;

public class StatCard extends VBox {

    private final Label valueLabel;
    private final Label titleLabel;

    public StatCard(String title, String value, Ikon icon, String accentColor) {
        getStyleClass().add("card-stat");
        setSpacing(16);
        setMinWidth(200);
        setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(this, Priority.ALWAYS);

        // Icon + value row
        var topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);

        var iconBox = new Region();
        iconBox.getStyleClass().add("stat-icon-box");
        iconBox.setStyle("-fx-background-color: " + accentColor + "18;");

        var fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(20);
        fontIcon.setStyle("-fx-icon-color: " + accentColor + ";");

        var iconContainer = new javafx.scene.layout.StackPane(iconBox, fontIcon);
        iconContainer.setMinSize(44, 44);
        iconContainer.setMaxSize(44, 44);

        valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        if ("—".equals(value)) {
            valueLabel.setStyle("-fx-text-fill: #D1D5DB;");
        }

        topRow.getChildren().addAll(iconContainer, valueLabel);

        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        getChildren().addAll(topRow, titleLabel);
    }

    public StatCard(String title, String value) {
        this(title, value, BytedanceIconsRegularAL.CHART_HISTOGRAM, "#6366F1");
    }

    public void setValue(String value) {
        valueLabel.setText(value);
        valueLabel.setStyle("-fx-text-fill: #111111;");

        var scale = new ScaleTransition(Duration.millis(300), valueLabel);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();

        var fade = new FadeTransition(Duration.millis(300), valueLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
