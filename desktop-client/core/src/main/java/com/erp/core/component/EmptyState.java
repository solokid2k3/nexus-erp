package com.erp.core.component;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

public class EmptyState extends VBox {

    public EmptyState(Ikon icon, String title, String subtitle) {
        this(icon, title, subtitle, null, null);
    }

    public EmptyState(Ikon icon, String title, String subtitle, String buttonText, Runnable onAction) {
        getStyleClass().add("empty-state");
        setAlignment(Pos.CENTER);
        setSpacing(12);

        javafx.scene.Node graphicNode = null;
        try {
            var imageStream = getClass().getResourceAsStream("/images/sticker_empty_box.png");
            if (imageStream != null) {
                var image = new javafx.scene.image.Image(imageStream);
                var imageView = new javafx.scene.image.ImageView(image);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(true);
                graphicNode = imageView;
            }
        } catch (Exception e) {
            // fallback
        }

        if (graphicNode == null) {
            var fontIcon = new FontIcon(icon);
            fontIcon.setIconSize(48);
            fontIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#D1D5DB"));
            graphicNode = fontIcon;
        }

        var titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-state-title");

        var subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("empty-state-subtitle");
        subtitleLabel.setWrapText(true);

        getChildren().addAll(graphicNode, titleLabel, subtitleLabel);

        if (buttonText != null && onAction != null) {
            var btn = new Button(buttonText);
            btn.getStyleClass().add("button-primary");
            btn.setOnAction(e -> onAction.run());
            getChildren().add(btn);
        }
    }
}
