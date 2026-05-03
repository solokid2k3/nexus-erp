package com.erp.core.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusBadge extends HBox {

    public StatusBadge(String text) {
        this(text, "default");
    }

    public StatusBadge(String text, String variant) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(3, 10, 3, 10));
        var label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 500;");
        getChildren().add(label);

        switch (variant.toLowerCase()) {
            case "success", "active", "approved", "completed", "delivered", "posted" ->
                    getStyleClass().add("badge-success");
            case "warning", "pending", "draft", "processing", "calculated" ->
                    getStyleClass().add("badge-warning");
            case "danger", "error", "cancelled", "terminated", "reversed", "rejected" ->
                    getStyleClass().add("badge-danger");
            case "info", "shipped" ->
                    getStyleClass().add("badge-info");
            default -> getStyleClass().add("badge");
        }
    }

    public static StatusBadge forStatus(String status) {
        if (status == null) return new StatusBadge("—", "default");
        return new StatusBadge(status, status);
    }
}
