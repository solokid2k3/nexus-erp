package com.erp.hr;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

public class HRDashboard extends VBox {
    public HRDashboard() {
        setSpacing(24);
        var welcomeBanner = new HBox(24);
        welcomeBanner.setStyle("-fx-background-color: linear-gradient(to right, #4F46E5, #3B82F6); -fx-background-radius: 12; -fx-padding: 32;");
        welcomeBanner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        var welcomeText = new VBox(8);
        var welcomeTitle = new Label("Welcome to HR Analytics");
        welcomeTitle.setStyle("-fx-font-family: 'Inter', sans-serif; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        var welcomeSub = new Label("Here's what's happening in your organization today.");
        welcomeSub.setStyle("-fx-font-family: 'Inter', sans-serif; -fx-font-size: 14px; -fx-text-fill: #E0E7FF;");
        welcomeText.getChildren().addAll(welcomeTitle, welcomeSub);
        
        javafx.scene.layout.HBox.setHgrow(welcomeText, javafx.scene.layout.Priority.ALWAYS);
        
        try {
            var imageStream = getClass().getResourceAsStream("/images/sticker_dashboard.png");
            if (imageStream != null) {
                var image = new javafx.scene.image.Image(imageStream);
                var imageView = new javafx.scene.image.ImageView(image);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(true);
                welcomeBanner.getChildren().addAll(welcomeText, imageView);
            } else {
                welcomeBanner.getChildren().add(welcomeText);
            }
        } catch (Exception e) {
            welcomeBanner.getChildren().add(welcomeText);
        }

        var statsRow = new HBox(16);
        var employees = new StatCard("Employees", "—", BytedanceIconsRegularAL.EVERY_USER, "#6366F1");
        var departments = new StatCard("Departments", "—", BytedanceIconsRegularAL.BUILDING_ONE, "#3B82F6");
        var leaves = new StatCard("Pending Leaves", "—", BytedanceIconsRegularMZ.VACATION, "#F59E0B");
        var payroll = new StatCard("Payroll Runs", "—", BytedanceIconsRegularMZ.WALLET, "#10B981");
        statsRow.getChildren().addAll(employees, departments, leaves, payroll);

        var skeleton = new SkeletonPane(3);
        getChildren().addAll(welcomeBanner, statsRow, skeleton);

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
