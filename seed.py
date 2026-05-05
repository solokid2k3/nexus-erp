#!/usr/bin/env python3
"""Seed all ERP tables with realistic data."""
import subprocess, uuid, random, json
from datetime import date, timedelta, datetime

def uid(): return str(uuid.uuid4())
def run_sql(sql):
    subprocess.run(["docker", "compose", "exec", "-T", "postgres",
        "psql", "-U", "erp_user", "-d", "erp_db", "-c", sql],
        capture_output=True, text=True, cwd="/home/sunset/Projects/nexus-erp")

def run_sql_file(path):
    with open(path) as f:
        sql = f.read()
    p = subprocess.run(["docker", "compose", "exec", "-T", "postgres",
        "psql", "-U", "erp_user", "-d", "erp_db"],
        input=sql, capture_output=True, text=True, cwd="/home/sunset/Projects/nexus-erp")
    if p.returncode != 0:
        print(f"ERROR: {p.stderr}")
    else:
        print(p.stdout[-500:] if len(p.stdout)>500 else p.stdout)

# ── HR: Departments ──
depts = []
dept_data = [
    ("ENG","Engineering"), ("FIN","Finance"), ("HR","Human Resources"),
    ("MKT","Marketing"), ("OPS","Operations"), ("SALES","Sales"),
    ("IT","Information Technology"), ("LEGAL","Legal"),
]
for code, name in dept_data:
    did = uid()
    depts.append((did, code, name))

# ── HR: Employees ──
first_names = ["James","Mary","Robert","Patricia","John","Jennifer","Michael","Linda",
    "David","Elizabeth","William","Barbara","Richard","Susan","Joseph","Jessica",
    "Thomas","Sarah","Charles","Karen","Daniel","Nancy","Matthew","Lisa"]
last_names = ["Smith","Johnson","Williams","Brown","Jones","Garcia","Miller","Davis",
    "Rodriguez","Martinez","Anderson","Taylor","Thomas","Jackson","White","Harris"]
emps = []
for i in range(30):
    eid = uid()
    fn = first_names[i % len(first_names)]
    ln = last_names[i % len(last_names)]
    dept = random.choice(depts)
    hd = date(2020,1,1) + timedelta(days=random.randint(0,1800))
    salary = random.randint(3500,12000) * 100
    emps.append((eid, f"EMP{i+1:04d}", fn, ln,
        f"{fn.lower()}.{ln.lower()}{i}@nexus.com", dept[0],
        random.choice(["Software Engineer","Accountant","HR Specialist","Sales Rep",
            "DevOps Engineer","Marketing Analyst","Operations Manager","Legal Counsel"]),
        hd.isoformat(), salary))

# ── Inventory: Categories ──
cats = []
cat_data = ["Electronics","Furniture","Office Supplies","Raw Materials","Packaging","Software Licenses"]
for name in cat_data:
    cats.append((uid(), name))

# ── Inventory: Warehouses ──
whs = []
wh_data = [("WH-MAIN","Main Warehouse","Bangkok"),("WH-NORTH","North Hub","Chiang Mai"),
    ("WH-SOUTH","South Hub","Hat Yai")]
for code,name,city in wh_data:
    whs.append((uid(), code, name, city))

# ── Inventory: Products ──
prods = []
prod_data = [
    ("LAP-001","Laptop Pro 15",120000,150000),("MON-001","27in 4K Monitor",35000,45000),
    ("KEY-001","Mechanical Keyboard",8000,12000),("MOU-001","Wireless Mouse",2500,4000),
    ("DSK-001","Standing Desk",45000,65000),("CHR-001","Ergonomic Chair",38000,55000),
    ("PAP-001","A4 Paper Ream",300,500),("PEN-001","Ballpoint Pen Box",150,300),
    ("CAB-001","Cat6 Cable 10m",800,1500),("SSD-001","1TB NVMe SSD",9000,13000),
    ("RAM-001","32GB DDR5 Kit",12000,18000),("HUB-001","USB-C Hub 7in1",3000,5000),
    ("WBC-001","HD Webcam",4500,7000),("HST-001","USB Headset",3500,5500),
    ("UPS-001","1500VA UPS",15000,22000),
]
for sku,name,cost,price in prod_data:
    prods.append((uid(), sku, name, random.choice([c[0] for c in cats]), cost, price))

# ── Orders: Customers ──
custs = []
cust_data = [
    ("CUST-001","Acme Corp","John Doe"), ("CUST-002","GlobalTech Ltd","Jane Wu"),
    ("CUST-003","SiamSoft Co","Somchai P."), ("CUST-004","BrightStar Inc","Alice Chen"),
    ("CUST-005","Metro Solutions","Bob Park"), ("CUST-006","CloudNine Systems","Maria L."),
    ("CUST-007","DataPrime Analytics","Tom R."), ("CUST-008","GreenLeaf Trading","Sara K."),
]
for code,name,contact in cust_data:
    custs.append((uid(), code, name, contact, f"{contact.split()[0].lower()}@{name.lower().replace(' ','')}"))

# ── Orders: Suppliers ──
supps = []
supp_data = [
    ("SUP-001","TechSource Global","Wei Zhang"), ("SUP-002","OfficeMax Supply","Rita S."),
    ("SUP-003","FurniPro Co","Anon T."), ("SUP-004","RawMat Industries","Kumar P."),
    ("SUP-005","PackRight Ltd","Lin Chen"),
]
for code,name,contact in supp_data:
    supps.append((uid(), code, name, contact))

# ── Build SQL ──
lines = ["BEGIN;"]

# Departments
for d in depts:
    lines.append(f"INSERT INTO hr.departments (id,code,name) VALUES ('{d[0]}','{d[1]}','{d[2]}');")

# Employees
for e in emps:
    lines.append(f"INSERT INTO hr.employees (id,employee_number,first_name,last_name,email,department_id,position_title,hire_date,base_salary_cents,status) "
        f"VALUES ('{e[0]}','{e[1]}','{e[2]}','{e[3]}','{e[4]}','{e[5]}','{e[6]}','{e[7]}',{e[8]},'ACTIVE');")

# Set dept managers
for i,d in enumerate(depts):
    if i < len(emps):
        lines.append(f"UPDATE hr.departments SET manager_id='{emps[i][0]}' WHERE id='{d[0]}';")

# Leave balances
leave_types_sql = "SELECT id FROM hr.leave_types"
for e in emps:
    lines.append(f"INSERT INTO hr.leave_balances (employee_id,leave_type_id,fiscal_year,total_allocated,used,pending) "
        f"SELECT '{e[0]}', id, '2026', default_days_per_year, {random.randint(0,5)}, {random.randint(0,2)} FROM hr.leave_types WHERE is_active=true;")

# Leave requests (recent ones)
for i in range(20):
    e = random.choice(emps)
    sd = date(2026,1,1) + timedelta(days=random.randint(0,120))
    days = random.randint(1,5)
    ed = sd + timedelta(days=days)
    status = random.choice(["PENDING","APPROVED","APPROVED","REJECTED"])
    lines.append(f"INSERT INTO hr.leave_requests (employee_id,leave_type_id,start_date,end_date,total_days,status,reason) "
        f"SELECT '{e[0]}', (SELECT id FROM hr.leave_types ORDER BY random() LIMIT 1), '{sd}','{ed}',{days},'{status}','Personal reasons';")

# Attendance (last 30 days)
for e in emps[:15]:
    for d in range(30):
        ad = date(2026,4,5) + timedelta(days=d)
        if ad.weekday() >= 5: continue
        ci = f"{ad} {8+random.randint(0,1)}:{random.randint(0,59):02d}:00+07"
        co = f"{ad} {17+random.randint(0,2)}:{random.randint(0,59):02d}:00+07"
        hrs = round(random.uniform(7.5,9.5),2)
        ot = round(max(0, hrs-8),2)
        st = random.choice(["PRESENT","PRESENT","PRESENT","LATE"])
        lines.append(f"INSERT INTO hr.attendance (employee_id,attendance_date,clock_in,clock_out,hours_worked,overtime_hours,status) "
            f"VALUES ('{e[0]}','{ad}','{ci}','{co}',{hrs},{ot},'{st}') ON CONFLICT DO NOTHING;")

# Payroll runs
pr_ids = []
for m in range(1,5):
    pid = uid()
    pr_ids.append(pid)
    ps = date(2026,m,1)
    pe = date(2026,m,28)
    pd = date(2026,m,28)
    lines.append(f"INSERT INTO hr.payroll_runs (id,run_number,period,'MONTHLY','PROCESSED','{ps}','{pe}','{pd}',0,0,0,0,'USD',{len(emps)}) "
        f"VALUES ('{pid}','PR-2026-{m:02d}','2026-{m:02d}','MONTHLY','PROCESSED','{ps}','{pe}','{pd}',0,0,0,0,'USD',{len(emps)});")

# Fix payroll SQL
lines2 = []
for l in lines:
    if "hr.payroll_runs" in l and "VALUES" in l and l.count("VALUES") > 1:
        pid = pr_ids[len([x for x in lines2 if "payroll_runs" in x])]
        continue
    lines2.append(l)

# Actually let me redo payroll properly
lines_final = [l for l in lines if "payroll_runs" not in l]
for m in range(1,5):
    pid = pr_ids[m-1]
    ps = date(2026,m,1)
    pe = date(2026,m,28)
    lines_final.append(f"INSERT INTO hr.payroll_runs (id,run_number,period,pay_frequency,status,period_start,period_end,payment_date,total_gross_cents,total_deductions_cents,total_net_cents,total_employer_cost_cents,currency,employee_count) "
        f"VALUES ('{pid}','PR-2026-{m:02d}','2026-{m:02d}','MONTHLY','PROCESSED','{ps}','{pe}','{pe}',{len(emps)*700000},{len(emps)*150000},{len(emps)*550000},{len(emps)*100000},'USD',{len(emps)});")

# Pay slips
for pid in pr_ids:
    for e in emps:
        sal = e[8]
        tax = int(sal * 0.1)
        ss = int(sal * 0.05)
        ded = tax + ss
        net = sal - ded
        lines_final.append(f"INSERT INTO hr.pay_slips (payroll_run_id,employee_id,base_salary_cents,total_earnings_cents,total_deductions_cents,net_pay_cents,income_tax_cents,social_security_cents,regular_hours) "
            f"VALUES ('{pid}','{e[0]}',{sal},{sal},{ded},{net},{tax},{ss},160);")

# Categories
for c in cats:
    lines_final.append(f"INSERT INTO inventory.categories (id,name) VALUES ('{c[0]}','{c[1]}');")

# Warehouses
for w in whs:
    lines_final.append(f"INSERT INTO inventory.warehouses (id,code,name,city,status,capacity_units) VALUES ('{w[0]}','{w[1]}','{w[2]}','{w[3]}','ACTIVE',10000);")

# Products
for p in prods:
    lines_final.append(f"INSERT INTO inventory.products (id,sku,name,category_id,unit_cost_cents,selling_price_cents,status,reorder_point,reorder_quantity) "
        f"VALUES ('{p[0]}','{p[1]}','{p[2]}','{p[3]}',{p[4]},{p[5]},'ACTIVE',{random.randint(5,20)},{random.randint(50,200)});")

# Stock levels
for p in prods:
    for w in whs:
        qty = random.randint(10,500)
        res = random.randint(0, min(qty, 20))
        lines_final.append(f"INSERT INTO inventory.stock_levels (product_id,warehouse_id,quantity_on_hand,quantity_reserved,bin_location) "
            f"VALUES ('{p[0]}','{w[0]}',{qty},{res},'A-{random.randint(1,10)}-{random.randint(1,5)}');")

# Stock movements
for _ in range(40):
    p = random.choice(prods)
    w = random.choice(whs)
    qty_change = random.choice([-5,-10,20,50,100])
    before = random.randint(50,300)
    lines_final.append(f"INSERT INTO inventory.stock_movements (product_id,warehouse_id,reason,quantity_change,quantity_before,quantity_after,reference_id,created_at) "
        f"VALUES ('{p[0]}','{w[0]}','{random.choice(['PURCHASE','SALE','ADJUSTMENT','TRANSFER'])}',{qty_change},{before},{before+qty_change},'REF-{random.randint(1000,9999)}',NOW()-interval '{random.randint(0,60)} days');")

# Customers
for c in custs:
    lines_final.append(f"INSERT INTO orders.customers (id,code,company_name,contact_name,email,status,tier,payment_terms,credit_limit_cents) "
        f"VALUES ('{c[0]}','{c[1]}','{c[2]}','{c[3]}','{c[4]}','ACTIVE','{random.choice(['BRONZE','SILVER','GOLD'])}','NET30',{random.randint(50,500)*10000});")

# Suppliers
for s in supps:
    lines_final.append(f"INSERT INTO orders.suppliers (id,code,company_name,contact_name,status,rating) "
        f"VALUES ('{s[0]}','{s[1]}','{s[2]}','{s[3]}','ACTIVE','{random.choice(['A','B','A','A'])}');")

# Sales orders
so_ids = []
for i in range(15):
    soid = uid()
    so_ids.append(soid)
    c = random.choice(custs)
    od = date(2026,1,1) + timedelta(days=random.randint(0,120))
    st = random.choice(["CONFIRMED","SHIPPED","DELIVERED","DRAFT"])
    subtotal = 0
    order_lines = []
    for ln in range(random.randint(1,4)):
        p = random.choice(prods)
        qty = random.randint(1,10)
        lt = p[5] * qty
        subtotal += lt
        order_lines.append((uid(), soid, ln+1, p[0], p[2], p[1], qty, p[5], lt))
    tax = int(subtotal * 0.07)
    ship = random.choice([0,5000,10000])
    total = subtotal + tax + ship
    lines_final.append(f"INSERT INTO orders.sales_orders (id,order_number,customer_id,status,subtotal_cents,tax_amount_cents,shipping_cost_cents,total_amount_cents,order_date) "
        f"VALUES ('{soid}','SO-{2026}{i+1:04d}','{c[0]}','{st}',{subtotal},{tax},{ship},{total},'{od}');")
    for ol in order_lines:
        lines_final.append(f"INSERT INTO orders.order_lines (id,order_id,line_number,product_id,product_name,sku,quantity_ordered,unit_price_cents,line_total_cents) "
            f"VALUES ('{ol[0]}','{ol[1]}',{ol[2]},'{ol[3]}','{ol[4]}','{ol[5]}',{ol[6]},{ol[7]},{ol[8]});")

# Purchase orders
for i in range(8):
    poid = uid()
    s = random.choice(supps)
    od = date(2026,1,1) + timedelta(days=random.randint(0,120))
    ed = od + timedelta(days=random.randint(7,30))
    st = random.choice(["APPROVED","RECEIVED","DRAFT"])
    subtotal = 0
    po_lines = []
    for ln in range(random.randint(1,3)):
        p = random.choice(prods)
        qty = random.randint(20,200)
        lt = p[4] * qty
        subtotal += lt
        po_lines.append((uid(), poid, ln+1, p[0], p[2], qty, p[4], lt))
    tax = int(subtotal * 0.07)
    total = subtotal + tax
    lines_final.append(f"INSERT INTO orders.purchase_orders (id,po_number,supplier_id,status,subtotal_cents,tax_amount_cents,total_amount_cents,order_date,expected_delivery) "
        f"VALUES ('{poid}','PO-{2026}{i+1:04d}','{s[0]}','{st}',{subtotal},{tax},{total},'{od}','{ed}');")
    for pl in po_lines:
        lines_final.append(f"INSERT INTO orders.purchase_order_lines (id,po_id,line_number,product_id,product_name,quantity_ordered,unit_cost_cents,line_total_cents) "
            f"VALUES ('{pl[0]}','{pl[1]}',{pl[2]},'{pl[3]}','{pl[4]}',{pl[5]},{pl[6]},{pl[7]});")

# Finance: Journal entries
je_ids = []
acct_sql_cache = {}
for i in range(10):
    jeid = uid()
    je_ids.append(jeid)
    ed = date(2026,1,1) + timedelta(days=random.randint(0,120))
    amt = random.randint(10000,500000)
    lines_final.append(f"INSERT INTO finance.journal_entries (id,entry_number,entry_date,description,status,total_debit_cents,total_credit_cents,fiscal_period) "
        f"VALUES ('{jeid}','JE-2026-{i+1:04d}','{ed}','Monthly entry #{i+1}','POSTED',{amt},{amt},'2026-{ed.month:02d}');")
    lines_final.append(f"INSERT INTO finance.journal_lines (journal_entry_id,line_number,account_id,debit_amount_cents,description) "
        f"SELECT '{jeid}', 1, id, {amt}, 'Debit entry' FROM finance.accounts WHERE account_number='6000';")
    lines_final.append(f"INSERT INTO finance.journal_lines (journal_entry_id,line_number,account_id,credit_amount_cents,description) "
        f"SELECT '{jeid}', 2, id, {amt}, 'Credit entry' FROM finance.accounts WHERE account_number='1010';")

# Finance: Invoices
for i in range(12):
    invid = uid()
    c = random.choice(custs)
    id_ = date(2026,1,1) + timedelta(days=random.randint(0,120))
    dd = id_ + timedelta(days=30)
    st = random.choice(["SENT","PAID","PAID","OVERDUE"])
    subtotal = random.randint(50000,2000000)
    tax = int(subtotal * 0.07)
    total = subtotal + tax
    paid = total if st == "PAID" else (0 if st == "SENT" else random.randint(0, total))
    due = total - paid
    lines_final.append(f"INSERT INTO finance.invoices (id,invoice_number,invoice_type,customer_id,customer_name,status,subtotal_cents,tax_amount_cents,total_amount_cents,amount_paid_cents,amount_due_cents,invoice_date,due_date,payment_terms) "
        f"VALUES ('{invid}','INV-2026-{i+1:04d}','SALES','{c[0]}','{c[2]}','{st}',{subtotal},{tax},{total},{paid},{due},'{id_}','{dd}','NET30');")
    for ln in range(random.randint(1,3)):
        p = random.choice(prods)
        qty = random.randint(1,10)
        lp = p[5]
        lt = lp * qty
        ltax = int(lt * 0.07)
        lines_final.append(f"INSERT INTO finance.invoice_lines (invoice_id,line_number,description,product_id,quantity,unit_price_cents,tax_rate,tax_amount_cents,line_total_cents) "
            f"VALUES ('{invid}',{ln+1},'{p[2]}','{p[0]}',{qty},{lp},0.07,{ltax},{lt});")
    if st == "PAID":
        lines_final.append(f"INSERT INTO finance.payments (invoice_id,amount_cents,payment_method,reference_number,payment_date) "
            f"VALUES ('{invid}',{total},'{random.choice(['BANK_TRANSFER','CREDIT_CARD','CHECK'])}','PAY-{random.randint(10000,99999)}','{dd - timedelta(days=random.randint(0,10))}');")

# Finance: Budgets
for dept in depts[:4]:
    bid = uid()
    total_b = random.randint(500000,5000000)
    actual = int(total_b * random.uniform(0.3,0.9))
    lines_final.append(f"INSERT INTO finance.budgets (id,name,fiscal_year,department_id,status,total_budget_cents,total_actual_cents,variance_cents) "
        f"VALUES ('{bid}','{dept[2]} Budget 2026','2026','{dept[0]}','APPROVED',{total_b},{actual},{total_b-actual});")
    for q in range(1,5):
        qb = total_b // 4
        qa = int(qb * random.uniform(0.2,1.1))
        lines_final.append(f"INSERT INTO finance.budget_lines (budget_id,account_id,period,budgeted_cents,actual_cents,variance_cents) "
            f"SELECT '{bid}', id, '2026-Q{q}', {qb}, {qa}, {qb-qa} FROM finance.accounts WHERE account_number='6000';")

lines_final.append("COMMIT;")

# Write to file
sql_path = "/home/sunset/Projects/nexus-erp/db/seed_data.sql"
with open(sql_path, "w") as f:
    f.write("\n".join(lines_final))

print(f"Generated {len(lines_final)} SQL statements")
print(f"Written to {sql_path}")

# Execute
run_sql_file(sql_path)
print("Done!")
