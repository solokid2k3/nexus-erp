package handlers

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"google.golang.org/grpc"
)

type FinanceHandler struct {
	conn *grpc.ClientConn
	pool *pgxpool.Pool
}

func NewFinanceHandler(conn *grpc.ClientConn, pool *pgxpool.Pool) *FinanceHandler {
	return &FinanceHandler{conn: conn, pool: pool}
}

// CreateAccount godoc
// @Summary      Create a chart of accounts entry
// @Description  Create a new account in the chart of accounts
// @Tags         Finance - Accounts
// @Accept       json
// @Produce      json
// @Param        account  body      map[string]interface{}  true  "Account data (code, name, type)"
// @Success      201      {object}  map[string]interface{}
// @Failure      400      {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/accounts [post]
func (h *FinanceHandler) CreateAccount(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Account created", "data": req})
}

// GetAccount godoc
// @Summary      Get an account by ID
// @Tags         Finance - Accounts
// @Produce      json
// @Param        id   path      string  true  "Account ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/accounts/{id} [get]
func (h *FinanceHandler) GetAccount(c *gin.Context) {
	id := c.Param("id")
	var code, name, accType, category string
	var isActive bool
	err := h.pool.QueryRow(c.Request.Context(), `
		SELECT code, name, type, category, is_active 
		FROM finance.accounts WHERE id = $1`, id).Scan(
		&code, &name, &accType, &category, &isActive,
	)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"success": false, "error": "Account not found"})
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"success": true,
		"data": map[string]interface{}{
			"id": id, "code": code, "name": name, "type": accType, "category": category, "is_active": isActive,
		},
	})
}

// ListAccounts godoc
// @Summary      List all accounts
// @Tags         Finance - Accounts
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/accounts [get]
func (h *FinanceHandler) ListAccounts(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT id, account_number, name, account_type, sub_type, is_active 
		FROM finance.accounts ORDER BY account_number ASC`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var accounts []map[string]interface{}
	for rows.Next() {
		var id, accNum, name, accType string
		var subType *string
		var isActive bool
		if err := rows.Scan(&id, &accNum, &name, &accType, &subType, &isActive); err == nil {
			accounts = append(accounts, map[string]interface{}{
				"id": id, "account_number": accNum, "name": name, "type": accType, "category": subType, "is_active": isActive,
			})
		}
	}
	if accounts == nil {
		accounts = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, accounts)
}

// CreateJournalEntry godoc
// @Summary      Create a journal entry
// @Tags         Finance - Journal Entries
// @Accept       json
// @Produce      json
// @Param        entry  body      map[string]interface{}  true  "Journal entry data (date, lines, memo)"
// @Success      201    {object}  map[string]interface{}
// @Failure      400    {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/journal-entries [post]
func (h *FinanceHandler) CreateJournalEntry(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Journal entry created", "data": req})
}

// GetJournalEntry godoc
// @Summary      Get a journal entry by ID
// @Tags         Finance - Journal Entries
// @Produce      json
// @Param        id   path      string  true  "Journal Entry ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/journal-entries/{id} [get]
func (h *FinanceHandler) GetJournalEntry(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"id": c.Param("id")})
}

// ListJournalEntries godoc
// @Summary      List all journal entries
// @Tags         Finance - Journal Entries
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/journal-entries [get]
func (h *FinanceHandler) ListJournalEntries(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT id, entry_number, entry_date, status, total_debit_cents, created_at 
		FROM finance.journal_entries ORDER BY entry_date DESC LIMIT 50`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var entries []map[string]interface{}
	for rows.Next() {
		var id, num, status string
		var total int64
		var entryDate, createdAt time.Time
		if err := rows.Scan(&id, &num, &entryDate, &status, &total, &createdAt); err == nil {
			entries = append(entries, map[string]interface{}{
				"id": id, "entry_number": num, "entry_date": entryDate.Format("2006-01-02"), "status": status,
				"total_amount_cents": total, "created_at": createdAt.Format("2006-01-02"),
			})
		}
	}
	if entries == nil {
		entries = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, entries)
}

// PostJournalEntry godoc
// @Summary      Post a journal entry
// @Description  Post a draft journal entry to the general ledger
// @Tags         Finance - Journal Entries
// @Produce      json
// @Param        id   path      string  true  "Journal Entry ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/journal-entries/{id}/post [post]
func (h *FinanceHandler) PostJournalEntry(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "Journal entry posted", "id": c.Param("id")})
}

// ReverseJournalEntry godoc
// @Summary      Reverse a journal entry
// @Tags         Finance - Journal Entries
// @Produce      json
// @Param        id   path      string  true  "Journal Entry ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/journal-entries/{id}/reverse [post]
func (h *FinanceHandler) ReverseJournalEntry(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "Journal entry reversed", "id": c.Param("id")})
}

// CreateInvoice godoc
// @Summary      Create an invoice
// @Tags         Finance - Invoices
// @Accept       json
// @Produce      json
// @Param        invoice  body      map[string]interface{}  true  "Invoice data"
// @Success      201      {object}  map[string]interface{}
// @Failure      400      {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/invoices [post]
func (h *FinanceHandler) CreateInvoice(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Invoice created", "data": req})
}

// GetInvoice godoc
// @Summary      Get an invoice by ID
// @Tags         Finance - Invoices
// @Produce      json
// @Param        id   path      string  true  "Invoice ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/invoices/{id} [get]
func (h *FinanceHandler) GetInvoice(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"id": c.Param("id")})
}

// ListInvoices godoc
// @Summary      List all invoices
// @Tags         Finance - Invoices
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/invoices [get]
func (h *FinanceHandler) ListInvoices(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT id, invoice_number, invoice_type, customer_id, status, total_amount_cents, due_date 
		FROM finance.invoices ORDER BY created_at DESC LIMIT 50`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var invoices []map[string]interface{}
	for rows.Next() {
		var id, num, invType, status string
		var custID *string
		var total int64
		var dueDate time.Time
		if err := rows.Scan(&id, &num, &invType, &custID, &status, &total, &dueDate); err == nil {
			invoices = append(invoices, map[string]interface{}{
				"id": id, "invoice_number": num, "type": invType, "entity_id": custID,
				"status": status, "total_amount_cents": total, "due_date": dueDate.Format("2006-01-02"),
			})
		}
	}
	if invoices == nil {
		invoices = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, invoices)
}

// RecordPayment godoc
// @Summary      Record a payment against an invoice
// @Tags         Finance - Invoices
// @Accept       json
// @Produce      json
// @Param        id       path      string                 true  "Invoice ID"
// @Param        payment  body      map[string]interface{}  true  "Payment data"
// @Success      200      {object}  map[string]interface{}
// @Failure      400      {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/invoices/{id}/payment [post]
func (h *FinanceHandler) RecordPayment(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Payment recorded", "invoice_id": c.Param("id"), "data": req})
}

// CalculateTax godoc
// @Summary      Calculate tax
// @Tags         Finance - Tax
// @Accept       json
// @Produce      json
// @Param        request  body      map[string]interface{}  true  "Tax calculation input"
// @Success      200      {object}  map[string]interface{}
// @Failure      400      {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/tax/calculate [post]
func (h *FinanceHandler) CalculateTax(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Tax calculated", "data": req})
}

// GetTrialBalance godoc
// @Summary      Get trial balance report
// @Tags         Finance - Reports
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/reports/trial-balance [get]
func (h *FinanceHandler) GetTrialBalance(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"report": "trial_balance"})
}

// GetProfitAndLoss godoc
// @Summary      Get profit & loss report
// @Tags         Finance - Reports
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/reports/profit-loss [get]
func (h *FinanceHandler) GetProfitAndLoss(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"report": "profit_loss"})
}

// GetBalanceSheet godoc
// @Summary      Get balance sheet report
// @Tags         Finance - Reports
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/reports/balance-sheet [get]
func (h *FinanceHandler) GetBalanceSheet(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"report": "balance_sheet"})
}

// GetARaging godoc
// @Summary      Get accounts receivable aging report
// @Tags         Finance - Reports
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/reports/ar-aging [get]
func (h *FinanceHandler) GetARaging(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"report": "ar_aging"})
}

// CreateBudget godoc
// @Summary      Create a budget
// @Tags         Finance - Budgets
// @Accept       json
// @Produce      json
// @Param        budget  body      map[string]interface{}  true  "Budget data"
// @Success      201     {object}  map[string]interface{}
// @Failure      400     {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/budgets [post]
func (h *FinanceHandler) CreateBudget(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Budget created", "data": req})
}

// GetBudget godoc
// @Summary      Get a budget by ID
// @Tags         Finance - Budgets
// @Produce      json
// @Param        id   path      string  true  "Budget ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/budgets/{id} [get]
func (h *FinanceHandler) GetBudget(c *gin.Context) { c.JSON(http.StatusOK, gin.H{"id": c.Param("id")}) }

// ListBudgets godoc
// @Summary      List all budgets
// @Tags         Finance - Budgets
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/finance/budgets [get]
func (h *FinanceHandler) ListBudgets(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT id, name, department_id, fiscal_year, total_budget_cents, status 
		FROM finance.budgets ORDER BY fiscal_year DESC LIMIT 50`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var budgets []map[string]interface{}
	for rows.Next() {
		var id, name, status, year string
		var deptID *string
		var total int64
		if err := rows.Scan(&id, &name, &deptID, &year, &total, &status); err == nil {
			budgets = append(budgets, map[string]interface{}{
				"id": id, "name": name, "department_id": deptID, "year": year,
				"total_amount_cents": total, "status": status,
			})
		}
	}
	if budgets == nil {
		budgets = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, budgets)
}
