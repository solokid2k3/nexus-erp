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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;
import java.util.Map;

public class SuppliersView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();
    @SuppressWarnings("unchecked")
    public SuppliersView() {
        setSpacing(16);
        var title = new Label("Suppliers"); title.getStyleClass().add("display-heading");
        var addBtn = new Button("Add Supplier"); addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD); ai.setIconSize(14); ai.setStyle("-fx-icon-color:#FFF;"); addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());
        var toolbar = new DataToolbar(new SearchField("Search suppliers..."), addBtn);
        var table = new TableView<>(items); VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var nameCol = new TableColumn<Map<String, Object>, String>("Name"); nameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "name")));
        var contactCol = new TableColumn<Map<String, Object>, String>("Contact"); contactCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "contact_name")));
        var emailCol = new TableColumn<Map<String, Object>, String>("Email"); emailCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "email")));
        var phoneCol = new TableColumn<Map<String, Object>, String>("Phone"); phoneCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "phone")));
        table.getColumns().addAll(nameCol, contactCol, emailCol, phoneCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularAL.FACTORY_BUILDING, "No suppliers", "Add your first supplier", "Add Supplier", this::showCreate));
        getChildren().addAll(title, toolbar, table); loadData();
    }
    @SuppressWarnings("unchecked")
    private void loadData() { ApiClient.getInstance().getRaw("/orders/suppliers").thenAccept(body -> Platform.runLater(() -> {
        try { var api = ApiClient.getInstance(); var d = api.getMapper().readTree(body); items.clear();
            if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class)); } catch (Exception ignored) {} })); }
    private void showCreate() {
        var dialog = new Dialog<Void>(); dialog.setTitle("Add Supplier");
        var nf = new TextField(); nf.setPromptText("Company name"); var cf = new TextField(); cf.setPromptText("Contact name");
        var ef = new TextField(); ef.setPromptText("Email"); var pf = new TextField(); pf.setPromptText("Phone");
        var form = new VBox(12, new FormField("Name", nf), new FormField("Contact", cf), new FormField("Email", ef), new FormField("Phone", pf));
        form.setPadding(new Insets(16)); dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK && !nf.getText().isBlank()) {
            ApiClient.getInstance().post("/orders/suppliers", Map.of("name", nf.getText(), "contact_name", cf.getText(), "email", ef.getText(), "phone", pf.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData)); } return null; }); dialog.showAndWait(); }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
