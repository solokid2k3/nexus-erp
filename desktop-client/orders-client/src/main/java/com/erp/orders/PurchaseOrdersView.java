package com.erp.orders;

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
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.Map;

public class PurchaseOrdersView extends VBox {
    private final ObservableList<Map<String, Object>> orders = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public PurchaseOrdersView() {
        setSpacing(16);
        var title = new Label("Purchase Orders");
        title.getStyleClass().add("display-heading");
        var addBtn = new Button("New PO");
        addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(FontAwesomeSolid.PLUS); ai.setIconSize(12); ai.setStyle("-fx-icon-color:#FFF;");
        addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreateDialog());
        var toolbar = new DataToolbar(new SearchField("Search POs..."), addBtn);

        var table = new TableView<>(orders);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var idCol = new TableColumn<Map<String, Object>, String>("PO #");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "id")));
        var supCol = new TableColumn<Map<String, Object>, String>("Supplier");
        supCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "supplier_name")));
        var dateCol = new TableColumn<Map<String, Object>, String>("Date");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "created_at")));
        var totalCol = new TableColumn<Map<String, Object>, String>("Total");
        totalCol.setCellValueFactory(c -> { var v = c.getValue().get("total_amount"); return new SimpleStringProperty(v != null ? "$" + v : "—"); });
        var statusCol = new TableColumn<Map<String, Object>, String>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if (empty || item == null) { setGraphic(null); setText(null); return; }
                setGraphic(StatusBadge.forStatus(item)); setText(null); }});
        table.getColumns().addAll(idCol, supCol, dateCol, totalCol, statusCol);
        table.setPlaceholder(new EmptyState(FontAwesomeSolid.TRUCK, "No purchase orders", "Create your first PO"));
        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/orders/purchase").thenAccept(body -> Platform.runLater(() -> {
            try { var api = ApiClient.getInstance(); var data = api.getMapper().readTree(body); orders.clear();
                if (data.isArray()) for (var n : data) orders.add(api.getMapper().convertValue(n, Map.class));
            } catch (Exception ignored) {} }));
    }

    private void showCreateDialog() {
        var dialog = new Dialog<Void>(); dialog.setTitle("New Purchase Order");
        var supField = new TextField(); supField.setPromptText("Supplier ID");
        var form = new VBox(12, new FormField("Supplier ID", supField)); form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK && !supField.getText().isBlank()) {
            ApiClient.getInstance().post("/orders/purchase", Map.of("supplier_id", supField.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData)); } return null; });
        dialog.showAndWait();
    }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
