# ERP System - Microservices Architecture

Enterprise Resource Planning system built with Go, Java, gRPC, PostgreSQL, and Redis.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (Go/Gin)                      │
│                   REST → gRPC Translation                    │
│              JWT Auth │ Rate Limiting │ CORS                 │
└──────┬──────────┬──────────┬──────────┬─────────────────────┘
       │          │          │          │
  ┌────▼────┐ ┌──▼───┐ ┌───▼────┐ ┌──▼──┐
  │Inventory│ │Order │ │Finance │ │ HR  │
  │Service  │ │Service│ │Service │ │Svc  │
  │  (Go)   │ │ (Go) │ │ (Java) │ │(Java)│
  └────┬────┘ └──┬───┘ └───┬────┘ └──┬──┘
       │         │         │         │
  ┌────▼─────────▼─────────▼─────────▼────┐
  │           PostgreSQL 16               │
  │  inventory │ orders │ finance │ hr    │
  └───────────────────────────────────────┘
  ┌───────────────────────────────────────┐
  │            Redis 7 (Cache)            │
  └───────────────────────────────────────┘
```

## Services

| Service | Language | Port | Protocol | Description |
|---------|----------|------|----------|-------------|
| Gateway | Go (Gin) | 8080 | REST/HTTP | API routing, auth, rate limiting |
| Inventory | Go | 50051 | gRPC | Products, warehouses, stock management |
| Order | Go | 50052 | gRPC | Customers, sales/purchase orders, suppliers |
| Finance | Java (Spring Boot) | 50053 | gRPC | Chart of accounts, journal entries, invoicing, reports |
| HR | Java (Spring Boot) | 50054 | gRPC | Employees, leave, attendance, payroll |

## Business Logic Highlights

### Inventory Service
- **Product Management**: CRUD with categories, attributes, pricing
- **Stock Management**: Atomic stock adjustments with PostgreSQL row-level locking
- **Stock Transfers**: Transactional inter-warehouse transfers
- **Reservations**: Stock reservation for orders with release capability
- **Low Stock Alerts**: Automatic detection based on reorder points (WARNING/CRITICAL)
- **Movement Audit Trail**: Full history of all stock changes
- **Redis Caching**: Product and stock level caching with TTL

### Order Service
- **Customer Management**: Tiers (BRONZE→PLATINUM), credit limits, payment terms
- **Sales Orders**: Full lifecycle with state machine (DRAFT→APPROVED→PROCESSING→SHIPPED→DELIVERED)
- **Credit Check**: Validates customer credit limit before order creation
- **Auto-Pricing**: Customer discount application, tax calculation
- **Purchase Orders**: Supplier ordering with receiving workflow
- **Order Analytics**: Summary statistics, fulfillment rates

### Finance Service (Double-Entry Bookkeeping)
- **Chart of Accounts**: Full ASSET/LIABILITY/EQUITY/REVENUE/EXPENSE hierarchy
- **Journal Entries**: Enforces debits = credits rule
- **Auto-Journaling**: Sales invoices automatically create AR/Revenue journal entries
- **Payment Recording**: Automatically adjusts AR and posts bank journal entries
- **Trial Balance**: Verifies books are balanced
- **Profit & Loss**: Revenue vs expense reporting
- **Balance Sheet**: Assets = Liabilities + Equity verification
- **AR Aging**: Current/1-30/31-60/61-90/90+ day buckets
- **Budget Management**: Budget vs actual tracking

### HR Service
- **Employee Lifecycle**: Hire → Active → Terminate with auto-numbering
- **Leave Management**: Request → Approve/Reject workflow with weekend exclusion
- **Attendance**: Clock-in/out with late detection and overtime calculation
- **Payroll Processing**: 
  - Progressive income tax calculation (US brackets)
  - Social Security & Medicare deductions
  - Employer contribution calculations
  - Overtime pay at 1.5x rate
  - Pro-rated salary for partial periods
  - State machine: DRAFT → CALCULATED → APPROVED → COMPLETED
- **HR Dashboard**: Employee counts, department summaries, leave stats

## Quick Start

```bash
# Start all services
docker-compose up -d

# Check health
curl http://localhost:8080/health

# Login (get JWT token)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Use the token for authenticated requests
export TOKEN="<access_token_from_response>"

# Create a product
curl -X POST http://localhost:8080/api/v1/inventory/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "WIDGET-001",
    "name": "Premium Widget",
    "unit_cost_cents": 1000,
    "selling_price_cents": 2500
  }'
```

## Demo Users

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |
| finance | finance123 | FINANCE |
| hr | hr123 | HR |
| sales | sales123 | SALES |

## Project Structure

```
erp-system/
├── proto/                          # Protobuf definitions
│   ├── common.proto                # Shared types
│   ├── inventory.proto             # Inventory service API
│   ├── order.proto                 # Order service API
│   ├── finance.proto               # Finance service API
│   └── hr.proto                    # HR service API
├── db/migrations/                  # PostgreSQL schema
│   └── 001_init.sql                # Full schema with seed data
├── gateway/                        # Go API Gateway
│   ├── main.go
│   ├── config/
│   ├── middleware/                  # JWT, CORS, rate limiting
│   └── handlers/                   # REST→gRPC proxy handlers
├── inventory-service/              # Go Inventory Service
│   ├── main.go
│   └── internal/
│       ├── model/
│       ├── repository/             # PostgreSQL with transactions
│       ├── service/                # Business logic + Redis cache
│       └── handler/                # gRPC handler
├── order-service/                  # Go Order Service
│   ├── main.go
│   └── internal/
│       ├── model/
│       ├── repository/
│       ├── service/                # Credit checks, state machine
│       └── handler/
├── finance-service/                # Java/Spring Boot Finance Service
│   ├── pom.xml
│   └── src/main/java/com/erp/finance/
│       ├── entity/                 # JPA entities
│       ├── repository/             # Spring Data JPA
│       └── service/                # Double-entry bookkeeping, reports
├── hr-service/                     # Java/Spring Boot HR Service
│   ├── pom.xml
│   └── src/main/java/com/erp/hr/
│       ├── entity/
│       ├── repository/
│       └── service/                # Payroll, leave, attendance
└── docker-compose.yml
```

## Technology Stack

- **Go 1.22** - Gateway, Inventory, Order services
- **Java 21 / Spring Boot 3.3** - Finance, HR services
- **gRPC** - Inter-service communication
- **PostgreSQL 16** - Primary database (4 schemas)
- **Redis 7** - Caching, sessions, rate limiting
- **Docker** - Containerization
- **Gin** - HTTP framework (Gateway)
- **pgx** - PostgreSQL driver (Go)
- **Spring Data JPA** - ORM (Java)

## Next Steps

1. Generate protobuf stubs: `protoc --go_out=. --go-grpc_out=. proto/*.proto`
2. Run `go mod tidy` in each Go service
3. Wire generated gRPC stubs into handlers
4. Add unit tests
5. Set up CI/CD pipeline
6. Add observability (OpenTelemetry, Prometheus)
