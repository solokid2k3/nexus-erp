package com.erp.core.component;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class SearchField extends StackPane {

    private final TextField textField;
    private Timer debounceTimer;
    private Consumer<String> onSearch;

    public SearchField(String placeholder) {
        textField = new TextField();
        textField.setPromptText(placeholder);
        textField.getStyleClass().add("search-field");
        textField.setPrefHeight(36);
        textField.setPrefWidth(260);

        var searchIcon = new FontIcon(FontAwesomeSolid.SEARCH);
        searchIcon.setIconSize(13);
        searchIcon.setStyle("-fx-icon-color: #9CA3AF;");

        StackPane.setAlignment(searchIcon, javafx.geometry.Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 12));

        getChildren().addAll(textField, searchIcon);

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (debounceTimer != null) debounceTimer.cancel();
            debounceTimer = new Timer();
            debounceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        if (onSearch != null) onSearch.accept(newVal);
                    });
                }
            }, 300);
        });
    }

    public SearchField() {
        this("Search...");
    }

    public void setOnSearch(Consumer<String> handler) {
        this.onSearch = handler;
    }

    public String getText() {
        return textField.getText();
    }

    public TextField getTextField() {
        return textField;
    }
}
