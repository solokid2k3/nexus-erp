package com.erp.hr;

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

public class LeaveView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public LeaveView() {
        setSpacing(16);
        var title = new Label("Leave Requests");
        title.getStyleClass().add("display-heading");

        var addBtn = new Button("New Request");
        addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(FontAwesomeSolid.PLUS);
        ai.setIconSize(12); ai.setStyle("-fx-icon-color:#FFF;");
        addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());

        var toolbar = new DataToolbar(new SearchField("Search..."), addBtn);
        var table = new TableView<>(items);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var empCol = new TableColumn<Map<String, Object>, String>("Employee");
        empCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "employee_name")));
        var typeCol = new TableColumn<Map<String, Object>, String>("Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "leave_type")));
        var startCol = new TableColumn<Map<String, Object>, String>("Start");
        startCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "start_date")));
        var endCol = new TableColumn<Map<String, Object>, String>("End");
        endCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "end_date")));
        var statusCol = new TableColumn<Map<String, Object>, String>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                setGraphic(StatusBadge.forStatus(item)); setText(null);
            }
        });

        table.getColumns().addAll(empCol, typeCol, startCol, endCol, statusCol);
        table.setPlaceholder(new EmptyState(FontAwesomeSolid.PLANE_DEPARTURE, "No leave requests", "Submit a leave request"));
        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/hr/leave/requests").thenAccept(body -> Platform.runLater(() -> {
            try {
                var api = ApiClient.getInstance();
                var d = api.getMapper().readTree(body);
                items.clear();
                if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class));
            } catch (Exception ignored) {}
        }));
    }

    private void showCreate() {
        var dialog = new Dialog<Void>();
        dialog.setTitle("Submit Leave Request");
        var eid = new TextField(); eid.setPromptText("Employee ID");
        var type = new TextField(); type.setPromptText("Leave type (annual/sick/personal)");
        var start = new TextField(); start.setPromptText("Start date (YYYY-MM-DD)");
        var end = new TextField(); end.setPromptText("End date (YYYY-MM-DD)");
        var form = new VBox(12,
            new FormField("Employee ID", eid), new FormField("Type", type),
            new FormField("Start Date", start), new FormField("End Date", end));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !eid.getText().isBlank()) {
                ApiClient.getInstance().post("/hr/leave/request",
                    Map.of("employee_id", eid.getText(), "leave_type", type.getText(),
                           "start_date", start.getText(), "end_date", end.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData));
            }
            return null;
        });
        dialog.showAndWait();
    }

    private String str(Map<String, Object> m, String k) {
        var v = m.get(k); return v != null ? v.toString() : "—";
    }
}
