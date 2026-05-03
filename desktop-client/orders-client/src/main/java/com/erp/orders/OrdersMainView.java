package com.erp.orders;

import com.erp.core.component.NavItem;
import com.erp.core.view.MainShell;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

import java.util.List;

public class OrdersMainView extends MainShell {

    public OrdersMainView() {
        super("Orders", BytedanceIconsRegularMZ.TRANSACTION_ORDER, List.of(
                new NavItem("Dashboard", BytedanceIconsRegularAL.DASHBOARD, OrdersDashboard::new),
                new NavItem("Sales Orders", BytedanceIconsRegularMZ.SHOPPING_CART, SalesOrdersView::new),
                new NavItem("Purchase Orders", BytedanceIconsRegularMZ.TRANSACTION, PurchaseOrdersView::new),
                new NavItem("Customers", BytedanceIconsRegularMZ.PEOPLES, CustomersView::new),
                new NavItem("Suppliers", BytedanceIconsRegularAL.FACTORY_BUILDING, SuppliersView::new)
        ), null);
    }
}
