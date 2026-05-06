# Plan: Nexus ERP Web Client

## Summary
Build a full-featured web client for the Nexus ERP system using the existing Next.js 16 scaffold at `web-client/`. The application provides dashboard views and CRUD data tables for all 4 ERP modules (HR, Inventory, Orders, Finance), authenticated via JWT against the gateway running at `localhost:8080`. Design follows the Cal.com-inspired design system documented in `design.md`.

## User Story
As an ERP operator,
I want a browser-based dashboard to view and manage HR, Inventory, Orders, and Finance data,
So that I can access the system from any device without installing the JavaFX desktop client.

## Problem → Solution
**Current state**: The only client is a JavaFX desktop app that requires local Java installation.
**Desired state**: A responsive web dashboard at `localhost:3000` that mirrors all desktop client functionality with a modern, premium UI.

## Metadata
- **Complexity**: Large
- **Source PRD**: N/A
- **PRD Phase**: N/A
- **Estimated Files**: ~27 new files
- **Tech Stack**: Next.js 16, React 19, TypeScript, SWR, Chart.js, Vanilla CSS, react-hot-toast

---

## UX Design

### Before
```
┌───────────────────────────────────────────┐
│  JavaFX Desktop App (requires JDK 21)     │
│  ┌──────────┬────────────────────────────┐│
│  │ Sidebar   │  Data Table               ││
│  │ Dashboard │  (employees, products...) ││
│  │ Employees │                           ││
│  │ ...       │                           ││
│  └──────────┴────────────────────────────┘│
└───────────────────────────────────────────┘
```

### After
```
┌───────────────────────────────────────────┐
│  Browser — http://localhost:3000          │
│  ┌──Top Nav─────────────────────────────┐│
│  │ NEXUS ERP   HR  INV  ORD  FIN  [User]│
│  └──────────────────────────────────────┘│
│  ┌──Sidebar──┬──Content─────────────────┐│
│  │ Dashboard │ ┌─Stat Cards──────────┐  ││
│  │ Employees │ │ 30 │ 8 │ 20 │ 4    │  ││
│  │ Depts     │ └─────────────────────┘  ││
│  │ Leave     │ ┌─Data Table──────────┐  ││
│  │ Attendance│ │ Name │Email │Status │  ││
│  │ Payroll   │ │ ─────┤──────┤───────│  ││
│  └───────────┴─┴─────────────────────┘──┘│
│  ┌──Dark Footer ────────────────────────┐│
│  │ © 2026 Nexus ERP                     ││
│  └──────────────────────────────────────┘│
└───────────────────────────────────────────┘
```

### Interaction Changes
| Touchpoint | Before | After | Notes |
|---|---|---|---|
| Launch | Open JavaFX app | Navigate to `localhost:3000` | No install required |
| Auth | Login dialog → token stored in memory | Login page → JWT in localStorage | Same API endpoint |
| Navigation | Sidebar tabs (per module) | Top-level module tabs + sidebar sub-nav | Unified shell |
| Data tables | JavaFX TableView | HTML table with sort/search | Same columns |
| Create forms | JavaFX Dialog | Modal overlay | Same fields |
| Dashboards | StatCard + skeleton loader | StatCard + Chart.js charts | Enhanced with charts |

---

## Mandatory Reading

Files that MUST be read before implementing:

| Priority | File | Lines | Why |
|---|---|---|---|
| P0 | [design.md](file:///home/sunset/Projects/nexus-erp/design.md) | all | Design system tokens, colors, typography, spacing |
| P0 | [auth.go](file:///home/sunset/Projects/nexus-erp/gateway/handlers/auth.go) | 36-75 | Login flow, JWT claims structure, demo users |
| P0 | [middleware.go](file:///home/sunset/Projects/nexus-erp/gateway/middleware/middleware.go) | 16-29, 45-97 | CORS config (allows `*`), JWT auth header format |
| P0 | [main.go](file:///home/sunset/Projects/nexus-erp/gateway/main.go) | 180-296 | All API route paths the client must call |
| P1 | [EmployeesView.java](file:///home/sunset/Projects/nexus-erp/desktop-client/hr-client/src/main/java/com/erp/hr/EmployeesView.java) | all | Data table columns and field names |
| P1 | [HRDashboard.java](file:///home/sunset/Projects/nexus-erp/desktop-client/hr-client/src/main/java/com/erp/hr/HRDashboard.java) | all | Dashboard stat card pattern |
| P1 | [HRMainView.java](file:///home/sunset/Projects/nexus-erp/desktop-client/hr-client/src/main/java/com/erp/hr/HRMainView.java) | all | Navigation structure |
| P2 | [package.json](file:///home/sunset/Projects/nexus-erp/web-client/package.json) | all | Already has Next.js 16, React 19, SWR, Chart.js |
| P2 | [tsconfig.json](file:///home/sunset/Projects/nexus-erp/web-client/tsconfig.json) | all | Path alias `@/*` → `./src/*` |

---

## Patterns to Mirror

### DESIGN_TOKENS
```css
/* SOURCE: design.md — Colors */
--color-primary: #111111;
--color-primary-active: #242424;
--color-canvas: #ffffff;
--color-surface-soft: #f8f9fa;
--color-surface-card: #f5f5f5;
--color-surface-strong: #e5e7eb;
--color-surface-dark: #101010;
--color-hairline: #e5e7eb;
--color-ink: #111111;
--color-body: #374151;
--color-muted: #6b7280;
--color-on-dark: #ffffff;
--color-on-dark-soft: #a1a1aa;
--color-success: #10b981;
--color-warning: #f59e0b;
--color-error: #ef4444;
--color-brand-accent: #3b82f6;

/* Typography */
font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
/* Display: 600 weight, negative letter-spacing */
/* Body: 400 weight, 0 letter-spacing */

/* Spacing: 4px base unit */
/* Radius: 8px buttons, 12px cards, 16px hero, pill for badges */
```

### API_CLIENT_PATTERN
```typescript
// SOURCE: desktop-client ApiClient.java — adapted for web
// All list endpoints return raw JSON arrays: [{...}, {...}]
// Auth: POST /api/v1/auth/login → { access_token, refresh_token, expires_in }
// Protected: Authorization: Bearer <token>
// Base URL: http://localhost:8080/api/v1
```

### DATA_TABLE_PATTERN
```typescript
// SOURCE: desktop-client EmployeesView.java
// Pattern: fetch array → map to table rows → search filter → create modal
// Fields follow snake_case from API (first_name, last_name, department_name, etc.)
```

### NAVIGATION_STRUCTURE
```
// SOURCE: desktop-client *MainView.java files
HR:        Dashboard, Employees, Departments, Leave, Attendance, Payroll
Inventory: Dashboard, Products, Stock, Categories, Warehouses
Orders:    Dashboard, Sales Orders, Purchase Orders, Customers, Suppliers
Finance:   Dashboard, Accounts, Journals, Invoices, Reports, Budgets, Tax
```

---

## Files to Change

### Infrastructure Layer
| File | Action | Justification |
|---|---|---|
| `web-client/src/lib/api.ts` | CREATE | Typed API client with JWT auth |
| `web-client/src/lib/auth.ts` | CREATE | Auth context provider + login/logout |
| `web-client/src/lib/types.ts` | CREATE | TypeScript interfaces for all API entities |

### Component Layer
| File | Action | Justification |
|---|---|---|
| `web-client/src/components/Sidebar.tsx` | CREATE | Module sub-navigation sidebar |
| `web-client/src/components/StatCard.tsx` | CREATE | Dashboard stat card (count + icon) |
| `web-client/src/components/DataTable.tsx` | CREATE | Reusable sortable/searchable table |
| `web-client/src/components/Modal.tsx` | CREATE | Reusable modal for create forms |
| `web-client/src/components/StatusBadge.tsx` | CREATE | Color-coded status pill |
| `web-client/src/components/TopNav.tsx` | CREATE | Top navigation bar with module tabs |
| `web-client/src/components/Footer.tsx` | CREATE | Dark footer matching design system |

### Page Layer
| File | Action | Justification |
|---|---|---|
| `web-client/src/app/globals.css` | CREATE | Design system CSS tokens + base styles |
| `web-client/src/app/layout.tsx` | UPDATE | Add fonts, globals.css, auth provider |
| `web-client/src/app/page.tsx` | UPDATE | Redirect to /login or /hr |
| `web-client/src/app/login/page.tsx` | CREATE | Login page |
| `web-client/src/app/(dashboard)/layout.tsx` | CREATE | Dashboard shell (TopNav + Sidebar + Footer) |
| `web-client/src/app/(dashboard)/hr/page.tsx` | CREATE | HR dashboard |
| `web-client/src/app/(dashboard)/hr/employees/page.tsx` | CREATE | Employees table |
| `web-client/src/app/(dashboard)/hr/departments/page.tsx` | CREATE | Departments table |
| `web-client/src/app/(dashboard)/hr/leave/page.tsx` | CREATE | Leave requests table |
| `web-client/src/app/(dashboard)/hr/attendance/page.tsx` | CREATE | Attendance table |
| `web-client/src/app/(dashboard)/hr/payroll/page.tsx` | CREATE | Payroll runs table |
| `web-client/src/app/(dashboard)/inventory/page.tsx` | CREATE | Inventory dashboard |
| `web-client/src/app/(dashboard)/inventory/products/page.tsx` | CREATE | Products table |
| `web-client/src/app/(dashboard)/inventory/stock/page.tsx` | CREATE | Stock levels table |
| `web-client/src/app/(dashboard)/inventory/categories/page.tsx` | CREATE | Categories table |
| `web-client/src/app/(dashboard)/inventory/warehouses/page.tsx` | CREATE | Warehouses table |
| `web-client/src/app/(dashboard)/orders/page.tsx` | CREATE | Orders dashboard |
| `web-client/src/app/(dashboard)/orders/sales/page.tsx` | CREATE | Sales orders table |
| `web-client/src/app/(dashboard)/orders/purchases/page.tsx` | CREATE | Purchase orders table |
| `web-client/src/app/(dashboard)/orders/customers/page.tsx` | CREATE | Customers table |
| `web-client/src/app/(dashboard)/orders/suppliers/page.tsx` | CREATE | Suppliers table |
| `web-client/src/app/(dashboard)/finance/page.tsx` | CREATE | Finance dashboard |
| `web-client/src/app/(dashboard)/finance/accounts/page.tsx` | CREATE | Accounts table |
| `web-client/src/app/(dashboard)/finance/journals/page.tsx` | CREATE | Journal entries table |
| `web-client/src/app/(dashboard)/finance/invoices/page.tsx` | CREATE | Invoices table |
| `web-client/src/app/(dashboard)/finance/budgets/page.tsx` | CREATE | Budgets table |

### Config
| File | Action | Justification |
|---|---|---|
| `web-client/next.config.ts` | UPDATE | Add API rewrite proxy to avoid CORS in dev |

## NOT Building
- User registration / sign-up (use demo users from auth.go)
- Role-based access control UI (all users see all modules)
- File upload / export
- Real-time WebSocket updates
- Mobile-optimized responsive layouts (desktop-first)
- Dark mode toggle
- Reports page charts (placeholder only — reports endpoints return stubs)
- Tax calculator form (endpoint is a stub)

---

## Step-by-Step Tasks

### Task 1: Install Dependencies & Configure
- **ACTION**: Run `npm install` in `web-client/`, update `next.config.ts` to proxy API
- **IMPLEMENT**: Add `rewrites()` in next.config.ts to proxy `/api/*` → `http://localhost:8080/api/*`
- **GOTCHA**: CORS is already `*` on the gateway, but the proxy avoids cookie/preflight issues in dev
- **VALIDATE**: `npm run dev` starts without errors

### Task 2: Design System — globals.css
- **ACTION**: Create `src/app/globals.css` with all design tokens from `design.md`
- **IMPLEMENT**: CSS custom properties for all colors, typography, spacing, border-radius. Base element styles. Utility classes for layout. Component classes for buttons, cards, inputs, badges, tables.
- **MIRROR**: DESIGN_TOKENS pattern — use exact hex values from design.md
- **GOTCHA**: Use Inter from Google Fonts (Cal Sans is proprietary). Weight 600 with -0.04em tracking for display headings.
- **VALIDATE**: Import in layout.tsx, verify font loads in browser

### Task 3: API Client & Auth
- **ACTION**: Create `src/lib/api.ts`, `src/lib/auth.ts`, `src/lib/types.ts`
- **IMPLEMENT**:
  - `api.ts`: `apiFetch(path, options)` wrapper that adds Bearer token, handles 401 → redirect to login. `useApiSWR(path)` hook wrapping SWR with auth headers.
  - `auth.ts`: React context with `login(username, password)`, `logout()`, `user` (decoded JWT claims). Store tokens in localStorage.
  - `types.ts`: Interfaces for Employee, Department, LeaveRequest, Attendance, PayrollRun, Product, Category, Warehouse, StockLevel, Customer, Supplier, SalesOrder, PurchaseOrder, Account, JournalEntry, Invoice, Budget.
- **MIRROR**: API_CLIENT_PATTERN — endpoints return raw arrays
- **IMPORTS**: `swr`, `react`, `react-hot-toast`
- **GOTCHA**: JWT claims: `{ sub, name, email, role, exp, iat }`. Token expires in 24h.
- **VALIDATE**: Login with `admin/admin123`, verify token stored, verify authenticated fetch returns data

### Task 4: Shared Components
- **ACTION**: Create TopNav, Sidebar, Footer, StatCard, DataTable, Modal, StatusBadge
- **IMPLEMENT**:
  - `TopNav`: NEXUS ERP logo, module tabs (HR, Inventory, Orders, Finance), user dropdown with logout
  - `Sidebar`: Sub-navigation for active module (matches NAVIGATION_STRUCTURE pattern)
  - `Footer`: Dark surface `#101010`, copyright text
  - `StatCard`: Icon + label + value + accent color, loading skeleton state
  - `DataTable`: Generic `<T>` component — column definitions, search filter, loading state, empty state, row click
  - `Modal`: Overlay with form content, cancel/submit buttons
  - `StatusBadge`: Pill badge with color mapping (ACTIVE→green, PENDING→yellow, TERMINATED→red, DRAFT→gray)
- **MIRROR**: DESIGN_TOKENS for all styling
- **GOTCHA**: DataTable must handle the raw array format. Use `'use client'` on interactive components.
- **VALIDATE**: Render each component in isolation

### Task 5: Login Page
- **ACTION**: Create `src/app/login/page.tsx`
- **IMPLEMENT**: Clean centered login form with username/password fields, submit button, error toast on failure. Auto-redirect to `/hr` on success.
- **MIRROR**: DESIGN_TOKENS — white canvas, black primary button, Inter font
- **GOTCHA**: Must be `'use client'`. Check localStorage for existing valid token on mount → skip login if valid.
- **VALIDATE**: Login with `admin/admin123` → redirects to HR dashboard

### Task 6: Dashboard Layout Shell
- **ACTION**: Create `src/app/(dashboard)/layout.tsx`
- **IMPLEMENT**: Authenticated layout with TopNav at top, Sidebar on left, main content area, Footer at bottom. Redirect to `/login` if no token. Parse current path to determine active module and sidebar items.
- **MIRROR**: NAVIGATION_STRUCTURE pattern for sidebar items per module
- **GOTCHA**: Use Next.js route groups `(dashboard)` so login page doesn't get the shell. Module detection from `pathname.split('/')[1]`.
- **VALIDATE**: Navigate between modules, sidebar updates correctly

### Task 7: HR Module Pages
- **ACTION**: Create dashboard + 5 data table pages for HR
- **IMPLEMENT**:
  - Dashboard: 4 stat cards (Employees, Departments, Pending Leaves, Payroll Runs) with counts from respective list endpoints. Optional Chart.js bar chart for attendance.
  - Employees: DataTable with columns [Name, Email, Department, Position, Status]. Create modal with first_name, last_name, email, position fields.
  - Departments: DataTable with columns [Code, Name, Manager].
  - Leave: DataTable with columns [Employee, Type, Start Date, End Date, Status].
  - Attendance: DataTable with columns [Employee, Date, Clock In, Clock Out, Status].
  - Payroll: DataTable with columns [Run Number, Period Start, Period End, Status, Total].
- **MIRROR**: DATA_TABLE_PATTERN — field names from API
- **IMPORTS**: `swr`, chart.js components
- **GOTCHA**: API returns `position` key (not `title`) for employees. Leave type comes from joined table. Attendance clock_in/clock_out can be null.
- **VALIDATE**: All 6 pages render with seeded data

### Task 8: Inventory Module Pages
- **ACTION**: Create dashboard + 4 data table pages for Inventory
- **IMPLEMENT**:
  - Dashboard: Stat cards (Products, Categories, Warehouses, Stock Items).
  - Products: DataTable [SKU, Name, Brand, UOM, Price, Status].
  - Stock: DataTable [Product, SKU, Warehouse, On Hand, Reserved, Bin Location]. Endpoint: `/inventory/stock`.
  - Categories: DataTable [Name, Description, Parent].
  - Warehouses: DataTable [Code, Name, City, Country, Status].
- **GOTCHA**: Products endpoint default filters by `status=ACTIVE`. description/brand can be null.
- **VALIDATE**: All 5 pages render with seeded data

### Task 9: Orders Module Pages
- **ACTION**: Create dashboard + 4 data table pages for Orders
- **IMPLEMENT**:
  - Dashboard: Stat cards (Sales Orders, Purchase Orders, Customers, Suppliers).
  - Sales Orders: DataTable [Order #, Customer, Status, Total, Date]. Endpoint: `/orders/sales`.
  - Purchase Orders: DataTable [PO #, Supplier, Status, Total, Delivery Date]. Endpoint: `/orders/purchases`.
  - Customers: DataTable [Code, Company, Contact, Email, City, Status].
  - Suppliers: DataTable [Code, Company, Contact, Email, City, Country].
- **GOTCHA**: Purchase orders use `/orders/purchases` route alias. total_amount_cents needs cents-to-dollars formatting.
- **VALIDATE**: All 5 pages render with seeded data

### Task 10: Finance Module Pages
- **ACTION**: Create dashboard + 4 data table pages for Finance (skip Reports/Tax — stub endpoints)
- **IMPLEMENT**:
  - Dashboard: Stat cards (Accounts, Journal Entries, Invoices, Budgets).
  - Accounts: DataTable [Account #, Name, Type, Category, Active].
  - Journals: DataTable [Entry #, Date, Status, Amount, Created].
  - Invoices: DataTable [Invoice #, Type, Entity, Status, Amount, Due Date].
  - Budgets: DataTable [Name, Department, Year, Amount, Status].
- **GOTCHA**: Accounts use `account_number` not `code`. Journal entries use `total_debit_cents`. Budgets use `fiscal_year`.
- **VALIDATE**: All 5 pages render with seeded data

---

## Testing Strategy

### Edge Cases Checklist
- [ ] Login with wrong credentials → shows error toast
- [ ] Expired/invalid token → redirects to login
- [ ] API returns empty array → shows empty state message
- [ ] API returns error 500 → shows error indicator
- [ ] Null fields (description, brand, contact) → displays "—" or empty
- [ ] cents-to-dollars conversion → `total_amount_cents / 100` formatted with 2 decimals
- [ ] Very long text in table cells → truncated with ellipsis
- [ ] Navigate directly to `/hr/employees` without login → redirects to `/login`

---

## Validation Commands

### Install Dependencies
```bash
cd web-client && npm install
```
EXPECT: Clean install, no peer dependency errors

### TypeScript Build
```bash
cd web-client && npx tsc --noEmit
```
EXPECT: Zero type errors

### ESLint
```bash
cd web-client && npm run lint
```
EXPECT: No errors (warnings acceptable)

### Dev Server
```bash
cd web-client && npm run dev
```
EXPECT: Server starts on port 3000

### Browser Validation
1. Open `http://localhost:3000` → should redirect to login
2. Login with `admin` / `admin123` → should redirect to HR dashboard
3. HR Dashboard → 4 stat cards showing real counts (30, 8, 20, 4)
4. HR > Employees → table with 30 rows
5. Navigate to Inventory → products table with 15 rows
6. Navigate to Orders → sales table with 15 rows
7. Navigate to Finance → accounts table with 20 rows
8. Logout → returns to login page

---

## Acceptance Criteria
- [ ] All tasks completed (1-10)
- [ ] All validation commands pass
- [ ] Login/logout flow works end-to-end
- [ ] All 4 module dashboards show real data from API
- [ ] All 17+ data table pages render with seeded data
- [ ] Create modals submit data to API
- [ ] Design follows Cal.com-inspired tokens from design.md
- [ ] No TypeScript errors
- [ ] No ESLint errors
- [ ] Responsive shell renders correctly at 1200px+ width

## Completion Checklist
- [ ] CSS design system uses tokens from design.md
- [ ] Inter font loaded from Google Fonts
- [ ] All interactive components use `'use client'`
- [ ] SWR handles loading/error states
- [ ] Status badges use semantic colors
- [ ] Footer uses dark surface (#101010)
- [ ] Navigation highlights active module and page
- [ ] No hardcoded auth tokens
- [ ] API client handles token refresh on 401

## Risks
| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Gateway CORS issues | Low | Medium | CORS already allows `*`; proxy fallback in next.config.ts |
| SWR cache stale data after create | Medium | Low | Mutate SWR cache after POST success |
| Chart.js SSR crash | Medium | High | Use dynamic import with `ssr: false` |
| Next.js 16 + React 19 compatibility | Low | High | Already scaffolded and working |

## Notes
- The `web-client` directory already has Next.js 16 scaffolded with React 19, SWR, Chart.js, and react-hot-toast in package.json. Dependencies just need `npm install`.
- The gateway runs on port 8080 inside Docker. CORS allows `*` so the web client at port 3000 can call it directly.
- All list API endpoints return **raw JSON arrays** (not wrapped in `{"data":[...]}`) — this was fixed in a previous session.
- Demo users: `admin/admin123`, `manager/manager123`, `finance/finance123`, `hr/hr123`, `sales/sales123`
- JWT claims contain: `sub` (user ID), `name`, `email`, `role`, `exp`, `iat`
- The design system in `design.md` is Cal.com-inspired: monochrome with Inter font, white canvas, black CTAs, light-gray cards, dark footer. Cal Sans is proprietary so we use Inter 600 as substitute.
