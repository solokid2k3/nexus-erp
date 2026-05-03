package com.erp.core.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class DataToolbar extends HBox {

    public DataToolbar(SearchField searchField, Button... actionButtons) {
        getStyleClass().add("data-toolbar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);

        if (searchField != null) {
            getChildren().add(searchField);
        }

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        for (var btn : actionButtons) {
            getChildren().add(btn);
        }
    }
}
