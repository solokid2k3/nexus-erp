import requests
import json
import uuid

BASE_URL = 'http://localhost:8080'
TOKEN = None

session = requests.Session()
session.headers.update({'Content-Type': 'application/json'})

def set_token(token):
    global TOKEN
    TOKEN = token
    session.headers.update({'Authorization': f'Bearer {token}'})

def test_post_api_v1_auth_login():
    url = f"{BASE_URL}/api/v1/auth/login"
    payload = {
        "password": "admin123",
        "username": "admin"
    }
    response = session.post(url, json=payload)
    print(f"POST /api/v1/auth/login -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_auth_refresh():
    url = f"{BASE_URL}/api/v1/auth/refresh"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/auth/refresh -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_accounts():
    url = f"{BASE_URL}/api/v1/finance/accounts"
    response = session.get(url)
    print(f"GET /api/v1/finance/accounts -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_finance_accounts():
    url = f"{BASE_URL}/api/v1/finance/accounts"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/finance/accounts -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_accounts_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/finance/accounts/{id}"
    response = session.get(url)
    print(f"GET /api/v1/finance/accounts/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_budgets():
    url = f"{BASE_URL}/api/v1/finance/budgets"
    response = session.get(url)
    print(f"GET /api/v1/finance/budgets -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_finance_budgets():
    url = f"{BASE_URL}/api/v1/finance/budgets"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/finance/budgets -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_budgets_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/finance/budgets/{id}"
    response = session.get(url)
    print(f"GET /api/v1/finance/budgets/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_invoices():
    url = f"{BASE_URL}/api/v1/finance/invoices"
    response = session.get(url)
    print(f"GET /api/v1/finance/invoices -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_finance_invoices():
    url = f"{BASE_URL}/api/v1/finance/invoices"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/finance/invoices -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_invoices_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/finance/invoices/{id}"
    response = session.get(url)
    print(f"GET /api/v1/finance/invoices/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_finance_invoices_id_payment():
    id = 'string'
    url = f"{BASE_URL}/api/v1/finance/invoices/{id}/payment"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/finance/invoices/{id}/payment -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_journal_entries():
    url = f"{BASE_URL}/api/v1/finance/journal-entries"
    response = session.get(url)
    print(f"GET /api/v1/finance/journal-entries -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_finance_journal_entries():
    url = f"{BASE_URL}/api/v1/finance/journal-entries"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/finance/journal-entries -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_journal_entries_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/finance/journal-entries/{id}"
    response = session.get(url)
    print(f"GET /api/v1/finance/journal-entries/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_finance_journal_entries_id_post():
    id = 'string'
    url = f"{BASE_URL}/api/v1/finance/journal-entries/{id}/post"
    response = session.post(url)
    print(f"POST /api/v1/finance/journal-entries/{id}/post -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_finance_journal_entries_id_reverse():
    id = 'string'
    url = f"{BASE_URL}/api/v1/finance/journal-entries/{id}/reverse"
    response = session.post(url)
    print(f"POST /api/v1/finance/journal-entries/{id}/reverse -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_reports_ar_aging():
    url = f"{BASE_URL}/api/v1/finance/reports/ar-aging"
    response = session.get(url)
    print(f"GET /api/v1/finance/reports/ar-aging -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_reports_balance_sheet():
    url = f"{BASE_URL}/api/v1/finance/reports/balance-sheet"
    response = session.get(url)
    print(f"GET /api/v1/finance/reports/balance-sheet -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_reports_profit_loss():
    url = f"{BASE_URL}/api/v1/finance/reports/profit-loss"
    response = session.get(url)
    print(f"GET /api/v1/finance/reports/profit-loss -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_finance_reports_trial_balance():
    url = f"{BASE_URL}/api/v1/finance/reports/trial-balance"
    response = session.get(url)
    print(f"GET /api/v1/finance/reports/trial-balance -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_finance_tax_calculate():
    url = f"{BASE_URL}/api/v1/finance/tax/calculate"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/finance/tax/calculate -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_attendance():
    url = f"{BASE_URL}/api/v1/hr/attendance"
    response = session.get(url)
    print(f"GET /api/v1/hr/attendance -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_attendance_clock_in():
    url = f"{BASE_URL}/api/v1/hr/attendance/clock-in"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/hr/attendance/clock-in -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_attendance_clock_out():
    url = f"{BASE_URL}/api/v1/hr/attendance/clock-out"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/hr/attendance/clock-out -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_dashboard():
    url = f"{BASE_URL}/api/v1/hr/dashboard"
    response = session.get(url)
    print(f"GET /api/v1/hr/dashboard -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_departments():
    url = f"{BASE_URL}/api/v1/hr/departments"
    response = session.get(url)
    print(f"GET /api/v1/hr/departments -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_departments():
    url = f"{BASE_URL}/api/v1/hr/departments"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/hr/departments -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_departments_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/hr/departments/{id}"
    response = session.get(url)
    print(f"GET /api/v1/hr/departments/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_employees():
    url = f"{BASE_URL}/api/v1/hr/employees"
    response = session.get(url)
    print(f"GET /api/v1/hr/employees -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_employees():
    url = f"{BASE_URL}/api/v1/hr/employees"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/hr/employees -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_employees_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/hr/employees/{id}"
    response = session.get(url)
    print(f"GET /api/v1/hr/employees/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_put_api_v1_hr_employees_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/hr/employees/{id}"
    payload = {}
    response = session.put(url, json=payload)
    print(f"PUT /api/v1/hr/employees/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_employees_id_terminate():
    id = 'string'
    url = f"{BASE_URL}/api/v1/hr/employees/{id}/terminate"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/hr/employees/{id}/terminate -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_leave_balance_employeeId():
    employeeId = 'string'
    url = f"{BASE_URL}/api/v1/hr/leave/balance/{employeeId}"
    response = session.get(url)
    print(f"GET /api/v1/hr/leave/balance/{employeeId} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_leave_request():
    url = f"{BASE_URL}/api/v1/hr/leave/request"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/hr/leave/request -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_leave_requests():
    url = f"{BASE_URL}/api/v1/hr/leave/requests"
    response = session.get(url)
    print(f"GET /api/v1/hr/leave/requests -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_leave_id_approve():
    id = 'string'
    url = f"{BASE_URL}/api/v1/hr/leave/{id}/approve"
    response = session.post(url)
    print(f"POST /api/v1/hr/leave/{id}/approve -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_leave_id_reject():
    id = 'string'
    url = f"{BASE_URL}/api/v1/hr/leave/{id}/reject"
    response = session.post(url)
    print(f"POST /api/v1/hr/leave/{id}/reject -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_payroll():
    url = f"{BASE_URL}/api/v1/hr/payroll"
    response = session.get(url)
    print(f"GET /api/v1/hr/payroll -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_payroll():
    url = f"{BASE_URL}/api/v1/hr/payroll"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/hr/payroll -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_payroll_runId():
    runId = 'string'
    url = f"{BASE_URL}/api/v1/hr/payroll/{runId}"
    response = session.get(url)
    print(f"GET /api/v1/hr/payroll/{runId} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_payroll_runId_approve():
    runId = 'string'
    url = f"{BASE_URL}/api/v1/hr/payroll/{runId}/approve"
    response = session.post(url)
    print(f"POST /api/v1/hr/payroll/{runId}/approve -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_payroll_runId_calculate():
    runId = 'string'
    url = f"{BASE_URL}/api/v1/hr/payroll/{runId}/calculate"
    response = session.post(url)
    print(f"POST /api/v1/hr/payroll/{runId}/calculate -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_hr_payroll_runId_payslip_employeeId():
    runId = 'string'
    employeeId = 'string'
    url = f"{BASE_URL}/api/v1/hr/payroll/{runId}/payslip/{employeeId}"
    response = session.get(url)
    print(f"GET /api/v1/hr/payroll/{runId}/payslip/{employeeId} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_hr_payroll_runId_process():
    runId = 'string'
    url = f"{BASE_URL}/api/v1/hr/payroll/{runId}/process"
    response = session.post(url)
    print(f"POST /api/v1/hr/payroll/{runId}/process -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_inventory_categories():
    url = f"{BASE_URL}/api/v1/inventory/categories"
    response = session.get(url)
    print(f"GET /api/v1/inventory/categories -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_inventory_categories():
    url = f"{BASE_URL}/api/v1/inventory/categories"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/inventory/categories -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_inventory_products():
    url = f"{BASE_URL}/api/v1/inventory/products"
    response = session.get(url)
    print(f"GET /api/v1/inventory/products -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_inventory_products():
    url = f"{BASE_URL}/api/v1/inventory/products"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/inventory/products -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_inventory_products_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/inventory/products/{id}"
    response = session.get(url)
    print(f"GET /api/v1/inventory/products/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_put_api_v1_inventory_products_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/inventory/products/{id}"
    payload = {}
    response = session.put(url, json=payload)
    print(f"PUT /api/v1/inventory/products/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_inventory_stock_adjust():
    url = f"{BASE_URL}/api/v1/inventory/stock/adjust"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/inventory/stock/adjust -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_inventory_stock_alerts():
    url = f"{BASE_URL}/api/v1/inventory/stock/alerts"
    response = session.get(url)
    print(f"GET /api/v1/inventory/stock/alerts -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_inventory_stock_movements():
    url = f"{BASE_URL}/api/v1/inventory/stock/movements"
    response = session.get(url)
    print(f"GET /api/v1/inventory/stock/movements -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_inventory_stock_reserve():
    url = f"{BASE_URL}/api/v1/inventory/stock/reserve"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/inventory/stock/reserve -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_inventory_stock_transfer():
    url = f"{BASE_URL}/api/v1/inventory/stock/transfer"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/inventory/stock/transfer -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_inventory_stock_productId():
    productId = 'string'
    url = f"{BASE_URL}/api/v1/inventory/stock/{productId}"
    response = session.get(url)
    print(f"GET /api/v1/inventory/stock/{productId} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_inventory_warehouses():
    url = f"{BASE_URL}/api/v1/inventory/warehouses"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/inventory/warehouses -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_customers():
    url = f"{BASE_URL}/api/v1/orders/customers"
    response = session.get(url)
    print(f"GET /api/v1/orders/customers -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_orders_customers():
    url = f"{BASE_URL}/api/v1/orders/customers"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/orders/customers -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_customers_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/orders/customers/{id}"
    response = session.get(url)
    print(f"GET /api/v1/orders/customers/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_purchase():
    url = f"{BASE_URL}/api/v1/orders/purchase"
    response = session.get(url)
    print(f"GET /api/v1/orders/purchase -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_orders_purchase():
    url = f"{BASE_URL}/api/v1/orders/purchase"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/orders/purchase -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_purchase_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/orders/purchase/{id}"
    response = session.get(url)
    print(f"GET /api/v1/orders/purchase/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_orders_purchase_id_receive():
    id = 'string'
    url = f"{BASE_URL}/api/v1/orders/purchase/{id}/receive"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/orders/purchase/{id}/receive -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_sales():
    url = f"{BASE_URL}/api/v1/orders/sales"
    response = session.get(url)
    print(f"GET /api/v1/orders/sales -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_orders_sales():
    url = f"{BASE_URL}/api/v1/orders/sales"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/orders/sales -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_sales_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/orders/sales/{id}"
    response = session.get(url)
    print(f"GET /api/v1/orders/sales/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_orders_sales_id_approve():
    id = 'string'
    url = f"{BASE_URL}/api/v1/orders/sales/{id}/approve"
    response = session.post(url)
    print(f"POST /api/v1/orders/sales/{id}/approve -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_put_api_v1_orders_sales_id_status():
    id = 'string'
    url = f"{BASE_URL}/api/v1/orders/sales/{id}/status"
    payload = {}
    response = session.put(url, json=payload)
    print(f"PUT /api/v1/orders/sales/{id}/status -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_summary():
    url = f"{BASE_URL}/api/v1/orders/summary"
    response = session.get(url)
    print(f"GET /api/v1/orders/summary -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_suppliers():
    url = f"{BASE_URL}/api/v1/orders/suppliers"
    response = session.get(url)
    print(f"GET /api/v1/orders/suppliers -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_post_api_v1_orders_suppliers():
    url = f"{BASE_URL}/api/v1/orders/suppliers"
    payload = {}
    response = session.post(url, json=payload)
    print(f"POST /api/v1/orders/suppliers -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

def test_get_api_v1_orders_suppliers_id():
    id = 'string'
    url = f"{BASE_URL}/api/v1/orders/suppliers/{id}"
    response = session.get(url)
    print(f"GET /api/v1/orders/suppliers/{id} -> Status: {response.status_code}")
    try:
        res_json = response.json()
        print(json.dumps(res_json, indent=2))
        return res_json
    except:
        print(response.text)
        return None

if __name__ == '__main__':
    print('Starting API tests...')
    print('--- LOGIN ---')
    login_res = test_post_api_v1_auth_login()
    if login_res and 'access_token' in login_res:
        set_token(login_res['access_token'])
    print('=' * 50)
    # Uncomment endpoints below to test them
    print('--- Testing test_post_api_v1_auth_refresh ---')
    test_post_api_v1_auth_refresh()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_accounts ---')
    test_get_api_v1_finance_accounts()
    print('=' * 50)
    print('--- Testing test_post_api_v1_finance_accounts ---')
    test_post_api_v1_finance_accounts()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_accounts_id ---')
    test_get_api_v1_finance_accounts_id()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_budgets ---')
    test_get_api_v1_finance_budgets()
    print('=' * 50)
    print('--- Testing test_post_api_v1_finance_budgets ---')
    test_post_api_v1_finance_budgets()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_budgets_id ---')
    test_get_api_v1_finance_budgets_id()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_invoices ---')
    test_get_api_v1_finance_invoices()
    print('=' * 50)
    print('--- Testing test_post_api_v1_finance_invoices ---')
    test_post_api_v1_finance_invoices()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_invoices_id ---')
    test_get_api_v1_finance_invoices_id()
    print('=' * 50)
    print('--- Testing test_post_api_v1_finance_invoices_id_payment ---')
    test_post_api_v1_finance_invoices_id_payment()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_journal_entries ---')
    test_get_api_v1_finance_journal_entries()
    print('=' * 50)
    print('--- Testing test_post_api_v1_finance_journal_entries ---')
    test_post_api_v1_finance_journal_entries()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_journal_entries_id ---')
    test_get_api_v1_finance_journal_entries_id()
    print('=' * 50)
    print('--- Testing test_post_api_v1_finance_journal_entries_id_post ---')
    test_post_api_v1_finance_journal_entries_id_post()
    print('=' * 50)
    print('--- Testing test_post_api_v1_finance_journal_entries_id_reverse ---')
    test_post_api_v1_finance_journal_entries_id_reverse()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_reports_ar_aging ---')
    test_get_api_v1_finance_reports_ar_aging()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_reports_balance_sheet ---')
    test_get_api_v1_finance_reports_balance_sheet()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_reports_profit_loss ---')
    test_get_api_v1_finance_reports_profit_loss()
    print('=' * 50)
    print('--- Testing test_get_api_v1_finance_reports_trial_balance ---')
    test_get_api_v1_finance_reports_trial_balance()
    print('=' * 50)
    print('--- Testing test_post_api_v1_finance_tax_calculate ---')
    test_post_api_v1_finance_tax_calculate()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_attendance ---')
    test_get_api_v1_hr_attendance()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_attendance_clock_in ---')
    test_post_api_v1_hr_attendance_clock_in()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_attendance_clock_out ---')
    test_post_api_v1_hr_attendance_clock_out()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_dashboard ---')
    test_get_api_v1_hr_dashboard()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_departments ---')
    test_get_api_v1_hr_departments()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_departments ---')
    test_post_api_v1_hr_departments()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_departments_id ---')
    test_get_api_v1_hr_departments_id()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_employees ---')
    test_get_api_v1_hr_employees()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_employees ---')
    test_post_api_v1_hr_employees()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_employees_id ---')
    test_get_api_v1_hr_employees_id()
    print('=' * 50)
    print('--- Testing test_put_api_v1_hr_employees_id ---')
    test_put_api_v1_hr_employees_id()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_employees_id_terminate ---')
    test_post_api_v1_hr_employees_id_terminate()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_leave_balance_employeeId ---')
    test_get_api_v1_hr_leave_balance_employeeId()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_leave_request ---')
    test_post_api_v1_hr_leave_request()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_leave_requests ---')
    test_get_api_v1_hr_leave_requests()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_leave_id_approve ---')
    test_post_api_v1_hr_leave_id_approve()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_leave_id_reject ---')
    test_post_api_v1_hr_leave_id_reject()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_payroll ---')
    test_get_api_v1_hr_payroll()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_payroll ---')
    test_post_api_v1_hr_payroll()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_payroll_runId ---')
    test_get_api_v1_hr_payroll_runId()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_payroll_runId_approve ---')
    test_post_api_v1_hr_payroll_runId_approve()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_payroll_runId_calculate ---')
    test_post_api_v1_hr_payroll_runId_calculate()
    print('=' * 50)
    print('--- Testing test_get_api_v1_hr_payroll_runId_payslip_employeeId ---')
    test_get_api_v1_hr_payroll_runId_payslip_employeeId()
    print('=' * 50)
    print('--- Testing test_post_api_v1_hr_payroll_runId_process ---')
    test_post_api_v1_hr_payroll_runId_process()
    print('=' * 50)
    print('--- Testing test_get_api_v1_inventory_categories ---')
    test_get_api_v1_inventory_categories()
    print('=' * 50)
    print('--- Testing test_post_api_v1_inventory_categories ---')
    test_post_api_v1_inventory_categories()
    print('=' * 50)
    print('--- Testing test_get_api_v1_inventory_products ---')
    test_get_api_v1_inventory_products()
    print('=' * 50)
    print('--- Testing test_post_api_v1_inventory_products ---')
    test_post_api_v1_inventory_products()
    print('=' * 50)
    print('--- Testing test_get_api_v1_inventory_products_id ---')
    test_get_api_v1_inventory_products_id()
    print('=' * 50)
    print('--- Testing test_put_api_v1_inventory_products_id ---')
    test_put_api_v1_inventory_products_id()
    print('=' * 50)
    print('--- Testing test_post_api_v1_inventory_stock_adjust ---')
    test_post_api_v1_inventory_stock_adjust()
    print('=' * 50)
    print('--- Testing test_get_api_v1_inventory_stock_alerts ---')
    test_get_api_v1_inventory_stock_alerts()
    print('=' * 50)
    print('--- Testing test_get_api_v1_inventory_stock_movements ---')
    test_get_api_v1_inventory_stock_movements()
    print('=' * 50)
    print('--- Testing test_post_api_v1_inventory_stock_reserve ---')
    test_post_api_v1_inventory_stock_reserve()
    print('=' * 50)
    print('--- Testing test_post_api_v1_inventory_stock_transfer ---')
    test_post_api_v1_inventory_stock_transfer()
    print('=' * 50)
    print('--- Testing test_get_api_v1_inventory_stock_productId ---')
    test_get_api_v1_inventory_stock_productId()
    print('=' * 50)
    print('--- Testing test_post_api_v1_inventory_warehouses ---')
    test_post_api_v1_inventory_warehouses()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_customers ---')
    test_get_api_v1_orders_customers()
    print('=' * 50)
    print('--- Testing test_post_api_v1_orders_customers ---')
    test_post_api_v1_orders_customers()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_customers_id ---')
    test_get_api_v1_orders_customers_id()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_purchase ---')
    test_get_api_v1_orders_purchase()
    print('=' * 50)
    print('--- Testing test_post_api_v1_orders_purchase ---')
    test_post_api_v1_orders_purchase()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_purchase_id ---')
    test_get_api_v1_orders_purchase_id()
    print('=' * 50)
    print('--- Testing test_post_api_v1_orders_purchase_id_receive ---')
    test_post_api_v1_orders_purchase_id_receive()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_sales ---')
    test_get_api_v1_orders_sales()
    print('=' * 50)
    print('--- Testing test_post_api_v1_orders_sales ---')
    test_post_api_v1_orders_sales()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_sales_id ---')
    test_get_api_v1_orders_sales_id()
    print('=' * 50)
    print('--- Testing test_post_api_v1_orders_sales_id_approve ---')
    test_post_api_v1_orders_sales_id_approve()
    print('=' * 50)
    print('--- Testing test_put_api_v1_orders_sales_id_status ---')
    test_put_api_v1_orders_sales_id_status()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_summary ---')
    test_get_api_v1_orders_summary()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_suppliers ---')
    test_get_api_v1_orders_suppliers()
    print('=' * 50)
    print('--- Testing test_post_api_v1_orders_suppliers ---')
    test_post_api_v1_orders_suppliers()
    print('=' * 50)
    print('--- Testing test_get_api_v1_orders_suppliers_id ---')
    test_get_api_v1_orders_suppliers_id()
    print('=' * 50)