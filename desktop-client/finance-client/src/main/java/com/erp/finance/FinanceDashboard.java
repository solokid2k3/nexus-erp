package com.erp.finance;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;

public class FinanceDashboard extends VBox {
    public FinanceDashboard() {
        setSpacing(24);
        var welcomeBanner = new HBox(24);
        welcomeBanner.setStyle("-fx-background-color: linear-gradient(to right, #10B981, #059669); -fx-background-radius: 12; -fx-padding: 32;");
        welcomeBanner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        var welcomeText = new VBox(8);
        var welcomeTitle = new Label("Welcome to Finance Analytics");
        welcomeTitle.setStyle("-fx-font-family: 'Inter', sans-serif; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        var welcomeSub = new Label("Monitor your accounts, budgets, and invoices.");
        welcomeSub.setStyle("-fx-font-family: 'Inter', sans-serif; -fx-font-size: 14px; -fx-text-fill: #D1FAE5;");
        welcomeText.getChildren().addAll(welcomeTitle, welcomeSub);
        
        javafx.scene.layout.HBox.setHgrow(welcomeText, javafx.scene.layout.Priority.ALWAYS);
        
        try {
            var imageStream = getClass().getResourceAsStream("/images/sticker_dashboard.png");
            if (imageStream != null) {
                var image = new javafx.scene.image.Image(imageStream);
                var imageView = new javafx.scene.image.ImageView(image);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(true);
                welcomeBanner.getChildren().addAll(welcomeText, imageView);
            } else {
                welcomeBanner.getChildren().add(welcomeText);
            }
        } catch (Exception e) {
            welcomeBanner.getChildren().add(welcomeText);
        }
        var statsRow = new HBox(16);
        var accounts = new StatCard("Accounts", "—", BytedanceIconsRegularAL.BANK, "#6366F1");
        var journals = new StatCard("Journal Entries", "—", BytedanceIconsRegularAL.BOOK_OPEN, "#10B981");
        var invoices = new StatCard("Pending Invoices", "—", BytedanceIconsRegularAL.BILL, "#F59E0B");
        var budgets = new StatCard("Budgets", "—", BytedanceIconsRegularAL.FUNDS, "#3B82F6");
        statsRow.getChildren().addAll(accounts, journals, invoices, budgets);
        var skeleton = new SkeletonPane(3);
        getChildren().addAll(welcomeBanner, statsRow, skeleton);

        var api = ApiClient.getInstance();
        api.getRaw("/finance/accounts").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) accounts.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/finance/journal-entries").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) journals.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/finance/invoices").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) invoices.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/finance/budgets").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) budgets.setValue(String.valueOf(d.size())); } catch (Exception ignored) {}
            getChildren().remove(skeleton);
        })).exceptionally(ex -> { Platform.runLater(() -> getChildren().remove(skeleton)); return null; });
    }
}
