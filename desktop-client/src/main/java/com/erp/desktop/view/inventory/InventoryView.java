package com.erp.desktop.view.inventory;

import com.erp.desktop.api.ApiClient;
import com.erp.desktop.component.LoadingSpinner;
import com.erp.desktop.component.Toast;
import com.erp.desktop.util.CurrencyFormatter;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.*;

public class InventoryView extends ScrollPane {
    private final StackPane toastParent;
    private final ApiClient api = ApiClient.getInstance();
    private final TableView<Map<String, Object>> productTable = new TableView<>();
    private final ObservableList<Map<String, Object>> products = FXCollections.observableArrayList();
    private final VBox contentRoot = new VBox(20);

    public InventoryView(StackPane toastParent) {
        this.toastParent = toastParent;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        contentRoot.setPadding(new Insets(32));

        var header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        var title = new Label("📦  Inventory");
        title.getStyleClass().add("heading-1");
        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        var addBtn = new Button("+ New Product");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> showCreateProductDialog());
        header.getChildren().addAll(title, spacer, addBtn);

        var tabBar = new HBox(6);
        var tabs = List.of("📋  Products", "⚠️  Stock Alerts", "🏷️  Categories");
        var buttons = new ArrayList<Button>();
        for (int i = 0; i < tabs.size(); i++) {
            var btn = new Button(tabs.get(i)); btn.getStyleClass().add("badge");
            if (i == 0) btn.getStyleClass().add("badge-info");
            int idx = i;
            btn.setOnAction(e -> { buttons.forEach(b -> b.getStyleClass().remove("badge-info")); btn.getStyleClass().add("badge-info"); showTab(idx); });
            buttons.add(btn); tabBar.getChildren().add(btn);
        }

        contentRoot.getChildren().addAll(header, tabBar);
        setContent(contentRoot);
        buildProductTable();
        showTab(0);
    }

    private void showTab(int idx) {
        while (contentRoot.getChildren().size() > 2) contentRoot.getChildren().removeLast();
        switch (idx) {
            case 0 -> showProductsTab();
            case 1 -> showStockAlerts();
            case 2 -> showCategories();
        }
    }

    @SuppressWarnings("unchecked")
    private void showProductsTab() {
        var spinner = new LoadingSpinner("Loading products...");
        contentRoot.getChildren().add(spinner);

        api.get("/inventory/products").thenAccept(resp -> Platform.runLater(() -> {
            contentRoot.getChildren().remove(spinner);
            products.clear();
            if (resp.containsKey("products")) {
                var list = (List<Map<String, Object>>) resp.get("products");
                if (list != null) products.addAll(list);
            }
            var card = new VBox(productTable);
            card.getStyleClass().add("card");
            card.setPadding(new Insets(0));
            productTable.setPrefHeight(500);
            contentRoot.getChildren().add(card);
            fadeIn(card);
        }));
    }

    @SuppressWarnings("unchecked")
    private void showStockAlerts() {
        var spinner = new LoadingSpinner("Loading stock alerts...");
        contentRoot.getChildren().add(spinner);

        api.get("/inventory/stock/alerts").thenAccept(resp -> Platform.runLater(() -> {
            contentRoot.getChildren().remove(spinner);
            var table = new TableView<Map<String, Object>>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
            table.setPrefHeight(500);
            table.getColumns().addAll(col("Product", "product_name"), col("Severity", "alert_level"), col("Qty", "quantity_on_hand"));
            if (resp.containsKey("alerts")) {
                var list = (List<Map<String, Object>>) resp.get("alerts");
                if (list != null) table.getItems().addAll(list);
            }
            if (table.getItems().isEmpty()) { table.setPlaceholder(new Label("📭  No stock alerts")); }
            var card = new VBox(table); card.getStyleClass().add("card"); card.setPadding(new Insets(0));
            contentRoot.getChildren().add(card);
            fadeIn(card);
        }));
    }

    @SuppressWarnings("unchecked")
    private void showCategories() {
        var spinner = new LoadingSpinner("Loading categories...");
        contentRoot.getChildren().add(spinner);

        api.get("/inventory/categories").thenAccept(resp -> Platform.runLater(() -> {
            contentRoot.getChildren().remove(spinner);
            var table = new TableView<Map<String, Object>>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
            table.setPrefHeight(500);
            table.getColumns().addAll(col("Name", "name"), col("Description", "description"));
            if (resp.containsKey("categories")) {
                var list = (List<Map<String, Object>>) resp.get("categories");
                if (list != null) table.getItems().addAll(list);
            }
            if (table.getItems().isEmpty()) { table.setPlaceholder(new Label("📭  No categories")); }
            var card = new VBox(table); card.getStyleClass().add("card"); card.setPadding(new Insets(0));
            contentRoot.getChildren().add(card);
            fadeIn(card);
        }));
    }

    private void buildProductTable() {
        productTable.setItems(products);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        productTable.getColumns().addAll(col("SKU", "sku"), col("Name", "name"), col("Status", "status"),
                colCurrency("Unit Cost", "unit_cost_cents"), colCurrency("Sell Price", "selling_price_cents"), col("Category", "category_id"));
        productTable.setPlaceholder(new Label("📭  No products found"));
    }

    private void showCreateProductDialog() {
        var dialog = new Dialog<Map<String, Object>>();
        dialog.setTitle("Create Product");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(400);
        var grid = new GridPane(); grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        var skuField = new TextField(); skuField.setPromptText("SKU");
        var nameField = new TextField(); nameField.setPromptText("Product Name");
        var costField = new TextField(); costField.setPromptText("Unit Cost (cents)");
        var priceField = new TextField(); priceField.setPromptText("Selling Price (cents)");
        grid.add(new Label("SKU"), 0, 0); grid.add(skuField, 1, 0);
        grid.add(new Label("Name"), 0, 1); grid.add(nameField, 1, 1);
        grid.add(new Label("Unit Cost"), 0, 2); grid.add(costField, 1, 2);
        grid.add(new Label("Sell Price"), 0, 3); grid.add(priceField, 1, 3);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                var data = new LinkedHashMap<String, Object>();
                data.put("sku", skuField.getText().trim());
                data.put("name", nameField.getText().trim());
                try { data.put("unit_cost_cents", Long.parseLong(costField.getText().trim())); } catch (Exception ignored) {}
                try { data.put("selling_price_cents", Long.parseLong(priceField.getText().trim())); } catch (Exception ignored) {}
                return data;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(data -> {
            api.post("/inventory/products", data).thenAccept(resp -> Platform.runLater(() -> {
                if (resp.containsKey("error")) Toast.show(toastParent, (String) resp.get("error"), Toast.Type.ERROR);
                else { Toast.show(toastParent, "Product created", Toast.Type.SUCCESS); showTab(0); }
            }));
        });
    }

    private TableColumn<Map<String, Object>, String> col(String title, String key) {
        var c = new TableColumn<Map<String, Object>, String>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get(key) != null ? cd.getValue().get(key).toString() : "—"));
        return c;
    }
    private TableColumn<Map<String, Object>, String> colCurrency(String title, String key) {
        var c = new TableColumn<Map<String, Object>, String>(title);
        c.setCellValueFactory(cd -> { var v = cd.getValue().get(key); return v instanceof Number n ? new SimpleStringProperty(CurrencyFormatter.fromCents(n.longValue())) : new SimpleStringProperty("—"); });
        return c;
    }
    private void fadeIn(javafx.scene.Node node) {
        node.setOpacity(0);
        var f = new FadeTransition(Duration.millis(250), node); f.setFromValue(0); f.setToValue(1); f.play();
    }
}
