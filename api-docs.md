# Nexus ERP API Documentation

**Version**: 1.0

Nexus Enterprise Resource Planning System API Gateway. Provides unified REST API access to Inventory, Order, Finance, and HR microservices.

## User login
**Endpoint**: `POST /api/v1/auth/login`

**Description**: Authenticate with username and password to receive JWT tokens

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `credentials` | body | handlers.LoginRequest | Yes | Login credentials |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | handlers.AuthResponse |
| 400 | Bad Request | object |
| 401 | Unauthorized | object |

---

## Refresh access token
**Endpoint**: `POST /api/v1/auth/refresh`

**Description**: Exchange a valid refresh token for a new access token

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `token` | body | object | Yes | Refresh token |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | handlers.AuthResponse |
| 400 | Bad Request | object |
| 401 | Unauthorized | object |

---

## List all accounts
**Endpoint**: `GET /api/v1/finance/accounts`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a chart of accounts entry
**Endpoint**: `POST /api/v1/finance/accounts`

**Description**: Create a new account in the chart of accounts

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `account` | body | object | Yes | Account data (code, name, type) |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get an account by ID
**Endpoint**: `GET /api/v1/finance/accounts/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Account ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List all budgets
**Endpoint**: `GET /api/v1/finance/budgets`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a budget
**Endpoint**: `POST /api/v1/finance/budgets`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `budget` | body | object | Yes | Budget data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get a budget by ID
**Endpoint**: `GET /api/v1/finance/budgets/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Budget ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List all invoices
**Endpoint**: `GET /api/v1/finance/invoices`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create an invoice
**Endpoint**: `POST /api/v1/finance/invoices`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `invoice` | body | object | Yes | Invoice data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get an invoice by ID
**Endpoint**: `GET /api/v1/finance/invoices/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Invoice ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Record a payment against an invoice
**Endpoint**: `POST /api/v1/finance/invoices/{id}/payment`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Invoice ID |
| `payment` | body | object | Yes | Payment data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## List all journal entries
**Endpoint**: `GET /api/v1/finance/journal-entries`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a journal entry
**Endpoint**: `POST /api/v1/finance/journal-entries`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `entry` | body | object | Yes | Journal entry data (date, lines, memo) |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get a journal entry by ID
**Endpoint**: `GET /api/v1/finance/journal-entries/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Journal Entry ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Post a journal entry
**Endpoint**: `POST /api/v1/finance/journal-entries/{id}/post`

**Description**: Post a draft journal entry to the general ledger

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Journal Entry ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Reverse a journal entry
**Endpoint**: `POST /api/v1/finance/journal-entries/{id}/reverse`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Journal Entry ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Get accounts receivable aging report
**Endpoint**: `GET /api/v1/finance/reports/ar-aging`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Get balance sheet report
**Endpoint**: `GET /api/v1/finance/reports/balance-sheet`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Get profit & loss report
**Endpoint**: `GET /api/v1/finance/reports/profit-loss`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Get trial balance report
**Endpoint**: `GET /api/v1/finance/reports/trial-balance`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Calculate tax
**Endpoint**: `POST /api/v1/finance/tax/calculate`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `request` | body | object | Yes | Tax calculation input |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## List attendance records
**Endpoint**: `GET /api/v1/hr/attendance`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Clock in attendance
**Endpoint**: `POST /api/v1/hr/attendance/clock-in`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `data` | body | object | Yes | Clock-in data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Clock out attendance
**Endpoint**: `POST /api/v1/hr/attendance/clock-out`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `data` | body | object | Yes | Clock-out data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Get HR dashboard
**Endpoint**: `GET /api/v1/hr/dashboard`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List all departments
**Endpoint**: `GET /api/v1/hr/departments`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a new department
**Endpoint**: `POST /api/v1/hr/departments`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `dept` | body | object | Yes | Department data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get a department by ID
**Endpoint**: `GET /api/v1/hr/departments/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Department ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List all employees
**Endpoint**: `GET /api/v1/hr/employees`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a new employee
**Endpoint**: `POST /api/v1/hr/employees`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `employee` | body | object | Yes | Employee data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get an employee by ID
**Endpoint**: `GET /api/v1/hr/employees/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Employee ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Update an employee
**Endpoint**: `PUT /api/v1/hr/employees/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Employee ID |
| `employee` | body | object | Yes | Updated employee data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Terminate an employee
**Endpoint**: `POST /api/v1/hr/employees/{id}/terminate`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Employee ID |
| `data` | body | object | Yes | Termination data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Get leave balance for an employee
**Endpoint**: `GET /api/v1/hr/leave/balance/{employeeId}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `employeeId` | path | string | Yes | Employee ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Submit a leave request
**Endpoint**: `POST /api/v1/hr/leave/request`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `leave` | body | object | Yes | Leave request data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## List all leave requests
**Endpoint**: `GET /api/v1/hr/leave/requests`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Approve a leave request
**Endpoint**: `POST /api/v1/hr/leave/{id}/approve`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Leave Request ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Reject a leave request
**Endpoint**: `POST /api/v1/hr/leave/{id}/reject`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Leave Request ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List all payroll runs
**Endpoint**: `GET /api/v1/hr/payroll`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a payroll run
**Endpoint**: `POST /api/v1/hr/payroll`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `payroll` | body | object | Yes | Payroll run data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get a payroll run by ID
**Endpoint**: `GET /api/v1/hr/payroll/{runId}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `runId` | path | string | Yes | Payroll Run ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Approve a payroll run
**Endpoint**: `POST /api/v1/hr/payroll/{runId}/approve`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `runId` | path | string | Yes | Payroll Run ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Calculate payroll for a run
**Endpoint**: `POST /api/v1/hr/payroll/{runId}/calculate`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `runId` | path | string | Yes | Payroll Run ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Get a pay slip
**Endpoint**: `GET /api/v1/hr/payroll/{runId}/payslip/{employeeId}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `runId` | path | string | Yes | Payroll Run ID |
| `employeeId` | path | string | Yes | Employee ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Process a payroll run
**Endpoint**: `POST /api/v1/hr/payroll/{runId}/process`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `runId` | path | string | Yes | Payroll Run ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List all categories
**Endpoint**: `GET /api/v1/inventory/categories`

**Description**: Retrieve all product categories

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a new category
**Endpoint**: `POST /api/v1/inventory/categories`

**Description**: Create a new product category

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `category` | body | object | Yes | Category data (name, description) |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## List all products
**Endpoint**: `GET /api/v1/inventory/products`

**Description**: Retrieve a paginated list of products with optional filters

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `page` | query | string | No | Page number |
| `page_size` | query | string | No | Items per page |
| `category_id` | query | string | No | Filter by category |
| `status` | query | string | No | Filter by status |
| `q` | query | string | No | Search query |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a new product
**Endpoint**: `POST /api/v1/inventory/products`

**Description**: Create a new product in the inventory system

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `product` | body | object | Yes | Product data (name, sku, category_id, price, etc.) |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Product created successfully | object |
| 400 | Invalid request body | object |

---

## Get a product by ID
**Endpoint**: `GET /api/v1/inventory/products/{id}`

**Description**: Retrieve a single product by its ID

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Product ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Update a product
**Endpoint**: `PUT /api/v1/inventory/products/{id}`

**Description**: Update an existing product by its ID

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Product ID |
| `product` | body | object | Yes | Updated product data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Adjust stock quantity
**Endpoint**: `POST /api/v1/inventory/stock/adjust`

**Description**: Manually adjust stock quantity for a product in a warehouse

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `adjustment` | body | object | Yes | Stock adjustment data (product_id, warehouse_id, quantity, reason) |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Get low stock alerts
**Endpoint**: `GET /api/v1/inventory/stock/alerts`

**Description**: Retrieve products that are below their minimum stock threshold

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List stock movements
**Endpoint**: `GET /api/v1/inventory/stock/movements`

**Description**: Retrieve a history of all stock movements (adjustments, transfers, etc.)

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Reserve stock for an order
**Endpoint**: `POST /api/v1/inventory/stock/reserve`

**Description**: Reserve stock quantity for a pending order

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `reservation` | body | object | Yes | Reservation data (product_id, warehouse_id, quantity, order_id) |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Transfer stock between warehouses
**Endpoint**: `POST /api/v1/inventory/stock/transfer`

**Description**: Transfer stock from one warehouse to another

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `transfer` | body | object | Yes | Transfer data (product_id, from_warehouse, to_warehouse, quantity) |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Get stock levels for a product
**Endpoint**: `GET /api/v1/inventory/stock/{productId}`

**Description**: Retrieve stock levels across all warehouses for a given product

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `productId` | path | string | Yes | Product ID |
| `warehouse_id` | query | string | No | Filter by warehouse |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a new warehouse
**Endpoint**: `POST /api/v1/inventory/warehouses`

**Description**: Create a new warehouse location

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `warehouse` | body | object | Yes | Warehouse data (name, code, address) |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## List all customers
**Endpoint**: `GET /api/v1/orders/customers`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a new customer
**Endpoint**: `POST /api/v1/orders/customers`

**Description**: Create a new customer record

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `customer` | body | object | Yes | Customer data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get a customer by ID
**Endpoint**: `GET /api/v1/orders/customers/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Customer ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List all purchase orders
**Endpoint**: `GET /api/v1/orders/purchase`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a purchase order
**Endpoint**: `POST /api/v1/orders/purchase`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `order` | body | object | Yes | PO data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get a purchase order by ID
**Endpoint**: `GET /api/v1/orders/purchase/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | PO ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Receive a purchase order
**Endpoint**: `POST /api/v1/orders/purchase/{id}/receive`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | PO ID |
| `receipt` | body | object | Yes | Receipt data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## List all sales orders
**Endpoint**: `GET /api/v1/orders/sales`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a new sales order
**Endpoint**: `POST /api/v1/orders/sales`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `order` | body | object | Yes | Order data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get a sales order by ID
**Endpoint**: `GET /api/v1/orders/sales/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Order ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Approve a sales order
**Endpoint**: `POST /api/v1/orders/sales/{id}/approve`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Order ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Update order status
**Endpoint**: `PUT /api/v1/orders/sales/{id}/status`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Order ID |
| `status` | body | object | Yes | New status |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |
| 400 | Bad Request | object |

---

## Get order summary dashboard
**Endpoint**: `GET /api/v1/orders/summary`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## List all suppliers
**Endpoint**: `GET /api/v1/orders/suppliers`

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Create a new supplier
**Endpoint**: `POST /api/v1/orders/suppliers`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `supplier` | body | object | Yes | Supplier data |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 201 | Created | object |
| 400 | Bad Request | object |

---

## Get a supplier by ID
**Endpoint**: `GET /api/v1/orders/suppliers/{id}`

### Parameters
| Name | In | Type | Required | Description |
| ---- | -- | ---- | -------- | ----------- |
| `id` | path | string | Yes | Supplier ID |

### Responses
| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | object |

---

## Models / Definitions

### handlers.AuthResponse
| Property | Type | Description |
| -------- | ---- | ----------- |
| `access_token` | string |  |
| `expires_in` | integer |  |
| `refresh_token` | string |  |
| `token_type` | string |  |

### handlers.LoginRequest
| Property | Type | Description |
| -------- | ---- | ----------- |
| `password` | string |  |
| `username` | string |  |
