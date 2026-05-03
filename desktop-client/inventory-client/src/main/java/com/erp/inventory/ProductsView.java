package com.erp.inventory;

import com.erp.core.api.ApiClient;
import com.erp.core.component.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;
import java.util.Map;

public class ProductsView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public ProductsView() {
        setSpacing(16);
        var title = new Label("Products"); title.getStyleClass().add("display-heading");
        var addBtn = new Button("Add Product"); addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD); ai.setIconSize(14); ai.setIconColor(javafx.scene.paint.Paint.valueOf("#FFFFFF")); addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());
        var toolbar = new DataToolbar(new SearchField("Search products..."), addBtn);
        var table = new TableView<>(items); VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var nameCol = new TableColumn<Map<String, Object>, String>("Name"); nameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "name")));
        var skuCol = new TableColumn<Map<String, Object>, String>("SKU"); skuCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "sku")));
        var priceCol = new TableColumn<Map<String, Object>, String>("Price"); priceCol.setCellValueFactory(c -> { var v = c.getValue().get("price"); return new SimpleStringProperty(v != null ? "$" + v : "—"); });
        var qtyCol = new TableColumn<Map<String, Object>, String>("Quantity"); qtyCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "quantity")));
        var statusCol = new TableColumn<Map<String, Object>, String>("Status"); statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty); if (empty || item == null) { setGraphic(null); setText(null); return; } setGraphic(StatusBadge.forStatus(item)); setText(null); }});
        table.getColumns().addAll(nameCol, skuCol, priceCol, qtyCol, statusCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularMZ.TAG_ONE, "No products", "Add your first product", "Add Product", this::showCreate));
        getChildren().addAll(title, toolbar, table); loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() { ApiClient.getInstance().getRaw("/inventory/products").thenAccept(body -> Platform.runLater(() -> {
        try { var api = ApiClient.getInstance(); var d = api.getMapper().readTree(body); items.clear();
            if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class)); } catch (Exception ignored) {} })); }

    private void showCreate() {
        var dialog = new Dialog<Void>(); dialog.setTitle("Add Product");
        var nf = new TextField(); nf.setPromptText("Name"); var sf = new TextField(); sf.setPromptText("SKU"); var pf = new TextField(); pf.setPromptText("Price");
        var form = new VBox(12, new FormField("Name", nf), new FormField("SKU", sf), new FormField("Price", pf));
        form.setPadding(new Insets(16)); dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK && !nf.getText().isBlank()) {
            ApiClient.getInstance().post("/inventory/products", Map.of("name", nf.getText(), "sku", sf.getText(), "price", pf.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData)); } return null; }); dialog.showAndWait(); }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
