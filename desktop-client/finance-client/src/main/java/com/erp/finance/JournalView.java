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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import java.util.Map;

public class JournalView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public JournalView() {
        setSpacing(16);
        var title = new Label("Journal Entries"); title.getStyleClass().add("display-heading");
        var addBtn = new Button("New Entry"); addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD); ai.setIconSize(14); ai.setStyle("-fx-icon-color:#FFF;"); addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());
        var toolbar = new DataToolbar(new SearchField("Search entries..."), addBtn);
        var table = new TableView<>(items); VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var dateCol = new TableColumn<Map<String, Object>, String>("Date"); dateCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "date")));
        var refCol = new TableColumn<Map<String, Object>, String>("Reference"); refCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "reference")));
        var memoCol = new TableColumn<Map<String, Object>, String>("Memo"); memoCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "memo")));
        var statusCol = new TableColumn<Map<String, Object>, String>("Status"); statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty); if (empty || item == null) { setGraphic(null); setText(null); return; } setGraphic(StatusBadge.forStatus(item)); setText(null); }});
        table.getColumns().addAll(dateCol, refCol, memoCol, statusCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularAL.BOOK_OPEN, "No journal entries", "Create your first entry"));
        getChildren().addAll(title, toolbar, table); loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() { ApiClient.getInstance().getRaw("/finance/journal-entries").thenAccept(body -> Platform.runLater(() -> {
        try { var api = ApiClient.getInstance(); var d = api.getMapper().readTree(body); items.clear();
            if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class)); } catch (Exception ignored) {} })); }

    private void showCreate() {
        var dialog = new Dialog<Void>(); dialog.setTitle("New Journal Entry");
        var df = new TextField(); df.setPromptText("Date (YYYY-MM-DD)"); var mf = new TextField(); mf.setPromptText("Memo");
        var form = new VBox(12, new FormField("Date", df), new FormField("Memo", mf)); form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK && !df.getText().isBlank()) {
            ApiClient.getInstance().post("/finance/journal-entries", Map.of("date", df.getText(), "memo", mf.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData)); } return null; }); dialog.showAndWait(); }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
