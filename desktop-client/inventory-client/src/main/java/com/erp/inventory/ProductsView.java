package com.erp.inventory;

import com.erp.core.api.ApiClient;
import com.erp.core.component.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Map;

public class ProductsView extends VBox {

    private final ObservableList<Map<String, Object>> products = FXCollections.observableArrayList();
    private final TableView<Map<String, Object>> table;

    @SuppressWarnings("unchecked")
    public ProductsView() {
        setSpacing(16);
        var title = new Label("Products");
        title.getStyleClass().add("display-heading");

        var addBtn = new Button("Add Product");
        addBtn.getStyleClass().add("button-primary");
        var addIcon = new FontIcon(FontAwesomeSolid.PLUS);
        addIcon.setIconSize(12);
        addIcon.setStyle("-fx-icon-color: #FFFFFF;");
        addBtn.setGraphic(addIcon);
        addBtn.setOnAction(e -> showCreateDialog());

        var search = new SearchField("Search products...");
        var toolbar = new DataToolbar(search, addBtn);

        table = new TableView<>(products);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        var nameCol = new TableColumn<Map<String, Object>, String>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "name")));
        nameCol.setPrefWidth(200);

        var skuCol = new TableColumn<Map<String, Object>, String>("SKU");
        skuCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "sku")));

        var priceCol = new TableColumn<Map<String, Object>, String>("Price");
        priceCol.setCellValueFactory(c -> {
            var v = c.getValue().get("price");
            return new SimpleStringProperty(v != null ? "$" + v : "—");
        });

        var statusCol = new TableColumn<Map<String, Object>, String>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                setGraphic(StatusBadge.forStatus(item));
                setText(null);
            }
        });

        table.getColumns().addAll(nameCol, skuCol, priceCol, statusCol);

        var emptyState = new EmptyState(FontAwesomeSolid.TAG, "No products yet",
                "Add your first product to get started", "Add Product", this::showCreateDialog);
        table.setPlaceholder(emptyState);

        getChildren().addAll(title, toolbar, table);

        search.setOnSearch(query -> loadProducts(query));
        loadProducts(null);
    }

    @SuppressWarnings("unchecked")
    private void loadProducts(String query) {
        var path = query != null && !query.isBlank() ? "/inventory/products?q=" + query : "/inventory/products";
        ApiClient.getInstance().getRaw(path).thenAccept(body -> Platform.runLater(() -> {
            try {
                var mapper = ApiClient.getInstance().getMapper();
                var data = mapper.readTree(body);
                products.clear();
                var items = data.isArray() ? data : (data.has("products") ? data.get("products") : data);
                if (items.isArray()) {
                    for (var node : items) {
                        products.add(mapper.convertValue(node, Map.class));
                    }
                }
            } catch (Exception ignored) {}
        })).exceptionally(ex -> null);
    }

    private void showCreateDialog() {
        var dialog = new Dialog<Map<String, String>>();
        dialog.setTitle("Add Product");
        dialog.setHeaderText("Create a new product");

        var nameField = new TextField();
        nameField.setPromptText("Product name");
        var skuField = new TextField();
        skuField.setPromptText("SKU");
        var priceField = new TextField();
        priceField.setPromptText("Price");

        var form = new VBox(12,
                new FormField("Name", nameField),
                new FormField("SKU", skuField),
                new FormField("Price", priceField));
        form.setPadding(new Insets(16));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                return Map.of("name", nameField.getText(), "sku", skuField.getText(), "price", priceField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            var body = Map.of("name", data.get("name"), "sku", data.get("sku"),
                    "price", Double.parseDouble(data.getOrDefault("price", "0")));
            ApiClient.getInstance().post("/inventory/products", body).thenAccept(r ->
                    Platform.runLater(() -> loadProducts(null)));
        });
    }

    private String str(Map<String, Object> map, String key) {
        var v = map.get(key);
        return v != null ? v.toString() : "—";
    }
}
