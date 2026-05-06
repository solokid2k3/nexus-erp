# Plan: Nexus ERP Web Client Overhaul

## Summary
The Nexus ERP web client is a Next.js 16 application with React 19, SWR for data fetching, Chart.js for charts, and Tailwind CSS 4 (via `@tailwindcss/postcss`). It has 4 module dashboards (HR, Inventory, Orders, Finance) with 21 pages total. The current state has several critical infrastructure gaps — the root layout is missing the globals.css import, the AuthProvider wrapper, and the Toaster component; the Next.js config has no API proxy to the Go gateway at `:8080`; several list pages lack create forms; POST handlers bypass the centralized `apiFetch` utility; and the homepage is a stub. This plan addresses all of these systematically.

## User Story
As an ERP administrator,
I want a polished, fully functional web client for all ERP modules,
So that I can manage HR, Inventory, Orders, and Finance data through a professional browser interface.

## Problem → Solution
**Current state**: Broken root layout (no CSS, no auth provider, no toast), no API proxy, inconsistent CRUD coverage, stub homepage, hardcoded chart data, raw UUID displays.
→ **Desired state**: Fully wired root layout, working API proxy to gateway `:8080`, CRUD forms on all entity pages, centralized POST via `apiFetch`, proper landing redirect, polished responsive design.

## Metadata
- **Complexity**: Large
- **Source PRD**: N/A
- **PRD Phase**: N/A
- **Estimated Files**: ~35

---

## UX Design

### Before
```
┌─────────────────────────────┐
│ Hello world! (root page)    │
│ No CSS loaded (missing      │
│   globals.css import)       │
│ No AuthProvider wrapping     │
│ No toast notifications       │
│ No API proxy → fetch fails   │
│ Some pages have CRUD, some   │
│   are read-only stubs        │
└─────────────────────────────┘
```

### After
```
┌─────────────────────────────┐
│ / redirects to /hr          │
│ Full design system loaded    │
│ AuthProvider wraps all pages │
│ Toast notifications working  │
│ API proxied → data fetches   │
│ All entity pages have CRUD   │
│ Consistent responsive layout │
└─────────────────────────────┘
```

### Interaction Changes
| Touchpoint | Before | After | Notes |
|---|---|---|---|
| Root page `/` | Shows "Hello world!" | Redirects to `/hr` | Server-side redirect |
| Root layout | No CSS, no providers | Full providers + CSS | Critical fix |
| API calls | Fail (no proxy) | Proxied to `:8080` | `next.config.ts` rewrite |
| Read-only pages | No create button | Add button + modal | Attendance, Leave, Stock, Warehouses, Journals, Invoices, Sales, Purchases |
| POST calls in forms | Raw `fetch()` | Uses `apiFetch()` | Centralized auth headers |
| Purchase Orders endpoint | `/orders/purchases` | `/orders/purchase` | Matches actual API |

---

## Mandatory Reading

Files that MUST be read before implementing:

| Priority | File | Lines | Why |
|---|---|---|---|
| P0 (critical) | `web-client/src/app/layout.tsx` | all | Root layout missing CSS + providers |
| P0 (critical) | `web-client/next.config.ts` | all | Missing API proxy config |
| P0 (critical) | `web-client/src/lib/api.ts` | all | Centralized fetch + SWR hooks |
| P0 (critical) | `web-client/src/lib/auth.tsx` | all | AuthProvider that must wrap the app |
| P1 (important) | `web-client/src/app/globals.css` | all | Full design system tokens |
| P1 (important) | `web-client/src/components/DataTable.tsx` | all | Generic table component |
| P1 (important) | `web-client/src/components/Modal.tsx` | all | Modal for create forms |
| P1 (important) | `web-client/src/lib/types.ts` | all | All TypeScript interfaces |
| P2 (reference) | `web-client/src/app/(dashboard)/hr/employees/page.tsx` | all | Reference CRUD page pattern |
| P2 (reference) | `web-client/src/app/(dashboard)/inventory/categories/page.tsx` | all | Reference CRUD page pattern |
| P2 (reference) | `design.md` | all | Cal Sans-inspired design spec |
| P2 (reference) | `api-docs.md` | all | All API endpoints |
| P2 (reference) | `docker-compose.yml` | 34-48 | Gateway port (8080) |

---

## Patterns to Mirror

Code patterns discovered in the codebase. Follow these exactly.

### NAMING_CONVENTION
```tsx
// SOURCE: web-client/src/app/(dashboard)/hr/employees/page.tsx:1-10
"use client";
import React, { useState } from 'react';
import { useApiSWR } from '@/lib/api';
import { Employee, Department } from '@/lib/types';
import { DataTable, ColumnDef } from '@/components/DataTable';
import { StatusBadge } from '@/components/StatusBadge';
import { Modal } from '@/components/Modal';
import toast from 'react-hot-toast';
```
- Pages: `export default function XxxPage()` or `export default function XxxDashboard()`
- Components: PascalCase named exports
- Types: PascalCase interfaces in `lib/types.ts`
- CSS classes: BEM-like utility classes from `globals.css` (`card-white`, `btn-primary`, `input-text`, etc.)

### ERROR_HANDLING
```tsx
// SOURCE: web-client/src/app/(dashboard)/hr/employees/page.tsx:46-64
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  try {
    const res = await fetch('/api/v1/hr/employees', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`
      },
      body: JSON.stringify(formData)
    });
    if (!res.ok) throw new Error('Failed to create employee');
    toast.success('Employee created');
    setIsModalOpen(false);
    mutate(); // refresh data
  } catch (err: unknown) {
    toast.error(err instanceof Error ? err.message : 'Error');
  }
};
```
NOTE: This pattern bypasses `apiFetch` — we will refactor all POST handlers to use `apiFetch` instead.

### PAGE_LAYOUT
```tsx
// SOURCE: web-client/src/app/(dashboard)/hr/departments/page.tsx:28-49
return (
  <div className="flex flex-col gap-6">
    <div className="flex justify-between items-center">
      <div>
        <h1 className="title-lg">Page Title</h1>
        <p className="body-sm color-muted">Description text.</p>
      </div>
      {/* Optional: <button className="btn-primary" onClick={...}>Add X</button> */}
    </div>

    <div className="card-white p-0 overflow-hidden">
      <div className="p-4">
        <DataTable data={data} columns={columns} loading={isLoading} searchPlaceholder="..." />
      </div>
    </div>
  </div>
);
```

### MODAL_FORM
```tsx
// SOURCE: web-client/src/app/(dashboard)/inventory/categories/page.tsx:75-90
<Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Add Category">
  <form onSubmit={handleSubmit} className="flex flex-col gap-4">
    <div>
      <label className="block caption mb-1">Name</label>
      <input required type="text" className="input-text" value={formData.name}
        onChange={e => setFormData({...formData, name: e.target.value})} />
    </div>
    <div className="flex justify-end gap-2 mt-4">
      <button type="button" className="btn-secondary" onClick={() => setIsModalOpen(false)}>Cancel</button>
      <button type="submit" className="btn-primary">Save</button>
    </div>
  </form>
</Modal>
```

### STAT_CARD_GRID
```tsx
// SOURCE: web-client/src/app/(dashboard)/hr/page.tsx:38-79
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
  <StatCard title="..." value={data?.length} loading={loading} icon={<svg>...</svg>} />
</div>
```

### DASHBOARD_CHART
```tsx
// SOURCE: web-client/src/app/(dashboard)/hr/page.tsx:81-93
<div className="card-white mt-4">
  <h2 className="title-md mb-4">Chart Title</h2>
  <div className="h-64">
    <Bar data={chartData} options={{ responsive: true, maintainAspectRatio: false }} />
  </div>
</div>
```

---

## Files to Change

### Phase 1: Critical Infrastructure Fixes

| File | Action | Justification |
|---|---|---|
| `web-client/next.config.ts` | UPDATE | Add `rewrites()` to proxy `/api/v1/*` → `http://localhost:8080/api/v1/*` |
| `web-client/src/app/layout.tsx` | UPDATE | Import `globals.css`, wrap children with `ClientProviders` |
| `web-client/src/components/ClientProviders.tsx` | CREATE | Client-side wrapper for AuthProvider + Toaster |
| `web-client/src/app/page.tsx` | UPDATE | Replace stub with redirect to `/hr` |

### Phase 2: Library Improvements

| File | Action | Justification |
|---|---|---|
| `web-client/src/lib/api.ts` | UPDATE | Add `apiPost`, `apiPut`, `apiDelete` helpers using `apiFetch` |

### Phase 3: Component Enhancements

| File | Action | Justification |
|---|---|---|
| `web-client/src/components/DataTable.tsx` | UPDATE | Add basic pagination (page size 20) |

### Phase 4: Page Fixes (per-module)

#### HR Module
| File | Action | Justification |
|---|---|---|
| `web-client/src/app/(dashboard)/hr/employees/page.tsx` | UPDATE | Refactor POST to use `apiPost`, add form reset |
| `web-client/src/app/(dashboard)/hr/departments/page.tsx` | UPDATE | Add "Add Department" button + modal create form |
| `web-client/src/app/(dashboard)/hr/leave/page.tsx` | UPDATE | Add approve/reject action buttons per row |
| `web-client/src/app/(dashboard)/hr/attendance/page.tsx` | UPDATE | Add clock-in/clock-out action buttons |
| `web-client/src/app/(dashboard)/hr/payroll/page.tsx` | UPDATE | Add "Create Payroll Run" button + modal |

#### Inventory Module
| File | Action | Justification |
|---|---|---|
| `web-client/src/app/(dashboard)/inventory/products/page.tsx` | UPDATE | Refactor POST to use `apiPost`, add form reset |
| `web-client/src/app/(dashboard)/inventory/categories/page.tsx` | UPDATE | Refactor POST to use `apiPost`, add form reset |
| `web-client/src/app/(dashboard)/inventory/stock/page.tsx` | UPDATE | Pivot to "Stock Alerts" using `/inventory/stock/alerts` |
| `web-client/src/app/(dashboard)/inventory/warehouses/page.tsx` | UPDATE | Add "Add Warehouse" button + modal create form |
| `web-client/src/app/(dashboard)/inventory/page.tsx` | UPDATE | Fix stock endpoint to use `/inventory/stock/alerts` |

#### Orders Module
| File | Action | Justification |
|---|---|---|
| `web-client/src/app/(dashboard)/orders/sales/page.tsx` | UPDATE | Add "Create Order" button + modal |
| `web-client/src/app/(dashboard)/orders/purchases/page.tsx` | UPDATE | Fix endpoint to `/orders/purchase`, add create form |
| `web-client/src/app/(dashboard)/orders/customers/page.tsx` | UPDATE | Refactor POST to use `apiPost` |
| `web-client/src/app/(dashboard)/orders/suppliers/page.tsx` | UPDATE | Refactor POST to use `apiPost` |
| `web-client/src/app/(dashboard)/orders/page.tsx` | UPDATE | Fix `/orders/purchases` → `/orders/purchase` |

#### Finance Module
| File | Action | Justification |
|---|---|---|
| `web-client/src/app/(dashboard)/finance/accounts/page.tsx` | UPDATE | Refactor POST to use `apiPost` |
| `web-client/src/app/(dashboard)/finance/journals/page.tsx` | UPDATE | Add create form + post/reverse actions |
| `web-client/src/app/(dashboard)/finance/invoices/page.tsx` | UPDATE | Add create form + payment recording |
| `web-client/src/app/(dashboard)/finance/budgets/page.tsx` | UPDATE | Refactor POST to use `apiPost` |

### Phase 5: Navigation Fix
| File | Action | Justification |
|---|---|---|
| `web-client/src/components/Sidebar.tsx` | UPDATE | Update sidebar stock label to "Stock Alerts" |

## NOT Building
- No new test suite (out of scope for this phase)
- No SSR/server components conversion (keeping `"use client"` pattern)
- No desktop-client changes
- No backend/gateway changes
- No new modules beyond the existing 4
- No real-time WebSocket features
- No dark mode toggle (footer dark is by design)
- No i18n / localization

---

## Step-by-Step Tasks

### Task 1: Fix Root Layout + Create ClientProviders
- **ACTION**: Create `ClientProviders.tsx`, update `layout.tsx` to import CSS and wrap children
- **IMPLEMENT**: Server component layout imports `./globals.css`, uses `<ClientProviders>` wrapper
- **MIRROR**: AuthProvider pattern from auth.tsx
- **IMPORTS**: `./globals.css`, `@/components/ClientProviders`
- **GOTCHA**: Root layout must stay server component for `metadata` export
- **VALIDATE**: App loads with correct styles and auth context

### Task 2: Add API Proxy in next.config.ts
- **ACTION**: Add `rewrites()` to proxy `/api/v1/*` → `http://localhost:8080/api/v1/*`
- **IMPLEMENT**: Standard Next.js rewrite config
- **MIRROR**: N/A
- **IMPORTS**: None
- **GOTCHA**: Gateway runs on port 8080 per docker-compose
- **VALIDATE**: Login form submits successfully

### Task 3: Fix Root Page Redirect
- **ACTION**: Replace "Hello world!" with `redirect('/hr')`
- **IMPLEMENT**: Server component using `redirect()` from `next/navigation`
- **MIRROR**: N/A
- **IMPORTS**: `next/navigation`
- **GOTCHA**: Use server-side `redirect()`, not client `useRouter()`
- **VALIDATE**: Navigating to `/` redirects to `/hr`

### Task 4: Add API Mutation Helpers
- **ACTION**: Add `apiPost`, `apiPut`, `apiDelete` to `api.ts`
- **IMPLEMENT**: Thin wrappers around `apiFetch` with correct HTTP methods
- **MIRROR**: Existing `apiFetch` pattern
- **IMPORTS**: None
- **GOTCHA**: `apiFetch` already sets Content-Type and auth headers
- **VALIDATE**: Helpers can be used in form handlers

### Task 5: Add Pagination to DataTable
- **ACTION**: Add client-side pagination (20 rows per page)
- **IMPLEMENT**: `currentPage` state, slice after filter, prev/next buttons
- **MIRROR**: Existing DataTable structure
- **IMPORTS**: None
- **GOTCHA**: Paginate AFTER filtering
- **VALIDATE**: Large datasets paginate correctly

### Task 6: Fix Endpoint Mismatches
- **ACTION**: Fix `purchases` → `purchase` in purchases page, orders dashboard, sidebar; fix stock endpoint
- **IMPLEMENT**: String replacements in useApiSWR calls
- **MIRROR**: API docs
- **IMPORTS**: None
- **GOTCHA**: Route paths stay as `/orders/purchases` but API endpoint uses `/orders/purchase`
- **VALIDATE**: Pages load data without 404s

### Task 7: Refactor POST Handlers to apiPost
- **ACTION**: Replace raw `fetch()` in 7 pages with `apiPost()`
- **IMPLEMENT**: Swap try/catch blocks to use centralized utility
- **MIRROR**: `apiFetch` error handling
- **IMPORTS**: `apiPost` from `@/lib/api`
- **GOTCHA**: `apiPost` auto-parses JSON and throws on error
- **VALIDATE**: Create forms still work

### Task 8: Add Missing Create Forms (8 pages)
- **ACTION**: Add create button + modal to: Departments, Warehouses, Leave, Payroll, Sales, Purchases, Journals, Invoices
- **IMPLEMENT**: Follow MODAL_FORM pattern from employees/categories pages
- **MIRROR**: MODAL_FORM, PAGE_LAYOUT patterns
- **IMPORTS**: Modal, apiPost, toast
- **GOTCHA**: Some forms need related data dropdowns
- **VALIDATE**: Each page has working "Add" button

### Task 9: Add Action Buttons (Workflow Pages)
- **ACTION**: Add approve/reject to Leave, approve to Payroll/Sales, post/reverse to Journals, payment to Invoices
- **IMPLEMENT**: Add Actions column with inline buttons
- **MIRROR**: PAGE_LAYOUT pattern
- **IMPORTS**: apiPost
- **GOTCHA**: Actions POST to `/{id}/approve` etc.
- **VALIDATE**: Action buttons trigger API calls

### Task 10: Pivot Stock Page to Stock Alerts
- **ACTION**: Change stock page from non-existent `/inventory/stock` to `/inventory/stock/alerts`
- **IMPLEMENT**: Update title to "Stock Alerts", adjust columns for alert data
- **MIRROR**: PAGE_LAYOUT pattern
- **IMPORTS**: May need StockAlert type
- **GOTCHA**: No global stock list endpoint exists
- **VALIDATE**: Stock page shows low-stock alerts

### Task 11: Update Root Layout Metadata
- **ACTION**: Replace "Create Next App" with "Nexus ERP" metadata
- **IMPLEMENT**: Update title and description strings
- **MIRROR**: N/A
- **IMPORTS**: None
- **GOTCHA**: None
- **VALIDATE**: Browser tab shows "Nexus ERP"

---

## Validation Commands

### Static Analysis
```bash
cd web-client && npx tsc --noEmit
```
EXPECT: Zero type errors

### Build Check
```bash
cd web-client && npm run build
```
EXPECT: Successful build with no errors

### Lint
```bash
cd web-client && npm run lint
```
EXPECT: No critical lint errors

### Dev Server
```bash
cd web-client && npm run dev
```
EXPECT: App loads at http://localhost:3000

### Browser Validation
1. Navigate to `http://localhost:3000` → should redirect to `/hr`
2. Login page renders with proper styling
3. Login with `admin / admin123` → redirects to HR dashboard
4. HR Dashboard shows 4 stat cards + attendance chart
5. Navigate through all 4 modules via top nav
6. Click "Add Employee" → modal opens → fill form → submit
7. Each module's sub-pages load data tables
8. Search filtering works in data tables
9. Footer shows dark background with copyright

---

## Acceptance Criteria
- [ ] All tasks completed (Tasks 1-11)
- [ ] All validation commands pass
- [ ] `npm run build` succeeds
- [ ] No type errors (`tsc --noEmit`)
- [ ] Root layout properly imports CSS + providers
- [ ] API proxy configured and working
- [ ] All 21 pages render without errors
- [ ] All entity pages have create functionality
- [ ] POST handlers use centralized `apiPost`
- [ ] Purchase orders endpoint corrected
- [ ] Stock page uses stock alerts endpoint
- [ ] Root page redirects (no more "Hello world!")

## Risks
| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Missing GET endpoints (warehouses, stock) | Medium | Medium | Handle 404 gracefully with empty states |
| Next.js 16 breaking changes vs training data | Low | High | Read AGENTS.md warning; test builds incrementally |
| Tailwind v4 class compatibility | Low | Medium | Current CSS uses custom classes, not Tailwind utilities directly |
| API response shape mismatch | Medium | Medium | Types were defined in prior sessions; verify at runtime |

## Notes
- The `design.md` specifies a Cal Sans-inspired design system. The CSS is already solid.
- Tailwind CSS 4 is configured via `@tailwindcss/postcss` and `@import "tailwindcss"` in globals.css.
- Charts use hardcoded data — acceptable for now, no analytics endpoints exist.
- The inventory stock page is the most problematic — no "list all stock" endpoint exists. Best approach: pivot to "Stock Alerts".
- The sidebar route for purchases stays as `/orders/purchases` (URL path) while the API endpoint is `/orders/purchase`.
