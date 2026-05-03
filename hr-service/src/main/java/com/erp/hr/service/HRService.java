package com.erp.hr.service;

import com.erp.hr.entity.*;
import com.erp.hr.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HRService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRunRepository payrollRunRepository;

    // Standard working hours
    private static final double STANDARD_HOURS = 8.0;
    private static final double OVERTIME_MULTIPLIER = 1.5;

    // Tax brackets (simplified US-style progressive tax)
    private static final long[][] TAX_BRACKETS = {
            {0, 1100000, 10},        // 0 - 11,000: 10%
            {1100000, 4472500, 12},   // 11,000 - 44,725: 12%
            {4472500, 9537500, 22},   // 44,725 - 95,375: 22%
            {9537500, 19105000, 24},  // 95,375 - 191,050: 24%
            {19105000, Long.MAX_VALUE, 32}
    };

    private static final double SOCIAL_SECURITY_RATE = 0.062;
    private static final double MEDICARE_RATE = 0.0145;
    private static final double EMPLOYER_SS_RATE = 0.062;
    private static final double EMPLOYER_MEDICARE_RATE = 0.0145;

    // ============================================================
    // Department Operations
    // ============================================================

    @Transactional
    public Department createDepartment(Department dept) {
        if (dept.getCode() == null || dept.getName() == null) {
            throw new IllegalArgumentException("Department code and name required");
        }
        dept.setIsActive(true);
        return departmentRepository.save(dept);
    }

    public Department getDepartment(String id) {
        return departmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
    }

    public List<Department> listDepartments(boolean activeOnly) {
        return activeOnly ? departmentRepository.findByIsActiveTrue() : departmentRepository.findAll();
    }

    // ============================================================
    // Employee Operations
    // ============================================================

    @Transactional
    public Employee createEmployee(Employee emp) {
        if (emp.getFirstName() == null || emp.getLastName() == null || emp.getEmail() == null) {
            throw new IllegalArgumentException("First name, last name, and email are required");
        }

        // Check for duplicate email
        if (employeeRepository.findByEmail(emp.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + emp.getEmail());
        }

        // Validate department exists
        if (emp.getDepartmentId() != null) {
            departmentRepository.findById(emp.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        }

        // Generate employee number
        Integer seq = employeeRepository.getNextEmployeeSequence();
        emp.setEmployeeNumber(String.format("EMP-%05d", seq));

        if (emp.getStatus() == null) emp.setStatus(Employee.EmployeeStatus.ACTIVE);
        if (emp.getEmploymentType() == null) emp.setEmploymentType(Employee.EmploymentType.FULL_TIME);
        if (emp.getHireDate() == null) emp.setHireDate(LocalDate.now());

        Employee saved = employeeRepository.save(emp);
        log.info("Created employee {} - {} {}", saved.getEmployeeNumber(), saved.getFirstName(), saved.getLastName());
        return saved;
    }

    public Employee getEmployee(String id) {
        return employeeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    @Transactional
    public Employee updateEmployee(String id, Employee updates) {
        Employee emp = getEmployee(id);
        if (updates.getEmail() != null) emp.setEmail(updates.getEmail());
        if (updates.getPhone() != null) emp.setPhone(updates.getPhone());
        if (updates.getDepartmentId() != null) emp.setDepartmentId(updates.getDepartmentId());
        if (updates.getPositionTitle() != null) emp.setPositionTitle(updates.getPositionTitle());
        if (updates.getJobGrade() != null) emp.setJobGrade(updates.getJobGrade());
        if (updates.getManagerId() != null) emp.setManagerId(updates.getManagerId());
        if (updates.getBaseSalaryCents() != null) emp.setBaseSalaryCents(updates.getBaseSalaryCents());
        if (updates.getBankAccount() != null) emp.setBankAccount(updates.getBankAccount());
        return employeeRepository.save(emp);
    }

    @Transactional
    public Employee terminateEmployee(String id, LocalDate terminationDate, String reason) {
        Employee emp = getEmployee(id);
        if (emp.getStatus() == Employee.EmployeeStatus.TERMINATED) {
            throw new IllegalStateException("Employee is already terminated");
        }
        emp.setStatus(Employee.EmployeeStatus.TERMINATED);
        emp.setTerminationDate(terminationDate != null ? terminationDate : LocalDate.now());
        emp.setTerminationReason(reason);
        log.info("Terminated employee {} - {}", emp.getEmployeeNumber(), reason);
        return employeeRepository.save(emp);
    }

    public Page<Employee> listEmployees(String departmentId, Employee.EmployeeStatus status,
                                         String search, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("lastName", "firstName"));
        if (search != null && !search.isEmpty()) {
            return employeeRepository.searchEmployees(search, pageRequest);
        }
        if (departmentId != null) {
            return employeeRepository.findByDepartmentId(UUID.fromString(departmentId), pageRequest);
        }
        if (status != null) {
            return employeeRepository.findByStatus(status, pageRequest);
        }
        return employeeRepository.findAll(pageRequest);
    }

    // ============================================================
    // Leave Management
    // ============================================================

    @Transactional
    public LeaveRequest submitLeaveRequest(String employeeId, String leaveTypeId,
                                            LocalDate startDate, LocalDate endDate, String reason) {
        Employee emp = getEmployee(employeeId);
        if (emp.getStatus() != Employee.EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Only active employees can request leave");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        // Exclude weekends (simplified)
        int workingDays = 0;
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }

        LeaveRequest request = LeaveRequest.builder()
                .employeeId(UUID.fromString(employeeId))
                .leaveTypeId(UUID.fromString(leaveTypeId))
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(workingDays)
                .reason(reason)
                .status(LeaveRequest.LeaveStatus.PENDING)
                .build();

        log.info("Leave request submitted: {} for {} days ({}-{})",
                emp.getFullName(), workingDays, startDate, endDate);
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest approveLeave(String requestId, String approvedBy) {
        LeaveRequest request = leaveRequestRepository.findById(UUID.fromString(requestId))
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING requests");
        }

        request.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setUpdatedAt(Instant.now());

        log.info("Leave request {} approved by {}", requestId, approvedBy);
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest rejectLeave(String requestId, String rejectedBy, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(UUID.fromString(requestId))
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING requests");
        }

        request.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        request.setApprovedBy(rejectedBy);
        request.setRejectionReason(reason);
        request.setUpdatedAt(Instant.now());

        return leaveRequestRepository.save(request);
    }

    // ============================================================
    // Attendance Management
    // ============================================================

    @Transactional
    public Attendance clockIn(String employeeId, String notes) {
        Employee emp = getEmployee(employeeId);
        LocalDate today = LocalDate.now();

        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndAttendanceDate(UUID.fromString(employeeId), today);
        if (existing.isPresent() && existing.get().getClockIn() != null) {
            throw new IllegalStateException("Employee already clocked in today");
        }

        Instant now = Instant.now();
        Attendance attendance = existing.orElse(Attendance.builder()
                .employeeId(UUID.fromString(employeeId))
                .attendanceDate(today)
                .build());

        attendance.setClockIn(now);
        attendance.setNotes(notes);

        // Check if late (after 9:00 AM)
        LocalTime clockInTime = now.atZone(ZoneId.systemDefault()).toLocalTime();
        if (clockInTime.isAfter(LocalTime.of(9, 15))) {
            attendance.setStatus(Attendance.AttendanceStatus.LATE);
        } else {
            attendance.setStatus(Attendance.AttendanceStatus.PRESENT);
        }

        log.info("Clock in: {} at {}", emp.getFullName(), clockInTime);
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance clockOut(String employeeId, String notes) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(UUID.fromString(employeeId), today)
                .orElseThrow(() -> new IllegalStateException("No clock-in record found for today"));

        if (attendance.getClockOut() != null) {
            throw new IllegalStateException("Employee already clocked out today");
        }

        Instant now = Instant.now();
        attendance.setClockOut(now);

        // Calculate hours worked
        double hoursWorked = Duration.between(attendance.getClockIn(), now).toMinutes() / 60.0;
        attendance.setHoursWorked(BigDecimal.valueOf(hoursWorked).setScale(2, RoundingMode.HALF_UP));

        // Calculate overtime (anything over 8 hours)
        if (hoursWorked > STANDARD_HOURS) {
            attendance.setOvertimeHours(BigDecimal.valueOf(hoursWorked - STANDARD_HOURS).setScale(2, RoundingMode.HALF_UP));
        }

        if (notes != null) {
            attendance.setNotes(attendance.getNotes() != null ?
                    attendance.getNotes() + " | " + notes : notes);
        }

        log.info("Clock out: employee {} - {:.2f} hours ({:.2f} OT)",
                employeeId, hoursWorked, attendance.getOvertimeHours());
        return attendanceRepository.save(attendance);
    }

    // ============================================================
    // Payroll Processing
    // ============================================================

    @Transactional
    public PayrollRun createPayrollRun(String payFrequency, LocalDate periodStart,
                                        LocalDate periodEnd, LocalDate paymentDate, String createdBy) {
        Integer seq = payrollRunRepository.getNextRunSequence();
        PayrollRun run = PayrollRun.builder()
                .runNumber(String.format("PR-%06d", seq))
                .period(periodStart.getYear() + "-" + String.format("%02d", periodStart.getMonthValue()))
                .payFrequency(payFrequency)
                .status(PayrollRun.PayrollStatus.DRAFT)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .paymentDate(paymentDate)
                .createdBy(createdBy)
                .build();

        log.info("Created payroll run {} for period {}-{}", run.getRunNumber(), periodStart, periodEnd);
        return payrollRunRepository.save(run);
    }

    @Transactional
    public PayrollRun calculatePayroll(String runId) {
        PayrollRun run = payrollRunRepository.findById(UUID.fromString(runId))
                .orElseThrow(() -> new IllegalArgumentException("Payroll run not found"));

        if (run.getStatus() != PayrollRun.PayrollStatus.DRAFT) {
            throw new IllegalStateException("Can only calculate DRAFT payroll runs");
        }

        // Get all eligible employees
        List<Employee> employees = employeeRepository.findByStatusAndPayFrequency(
                Employee.EmployeeStatus.ACTIVE, run.getPayFrequency());

        long totalGross = 0;
        long totalDeductions = 0;
        long totalNet = 0;
        long totalEmployerCost = 0;

        for (Employee emp : employees) {
            PaySlipCalculation calc = calculatePaySlip(emp, run.getPeriodStart(), run.getPeriodEnd());

            totalGross += calc.grossPay;
            totalDeductions += calc.totalDeductions;
            totalNet += calc.netPay;
            totalEmployerCost += calc.employerCost;

            log.debug("PaySlip for {}: gross={}, deductions={}, net={}",
                    emp.getFullName(), calc.grossPay, calc.totalDeductions, calc.netPay);
        }

        run.setTotalGrossCents(totalGross);
        run.setTotalDeductionsCents(totalDeductions);
        run.setTotalNetCents(totalNet);
        run.setTotalEmployerCostCents(totalEmployerCost);
        run.setEmployeeCount(employees.size());
        run.setStatus(PayrollRun.PayrollStatus.CALCULATED);

        log.info("Payroll {} calculated: {} employees, gross={}, net={}",
                run.getRunNumber(), employees.size(), totalGross, totalNet);
        return payrollRunRepository.save(run);
    }

    @Transactional
    public PayrollRun approvePayroll(String runId, String approvedBy) {
        PayrollRun run = payrollRunRepository.findById(UUID.fromString(runId))
                .orElseThrow(() -> new IllegalArgumentException("Payroll run not found"));

        if (run.getStatus() != PayrollRun.PayrollStatus.CALCULATED) {
            throw new IllegalStateException("Can only approve CALCULATED payroll runs");
        }

        run.setStatus(PayrollRun.PayrollStatus.APPROVED);
        log.info("Payroll {} approved by {}", run.getRunNumber(), approvedBy);
        return payrollRunRepository.save(run);
    }

    @Transactional
    public PayrollRun processPayroll(String runId, String processedBy) {
        PayrollRun run = payrollRunRepository.findById(UUID.fromString(runId))
                .orElseThrow(() -> new IllegalArgumentException("Payroll run not found"));

        if (run.getStatus() != PayrollRun.PayrollStatus.APPROVED) {
            throw new IllegalStateException("Can only process APPROVED payroll runs");
        }

        // In production: trigger bank transfers, create journal entries via finance service gRPC
        run.setStatus(PayrollRun.PayrollStatus.COMPLETED);
        log.info("Payroll {} processed by {} - {} employees paid",
                run.getRunNumber(), processedBy, run.getEmployeeCount());
        return payrollRunRepository.save(run);
    }

    // ============================================================
    // Payroll Calculation Logic
    // ============================================================

    private PaySlipCalculation calculatePaySlip(Employee emp, LocalDate periodStart, LocalDate periodEnd) {
        long baseSalary = emp.getBaseSalaryCents();

        // Pro-rate for partial months
        long daysInPeriod = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        long monthlyPay = baseSalary; // Assuming monthly salary
        if (daysInPeriod < 28) {
            monthlyPay = baseSalary * daysInPeriod / 30; // Pro-rate
        }

        // Calculate overtime from attendance
        List<Attendance> attendance = attendanceRepository.findByEmployeeIdAndAttendanceDateBetween(
                emp.getId(), periodStart, periodEnd);
        double totalOTHours = attendance.stream()
                .map(a -> a.getOvertimeHours() != null ? a.getOvertimeHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
        long hourlyRate = baseSalary / (22 * 8); // ~22 working days * 8 hours
        long overtimePay = (long) (totalOTHours * hourlyRate * OVERTIME_MULTIPLIER);

        long grossPay = monthlyPay + overtimePay;

        // Tax calculation (progressive)
        long annualizedGross = grossPay * 12;
        long annualTax = calculateIncomeTax(annualizedGross);
        long monthlyTax = annualTax / 12;

        // Social security & medicare
        long socialSecurity = (long) (grossPay * SOCIAL_SECURITY_RATE);
        long medicare = (long) (grossPay * MEDICARE_RATE);

        long totalDeductions = monthlyTax + socialSecurity + medicare;
        long netPay = grossPay - totalDeductions;

        // Employer costs
        long employerSS = (long) (grossPay * EMPLOYER_SS_RATE);
        long employerMedicare = (long) (grossPay * EMPLOYER_MEDICARE_RATE);
        long employerCost = grossPay + employerSS + employerMedicare;

        return new PaySlipCalculation(grossPay, monthlyTax, socialSecurity, medicare,
                totalDeductions, netPay, employerCost, overtimePay, totalOTHours);
    }

    private long calculateIncomeTax(long annualIncome) {
        long tax = 0;
        long remaining = annualIncome;

        for (long[] bracket : TAX_BRACKETS) {
            long bracketSize = bracket[1] - bracket[0];
            long taxableInBracket = Math.min(remaining, bracketSize);
            if (taxableInBracket <= 0) break;
            tax += taxableInBracket * bracket[2] / 100;
            remaining -= taxableInBracket;
        }
        return tax;
    }

    // ============================================================
    // HR Dashboard / Analytics
    // ============================================================

    public HRDashboard getDashboard(String departmentId) {
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE);
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.PENDING);
        int onLeaveToday = leaveRequestRepository.findApprovedForDate(LocalDate.now()).size();

        List<Object[]> deptCounts = employeeRepository.countByDepartment();
        List<DepartmentSummary> deptSummaries = new ArrayList<>();
        for (Object[] row : deptCounts) {
            UUID deptId = (UUID) row[0];
            long count = (Long) row[1];
            if (deptId != null) {
                Department dept = departmentRepository.findById(deptId).orElse(null);
                deptSummaries.add(new DepartmentSummary(
                        deptId.toString(), dept != null ? dept.getName() : "Unknown", (int) count));
            }
        }

        return new HRDashboard(
                (int) totalEmployees, (int) activeEmployees,
                0, 0, // new hires / terminations - would need date range queries
                0.0, 0.0, // turnover / avg tenure
                (int) pendingLeaves, onLeaveToday, deptSummaries);
    }

    // ============================================================
    // Internal Records
    // ============================================================

    private record PaySlipCalculation(long grossPay, long incomeTax, long socialSecurity,
                                       long medicare, long totalDeductions, long netPay,
                                       long employerCost, long overtimePay, double overtimeHours) {}

    public record HRDashboard(int totalEmployees, int activeEmployees, int newHires, int terminations,
                               double turnoverRate, double avgTenure, int pendingLeaves,
                               int onLeaveToday, List<DepartmentSummary> departments) {}

    public record DepartmentSummary(String departmentId, String departmentName, int employeeCount) {}
}
