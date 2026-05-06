export interface User {
  id: string;
  name: string;
  email: string;
  role: string;
}

export interface Employee {
  id: string;
  first_name: string;
  last_name: string;
  email: string;
  department_id: string;
  department_name?: string;
  position: string;
  status: string;
}

export interface Department {
  id: string;
  code: string;
  name: string;
  manager_id?: string;
}

export interface LeaveRequest {
  id: string;
  employee_id: string;
  employee_name?: string;
  leave_type_id: string;
  leave_type?: string;
  start_date: string;
  end_date: string;
  status: string;
}

export interface Attendance {
  id: string;
  employee_id: string;
  employee_name?: string;
  date: string;
  clock_in?: string;
  clock_out?: string;
  status: string;
}

export interface PayrollRun {
  id: string;
  run_number: string;
  period_start: string;
  period_end: string;
  status: string;
  total_amount_cents?: number;
}

export interface Product {
  id: string;
  sku: string;
  name: string;
  description?: string;
  category_id?: string;
  brand?: string;
  unit_of_measure: string;
  unit_cost_cents: number;
  selling_price_cents: number;
  status: string;
}

export interface StockLevel {
  id: string;
  product_id: string;
  product_name?: string;
  sku?: string;
  warehouse_id: string;
  warehouse_name?: string;
  quantity_on_hand: number;
  quantity_reserved: number;
  bin_location?: string;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  parent_id?: string;
}

export interface Warehouse {
  id: string;
  code: string;
  name: string;
  city?: string;
  country?: string;
  status: string;
}

export interface SalesOrder {
  id: string;
  order_number: string;
  customer_id: string;
  customer_name?: string;
  status: string;
  total_amount_cents: number;
  order_date: string;
  created_at: string;
}

export interface PurchaseOrder {
  id: string;
  po_number: string;
  supplier_id: string;
  status: string;
  total_amount_cents: number;
  expected_delivery_date?: string;
}

export interface Customer {
  id: string;
  code: string;
  company_name: string;
  contact_name?: string;
  email?: string;
  phone?: string;
  billing_city?: string;
  billing_country?: string;
  status: string;
}

export interface Supplier {
  id: string;
  code: string;
  company_name: string;
  contact_name?: string;
  email?: string;
  phone?: string;
  city?: string;
  country?: string;
}

export interface Account {
  id: string;
  account_number: string;
  name: string;
  type: string;
  category: string;
  is_active: boolean;
}

export interface JournalEntry {
  id: string;
  entry_number: string;
  entry_date: string;
  memo?: string;
  status: string;
  total_amount_cents: number;
  created_at: string;
}

export interface Invoice {
  id: string;
  invoice_number: string;
  type: string;
  entity_id?: string;
  status: string;
  total_amount_cents: number;
  due_date: string;
}

export interface Budget {
  id: string;
  department_id?: string;
  year: string;
  name: string;
  total_amount_cents: number;
  status: string;
}
