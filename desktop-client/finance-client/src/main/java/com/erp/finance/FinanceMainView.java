package com.erp.finance;

import com.erp.core.component.NavItem;
import com.erp.core.view.MainShell;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

import java.util.List;

public class FinanceMainView extends MainShell {

    public FinanceMainView() {
        super("Finance", BytedanceIconsRegularAL.FINANCE, List.of(
                new NavItem("Dashboard", BytedanceIconsRegularAL.DASHBOARD, FinanceDashboard::new),
                new NavItem("Accounts", BytedanceIconsRegularAL.BANK, AccountsView::new),
                new NavItem("Journals", BytedanceIconsRegularAL.BOOK_OPEN, JournalView::new),
                new NavItem("Invoices", BytedanceIconsRegularAL.BILL, InvoicesView::new),
                new NavItem("Reports", BytedanceIconsRegularAL.CHART_LINE, ReportsView::new),
                new NavItem("Budgets", BytedanceIconsRegularAL.FUNDS, BudgetsView::new),
                new NavItem("Tax", BytedanceIconsRegularAL.CALCULATOR, TaxView::new)
        ), null);
    }
}
