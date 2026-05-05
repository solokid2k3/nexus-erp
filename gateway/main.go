package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"erp-system/gateway/config"
	_ "erp-system/gateway/docs"
	"erp-system/gateway/handlers"
	"erp-system/gateway/middleware"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

// @title           ERP System API
// @version         1.0
// @description     Enterprise Resource Planning System API Gateway. Provides unified REST API access to Inventory, Order, Finance, and HR microservices.
// @termsOfService  http://swagger.io/terms/

// @contact.name   ERP System Support
// @contact.email  support@erp-system.com

// @license.name  Apache 2.0
// @license.url   http://www.apache.org/licenses/LICENSE-2.0.html

// @host      localhost:8080
// @BasePath  /

// @securityDefinitions.apikey  BearerAuth
// @in                          header
// @name                        Authorization
// @description                 Enter your JWT token with the Bearer prefix, e.g. "Bearer eyJhbGciOiJI..."

// @tag.name         Authentication
// @tag.description  User authentication and token management

// @tag.name         Inventory - Products
// @tag.description  Product catalog management

// @tag.name         Inventory - Categories
// @tag.description  Product category management

// @tag.name         Inventory - Warehouses
// @tag.description  Warehouse management

// @tag.name         Inventory - Stock
// @tag.description  Stock level management, adjustments, transfers, and alerts

// @tag.name         Orders - Customers
// @tag.description  Customer record management

// @tag.name         Orders - Sales Orders
// @tag.description  Sales order lifecycle management

// @tag.name         Orders - Purchase Orders
// @tag.description  Purchase order management

// @tag.name         Orders - Suppliers
// @tag.description  Supplier record management

// @tag.name         Finance - Accounts
// @tag.description  Chart of accounts management

// @tag.name         Finance - Journal Entries
// @tag.description  General ledger journal entry management

// @tag.name         Finance - Invoices
// @tag.description  Invoice and payment management

// @tag.name         Finance - Tax
// @tag.description  Tax calculation services

// @tag.name         Finance - Reports
// @tag.description  Financial reporting (Trial Balance, P&L, Balance Sheet, AR Aging)

// @tag.name         Finance - Budgets
// @tag.description  Budget management

// @tag.name         HR - Departments
// @tag.description  Department management

// @tag.name         HR - Employees
// @tag.description  Employee lifecycle management

// @tag.name         HR - Leave Management
// @tag.description  Leave requests and approvals

// @tag.name         HR - Attendance
// @tag.description  Attendance tracking (clock-in/out)

// @tag.name         HR - Payroll
// @tag.description  Payroll run lifecycle management

// @tag.name         HR - Dashboard
// @tag.description  HR dashboard and analytics

func main() {
	cfg := config.Load()

	// Redis client
	rdb := redis.NewClient(&redis.Options{
		Addr:     cfg.RedisAddr,
		DB:       0,
		PoolSize: 20,
	})
	defer rdb.Close()

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := rdb.Ping(ctx).Err(); err != nil {
		log.Fatalf("Failed to connect to Redis: %v", err)
	}
	log.Println("Connected to Redis")

	// Database connection pool
	poolCfg, err := pgxpool.ParseConfig(cfg.DatabaseURL)
	if err != nil {
		log.Fatalf("Failed to parse DB config: %v", err)
	}
	poolCfg.MaxConns = 20
	pool, err := pgxpool.NewWithConfig(context.Background(), poolCfg)
	if err != nil {
		log.Fatalf("Failed to connect to DB: %v", err)
	}
	defer pool.Close()
	log.Println("Connected to PostgreSQL")

	// gRPC connections to downstream services
	inventoryConn := dialGRPC(cfg.InventoryServiceAddr)
	defer inventoryConn.Close()

	orderConn := dialGRPC(cfg.OrderServiceAddr)
	defer orderConn.Close()

	financeConn := dialGRPC(cfg.FinanceServiceAddr)
	defer financeConn.Close()

	hrConn := dialGRPC(cfg.HRServiceAddr)
	defer hrConn.Close()

	// Initialize handlers
	invHandler := handlers.NewInventoryHandler(inventoryConn, pool)
	orderHandler := handlers.NewOrderHandler(orderConn, pool)
	financeHandler := handlers.NewFinanceHandler(financeConn, pool)
	hrHandler := handlers.NewHRHandler(hrConn, pool)

	// Setup Gin router
	gin.SetMode(gin.ReleaseMode)
	router := gin.New()
	router.Use(gin.Logger(), gin.Recovery())
	router.Use(middleware.CORS())
	router.Use(middleware.RequestID())

	// Health check
	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "healthy", "service": "erp-gateway"})
	})

	// Swagger documentation endpoint
	router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	// Public routes
	auth := router.Group("/api/v1/auth")
	{
		auth.POST("/login", handlers.Login(cfg.JWTSecret, rdb))
		auth.POST("/refresh", handlers.RefreshToken(cfg.JWTSecret, rdb))
	}

	// Protected routes
	api := router.Group("/api/v1")
	api.Use(middleware.JWTAuth(cfg.JWTSecret, rdb))
	{
		// Inventory
		inv := api.Group("/inventory")
		{
			inv.POST("/products", invHandler.CreateProduct)
			inv.GET("/products", invHandler.ListProducts)
			inv.GET("/products/:id", invHandler.GetProduct)
			inv.PUT("/products/:id", invHandler.UpdateProduct)

			inv.POST("/categories", invHandler.CreateCategory)
			inv.GET("/categories", invHandler.ListCategories)

			inv.POST("/warehouses", invHandler.CreateWarehouse)
			inv.GET("/warehouses", invHandler.ListWarehouses)
			inv.GET("/warehouses/:id", invHandler.GetWarehouse)

			inv.GET("/stock", invHandler.ListStock)
			inv.GET("/stock/:productId", invHandler.GetStockLevels)
			inv.POST("/stock/adjust", invHandler.AdjustStock)
			inv.POST("/stock/transfer", invHandler.TransferStock)
			inv.POST("/stock/reserve", invHandler.ReserveStock)

			inv.GET("/stock/movements", invHandler.ListStockMovements)
			inv.GET("/stock/alerts", invHandler.GetLowStockAlerts)
		}

		// Orders
		ord := api.Group("/orders")
		{
			ord.POST("/customers", orderHandler.CreateCustomer)
			ord.GET("/customers", orderHandler.ListCustomers)
			ord.GET("/customers/:id", orderHandler.GetCustomer)

			ord.POST("/sales", orderHandler.CreateOrder)
			ord.GET("/sales", orderHandler.ListOrders)
			ord.GET("/sales/:id", orderHandler.GetOrder)
			ord.PUT("/sales/:id/status", orderHandler.UpdateOrderStatus)
			ord.POST("/sales/:id/approve", orderHandler.ApproveOrder)

			ord.POST("/purchase", orderHandler.CreatePurchaseOrder)
			ord.GET("/purchase", orderHandler.ListPurchaseOrders)
			ord.GET("/purchase/:id", orderHandler.GetPurchaseOrder)
			ord.POST("/purchase/:id/receive", orderHandler.ReceivePurchaseOrder)

			ord.POST("/suppliers", orderHandler.CreateSupplier)
			ord.GET("/suppliers", orderHandler.ListSuppliers)
			ord.GET("/suppliers/:id", orderHandler.GetSupplier)

			ord.GET("/summary", orderHandler.GetOrderSummary)

			// Alias for desktop client compatibility
			ord.GET("/purchases", orderHandler.ListPurchaseOrders)
		}

		// Finance
		fin := api.Group("/finance")
		{
			fin.POST("/accounts", financeHandler.CreateAccount)
			fin.GET("/accounts", financeHandler.ListAccounts)
			fin.GET("/accounts/:id", financeHandler.GetAccount)

			fin.POST("/journal-entries", financeHandler.CreateJournalEntry)
			fin.GET("/journal-entries", financeHandler.ListJournalEntries)
			fin.GET("/journal-entries/:id", financeHandler.GetJournalEntry)
			fin.POST("/journal-entries/:id/post", financeHandler.PostJournalEntry)
			fin.POST("/journal-entries/:id/reverse", financeHandler.ReverseJournalEntry)

			fin.POST("/invoices", financeHandler.CreateInvoice)
			fin.GET("/invoices", financeHandler.ListInvoices)
			fin.GET("/invoices/:id", financeHandler.GetInvoice)
			fin.POST("/invoices/:id/payment", financeHandler.RecordPayment)

			fin.POST("/tax/calculate", financeHandler.CalculateTax)

			fin.GET("/reports/trial-balance", financeHandler.GetTrialBalance)
			fin.GET("/reports/profit-loss", financeHandler.GetProfitAndLoss)
			fin.GET("/reports/balance-sheet", financeHandler.GetBalanceSheet)
			fin.GET("/reports/ar-aging", financeHandler.GetARaging)

			fin.POST("/budgets", financeHandler.CreateBudget)
			fin.GET("/budgets", financeHandler.ListBudgets)
			fin.GET("/budgets/:id", financeHandler.GetBudget)
		}

		// HR
		human := api.Group("/hr")
		{
			human.POST("/departments", hrHandler.CreateDepartment)
			human.GET("/departments", hrHandler.ListDepartments)
			human.GET("/departments/:id", hrHandler.GetDepartment)

			human.POST("/employees", hrHandler.CreateEmployee)
			human.GET("/employees", hrHandler.ListEmployees)
			human.GET("/employees/:id", hrHandler.GetEmployee)
			human.PUT("/employees/:id", hrHandler.UpdateEmployee)
			human.POST("/employees/:id/terminate", hrHandler.TerminateEmployee)

			human.POST("/leave/request", hrHandler.SubmitLeaveRequest)
			human.POST("/leave/:id/approve", hrHandler.ApproveLeave)
			human.POST("/leave/:id/reject", hrHandler.RejectLeave)
			human.GET("/leave/requests", hrHandler.ListLeaveRequests)
			human.GET("/leave/balance/:employeeId", hrHandler.GetLeaveBalance)

			human.POST("/attendance/clock-in", hrHandler.ClockIn)
			human.POST("/attendance/clock-out", hrHandler.ClockOut)
			human.GET("/attendance", hrHandler.ListAttendance)

			human.POST("/payroll", hrHandler.CreatePayrollRun)
			human.POST("/payroll/:runId/calculate", hrHandler.CalculatePayroll)
			human.POST("/payroll/:runId/approve", hrHandler.ApprovePayroll)
			human.POST("/payroll/:runId/process", hrHandler.ProcessPayroll)
			human.GET("/payroll", hrHandler.ListPayrollRuns)
			human.GET("/payroll/:runId", hrHandler.GetPayrollRun)
			human.GET("/payroll/:runId/payslip/:employeeId", hrHandler.GetPaySlip)

			human.GET("/dashboard", hrHandler.GetDashboard)
		}
	}

	// Start server
	srv := &http.Server{
		Addr:         ":" + cfg.ServerPort,
		Handler:      router,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 30 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	go func() {
		log.Printf("ERP Gateway starting on port %s", cfg.ServerPort)
		log.Printf("Swagger UI available at http://localhost:%s/swagger/index.html", cfg.ServerPort)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Server error: %v", err)
		}
	}()

	// Graceful shutdown
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down gateway...")

	shutCtx, shutCancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer shutCancel()
	if err := srv.Shutdown(shutCtx); err != nil {
		log.Fatalf("Forced shutdown: %v", err)
	}
	log.Println("Gateway stopped")
}

func dialGRPC(addr string) *grpc.ClientConn {
	conn, err := grpc.NewClient(addr,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
	)
	if err != nil {
		log.Fatalf("Failed to connect to %s: %v", addr, err)
	}
	log.Printf("Connected to gRPC service at %s", addr)
	return conn
}
