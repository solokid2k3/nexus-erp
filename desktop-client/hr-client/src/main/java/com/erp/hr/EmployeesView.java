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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import java.util.Map;

public class EmployeesView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public EmployeesView() {
        setSpacing(16);
        var title = new Label("Employees");
        title.getStyleClass().add("display-heading");

        var addBtn = new Button("Add Employee");
        addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD);
        ai.setIconSize(14); ai.setStyle("-fx-icon-color:#FFF;");
        addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());

        var toolbar = new DataToolbar(new SearchField("Search employees..."), addBtn);
        var table = new TableView<>(items);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var nameCol = new TableColumn<Map<String, Object>, String>("Name");
        nameCol.setCellValueFactory(c -> {
            var fn = str(c.getValue(), "first_name");
            var ln = str(c.getValue(), "last_name");
            return new SimpleStringProperty(fn + " " + ln);
        });
        var emailCol = new TableColumn<Map<String, Object>, String>("Email");
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "email")));
        var deptCol = new TableColumn<Map<String, Object>, String>("Department");
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "department_name")));
        var posCol = new TableColumn<Map<String, Object>, String>("Position");
        posCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "position")));
        var statusCol = new TableColumn<Map<String, Object>, String>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                setGraphic(StatusBadge.forStatus(item)); setText(null);
            }
        });

        table.getColumns().addAll(nameCol, emailCol, deptCol, posCol, statusCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularAL.EVERY_USER, "No employees", "Add your first employee"));
        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/hr/employees").thenAccept(body -> Platform.runLater(() -> {
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
        dialog.setTitle("Add Employee");
        var fn = new TextField(); fn.setPromptText("First name");
        var ln = new TextField(); ln.setPromptText("Last name");
        var ef = new TextField(); ef.setPromptText("Email");
        var pf = new TextField(); pf.setPromptText("Position");
        var form = new VBox(12,
            new FormField("First Name", fn), new FormField("Last Name", ln),
            new FormField("Email", ef), new FormField("Position", pf));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !fn.getText().isBlank()) {
                ApiClient.getInstance().post("/hr/employees",
                    Map.of("first_name", fn.getText(), "last_name", ln.getText(),
                           "email", ef.getText(), "position", pf.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData));
            }
            return null;
        });
        dialog.showAndWait();
    }

    private String str(Map<String, Object> m, String k) {
        var v = m.get(k); return v != null ? v.toString() : "";
    }
}
