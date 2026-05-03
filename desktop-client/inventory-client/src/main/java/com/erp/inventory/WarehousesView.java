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

public class WarehousesView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public WarehousesView() {
        setSpacing(16);
        var title = new Label("Warehouses"); title.getStyleClass().add("display-heading");
        var addBtn = new Button("Add Warehouse"); addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD); ai.setIconSize(14); ai.setIconColor(javafx.scene.paint.Paint.valueOf("#FFFFFF")); addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());
        var toolbar = new DataToolbar(new SearchField("Search warehouses..."), addBtn);
        var table = new TableView<>(items); VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var nameCol = new TableColumn<Map<String, Object>, String>("Name"); nameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "name")));
        var locCol = new TableColumn<Map<String, Object>, String>("Location"); locCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "location")));
        var capCol = new TableColumn<Map<String, Object>, String>("Capacity"); capCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "capacity")));
        table.getColumns().addAll(nameCol, locCol, capCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularMZ.WAREHOUSING, "No warehouses", "Add your first warehouse", "Add Warehouse", this::showCreate));
        getChildren().addAll(title, toolbar, table); loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() { ApiClient.getInstance().getRaw("/inventory/warehouses").thenAccept(body -> Platform.runLater(() -> {
        try { var api = ApiClient.getInstance(); var d = api.getMapper().readTree(body); items.clear();
            if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class)); } catch (Exception ignored) {} })); }

    private void showCreate() {
        var dialog = new Dialog<Void>(); dialog.setTitle("Add Warehouse");
        var nf = new TextField(); nf.setPromptText("Name"); var lf = new TextField(); lf.setPromptText("Location"); var cf = new TextField(); cf.setPromptText("Capacity");
        var form = new VBox(12, new FormField("Name", nf), new FormField("Location", lf), new FormField("Capacity", cf)); form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK && !nf.getText().isBlank()) {
            ApiClient.getInstance().post("/inventory/warehouses", Map.of("name", nf.getText(), "location", lf.getText(), "capacity", cf.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData)); } return null; }); dialog.showAndWait(); }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
