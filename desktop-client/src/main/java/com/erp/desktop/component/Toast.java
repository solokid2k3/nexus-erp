package com.erp.desktop.component;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Toast extends HBox {

    public enum Type { SUCCESS, ERROR, INFO, WARNING }

    public static void show(StackPane parent, String message, Type type) {
        var toast = new Toast(message, type);
        parent.getChildren().add(toast);
        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new Insets(16, 0, 0, 0));

        var fadeIn = new FadeTransition(Duration.millis(250), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        var slideIn = new TranslateTransition(Duration.millis(250), toast);
        slideIn.setFromY(-20);
        slideIn.setToY(0);

        var fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.setOnFinished(e -> parent.getChildren().remove(toast));

        fadeIn.play();
        slideIn.play();
        fadeOut.play();
    }

    private Toast(String message, Type type) {
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(12, 20, 12, 20));
        setMaxWidth(420);
        setSpacing(8);

        String emoji = switch (type) {
            case SUCCESS -> "✅";
            case ERROR -> "❌";
            case WARNING -> "⚠️";
            case INFO -> "ℹ️";
        };
        String borderColor = switch (type) {
            case SUCCESS -> "#10B981";
            case ERROR -> "#EF4444";
            case WARNING -> "#F59E0B";
            case INFO -> "#3B82F6";
        };

        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; "
                + "-fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4);");

        var emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        var textLabel = new Label(message);
        textLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px; -fx-font-weight: 500;");

        getChildren().addAll(emojiLabel, textLabel);
    }
}
