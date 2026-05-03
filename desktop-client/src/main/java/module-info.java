module com.erp.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.material2;
    requires org.slf4j;

    opens com.erp.desktop to javafx.fxml;
    opens com.erp.desktop.view to javafx.fxml;
    opens com.erp.desktop.view.inventory to javafx.fxml;
    opens com.erp.desktop.view.orders to javafx.fxml;
    opens com.erp.desktop.view.finance to javafx.fxml;
    opens com.erp.desktop.view.hr to javafx.fxml;
    opens com.erp.desktop.component to javafx.fxml;

    exports com.erp.desktop;
    exports com.erp.desktop.api;
    exports com.erp.desktop.auth;
    exports com.erp.desktop.config;
    exports com.erp.desktop.util;
    exports com.erp.desktop.component;
    exports com.erp.desktop.view;
    exports com.erp.desktop.view.inventory;
    exports com.erp.desktop.view.orders;
    exports com.erp.desktop.view.finance;
    exports com.erp.desktop.view.hr;
}
