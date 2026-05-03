package com.erp.hr;

import com.erp.core.component.NavItem;
import com.erp.core.view.MainShell;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.List;

public class HRMainView extends MainShell {

    public HRMainView() {
        super("HR", FontAwesomeSolid.USERS, List.of(
                new NavItem("Dashboard", FontAwesomeSolid.TACHOMETER_ALT, HRDashboard::new),
                new NavItem("Employees", FontAwesomeSolid.USER_TIE, EmployeesView::new),
                new NavItem("Departments", FontAwesomeSolid.BUILDING, DepartmentsView::new),
                new NavItem("Leave", FontAwesomeSolid.PLANE_DEPARTURE, LeaveView::new),
                new NavItem("Attendance", FontAwesomeSolid.CLOCK, AttendanceView::new),
                new NavItem("Payroll", FontAwesomeSolid.MONEY_BILL_ALT, PayrollView::new)
        ), null);
    }
}
