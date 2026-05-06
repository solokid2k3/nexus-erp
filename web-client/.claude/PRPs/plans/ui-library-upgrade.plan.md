# Plan: Upgrade Nexus ERP Web Client UI with Ant Design

## Summary
Replace the hand-rolled CSS component system (custom buttons, tables, modals, badges, inputs) with **Ant Design v6** — a comprehensive, enterprise-grade React UI library purpose-built for data-heavy dashboards. This gives us production-ready tables with sorting/filtering/pagination, polished forms, professional modals, rich charts integration, and a cohesive design language across all 4 ERP modules (HR, Inventory, Orders, Finance) without building everything from scratch.

## User Story
As an ERP user, I want a polished, professional UI with rich interactive components (sortable tables, validated forms, notification toasts, proper loading states), so that the application feels like an enterprise-grade product rather than a prototype.

## Problem → Solution
**Current state:** Hand-coded CSS classes (`btn-primary`, `card-white`, `input-text`, `table-container`), a custom `DataTable` with basic search/pagination, a raw `Modal` with manual scroll lock, inline SVG icons everywhere, and inconsistent styling between Tailwind utilities and custom CSS classes.

**Desired state:** Ant Design's `Table`, `Modal`, `Form`, `Button`, `Input`, `Select`, `Badge`, `Card`, `Statistic`, `Layout`, `Menu`, `notification` — all following a unified design system with built-in accessibility, animations, and responsive behavior.

## Metadata
- **Complexity**: Large
- **Source PRD**: N/A
- **PRD Phase**: N/A
- **Estimated Files**: ~30 files (8 components + 21 pages + layout + config)

---

## UX Design

### Before
```
┌──────────────────────────────────────────────────┐
│ [■] NEXUS ERP   HR  Inventory  Orders  Finance   │ ← Plain white header, text-only nav
├────────────┬─────────────────────────────────────┤
│ Dashboard  │  HR Dashboard                       │
│ Employees  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌────┐ │
│ Departments│  │ 42   │ │  5   │ │  3   │ │ 12 │ │ ← Basic stat cards, no icons
│ Leave      │  └──────┘ └──────┘ └──────┘ └────┘ │
│ Attendance │                                     │
│ Payroll    │  ┌─ Weekly Attendance ─────────────┐│
│            │  │  [Basic Chart.js bar chart]     ││
│            │  └─────────────────────────────────┘│
│            │                                     │
├────────────┴─────────────────────────────────────┤
│ © 2026 Nexus ERP                                 │ ← Dark footer
└──────────────────────────────────────────────────┘

Data pages: Basic <table> with custom search input, manual pagination buttons
Modals: Custom div overlay with manual scroll-lock useEffect
Forms: Raw <input> and <select> with manual onChange handlers
```

### After
```
┌──────────────────────────────────────────────────┐
│ 🔷 Nexus ERP                             Admin ▼ │ ← Ant Design Header with Dropdown
├────────────┬─────────────────────────────────────┤
│ ≡ HR       │  HR Dashboard                       │
│ ━━━━━━━━━━ │  ┌──────┐ ┌──────┐ ┌──────┐ ┌────┐ │
│ 📊 Dashboard│ │👥 42 │ │🏢 5 │ │📅 3 │ │💰12│ │ ← Ant Statistic with icons + trend
│ 👤 Employees│ │▲ +5% │ │     │ │     │ │    │ │
│ 🏢 Depts   │  └──────┘ └──────┘ └──────┘ └────┘ │
│ 📅 Leave   │                                     │
│ ⏰ Attend. │  ┌─ Weekly Attendance ─────────────┐│
│ 💰 Payroll │  │  [Ant Design Charts / Chart.js] ││
│            │  └─────────────────────────────────┘│
├────────────┴─────────────────────────────────────┤
│ © 2026 Nexus ERP                                 │
└──────────────────────────────────────────────────┘

Data pages: Ant Design <Table> with built-in sort, filter, search, pagination
Modals: Ant Design <Modal> with animations, proper focus trap
Forms: Ant Design <Form> with validation, <Input>, <Select> with search
Notifications: Ant Design notification/message API (replaces react-hot-toast)
Icons: @ant-design/icons (replaces inline SVGs)
```

### Interaction Changes
| Touchpoint | Before | After | Notes |
|---|---|---|---|
| Navigation | Text links with manual active state | Ant `Menu` with icons, selected state, collapse | Better visual hierarchy |
| Data Tables | Custom `DataTable` component, basic pagination | Ant `Table` with sort, filter, column config | Massive upgrade |
| Forms/Modals | Custom `Modal` + raw `<input>` | Ant `Modal` + `Form` with validation rules | Validation built-in |
| Stat Cards | Custom `StatCard` with inline SVGs | Ant `Card` + `Statistic` + `@ant-design/icons` | Animated counters |
| Notifications | `react-hot-toast` | Ant `notification` / `message` API | Consistent with design |
| Loading States | Manual spinner divs | Ant `Spin`, `Skeleton` components | Better UX |
| Buttons | Custom CSS `.btn-primary/.btn-secondary` | Ant `Button` with `type="primary"` etc. | Consistent sizing |
| Badges | Custom CSS `.badge` classes | Ant `Tag` / `Badge` components | More variants |
| Select Inputs | Raw `<select>` elements | Ant `Select` with search, clear, multi | Much better UX |

---

## Mandatory Reading

Files that MUST be read before implementing:

| Priority | File | Lines | Why |
|---|---|---|---|
| P0 | `src/app/layout.tsx` | all | Root layout — must wrap with AntdRegistry |
| P0 | `src/app/(dashboard)/layout.tsx` | all | Dashboard shell — Sidebar + TopNav + main |
| P0 | `src/components/DataTable.tsx` | all | Current table component — must map API to Ant Table |
| P0 | `src/lib/types.ts` | all | All TypeScript interfaces — columns must reference these |
| P0 | `src/lib/api.ts` | all | API layer — keep SWR, adapt to Ant Table loading |
| P1 | `src/components/Sidebar.tsx` | all | Current nav structure — port to Ant Menu |
| P1 | `src/components/TopNav.tsx` | all | Current header — port to Ant Layout.Header |
| P1 | `src/components/Modal.tsx` | all | Current modal — replace with Ant Modal |
| P1 | `src/components/StatCard.tsx` | all | Current stats — replace with Ant Card+Statistic |
| P1 | `src/components/StatusBadge.tsx` | all | Current badges — replace with Ant Tag |
| P2 | `src/app/(dashboard)/hr/employees/page.tsx` | all | Representative CRUD page pattern |
| P2 | `src/app/(dashboard)/hr/page.tsx` | all | Representative dashboard page pattern |
| P2 | `src/app/login/page.tsx` | all | Login form — port to Ant Form |
| P2 | `src/app/globals.css` | all | Must gut custom CSS, keep Tailwind for layout |
| P2 | `postcss.config.mjs` | all | PostCSS + Tailwind config |
| P2 | `next.config.ts` | all | Next.js config — API proxy |
| P2 | `package.json` | all | Dependencies to add/remove |

---

## Patterns to Mirror

### PAGE_LAYOUT (Keep this pattern — just swap the component imports)
```tsx
// SOURCE: src/app/(dashboard)/hr/employees/page.tsx:59-80
// Every data page follows: header row → card-wrapped DataTable → Modal for Create
<div className="flex flex-col gap-6">
  <div className="flex justify-between items-center">
    <div>
      <h1 className="title-lg">Employees</h1>
      <p className="body-sm color-muted">Manage employee records.</p>
    </div>
    <button className="btn-primary" onClick={() => setIsModalOpen(true)}>
      Add Employee
    </button>
  </div>
  <div className="card-white p-0 overflow-hidden">
    <DataTable data={data} columns={columns} loading={isLoading} />
  </div>
  <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Add Employee">
    <form>...</form>
  </Modal>
</div>
```

### DATA_FETCHING (Keep SWR hooks unchanged)
```tsx
// SOURCE: src/lib/api.ts:41-55
export function useApiSWR<T>(endpoint: string | null, options?: SWRConfiguration) {
  const fetcher = (url: string) => apiFetch(url);
  const swr = useSWR<T>(endpoint, fetcher, {
    revalidateOnFocus: false,
    shouldRetryOnError: false,
    onError: (err) => { ... },
    ...options,
  });
  return swr;
}
```

### ERROR_HANDLING
```tsx
// SOURCE: src/app/(dashboard)/hr/employees/page.tsx:46-57
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  try {
    await apiPost('/hr/employees', formData);
    toast.success('Employee created');
    setIsModalOpen(false);
    mutate();
  } catch (err: unknown) {
    toast.error(err instanceof Error ? err.message : 'Error');
  }
};
```

### COLUMN_DEFINITION (Will change from custom ColumnDef to Ant ColumnsType)
```tsx
// SOURCE: src/app/(dashboard)/hr/employees/page.tsx:19-44
// BEFORE: Custom ColumnDef<Employee> with accessor functions
// AFTER:  Ant Design ColumnsType<Employee> with dataIndex + render
```

---

## Library Choice Decision

### Why Ant Design over shadcn/ui or Mantine?

| Factor | Ant Design v6 | shadcn/ui | Mantine |
|---|---|---|---|
| **ERP Fit** | ★★★★★ Built for enterprise data UIs | ★★★ Needs manual assembly | ★★★★ Good but less battle-tested |
| **Table Component** | ★★★★★ Built-in sort/filter/pagination/expand | ★★ Requires TanStack Table integration | ★★★★ Good table but less features |
| **Form Validation** | ★★★★★ Built-in rules + error display | ★★★ Need react-hook-form | ★★★★ useForm hook |
| **Setup Complexity** | ★★★★ `npm install antd` + AntdRegistry | ★★★ CLI init + copy components | ★★★★ npm install |
| **React 19 / Next.js 16** | ✅ v6 native support | ✅ Supported | ✅ Supported |
| **Icon System** | ✅ @ant-design/icons | ❌ Need lucide-react | ✅ @tabler/icons-react |
| **Bundle Impact** | Moderate (tree-shakeable) | Minimal | Moderate |

**Decision: Ant Design v6** — best fit for an ERP with heavy table/form/data requirements. The built-in Table component alone saves weeks of work compared to building custom sorting/filtering with shadcn + TanStack Table.

---

## Files to Change

### Dependencies & Config
| File | Action | Justification |
|---|---|---|
| `package.json` | UPDATE | Add antd, @ant-design/icons, @ant-design/nextjs-registry; remove react-hot-toast |
| `src/app/layout.tsx` | UPDATE | Wrap with AntdRegistry, configure Ant ConfigProvider theme |
| `src/app/globals.css` | UPDATE | Remove custom component CSS (keep base reset + Tailwind layout utilities) |
| `postcss.config.mjs` | KEEP | Tailwind CSS stays for layout utilities |
| `next.config.ts` | KEEP | API proxy unchanged |

### Components (Replace)
| File | Action | Justification |
|---|---|---|
| `src/components/ClientProviders.tsx` | UPDATE | Remove Toaster, add Ant App wrapper |
| `src/components/Sidebar.tsx` | UPDATE | Rewrite with Ant Menu |
| `src/components/TopNav.tsx` | UPDATE | Rewrite with Ant Layout.Header + Dropdown |
| `src/components/DataTable.tsx` | DELETE | Replaced by direct Ant Table usage |
| `src/components/Modal.tsx` | DELETE | Replaced by Ant Modal |
| `src/components/StatCard.tsx` | DELETE | Replaced by Ant Card + Statistic |
| `src/components/StatusBadge.tsx` | UPDATE | Rewrite as thin wrapper around Ant Tag |
| `src/components/Footer.tsx` | UPDATE | Rewrite with Ant Layout.Footer |

### Layout
| File | Action | Justification |
|---|---|---|
| `src/app/(dashboard)/layout.tsx` | UPDATE | Use Ant Layout/Sider/Content structure |

### Pages (Update all 21 pages)
| File | Action | Justification |
|---|---|---|
| `src/app/login/page.tsx` | UPDATE | Port to Ant Form + Input + Button |
| `src/app/page.tsx` | KEEP | Root redirect — unchanged |
| 4× dashboard pages (hr, inventory, orders, finance) | UPDATE | StatCard → Ant Statistic, charts stay |
| 16× data pages | UPDATE | DataTable → Ant Table, Modal → Ant Modal, forms → Ant Form |

### Lib (Untouched)
| File | Action | Justification |
|---|---|---|
| `src/lib/api.ts` | KEEP | SWR + fetch wrapper unchanged |
| `src/lib/auth.tsx` | KEEP | Auth context unchanged |
| `src/lib/types.ts` | KEEP | Type interfaces unchanged |

## NOT Building
- Custom theme / dark mode toggle (out of scope, can add later)
- New pages or new CRUD features
- Backend API changes
- Migration from SWR to TanStack Query
- Advanced table features (column resizing, row selection, bulk actions) — can add later
- Chart library replacement (Chart.js stays, can switch to @ant-design/charts later)

---

## Step-by-Step Tasks

### Task 1: Install Dependencies
- **ACTION**: Install Ant Design ecosystem packages and remove react-hot-toast
- **IMPLEMENT**:
  ```bash
  npm install antd @ant-design/icons @ant-design/nextjs-registry
  npm uninstall react-hot-toast
  ```
- **VALIDATE**: `npm ls antd` shows installed, `npm run build` doesn't error

### Task 2: Configure Root Layout with AntdRegistry + Theme
- **ACTION**: Update `src/app/layout.tsx` to wrap children with AntdRegistry and ConfigProvider
- **IMPLEMENT**:
  ```tsx
  import { AntdRegistry } from '@ant-design/nextjs-registry';
  import { ConfigProvider } from 'antd';
  
  // Theme configuration matching existing color system
  const theme = {
    token: {
      colorPrimary: '#111111',
      borderRadius: 8,
      fontFamily: "'Inter', -apple-system, sans-serif",
    },
  };
  
  // Wrap: <AntdRegistry><ConfigProvider theme={theme}>...</ConfigProvider></AntdRegistry>
  ```
- **MIRROR**: Current layout wraps with `<ClientProviders>`
- **GOTCHA**: AntdRegistry must be outside ConfigProvider. `@import "tailwindcss"` stays.
- **VALIDATE**: App renders without FOUC, Ant Design components pick up theme colors

### Task 3: Update ClientProviders (Remove Toaster)
- **ACTION**: Remove `react-hot-toast` Toaster, add Ant `App` component wrapper
- **IMPLEMENT**:
  ```tsx
  import { App } from 'antd';
  
  export function ClientProviders({ children }: { children: React.ReactNode }) {
    return (
      <AuthProvider>
        <App>{children}</App>
      </AuthProvider>
    );
  }
  ```
- **GOTCHA**: Ant `App` component provides context for `message`, `notification`, `modal` static methods
- **VALIDATE**: Notifications work when called from page components

### Task 4: Gut globals.css — Remove Custom Component CSS
- **ACTION**: Remove all custom component classes (.btn-primary, .card-white, .input-text, .table-container, .badge, etc). Keep `@import "tailwindcss"`, `:root` CSS variables, and basic reset.
- **IMPLEMENT**: Keep lines 1-3 (imports), keep `*` reset, remove everything from `.btn-primary` onward. Keep Tailwind layout utilities and color variables for any custom needs.
- **VALIDATE**: No visual regressions because all pages will be updated to use Ant components

### Task 5: Rewrite Dashboard Layout with Ant Layout
- **ACTION**: Replace custom div-based layout in `src/app/(dashboard)/layout.tsx` with Ant's `Layout`, `Layout.Sider`, `Layout.Content`
- **IMPLEMENT**:
  ```tsx
  import { Layout } from 'antd';
  const { Sider, Content } = Layout;
  
  <Layout style={{ minHeight: '100vh' }}>
    <TopNav />
    <Layout>
      <Sider width={256} theme="light" style={{ borderRight: '1px solid #f0f0f0' }}>
        <Sidebar />
      </Sider>
      <Content style={{ padding: 24, background: '#f5f5f5' }}>
        {children}
      </Content>
    </Layout>
    <Footer />
  </Layout>
  ```
- **GOTCHA**: Ant Layout uses `style` props, mix with className for Tailwind spacing
- **VALIDATE**: Sidebar + content + header render correctly, scrolling works

### Task 6: Rewrite TopNav with Ant Header
- **ACTION**: Replace custom header in `TopNav.tsx` with Ant Design components
- **IMPLEMENT**: Use Ant `Layout.Header`, `Menu` for module navigation (horizontal), `Dropdown` + `Avatar` for user section, `Button` for logout
- **IMPORTS**: `Layout, Menu, Dropdown, Avatar, Button, Space` from 'antd'; icons from '@ant-design/icons'
- **VALIDATE**: Module switching works, user info displays, logout functions

### Task 7: Rewrite Sidebar with Ant Menu
- **ACTION**: Replace custom aside + Link list with Ant `Menu` (vertical, inline mode)
- **IMPLEMENT**: 
  ```tsx
  import { Menu } from 'antd';
  import type { MenuProps } from 'antd';
  import { TeamOutlined, CalendarOutlined, ... } from '@ant-design/icons';
  
  const items: MenuProps['items'] = navItems[currentModule].map(item => ({
    key: item.href,
    icon: <TeamOutlined />,
    label: <Link href={item.href}>{item.label}</Link>,
  }));
  
  <Menu mode="inline" selectedKeys={[pathname]} items={items} />
  ```
- **GOTCHA**: Use `selectedKeys` (not `defaultSelectedKeys`) for controlled navigation
- **VALIDATE**: Active state highlights correctly, navigation works

### Task 8: Rewrite StatusBadge with Ant Tag
- **ACTION**: Replace custom badge CSS classes with Ant `Tag` component
- **IMPLEMENT**:
  ```tsx
  import { Tag } from 'antd';
  
  const colorMap: Record<string, string> = {
    ACTIVE: 'green', COMPLETED: 'green', APPROVED: 'green',
    PENDING: 'orange', PROCESSING: 'blue',
    TERMINATED: 'red', REJECTED: 'red', CANCELLED: 'red',
    DRAFT: 'default', INACTIVE: 'default',
  };
  
  <Tag color={colorMap[status.toUpperCase()] || 'default'}>{status}</Tag>
  ```
- **VALIDATE**: All status values render with appropriate colors

### Task 9: Delete DataTable.tsx, Modal.tsx, StatCard.tsx
- **ACTION**: Remove custom components that are fully replaced by Ant Design
- **VALIDATE**: No import references remain (will be updated in page tasks)

### Task 10: Rewrite Footer with Ant Layout.Footer
- **ACTION**: Replace custom footer with `Layout.Footer`
- **IMPLEMENT**: Minimal footer using Ant's Footer component
- **VALIDATE**: Footer renders at bottom of page

### Task 11: Update Login Page
- **ACTION**: Replace raw form with Ant `Form`, `Input`, `Button`, `Card`, `Typography`
- **IMPLEMENT**: Use `Form.useForm()`, `Form.Item` with `rules` for validation, `Input.Password` for password field, `App.useApp()` for notifications
- **GOTCHA**: Replace `toast.success/error` with `message.success/error` from `App.useApp()`
- **VALIDATE**: Login works, validation errors show inline, success/error notifications display

### Task 12: Update HR Dashboard Page
- **ACTION**: Replace `StatCard` with Ant `Card` + `Statistic`, replace inline SVGs with `@ant-design/icons`
- **IMPLEMENT**:
  ```tsx
  import { Card, Statistic, Row, Col } from 'antd';
  import { TeamOutlined, BankOutlined, CalendarOutlined, DollarOutlined } from '@ant-design/icons';
  
  <Row gutter={[24, 24]}>
    <Col xs={24} sm={12} lg={6}>
      <Card>
        <Statistic title="Total Employees" value={employees?.length} prefix={<TeamOutlined />} loading={loadingEmp} />
      </Card>
    </Col>
    ...
  </Row>
  ```
- **VALIDATE**: Stats display with icons and loading states

### Task 13: Update Employees Page (Template for all CRUD pages)
- **ACTION**: Replace DataTable → Ant Table, Modal → Ant Modal, form inputs → Ant Form
- **IMPLEMENT**:
  ```tsx
  import { Table, Modal, Form, Input, Select, Button, Space, App } from 'antd';
  import { PlusOutlined } from '@ant-design/icons';
  import type { ColumnsType } from 'antd/es/table';
  
  const columns: ColumnsType<Employee> = [
    { title: 'Name', dataIndex: 'first_name', 
      render: (_, record) => `${record.first_name} ${record.last_name}`,
      sorter: (a, b) => a.first_name.localeCompare(b.first_name) },
    { title: 'Email', dataIndex: 'email', sorter: true },
    { title: 'Department', dataIndex: 'department_name' },
    { title: 'Position', dataIndex: 'position' },
    { title: 'Status', dataIndex: 'status', 
      render: (status) => <StatusBadge status={status} />,
      filters: [{ text: 'Active', value: 'ACTIVE' }, ...] },
  ];
  
  <Table
    columns={columns}
    dataSource={data}
    loading={isLoading}
    rowKey="id"
    pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }}
  />
  
  <Modal title="Add Employee" open={isModalOpen} onCancel={close} footer={null}>
    <Form layout="vertical" onFinish={handleSubmit}>
      <Form.Item name="first_name" label="First Name" rules={[{ required: true }]}>
        <Input />
      </Form.Item>
      ...
      <Space style={{ display: 'flex', justifyContent: 'flex-end' }}>
        <Button onClick={close}>Cancel</Button>
        <Button type="primary" htmlType="submit">Save Employee</Button>
      </Space>
    </Form>
  </Modal>
  ```
- **GOTCHA**: Ant Form uses `onFinish` (not `onSubmit`), receives values object (not event). Must set `rowKey="id"` on Table.
- **VALIDATE**: Table renders with data, sorting works, modal opens/closes, form submits

### Task 14–17: Update Remaining Dashboard Pages
- **ACTION**: Apply Task 12 pattern to Inventory, Orders, Finance dashboard pages
- **IMPLEMENT**: Same pattern: `Card` + `Statistic` + `Row`/`Col` + appropriate icons
- **VALIDATE**: All 4 dashboards render with Ant components

### Task 18–28: Update Remaining CRUD Pages (11 pages)
- **ACTION**: Apply Task 13 pattern to: departments, leave, attendance, payroll, products, stock, categories, warehouses, sales, purchases, customers, suppliers, accounts, journals, invoices, budgets
- **IMPLEMENT**: Each page: swap DataTable → Table, Modal → Modal, form → Form. Customize columns for each entity type.
- **GOTCHA**: Leave page has approve/reject actions — use `Popconfirm` for destructive actions
- **VALIDATE**: Each page loads data, search works (use Table's built-in search), CRUD operations work

### Task 29: Final Cleanup
- **ACTION**: Remove any remaining references to deleted components, clean up unused CSS, verify no TypeScript errors
- **IMPLEMENT**: `npx tsc --noEmit`, search for dead imports
- **VALIDATE**: Full build succeeds with zero errors

---

## Testing Strategy

### Smoke Tests (Browser)

| Test | Expected | 
|---|---|
| Navigate to /login | Ant Design Form renders, validation works |
| Login with admin/admin123 | Redirects to /hr, notification shows |
| Navigate to /hr | Dashboard with 4 stat cards, chart renders |
| Navigate to /hr/employees | Ant Table with employee data, pagination |
| Click column header | Sorting works (ascending/descending) |
| Click "Add Employee" | Ant Modal opens with validated form |
| Submit empty form | Inline validation errors appear |
| Fill and submit form | Employee created, table refreshes |
| Navigate across all modules | All pages render without errors |
| Logout | Redirects to /login |

---

## Validation Commands

### Static Analysis
```bash
npx tsc --noEmit
```
EXPECT: Zero type errors

### Build
```bash
npm run build
```
EXPECT: Successful production build

### Dev Server
```bash
npm run dev
```
EXPECT: App loads at localhost:3000, all pages functional

### Manual Validation
- [ ] Login page renders with Ant Design components
- [ ] Dashboard stat cards use Ant Statistic
- [ ] All data tables use Ant Table with sort + pagination
- [ ] Modals use Ant Modal (animated, proper focus management)
- [ ] Forms use Ant Form with inline validation
- [ ] StatusBadge renders as Ant Tag with colors
- [ ] Sidebar uses Ant Menu with active state
- [ ] TopNav uses Ant Header with proper layout
- [ ] No `react-hot-toast` references remain
- [ ] No references to deleted components (DataTable, Modal, StatCard)
- [ ] No visual regressions on any page

---

## Acceptance Criteria
- [ ] All 21 pages updated to use Ant Design components
- [ ] Custom DataTable, Modal, StatCard components removed
- [ ] react-hot-toast removed, replaced with Ant notification/message
- [ ] All forms have inline validation
- [ ] Tables have sort + pagination + record count
- [ ] No TypeScript errors (`tsc --noEmit`)
- [ ] Production build succeeds (`npm run build`)
- [ ] All CRUD operations still work (create via modals, read via tables)
- [ ] Login/logout flow unchanged

## Completion Checklist
- [ ] Ant Design v6 + @ant-design/icons + @ant-design/nextjs-registry installed
- [ ] react-hot-toast removed
- [ ] AntdRegistry + ConfigProvider in root layout
- [ ] All `toast.*` calls replaced with `message.*` or `notification.*`
- [ ] All inline SVGs replaced with @ant-design/icons
- [ ] globals.css cleaned of custom component CSS
- [ ] No dead imports or references
- [ ] TypeScript builds clean
- [ ] Production build succeeds

## Risks
| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Ant Design CSS conflicts with Tailwind | Medium | Medium | Ant uses CSS-in-JS, Tailwind for layout only. Test early. |
| Bundle size increase | Medium | Low | antd is tree-shakeable. Monitor with `npm run build`. |
| AntdRegistry SSR issues with Turbopack | Low | Medium | Fall back to `--no-turbo` if needed |
| Chart.js style conflicts with Ant theme | Low | Low | Charts are isolated in canvas elements |
| Form submission pattern change | Low | High | Ant Form uses `onFinish(values)` not `onSubmit(event)` — careful migration |

## Notes
- **Ant Design v6** is chosen over v5 because it has native React 19 support without needing the `@ant-design/v5-patch-for-react-19` package.
- **Tailwind CSS is kept** for layout utilities (flex, grid, padding, margin). Ant handles component styling.
- **SWR is kept** as the data fetching layer. Ant Table's `loading` prop integrates cleanly with `isLoading` from SWR.
- **Chart.js is kept** for now. Can migrate to `@ant-design/charts` later as a separate effort.
- The icon mapping (inline SVG → @ant-design/icons) should use contextually appropriate icons. Reference: https://ant.design/components/icon
