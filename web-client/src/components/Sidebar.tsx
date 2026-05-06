"use client";

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Layout, Menu } from 'antd';
import {
  DashboardOutlined,
  TeamOutlined,
  BankOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  DollarOutlined,
  TagOutlined,
  AppstoreOutlined,
  InboxOutlined,
  WarningOutlined,
  ShoppingCartOutlined,
  ShoppingOutlined,
  UserOutlined,
  SolutionOutlined,
  CreditCardOutlined,
  BookOutlined,
  FileTextOutlined,
  PieChartOutlined,
} from '@ant-design/icons';

const { Sider } = Layout;

const iconMap: Record<string, React.ReactNode> = {
  'Dashboard': <DashboardOutlined />,
  'Employees': <TeamOutlined />,
  'Departments': <BankOutlined />,
  'Leave': <CalendarOutlined />,
  'Attendance': <ClockCircleOutlined />,
  'Payroll': <DollarOutlined />,
  'Products': <TagOutlined />,
  'Stock Alerts': <WarningOutlined />,
  'Categories': <AppstoreOutlined />,
  'Warehouses': <InboxOutlined />,
  'Sales Orders': <ShoppingCartOutlined />,
  'Purchase Orders': <ShoppingOutlined />,
  'Customers': <UserOutlined />,
  'Suppliers': <SolutionOutlined />,
  'Accounts': <CreditCardOutlined />,
  'Journals': <BookOutlined />,
  'Invoices': <FileTextOutlined />,
  'Budgets': <PieChartOutlined />,
};

const navItems: Record<string, { label: string; href: string }[]> = {
  hr: [
    { label: 'Dashboard', href: '/hr' },
    { label: 'Employees', href: '/hr/employees' },
    { label: 'Departments', href: '/hr/departments' },
    { label: 'Leave', href: '/hr/leave' },
    { label: 'Attendance', href: '/hr/attendance' },
    { label: 'Payroll', href: '/hr/payroll' },
  ],
  inventory: [
    { label: 'Dashboard', href: '/inventory' },
    { label: 'Products', href: '/inventory/products' },
    { label: 'Stock Alerts', href: '/inventory/stock' },
    { label: 'Categories', href: '/inventory/categories' },
    { label: 'Warehouses', href: '/inventory/warehouses' },
  ],
  orders: [
    { label: 'Dashboard', href: '/orders' },
    { label: 'Sales Orders', href: '/orders/sales' },
    { label: 'Purchase Orders', href: '/orders/purchases' },
    { label: 'Customers', href: '/orders/customers' },
    { label: 'Suppliers', href: '/orders/suppliers' },
  ],
  finance: [
    { label: 'Dashboard', href: '/finance' },
    { label: 'Accounts', href: '/finance/accounts' },
    { label: 'Journals', href: '/finance/journals' },
    { label: 'Invoices', href: '/finance/invoices' },
    { label: 'Budgets', href: '/finance/budgets' },
  ],
};

export function Sidebar() {
  const pathname = usePathname();
  const currentModule = pathname.split('/')[1] || 'hr';
  const links = navItems[currentModule] || [];

  const menuItems = links.map((link) => ({
    key: link.href,
    icon: iconMap[link.label] || <DashboardOutlined />,
    label: <Link href={link.href}>{link.label}</Link>,
  }));

  return (
    <Sider
      width={240}
      theme="light"
      style={{
        borderRight: '1px solid #f0f0f0',
        background: '#fff',
      }}
    >
      <Menu
        mode="inline"
        selectedKeys={[pathname]}
        items={menuItems}
        style={{ border: 'none', paddingTop: 8 }}
      />
    </Sider>
  );
}
