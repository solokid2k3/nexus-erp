package com.erp.finance;

import com.erp.core.component.NavItem;
import com.erp.core.view.MainShell;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.List;

public class FinanceMainView extends MainShell {

    public FinanceMainView() {
        super("Finance", FontAwesomeSolid.COINS, List.of(
                new NavItem("Dashboard", FontAwesomeSolid.TACHOMETER_ALT, FinanceDashboard::new),
                new NavItem("Accounts", FontAwesomeSolid.UNIVERSITY, AccountsView::new),
                new NavItem("Journals", FontAwesomeSolid.BOOK, JournalView::new),
                new NavItem("Invoices", FontAwesomeSolid.FILE_INVOICE_DOLLAR, InvoicesView::new),
                new NavItem("Reports", FontAwesomeSolid.CHART_LINE, ReportsView::new),
                new NavItem("Budgets", FontAwesomeSolid.MONEY_CHECK_ALT, BudgetsView::new),
                new NavItem("Tax", FontAwesomeSolid.CALCULATOR, TaxView::new)
        ), null);
    }
}
