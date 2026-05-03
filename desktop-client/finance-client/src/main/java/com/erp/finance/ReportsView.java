package com.erp.finance;

import com.erp.core.api.ApiClient;
import com.erp.core.component.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

public class ReportsView extends VBox {
    public ReportsView() {
        setSpacing(24);
        var title = new Label("Financial Reports"); title.getStyleClass().add("display-heading");
        var subtitle = new Label("Select a report to generate"); subtitle.getStyleClass().add("body-muted");

        var grid = new FlowPane(16, 16);
        grid.setPadding(new Insets(8, 0, 0, 0));

        grid.getChildren().addAll(
                createReportCard("Trial Balance", BytedanceIconsRegularMZ.SCALE, "#6366F1", "/finance/reports/trial-balance"),
                createReportCard("Profit & Loss", BytedanceIconsRegularAL.CHART_LINE, "#10B981", "/finance/reports/profit-loss"),
                createReportCard("Balance Sheet", BytedanceIconsRegularAL.DOCUMENT_FOLDER, "#3B82F6", "/finance/reports/balance-sheet"),
                createReportCard("AR Aging", BytedanceIconsRegularMZ.TIME, "#F59E0B", "/finance/reports/ar-aging")
        );

        getChildren().addAll(title, subtitle, grid);
    }

    private VBox createReportCard(String name, Ikon icon, String color, String endpoint) {
        var card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));
        card.setPrefWidth(260);
        card.setAlignment(Pos.CENTER_LEFT);

        var fi = new FontIcon(icon); fi.setIconSize(28); fi.setStyle("-fx-icon-color: " + color + ";");
        var label = new Label(name); label.getStyleClass().add("heading-3");
        var desc = new Label("Click to generate this report"); desc.getStyleClass().add("body-muted");

        var genBtn = new Button("Generate"); genBtn.getStyleClass().add("button-secondary");
        genBtn.setOnAction(e -> {
            genBtn.setDisable(true); genBtn.setText("Loading...");
            ApiClient.getInstance().getRaw(endpoint).thenAccept(body -> Platform.runLater(() -> {
                genBtn.setDisable(false); genBtn.setText("Generate");
                var resultDialog = new javafx.scene.control.Dialog<Void>();
                resultDialog.setTitle(name);
                var ta = new javafx.scene.control.TextArea(body);
                ta.setEditable(false); ta.setPrefRowCount(20); ta.setPrefColumnCount(60);
                resultDialog.getDialogPane().setContent(ta);
                resultDialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
                resultDialog.showAndWait();
            })).exceptionally(ex -> { Platform.runLater(() -> { genBtn.setDisable(false); genBtn.setText("Generate"); }); return null; });
        });

        card.getChildren().addAll(fi, label, desc, genBtn);
        return card;
    }
}
