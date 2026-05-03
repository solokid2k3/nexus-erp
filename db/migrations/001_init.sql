-- ERP System Database Schema
-- PostgreSQL Migration 001: Initial Schema

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- INVENTORY SCHEMA
-- ============================================================

CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE inventory.categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES inventory.categories(id),
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE inventory.warehouses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    street_line1 VARCHAR(255),
    street_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','MAINTENANCE')),
    capacity_units BIGINT DEFAULT 0,
    used_units BIGINT DEFAULT 0,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE inventory.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES inventory.categories(id),
    brand VARCHAR(100),
    unit_of_measure VARCHAR(20) DEFAULT 'PCS',
    unit_cost_cents BIGINT DEFAULT 0,
    selling_price_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    weight_kg NUMERIC(10,3),
    attributes JSONB DEFAULT '{}',
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','DISCONTINUED')),
    reorder_point INT DEFAULT 0,
    reorder_quantity INT DEFAULT 0,
    lead_time_days INT DEFAULT 0,
    supplier_id UUID,
    tax_category VARCHAR(50),
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_products_sku ON inventory.products(sku);
CREATE INDEX idx_products_category ON inventory.products(category_id);
CREATE INDEX idx_products_status ON inventory.products(status);
CREATE INDEX idx_products_supplier ON inventory.products(supplier_id);

CREATE TABLE inventory.stock_levels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES inventory.products(id),
    warehouse_id UUID NOT NULL REFERENCES inventory.warehouses(id),
    quantity_on_hand INT DEFAULT 0,
    quantity_reserved INT DEFAULT 0,
    quantity_incoming INT DEFAULT 0,
    bin_location VARCHAR(50),
    last_counted TIMESTAMPTZ,
    UNIQUE(product_id, warehouse_id)
);

CREATE INDEX idx_stock_product ON inventory.stock_levels(product_id);
CREATE INDEX idx_stock_warehouse ON inventory.stock_levels(warehouse_id);

CREATE TABLE inventory.stock_reservations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES inventory.products(id),
    warehouse_id UUID NOT NULL REFERENCES inventory.warehouses(id),
    order_id VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    reserved_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    released_at TIMESTAMPTZ
);

CREATE TABLE inventory.stock_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES inventory.products(id),
    warehouse_id UUID NOT NULL REFERENCES inventory.warehouses(id),
    reason VARCHAR(50) NOT NULL,
    quantity_change INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reference_id VARCHAR(100),
    notes TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_movements_product ON inventory.stock_movements(product_id);
CREATE INDEX idx_movements_warehouse ON inventory.stock_movements(warehouse_id);
CREATE INDEX idx_movements_created ON inventory.stock_movements(created_at);

-- ============================================================
-- ORDER SCHEMA
-- ============================================================

CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE orders.customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    fax VARCHAR(50),
    website VARCHAR(255),
    billing_street VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_postal VARCHAR(20),
    billing_country VARCHAR(100),
    shipping_street VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal VARCHAR(20),
    shipping_country VARCHAR(100),
    tax_id VARCHAR(50),
    payment_terms VARCHAR(20) DEFAULT 'NET30',
    credit_limit_cents BIGINT DEFAULT 0,
    outstanding_balance_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    tier VARCHAR(20) DEFAULT 'BRONZE',
    discount_percent NUMERIC(5,2) DEFAULT 0,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE orders.suppliers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    tax_id VARCHAR(50),
    payment_terms VARCHAR(20) DEFAULT 'NET30',
    rating VARCHAR(10),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE orders.sales_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES orders.customers(id),
    status VARCHAR(30) DEFAULT 'DRAFT',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    shipping_street VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal VARCHAR(20),
    shipping_country VARCHAR(100),
    shipping_method VARCHAR(50),
    subtotal_cents BIGINT DEFAULT 0,
    tax_amount_cents BIGINT DEFAULT 0,
    shipping_cost_cents BIGINT DEFAULT 0,
    discount_amount_cents BIGINT DEFAULT 0,
    total_amount_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_terms VARCHAR(20),
    notes TEXT,
    warehouse_id UUID,
    order_date TIMESTAMPTZ DEFAULT NOW(),
    required_date TIMESTAMPTZ,
    shipped_date TIMESTAMPTZ,
    invoice_id UUID,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_sales_orders_customer ON orders.sales_orders(customer_id);
CREATE INDEX idx_sales_orders_status ON orders.sales_orders(status);
CREATE INDEX idx_sales_orders_date ON orders.sales_orders(order_date);

CREATE TABLE orders.order_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders.sales_orders(id) ON DELETE CASCADE,
    line_number INT NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255),
    sku VARCHAR(100),
    quantity_ordered INT NOT NULL,
    quantity_shipped INT DEFAULT 0,
    quantity_returned INT DEFAULT 0,
    unit_price_cents BIGINT NOT NULL,
    discount_percent NUMERIC(5,2) DEFAULT 0,
    line_total_cents BIGINT NOT NULL,
    tax_amount_cents BIGINT DEFAULT 0,
    tax_category VARCHAR(50),
    currency VARCHAR(3) DEFAULT 'USD',
    notes TEXT
);

CREATE TABLE orders.purchase_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    po_number VARCHAR(50) UNIQUE NOT NULL,
    supplier_id UUID NOT NULL REFERENCES orders.suppliers(id),
    status VARCHAR(30) DEFAULT 'DRAFT',
    subtotal_cents BIGINT DEFAULT 0,
    tax_amount_cents BIGINT DEFAULT 0,
    total_amount_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_terms VARCHAR(20),
    delivery_terms VARCHAR(50),
    warehouse_id UUID,
    order_date TIMESTAMPTZ DEFAULT NOW(),
    expected_delivery TIMESTAMPTZ,
    actual_delivery TIMESTAMPTZ,
    notes TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE orders.purchase_order_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    po_id UUID NOT NULL REFERENCES orders.purchase_orders(id) ON DELETE CASCADE,
    line_number INT NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255),
    quantity_ordered INT NOT NULL,
    quantity_received INT DEFAULT 0,
    unit_cost_cents BIGINT NOT NULL,
    line_total_cents BIGINT NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD'
);

-- ============================================================
-- FINANCE SCHEMA
-- ============================================================

CREATE SCHEMA IF NOT EXISTS finance;

CREATE TABLE finance.accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('ASSET','LIABILITY','EQUITY','REVENUE','EXPENSE')),
    sub_type VARCHAR(50),
    parent_id UUID REFERENCES finance.accounts(id),
    balance_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    normal_balance VARCHAR(10) NOT NULL CHECK (normal_balance IN ('DEBIT','CREDIT')),
    is_active BOOLEAN DEFAULT TRUE,
    is_system BOOLEAN DEFAULT FALSE,
    tax_code VARCHAR(20),
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE finance.journal_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entry_number VARCHAR(50) UNIQUE NOT NULL,
    entry_date TIMESTAMPTZ NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','POSTED','REVERSED')),
    source_type VARCHAR(30),
    source_id VARCHAR(100),
    fiscal_period VARCHAR(10),
    total_debit_cents BIGINT DEFAULT 0,
    total_credit_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_journal_date ON finance.journal_entries(entry_date);
CREATE INDEX idx_journal_status ON finance.journal_entries(status);

CREATE TABLE finance.journal_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    journal_entry_id UUID NOT NULL REFERENCES finance.journal_entries(id) ON DELETE CASCADE,
    line_number INT NOT NULL,
    account_id UUID NOT NULL REFERENCES finance.accounts(id),
    debit_amount_cents BIGINT DEFAULT 0,
    credit_amount_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    description TEXT,
    department_id VARCHAR(100),
    cost_center VARCHAR(100)
);

CREATE TABLE finance.invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    invoice_type VARCHAR(20) NOT NULL CHECK (invoice_type IN ('SALES','PURCHASE','CREDIT_NOTE','DEBIT_NOTE')),
    customer_id UUID,
    customer_name VARCHAR(255),
    order_id UUID,
    status VARCHAR(20) DEFAULT 'DRAFT',
    subtotal_cents BIGINT DEFAULT 0,
    tax_amount_cents BIGINT DEFAULT 0,
    discount_amount_cents BIGINT DEFAULT 0,
    total_amount_cents BIGINT DEFAULT 0,
    amount_paid_cents BIGINT DEFAULT 0,
    amount_due_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_terms VARCHAR(20),
    invoice_date TIMESTAMPTZ NOT NULL,
    due_date TIMESTAMPTZ NOT NULL,
    paid_date TIMESTAMPTZ,
    notes TEXT,
    journal_entry_id UUID,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_invoices_customer ON finance.invoices(customer_id);
CREATE INDEX idx_invoices_status ON finance.invoices(status);
CREATE INDEX idx_invoices_due ON finance.invoices(due_date);

CREATE TABLE finance.invoice_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES finance.invoices(id) ON DELETE CASCADE,
    line_number INT NOT NULL,
    description TEXT,
    product_id UUID,
    quantity INT DEFAULT 1,
    unit_price_cents BIGINT NOT NULL,
    tax_rate NUMERIC(5,2) DEFAULT 0,
    tax_amount_cents BIGINT DEFAULT 0,
    line_total_cents BIGINT NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    account_id UUID REFERENCES finance.accounts(id)
);

CREATE TABLE finance.payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES finance.invoices(id),
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_method VARCHAR(20),
    reference_number VARCHAR(100),
    payment_date TIMESTAMPTZ NOT NULL,
    bank_account_id UUID,
    notes TEXT,
    recorded_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE finance.tax_rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100),
    rate NUMERIC(5,4) NOT NULL,
    region VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    effective_from TIMESTAMPTZ,
    effective_to TIMESTAMPTZ
);

CREATE TABLE finance.budgets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    fiscal_year VARCHAR(4) NOT NULL,
    department_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'DRAFT',
    total_budget_cents BIGINT DEFAULT 0,
    total_actual_cents BIGINT DEFAULT 0,
    variance_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE finance.budget_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    budget_id UUID NOT NULL REFERENCES finance.budgets(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES finance.accounts(id),
    period VARCHAR(10),
    budgeted_cents BIGINT DEFAULT 0,
    actual_cents BIGINT DEFAULT 0,
    variance_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD'
);

-- ============================================================
-- HR SCHEMA
-- ============================================================

CREATE SCHEMA IF NOT EXISTS hr;

CREATE TABLE hr.departments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES hr.departments(id),
    manager_id UUID,
    budget_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE hr.employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    date_of_birth DATE,
    gender VARCHAR(10),
    national_id VARCHAR(50),
    tax_id VARCHAR(50),
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    department_id UUID REFERENCES hr.departments(id),
    position_title VARCHAR(100),
    job_grade VARCHAR(20),
    manager_id UUID REFERENCES hr.employees(id),
    employment_type VARCHAR(20) DEFAULT 'FULL_TIME',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    hire_date DATE NOT NULL,
    termination_date DATE,
    termination_reason TEXT,
    base_salary_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    pay_frequency VARCHAR(20) DEFAULT 'MONTHLY',
    bank_account VARCHAR(50),
    bank_name VARCHAR(100),
    emergency_name VARCHAR(100),
    emergency_relationship VARCHAR(50),
    emergency_phone VARCHAR(50),
    emergency_email VARCHAR(255),
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_employees_dept ON hr.employees(department_id);
CREATE INDEX idx_employees_status ON hr.employees(status);
CREATE INDEX idx_employees_manager ON hr.employees(manager_id);

CREATE TABLE hr.leave_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    default_days_per_year INT DEFAULT 0,
    is_paid BOOLEAN DEFAULT TRUE,
    requires_approval BOOLEAN DEFAULT TRUE,
    allow_carry_forward BOOLEAN DEFAULT FALSE,
    max_carry_forward_days INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE hr.leave_balances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    leave_type_id UUID NOT NULL REFERENCES hr.leave_types(id),
    fiscal_year VARCHAR(4) NOT NULL,
    total_allocated INT DEFAULT 0,
    used INT DEFAULT 0,
    pending INT DEFAULT 0,
    carried_forward INT DEFAULT 0,
    UNIQUE(employee_id, leave_type_id, fiscal_year)
);

CREATE TABLE hr.leave_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    leave_type_id UUID NOT NULL REFERENCES hr.leave_types(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    reason TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    rejection_reason TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE hr.attendance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    attendance_date DATE NOT NULL,
    clock_in TIMESTAMPTZ,
    clock_out TIMESTAMPTZ,
    hours_worked NUMERIC(5,2) DEFAULT 0,
    overtime_hours NUMERIC(5,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PRESENT',
    notes TEXT,
    UNIQUE(employee_id, attendance_date)
);

CREATE TABLE hr.payroll_runs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    run_number VARCHAR(50) UNIQUE NOT NULL,
    period VARCHAR(10) NOT NULL,
    pay_frequency VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    payment_date DATE NOT NULL,
    total_gross_cents BIGINT DEFAULT 0,
    total_deductions_cents BIGINT DEFAULT 0,
    total_net_cents BIGINT DEFAULT 0,
    total_employer_cost_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    employee_count INT DEFAULT 0,
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE hr.pay_slips (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payroll_run_id UUID NOT NULL REFERENCES hr.payroll_runs(id),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    base_salary_cents BIGINT DEFAULT 0,
    total_earnings_cents BIGINT DEFAULT 0,
    total_deductions_cents BIGINT DEFAULT 0,
    total_employer_cents BIGINT DEFAULT 0,
    net_pay_cents BIGINT DEFAULT 0,
    income_tax_cents BIGINT DEFAULT 0,
    social_security_cents BIGINT DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    regular_hours NUMERIC(5,2) DEFAULT 0,
    overtime_hours NUMERIC(5,2) DEFAULT 0,
    leave_days INT DEFAULT 0,
    absent_days INT DEFAULT 0,
    earnings JSONB DEFAULT '[]',
    deductions JSONB DEFAULT '[]',
    employer_contributions JSONB DEFAULT '[]',
    UNIQUE(payroll_run_id, employee_id)
);

-- Seed default chart of accounts
INSERT INTO finance.accounts (account_number, name, account_type, sub_type, normal_balance, is_system) VALUES
('1000', 'Cash', 'ASSET', 'CASH', 'DEBIT', true),
('1010', 'Bank Account', 'ASSET', 'BANK', 'DEBIT', true),
('1200', 'Accounts Receivable', 'ASSET', 'ACCOUNTS_RECEIVABLE', 'DEBIT', true),
('1300', 'Inventory', 'ASSET', 'INVENTORY', 'DEBIT', true),
('1500', 'Fixed Assets', 'ASSET', 'FIXED_ASSET', 'DEBIT', true),
('2000', 'Accounts Payable', 'LIABILITY', 'ACCOUNTS_PAYABLE', 'CREDIT', true),
('2100', 'Accrued Liabilities', 'LIABILITY', 'ACCRUED', 'CREDIT', true),
('2200', 'Tax Payable', 'LIABILITY', 'TAX_PAYABLE', 'CREDIT', true),
('2500', 'Long Term Debt', 'LIABILITY', 'LONG_TERM_DEBT', 'CREDIT', true),
('3000', 'Common Stock', 'EQUITY', 'COMMON_STOCK', 'CREDIT', true),
('3100', 'Retained Earnings', 'EQUITY', 'RETAINED_EARNINGS', 'CREDIT', true),
('4000', 'Sales Revenue', 'REVENUE', 'SALES_REVENUE', 'CREDIT', true),
('4100', 'Service Revenue', 'REVENUE', 'SERVICE_REVENUE', 'CREDIT', true),
('4200', 'Other Income', 'REVENUE', 'OTHER_INCOME', 'CREDIT', true),
('5000', 'Cost of Goods Sold', 'EXPENSE', 'COST_OF_GOODS', 'DEBIT', true),
('6000', 'Operating Expenses', 'EXPENSE', 'OPERATING_EXPENSE', 'DEBIT', true),
('6100', 'Payroll Expense', 'EXPENSE', 'PAYROLL_EXPENSE', 'DEBIT', true),
('6200', 'Depreciation', 'EXPENSE', 'DEPRECIATION', 'DEBIT', true),
('7000', 'Interest Expense', 'EXPENSE', 'INTEREST_EXPENSE', 'DEBIT', true),
('8000', 'Tax Expense', 'EXPENSE', 'TAX_EXPENSE', 'DEBIT', true);

-- Seed default leave types
INSERT INTO hr.leave_types (code, name, default_days_per_year, is_paid, requires_approval, allow_carry_forward, max_carry_forward_days) VALUES
('AL', 'Annual Leave', 20, true, true, true, 5),
('SL', 'Sick Leave', 10, true, false, false, 0),
('ML', 'Maternity Leave', 90, true, true, false, 0),
('PL', 'Paternity Leave', 14, true, true, false, 0),
('UL', 'Unpaid Leave', 30, false, true, false, 0),
('CL', 'Compassionate Leave', 5, true, true, false, 0);

-- Seed default tax rates
INSERT INTO finance.tax_rates (code, name, rate, region, is_active) VALUES
('GST', 'Goods & Services Tax', 0.10, 'DEFAULT', true),
('VAT_STD', 'Standard VAT', 0.20, 'EU', true),
('VAT_RED', 'Reduced VAT', 0.05, 'EU', true),
('SALES_TAX', 'Sales Tax', 0.08, 'US', true),
('EXEMPT', 'Tax Exempt', 0.00, 'DEFAULT', true);
