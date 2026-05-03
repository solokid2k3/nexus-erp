package com.erp.hr;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class HRDashboard extends VBox {
    public HRDashboard() {
        setSpacing(24);
        var title = new Label("Dashboard");
        title.getStyleClass().add("display-heading");

        var statsRow = new HBox(16);
        var employees = new StatCard("Employees", "—", FontAwesomeSolid.USER_TIE, "#6366F1");
        var departments = new StatCard("Departments", "—", FontAwesomeSolid.BUILDING, "#3B82F6");
        var leaves = new StatCard("Pending Leaves", "—", FontAwesomeSolid.PLANE_DEPARTURE, "#F59E0B");
        var payroll = new StatCard("Payroll Runs", "—", FontAwesomeSolid.MONEY_BILL_ALT, "#10B981");
        statsRow.getChildren().addAll(employees, departments, leaves, payroll);

        var skeleton = new SkeletonPane(3);
        getChildren().addAll(title, statsRow, skeleton);

        var api = ApiClient.getInstance();
        api.getRaw("/hr/dashboard").thenAccept(b -> Platform.runLater(() -> {
            try {
                var d = api.getMapper().readTree(b);
                if (d.has("total_employees")) employees.setValue(String.valueOf(d.get("total_employees").asInt()));
                if (d.has("total_departments")) departments.setValue(String.valueOf(d.get("total_departments").asInt()));
                if (d.has("pending_leaves")) leaves.setValue(String.valueOf(d.get("pending_leaves").asInt()));
                if (d.has("payroll_runs")) payroll.setValue(String.valueOf(d.get("payroll_runs").asInt()));
            } catch (Exception ignored) {}
            getChildren().remove(skeleton);
        })).exceptionally(ex -> {
            Platform.runLater(() -> getChildren().remove(skeleton));
            return null;
        });
    }
}
