package com.erp.inventory;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

public class InventoryDashboard extends VBox {
    public InventoryDashboard() {
        setSpacing(24);
        var welcomeBanner = new HBox(24);
        welcomeBanner.setStyle("-fx-background-color: linear-gradient(to right, #F59E0B, #D97706); -fx-background-radius: 12; -fx-padding: 32;");
        welcomeBanner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        var welcomeText = new VBox(8);
        var welcomeTitle = new Label("Welcome to Inventory Operations");
        welcomeTitle.setStyle("-fx-font-family: 'Inter', sans-serif; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        var welcomeSub = new Label("Track stock, products, and warehouse metrics.");
        welcomeSub.setStyle("-fx-font-family: 'Inter', sans-serif; -fx-font-size: 14px; -fx-text-fill: #FEF3C7;");
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
        var products = new StatCard("Products", "—", BytedanceIconsRegularMZ.TAG_ONE, "#6366F1");
        var stock = new StatCard("Stock Items", "—", BytedanceIconsRegularAL.BOX, "#10B981");
        var categories = new StatCard("Categories", "—", BytedanceIconsRegularAL.FOLDER_CLOSE, "#F59E0B");
        var warehouses = new StatCard("Warehouses", "—", BytedanceIconsRegularMZ.WAREHOUSING, "#3B82F6");
        statsRow.getChildren().addAll(products, stock, categories, warehouses);
        var skeleton = new SkeletonPane(3);
        getChildren().addAll(welcomeBanner, statsRow, skeleton);

        var api = ApiClient.getInstance();
        api.getRaw("/inventory/products").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) products.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/inventory/stock").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) stock.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/inventory/categories").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) categories.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/inventory/warehouses").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) warehouses.setValue(String.valueOf(d.size())); } catch (Exception ignored) {}
            getChildren().remove(skeleton);
        })).exceptionally(ex -> { Platform.runLater(() -> getChildren().remove(skeleton)); return null; });
    }
}
