package com.erp.hr;

import com.erp.core.api.ApiClient;
import com.erp.core.component.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;
import java.util.Map;

public class AttendanceView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public AttendanceView() {
        setSpacing(16);
        var title = new Label("Attendance");
        title.getStyleClass().add("display-heading");

        var clockInBtn = new Button("Clock In");
        clockInBtn.getStyleClass().add("button-success");
        var ciIcon = new FontIcon(BytedanceIconsRegularAL.LOGIN);
        ciIcon.setIconSize(14); ciIcon.setStyle("-fx-icon-color:#FFF;");
        clockInBtn.setGraphic(ciIcon);
        clockInBtn.setOnAction(e -> ApiClient.getInstance()
            .post("/hr/attendance/clock-in", Map.of("employee_id", "self"))
            .thenAccept(r -> Platform.runLater(this::loadData)));

        var clockOutBtn = new Button("Clock Out");
        clockOutBtn.getStyleClass().add("button-danger");
        var coIcon = new FontIcon(BytedanceIconsRegularAL.LOGOUT);
        coIcon.setIconSize(14); coIcon.setStyle("-fx-icon-color:#DC2626;");
        clockOutBtn.setGraphic(coIcon);
        clockOutBtn.setOnAction(e -> ApiClient.getInstance()
            .post("/hr/attendance/clock-out", Map.of("employee_id", "self"))
            .thenAccept(r -> Platform.runLater(this::loadData)));

        var toolbar = new DataToolbar(new SearchField("Search..."), clockInBtn, clockOutBtn);
        var table = new TableView<>(items);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var empCol = new TableColumn<Map<String, Object>, String>("Employee");
        empCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "employee_name")));
        var dateCol = new TableColumn<Map<String, Object>, String>("Date");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "date")));
        var inCol = new TableColumn<Map<String, Object>, String>("Clock In");
        inCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "clock_in")));
        var outCol = new TableColumn<Map<String, Object>, String>("Clock Out");
        outCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "clock_out")));
        var hoursCol = new TableColumn<Map<String, Object>, String>("Hours");
        hoursCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "hours_worked")));

        table.getColumns().addAll(empCol, dateCol, inCol, outCol, hoursCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularMZ.TIME, "No attendance records", "Clock in to start"));
        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/hr/attendance").thenAccept(body -> Platform.runLater(() -> {
            try {
                var api = ApiClient.getInstance();
                var d = api.getMapper().readTree(body);
                items.clear();
                if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class));
            } catch (Exception ignored) {}
        }));
    }

    private String str(Map<String, Object> m, String k) {
        var v = m.get(k); return v != null ? v.toString() : "—";
    }
}
