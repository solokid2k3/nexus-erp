package handlers

import (
	"fmt"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"google.golang.org/grpc"
)

type HRHandler struct {
	conn *grpc.ClientConn
	pool *pgxpool.Pool
}

func NewHRHandler(conn *grpc.ClientConn, pool *pgxpool.Pool) *HRHandler {
	return &HRHandler{conn: conn, pool: pool}
}

// CreateDepartment godoc
// @Summary      Create a new department
// @Tags         HR - Departments
// @Accept       json
// @Produce      json
// @Param        dept  body      map[string]interface{}  true  "Department data"
// @Success      201   {object}  map[string]interface{}
// @Failure      400   {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/departments [post]
func (h *HRHandler) CreateDepartment(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Department created", "data": req})
}

// GetDepartment godoc
// @Summary      Get a department by ID
// @Tags         HR - Departments
// @Produce      json
// @Param        id   path      string  true  "Department ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/departments/{id} [get]
func (h *HRHandler) GetDepartment(c *gin.Context) {
	id := c.Param("id")
	var code, name string
	var managerID *string
	err := h.pool.QueryRow(c.Request.Context(), `
		SELECT code, name, manager_id 
		FROM hr.departments WHERE id = $1`, id).Scan(&code, &name, &managerID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"success": false, "error": "Department not found"})
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"success": true,
		"data": map[string]interface{}{
			"id": id, "code": code, "name": name, "manager_id": managerID,
		},
	})
}

// ListDepartments godoc
// @Summary      List all departments
// @Tags         HR - Departments
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/departments [get]
func (h *HRHandler) ListDepartments(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `SELECT id, code, name, manager_id FROM hr.departments ORDER BY name`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var depts []map[string]interface{}
	for rows.Next() {
		var id, code, name string
		var mID *string
		if err := rows.Scan(&id, &code, &name, &mID); err == nil {
			depts = append(depts, map[string]interface{}{
				"id": id, "code": code, "name": name, "manager_id": mID,
			})
		}
	}
	if depts == nil {
		depts = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, depts)
}

// CreateEmployee godoc
// @Summary      Create a new employee
// @Tags         HR - Employees
// @Accept       json
// @Produce      json
// @Param        employee  body      map[string]interface{}  true  "Employee data"
// @Success      201       {object}  map[string]interface{}
// @Failure      400       {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/employees [post]
func (h *HRHandler) CreateEmployee(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Employee created", "data": req})
}

// GetEmployee godoc
// @Summary      Get an employee by ID
// @Tags         HR - Employees
// @Produce      json
// @Param        id   path      string  true  "Employee ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/employees/{id} [get]
func (h *HRHandler) GetEmployee(c *gin.Context) { c.JSON(http.StatusOK, gin.H{"id": c.Param("id")}) }

// ListEmployees godoc
// @Summary      List all employees
// @Tags         HR - Employees
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/employees [get]
func (h *HRHandler) ListEmployees(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT e.id, e.employee_number, e.first_name, e.last_name, e.email, e.department_id, 
		       COALESCE(d.name, '') as department_name, e.position_title, e.status, e.hire_date 
		FROM hr.employees e LEFT JOIN hr.departments d ON e.department_id = d.id
		ORDER BY e.last_name, e.first_name LIMIT 50`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var emps []map[string]interface{}
	for rows.Next() {
		var id, num, fName, lName, email, deptName, title, status string
		var dID *string
		var hireDate time.Time
		if err := rows.Scan(&id, &num, &fName, &lName, &email, &dID, &deptName, &title, &status, &hireDate); err == nil {
			emps = append(emps, map[string]interface{}{
				"id": id, "employee_number": num, "first_name": fName, "last_name": lName,
				"email": email, "department_id": dID, "department_name": deptName,
				"position": title, "status": status, "hire_date": hireDate.Format("2006-01-02"),
			})
		} else {
			fmt.Printf("Scan error: %v\n", err)
		}
	}
	if emps == nil {
		emps = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, emps)
}

// UpdateEmployee godoc
// @Summary      Update an employee
// @Tags         HR - Employees
// @Accept       json
// @Produce      json
// @Param        id        path      string                 true  "Employee ID"
// @Param        employee  body      map[string]interface{}  true  "Updated employee data"
// @Success      200       {object}  map[string]interface{}
// @Failure      400       {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/employees/{id} [put]
func (h *HRHandler) UpdateEmployee(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Employee updated", "id": c.Param("id"), "data": req})
}

// TerminateEmployee godoc
// @Summary      Terminate an employee
// @Tags         HR - Employees
// @Accept       json
// @Produce      json
// @Param        id   path      string                 true  "Employee ID"
// @Param        data body      map[string]interface{}  true  "Termination data"
// @Success      200  {object}  map[string]interface{}
// @Failure      400  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/employees/{id}/terminate [post]
func (h *HRHandler) TerminateEmployee(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Employee terminated", "id": c.Param("id")})
}

// SubmitLeaveRequest godoc
// @Summary      Submit a leave request
// @Tags         HR - Leave Management
// @Accept       json
// @Produce      json
// @Param        leave  body      map[string]interface{}  true  "Leave request data"
// @Success      201    {object}  map[string]interface{}
// @Failure      400    {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/leave/request [post]
func (h *HRHandler) SubmitLeaveRequest(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Leave request submitted", "data": req})
}

// ApproveLeave godoc
// @Summary      Approve a leave request
// @Tags         HR - Leave Management
// @Produce      json
// @Param        id   path      string  true  "Leave Request ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/leave/{id}/approve [post]
func (h *HRHandler) ApproveLeave(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "Leave approved", "id": c.Param("id")})
}

// RejectLeave godoc
// @Summary      Reject a leave request
// @Tags         HR - Leave Management
// @Produce      json
// @Param        id   path      string  true  "Leave Request ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/leave/{id}/reject [post]
func (h *HRHandler) RejectLeave(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "Leave rejected", "id": c.Param("id")})
}

// ListLeaveRequests godoc
// @Summary      List all leave requests
// @Tags         HR - Leave Management
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/leave/requests [get]
func (h *HRHandler) ListLeaveRequests(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT lr.id, lr.employee_id, lt.name as leave_type, lr.start_date, lr.end_date, lr.status 
		FROM hr.leave_requests lr JOIN hr.leave_types lt ON lr.leave_type_id = lt.id
		ORDER BY lr.created_at DESC LIMIT 50`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var leaves []map[string]interface{}
	for rows.Next() {
		var id, eID, lType, status string
		var sDate, eDate time.Time
		if err := rows.Scan(&id, &eID, &lType, &sDate, &eDate, &status); err == nil {
			leaves = append(leaves, map[string]interface{}{
				"id": id, "employee_id": eID, "leave_type": lType,
				"start_date": sDate.Format("2006-01-02"), "end_date": eDate.Format("2006-01-02"), "status": status,
			})
		}
	}
	if leaves == nil {
		leaves = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, leaves)
}

// GetLeaveBalance godoc
// @Summary      Get leave balance for an employee
// @Tags         HR - Leave Management
// @Produce      json
// @Param        employeeId  path      string  true  "Employee ID"
// @Success      200         {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/leave/balance/{employeeId} [get]
func (h *HRHandler) GetLeaveBalance(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"employee_id": c.Param("employeeId")})
}

// ClockIn godoc
// @Summary      Clock in attendance
// @Tags         HR - Attendance
// @Accept       json
// @Produce      json
// @Param        data  body      map[string]interface{}  true  "Clock-in data"
// @Success      200   {object}  map[string]interface{}
// @Failure      400   {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/attendance/clock-in [post]
func (h *HRHandler) ClockIn(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Clocked in", "data": req})
}

// ClockOut godoc
// @Summary      Clock out attendance
// @Tags         HR - Attendance
// @Accept       json
// @Produce      json
// @Param        data  body      map[string]interface{}  true  "Clock-out data"
// @Success      200   {object}  map[string]interface{}
// @Failure      400   {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/attendance/clock-out [post]
func (h *HRHandler) ClockOut(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Clocked out", "data": req})
}

// ListAttendance godoc
// @Summary      List attendance records
// @Tags         HR - Attendance
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/attendance [get]
func (h *HRHandler) ListAttendance(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT id, employee_id, attendance_date, clock_in, clock_out, status 
		FROM hr.attendance ORDER BY attendance_date DESC, clock_in DESC LIMIT 50`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var attendance []map[string]interface{}
	for rows.Next() {
		var id, eID, status string
		var aDate time.Time
		var cIn, cOut *time.Time
		if err := rows.Scan(&id, &eID, &aDate, &cIn, &cOut, &status); err == nil {
			var clockIn, clockOut *string
			if cIn != nil { s := cIn.Format("15:04"); clockIn = &s }
			if cOut != nil { s := cOut.Format("15:04"); clockOut = &s }
			attendance = append(attendance, map[string]interface{}{
				"id": id, "employee_id": eID, "date": aDate.Format("2006-01-02"),
				"clock_in": clockIn, "clock_out": clockOut, "status": status,
			})
		}
	}
	if attendance == nil {
		attendance = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, attendance)
}

// CreatePayrollRun godoc
// @Summary      Create a payroll run
// @Tags         HR - Payroll
// @Accept       json
// @Produce      json
// @Param        payroll  body      map[string]interface{}  true  "Payroll run data"
// @Success      201      {object}  map[string]interface{}
// @Failure      400      {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/payroll [post]
func (h *HRHandler) CreatePayrollRun(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Payroll run created", "data": req})
}

// CalculatePayroll godoc
// @Summary      Calculate payroll for a run
// @Tags         HR - Payroll
// @Produce      json
// @Param        runId  path      string  true  "Payroll Run ID"
// @Success      200    {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/payroll/{runId}/calculate [post]
func (h *HRHandler) CalculatePayroll(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "Payroll calculated", "id": c.Param("runId")})
}

// ApprovePayroll godoc
// @Summary      Approve a payroll run
// @Tags         HR - Payroll
// @Produce      json
// @Param        runId  path      string  true  "Payroll Run ID"
// @Success      200    {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/payroll/{runId}/approve [post]
func (h *HRHandler) ApprovePayroll(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "Payroll approved", "id": c.Param("runId")})
}

// ProcessPayroll godoc
// @Summary      Process a payroll run
// @Tags         HR - Payroll
// @Produce      json
// @Param        runId  path      string  true  "Payroll Run ID"
// @Success      200    {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/payroll/{runId}/process [post]
func (h *HRHandler) ProcessPayroll(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "Payroll processed", "id": c.Param("runId")})
}

// ListPayrollRuns godoc
// @Summary      List all payroll runs
// @Tags         HR - Payroll
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/payroll [get]
func (h *HRHandler) ListPayrollRuns(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT id, run_number, period_start, period_end, status, total_gross_cents 
		FROM hr.payroll_runs ORDER BY period_start DESC LIMIT 50`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var runs []map[string]interface{}
	for rows.Next() {
		var id, runNumber, status string
		var pStart, pEnd time.Time
		var total int64
		if err := rows.Scan(&id, &runNumber, &pStart, &pEnd, &status, &total); err == nil {
			runs = append(runs, map[string]interface{}{
				"id": id, "run_number": runNumber,
				"period_start": pStart.Format("2006-01-02"), "period_end": pEnd.Format("2006-01-02"),
				"status": status, "total_amount_cents": total,
			})
		}
	}
	if runs == nil {
		runs = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, runs)
}

// GetPayrollRun godoc
// @Summary      Get a payroll run by ID
// @Tags         HR - Payroll
// @Produce      json
// @Param        runId  path      string  true  "Payroll Run ID"
// @Success      200    {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/payroll/{runId} [get]
func (h *HRHandler) GetPayrollRun(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"id": c.Param("runId")})
}

// GetPaySlip godoc
// @Summary      Get a pay slip
// @Tags         HR - Payroll
// @Produce      json
// @Param        runId       path      string  true  "Payroll Run ID"
// @Param        employeeId  path      string  true  "Employee ID"
// @Success      200         {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/payroll/{runId}/payslip/{employeeId} [get]
func (h *HRHandler) GetPaySlip(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"run_id": c.Param("runId"), "employee_id": c.Param("employeeId")})
}

// GetDashboard godoc
// @Summary      Get HR dashboard
// @Tags         HR - Dashboard
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/hr/dashboard [get]
func (h *HRHandler) GetDashboard(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "HR dashboard"})
}
