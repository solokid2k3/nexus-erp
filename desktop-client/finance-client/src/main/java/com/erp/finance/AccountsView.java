package com.erp.finance;

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

public class AccountsView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();
    @SuppressWarnings("unchecked")
    public AccountsView() {
        setSpacing(16);
        var title = new Label("Chart of Accounts"); title.getStyleClass().add("display-heading");
        var addBtn = new Button("Add Account"); addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(FontAwesomeSolid.PLUS); ai.setIconSize(12); ai.setStyle("-fx-icon-color:#FFF;"); addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());
        var toolbar = new DataToolbar(new SearchField("Search accounts..."), addBtn);
        var table = new TableView<>(items); VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var codeCol = new TableColumn<Map<String, Object>, String>("Code"); codeCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "code")));
        var nameCol = new TableColumn<Map<String, Object>, String>("Name"); nameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "name")));
        var typeCol = new TableColumn<Map<String, Object>, String>("Type"); typeCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "type")));
        typeCol.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty); if (empty || item == null) { setGraphic(null); setText(null); return; } setGraphic(StatusBadge.forStatus(item)); setText(null); }});
        var balCol = new TableColumn<Map<String, Object>, String>("Balance"); balCol.setCellValueFactory(c -> { var v = c.getValue().get("balance"); return new SimpleStringProperty(v != null ? "$" + v : "$0"); });
        table.getColumns().addAll(codeCol, nameCol, typeCol, balCol);
        table.setPlaceholder(new EmptyState(FontAwesomeSolid.UNIVERSITY, "No accounts", "Create your first account"));
        getChildren().addAll(title, toolbar, table); loadData();
    }
    @SuppressWarnings("unchecked")
    private void loadData() { ApiClient.getInstance().getRaw("/finance/accounts").thenAccept(body -> Platform.runLater(() -> {
        try { var api = ApiClient.getInstance(); var d = api.getMapper().readTree(body); items.clear();
            if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class)); } catch (Exception ignored) {} })); }
    private void showCreate() {
        var dialog = new Dialog<Void>(); dialog.setTitle("Add Account");
        var cf = new TextField(); cf.setPromptText("Code"); var nf = new TextField(); nf.setPromptText("Name"); var tf = new TextField(); tf.setPromptText("Type (asset/liability/equity/revenue/expense)");
        var form = new VBox(12, new FormField("Code", cf), new FormField("Name", nf), new FormField("Type", tf)); form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK && !cf.getText().isBlank()) {
            ApiClient.getInstance().post("/finance/accounts", Map.of("code", cf.getText(), "name", nf.getText(), "type", tf.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData)); } return null; }); dialog.showAndWait(); }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
