package com.erp.hr;

import com.erp.core.component.NavItem;
import com.erp.core.view.MainShell;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

import java.util.List;

public class HRMainView extends MainShell {

    public HRMainView() {
        super("HR", BytedanceIconsRegularMZ.PEOPLES, List.of(
                new NavItem("Dashboard", BytedanceIconsRegularAL.DASHBOARD, HRDashboard::new),
                new NavItem("Employees", BytedanceIconsRegularAL.EVERY_USER, EmployeesView::new),
                new NavItem("Departments", BytedanceIconsRegularAL.BUILDING_ONE, DepartmentsView::new),
                new NavItem("Leave", BytedanceIconsRegularMZ.VACATION, LeaveView::new),
                new NavItem("Attendance", BytedanceIconsRegularMZ.TIME, AttendanceView::new),
                new NavItem("Payroll", BytedanceIconsRegularMZ.WALLET, PayrollView::new)
        ), null);
    }
}
