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
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Map;

public class WarehousesView extends VBox {

    private final ObservableList<Map<String, Object>> warehouses = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public WarehousesView() {
        setSpacing(16);
        var title = new Label("Warehouses");
        title.getStyleClass().add("display-heading");

        var addBtn = new Button("Add Warehouse");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setGraphic(createIcon(FontAwesomeSolid.PLUS, "#FFFFFF"));
        addBtn.setOnAction(e -> showCreateDialog());

        var toolbar = new DataToolbar(null, addBtn);

        var table = new TableView<>(warehouses);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var nameCol = new TableColumn<Map<String, Object>, String>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "name")));
        var codeCol = new TableColumn<Map<String, Object>, String>("Code");
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "code")));
        var addrCol = new TableColumn<Map<String, Object>, String>("Address");
        addrCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "address")));
        table.getColumns().addAll(nameCol, codeCol, addrCol);

        table.setPlaceholder(new EmptyState(FontAwesomeSolid.BUILDING, "No warehouses", "Create your first warehouse", "Add Warehouse", this::showCreateDialog));

        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/inventory/warehouses").thenAccept(body -> Platform.runLater(() -> {
            try {
                var api = ApiClient.getInstance();
                var data = api.getMapper().readTree(body);
                warehouses.clear();
                if (data.isArray()) for (var n : data) warehouses.add(api.getMapper().convertValue(n, Map.class));
            } catch (Exception ignored) {}
        })).exceptionally(ex -> null);
    }

    private void showCreateDialog() {
        var dialog = new Dialog<Void>();
        dialog.setTitle("Add Warehouse");
        var nameField = new TextField(); nameField.setPromptText("Warehouse name");
        var codeField = new TextField(); codeField.setPromptText("Code (e.g. WH-01)");
        var addrField = new TextField(); addrField.setPromptText("Address");
        var form = new VBox(12, new FormField("Name", nameField), new FormField("Code", codeField), new FormField("Address", addrField));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !nameField.getText().isBlank()) {
                ApiClient.getInstance().post("/inventory/warehouses", Map.of("name", nameField.getText(), "code", codeField.getText(), "address", addrField.getText()))
                        .thenAccept(r -> Platform.runLater(this::loadData));
            }
            return null;
        });
        dialog.showAndWait();
    }

    private FontIcon createIcon(FontAwesomeSolid icon, String color) { var fi = new FontIcon(icon); fi.setIconSize(12); fi.setStyle("-fx-icon-color: " + color + ";"); return fi; }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
