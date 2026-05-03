package service

import (
	"context"
	"fmt"
	"log"
	"time"

	"erp-system/order-service/internal/model"
	"erp-system/order-service/internal/repository"

	"github.com/redis/go-redis/v9"
	"google.golang.org/grpc"
)

// Order status transition rules
var validTransitions = map[string][]string{
	"DRAFT":            {"PENDING_APPROVAL", "CANCELLED"},
	"PENDING_APPROVAL": {"APPROVED", "CANCELLED"},
	"APPROVED":         {"PROCESSING", "ON_HOLD", "CANCELLED"},
	"PROCESSING":       {"PICKING", "ON_HOLD", "CANCELLED"},
	"PICKING":          {"PACKED", "ON_HOLD"},
	"PACKED":           {"SHIPPED"},
	"SHIPPED":          {"DELIVERED", "RETURNED"},
	"DELIVERED":        {"RETURNED"},
	"ON_HOLD":          {"PROCESSING", "CANCELLED"},
}

type OrderService struct {
	repo     *repository.OrderRepository
	rdb      *redis.Client
	invConn  *grpc.ClientConn // inventory service
	finConn  *grpc.ClientConn // finance service
}

func NewOrderService(repo *repository.OrderRepository, rdb *redis.Client,
	invConn, finConn *grpc.ClientConn) *OrderService {
	return &OrderService{repo: repo, rdb: rdb, invConn: invConn, finConn: finConn}
}

// ============================================================
// Customer Operations
// ============================================================

func (s *OrderService) CreateCustomer(ctx context.Context, c *model.Customer) error {
	if c.Code == "" || c.CompanyName == "" {
		return fmt.Errorf("customer code and company name are required")
	}
	if c.Currency == "" {
		c.Currency = "USD"
	}
	if c.Status == "" {
		c.Status = "ACTIVE"
	}
	if c.Tier == "" {
		c.Tier = "BRONZE"
	}
	if c.PaymentTerms == "" {
		c.PaymentTerms = "NET30"
	}
	return s.repo.CreateCustomer(ctx, c)
}

func (s *OrderService) GetCustomer(ctx context.Context, id string) (*model.Customer, error) {
	return s.repo.GetCustomer(ctx, id)
}

// ============================================================
// Sales Order Operations with Business Logic
// ============================================================

func (s *OrderService) CreateOrder(ctx context.Context, o *model.SalesOrder) error {
	if o.CustomerID == "" {
		return fmt.Errorf("customer_id is required")
	}
	if len(o.Lines) == 0 {
		return fmt.Errorf("order must have at least one line item")
	}

	// Validate customer exists and check credit
	customer, err := s.repo.GetCustomer(ctx, o.CustomerID)
	if err != nil {
		return fmt.Errorf("customer not found: %w", err)
	}
	if customer.Status != "ACTIVE" {
		return fmt.Errorf("customer %s is not active (status: %s)", customer.Code, customer.Status)
	}

	// Apply customer discount to lines
	o.CustomerName = customer.CompanyName
	if o.Currency == "" {
		o.Currency = customer.Currency
	}
	if o.PaymentTerms == "" {
		o.PaymentTerms = customer.PaymentTerms
	}

	// Calculate line totals and order totals
	var subtotal int64
	for _, line := range o.Lines {
		// Apply customer-level discount if no line-level discount
		if line.DiscountPercent == 0 && customer.DiscountPercent > 0 {
			line.DiscountPercent = customer.DiscountPercent
		}

		lineGross := int64(line.QuantityOrdered) * line.UnitPriceCents
		discountAmount := int64(float64(lineGross) * line.DiscountPercent / 100)
		line.LineTotalCents = lineGross - discountAmount

		// Calculate tax (simplified - 10% default)
		taxRate := 0.10
		line.TaxAmountCents = int64(float64(line.LineTotalCents) * taxRate)

		subtotal += line.LineTotalCents
	}

	o.SubtotalCents = subtotal
	o.TaxAmountCents = int64(float64(subtotal) * 0.10) // simplified
	o.TotalAmountCents = o.SubtotalCents + o.TaxAmountCents + o.ShippingCostCents - o.DiscountCents

	// Credit limit check
	newOutstanding := customer.OutstandingCents + o.TotalAmountCents
	if customer.CreditLimitCents > 0 && newOutstanding > customer.CreditLimitCents {
		return fmt.Errorf("order exceeds credit limit: outstanding=%d, order=%d, limit=%d",
			customer.OutstandingCents, o.TotalAmountCents, customer.CreditLimitCents)
	}

	if o.Status == "" {
		o.Status = "DRAFT"
	}
	if o.Priority == "" {
		o.Priority = "NORMAL"
	}

	// Reserve stock for each line via inventory service (if connected)
	if s.invConn != nil {
		for _, line := range o.Lines {
			log.Printf("Reserving stock: product=%s, qty=%d, warehouse=%s",
				line.ProductID, line.QuantityOrdered, o.WarehouseID)
			// In production: call inventory gRPC to reserve stock
		}
	}

	return s.repo.CreateSalesOrder(ctx, o)
}

func (s *OrderService) GetOrder(ctx context.Context, id string) (*model.SalesOrder, error) {
	return s.repo.GetSalesOrder(ctx, id)
}

func (s *OrderService) UpdateOrderStatus(ctx context.Context, id, newStatus, notes, updatedBy string) error {
	// Get current order
	order, err := s.repo.GetSalesOrder(ctx, id)
	if err != nil {
		return fmt.Errorf("order not found: %w", err)
	}

	// Validate state transition
	if !isValidTransition(order.Status, newStatus) {
		return fmt.Errorf("invalid status transition from %s to %s", order.Status, newStatus)
	}

	// Business logic for specific transitions
	switch newStatus {
	case "SHIPPED":
		// In production: deduct inventory, create shipment record
		log.Printf("Order %s shipped - deducting inventory", order.OrderNumber)
	case "CANCELLED":
		// Release stock reservations
		log.Printf("Order %s cancelled - releasing reservations", order.OrderNumber)
	case "APPROVED":
		// Generate invoice via finance service
		if s.finConn != nil {
			log.Printf("Order %s approved - generating invoice", order.OrderNumber)
		}
	}

	return s.repo.UpdateOrderStatus(ctx, id, newStatus, notes, updatedBy)
}

func (s *OrderService) ApproveOrder(ctx context.Context, id, approvedBy, notes string) error {
	return s.UpdateOrderStatus(ctx, id, "APPROVED", notes, approvedBy)
}

func (s *OrderService) ListOrders(ctx context.Context, customerID, status, priority string,
	startDate, endDate time.Time, page, pageSize int) ([]*model.SalesOrder, int, error) {
	if page < 1 { page = 1 }
	if pageSize < 1 || pageSize > 100 { pageSize = 20 }
	return s.repo.ListSalesOrders(ctx, customerID, status, priority, startDate, endDate, (page-1)*pageSize, pageSize)
}

// ============================================================
// Purchase Order Operations
// ============================================================

func (s *OrderService) CreatePurchaseOrder(ctx context.Context, po *model.PurchaseOrder) error {
	if po.SupplierID == "" {
		return fmt.Errorf("supplier_id is required")
	}
	if len(po.Lines) == 0 {
		return fmt.Errorf("PO must have at least one line")
	}

	if po.Currency == "" { po.Currency = "USD" }
	if po.Status == "" { po.Status = "DRAFT" }

	var subtotal int64
	for _, line := range po.Lines {
		line.LineTotalCents = int64(line.QuantityOrdered) * line.UnitCostCents
		subtotal += line.LineTotalCents
	}

	po.SubtotalCents = subtotal
	po.TaxAmountCents = int64(float64(subtotal) * 0.10)
	po.TotalAmountCents = po.SubtotalCents + po.TaxAmountCents

	return s.repo.CreatePurchaseOrder(ctx, po)
}

func (s *OrderService) ReceivePurchaseOrder(ctx context.Context, poID string,
	lineReceipts map[string]int32, receivedBy string) error {
	// In production: update PO line quantities, adjust inventory via gRPC
	log.Printf("Receiving PO %s - updating inventory", poID)
	return nil
}

// ============================================================
// Supplier Operations
// ============================================================

func (s *OrderService) CreateSupplier(ctx context.Context, sup *model.Supplier) error {
	if sup.Code == "" || sup.CompanyName == "" {
		return fmt.Errorf("supplier code and company name required")
	}
	if sup.Status == "" { sup.Status = "ACTIVE" }
	if sup.PaymentTerms == "" { sup.PaymentTerms = "NET30" }
	return s.repo.CreateSupplier(ctx, sup)
}

// ============================================================
// Analytics
// ============================================================

func (s *OrderService) GetOrderSummary(ctx context.Context, customerID string,
	startDate, endDate time.Time) (*model.OrderSummary, error) {
	return s.repo.GetOrderSummary(ctx, customerID, startDate, endDate)
}

func isValidTransition(current, target string) bool {
	valid, ok := validTransitions[current]
	if !ok {
		return false
	}
	for _, v := range valid {
		if v == target {
			return true
		}
	}
	return false
}
