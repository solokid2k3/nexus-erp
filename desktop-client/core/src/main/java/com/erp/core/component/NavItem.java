package com.erp.core.component;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Supplier;

public class NavItem {

    private final String label;
    private final Ikon icon;
    private final Supplier<javafx.scene.layout.Region> viewFactory;
    private Button button;

    public NavItem(String label, Ikon icon, Supplier<javafx.scene.layout.Region> viewFactory) {
        this.label = label;
        this.icon = icon;
        this.viewFactory = viewFactory;
    }

    public String getLabel() { return label; }
    public Ikon getIcon() { return icon; }
    public Supplier<javafx.scene.layout.Region> getViewFactory() { return viewFactory; }

    public Button createButton() {
        var fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);

        button = new Button(label);
        button.setGraphic(fontIcon);
        button.getStyleClass().add("nav-item");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setTooltip(new Tooltip(label));
        return button;
    }

    public void setActive(boolean active) {
        if (button == null) return;
        if (active) {
            if (!button.getStyleClass().contains("nav-item-active"))
                button.getStyleClass().add("nav-item-active");
        } else {
            button.getStyleClass().remove("nav-item-active");
        }
    }
}
