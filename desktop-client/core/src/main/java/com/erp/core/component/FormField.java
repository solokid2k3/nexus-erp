package com.erp.core.component;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class FormField extends VBox {

    private final Label errorLabel;

    public FormField(String labelText, Node inputNode) {
        getStyleClass().add("form-group");
        setSpacing(6);

        var label = new Label(labelText);
        label.getStyleClass().add("form-label");

        errorLabel = new Label();
        errorLabel.getStyleClass().add("form-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        getChildren().addAll(label, inputNode, errorLabel);
    }

    public void setError(String message) {
        if (message == null || message.isBlank()) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        } else {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    public void clearError() {
        setError(null);
    }
}
