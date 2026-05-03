package com.erp.orders;

import com.erp.core.component.NavItem;
import com.erp.core.view.MainShell;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.List;

public class OrdersMainView extends MainShell {

    public OrdersMainView() {
        super("Orders", FontAwesomeSolid.SHOPPING_CART, List.of(
                new NavItem("Dashboard", FontAwesomeSolid.TACHOMETER_ALT, OrdersDashboard::new),
                new NavItem("Sales Orders", FontAwesomeSolid.FILE_INVOICE_DOLLAR, SalesOrdersView::new),
                new NavItem("Purchase Orders", FontAwesomeSolid.TRUCK, PurchaseOrdersView::new),
                new NavItem("Customers", FontAwesomeSolid.USERS, CustomersView::new),
                new NavItem("Suppliers", FontAwesomeSolid.INDUSTRY, SuppliersView::new)
        ), null);
    }
}
