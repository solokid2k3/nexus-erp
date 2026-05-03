package com.erp.finance;

import com.erp.core.api.ApiClient;
import com.erp.core.component.FormField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;

import java.util.Map;

public class TaxView extends VBox {
    public TaxView() {
        setSpacing(20);
        var title = new Label("Tax Calculator");
        title.getStyleClass().add("display-heading");

        var icon = new FontIcon(BytedanceIconsRegularAL.CALCULATOR);
        icon.setIconSize(36);
        icon.setStyle("-fx-icon-color: #6366F1;");

        var amountField = new TextField();
        amountField.setPromptText("Enter amount");
        var rateField = new TextField();
        rateField.setPromptText("Tax rate (%)");

        var resultLabel = new Label();
        resultLabel.getStyleClass().add("heading-2");
        resultLabel.setVisible(false);

        var calcBtn = new Button("Calculate Tax");
        calcBtn.getStyleClass().add("button-primary");
        calcBtn.setOnAction(e -> {
            var amount = amountField.getText();
            var rate = rateField.getText();
            if (amount.isBlank() || rate.isBlank()) return;
            calcBtn.setDisable(true);
            ApiClient.getInstance().post("/finance/tax/calculate",
                Map.of("amount", Double.parseDouble(amount), "rate", Double.parseDouble(rate)))
                .thenAccept(resp -> Platform.runLater(() -> {
                    calcBtn.setDisable(false);
                    var taxAmt = resp.getOrDefault("tax_amount", "0");
                    var total = resp.getOrDefault("total", "0");
                    resultLabel.setText("Tax: $" + taxAmt + "  |  Total: $" + total);
                    resultLabel.setVisible(true);
                })).exceptionally(ex -> {
                    Platform.runLater(() -> { calcBtn.setDisable(false); resultLabel.setText("API error"); resultLabel.setVisible(true); });
                    return null;
                });
        });

        var form = new VBox(16, new FormField("Amount", amountField), new FormField("Tax Rate (%)", rateField), calcBtn, resultLabel);
        form.setMaxWidth(400);
        form.setPadding(new Insets(24));
        form.getStyleClass().add("card");

        getChildren().addAll(title, icon, form);
    }
}
