"use client";

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/lib/auth';
import { Layout, Menu, Dropdown, Space, Typography, Avatar, Button } from 'antd';
import {
  AppstoreOutlined,
  TeamOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  UserOutlined,
  LogoutOutlined,
  DownOutlined,
} from '@ant-design/icons';

const { Header } = Layout;
const { Text } = Typography;

const moduleIcons: Record<string, React.ReactNode> = {
  hr: <TeamOutlined />,
  inventory: <AppstoreOutlined />,
  orders: <ShoppingCartOutlined />,
  finance: <DollarOutlined />,
};

const modules = [
  { key: 'hr', label: 'HR' },
  { key: 'inventory', label: 'Inventory' },
  { key: 'orders', label: 'Orders' },
  { key: 'finance', label: 'Finance' },
];

export function TopNav() {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const currentModule = pathname.split('/')[1] || 'hr';

  const menuItems = modules.map((m) => ({
    key: m.key,
    icon: moduleIcons[m.key],
    label: <Link href={`/${m.key}`}>{m.label}</Link>,
  }));

  const userMenuItems = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: logout,
    },
  ];

  return (
    <Header
      style={{
        background: 'rgba(255, 255, 255, 0.85)',
        backdropFilter: 'blur(12px)',
        WebkitBackdropFilter: 'blur(12px)',
        borderBottom: '1px solid rgba(0, 0, 0, 0.06)',
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 10,
        height: 56,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
        <Link href="/" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <div style={{ width: 24, height: 24, background: '#111', borderRadius: 6 }} />
          <Text strong style={{ fontSize: 16, letterSpacing: '-0.5px' }}>NEXUS ERP</Text>
        </Link>
        <Menu
          mode="horizontal"
          selectedKeys={[currentModule]}
          items={menuItems}
          style={{ border: 'none', lineHeight: '54px', minWidth: 400 }}
        />
      </div>

      <div>
        {user ? (
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size="small" icon={<UserOutlined />} style={{ background: '#111' }} />
              <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.3 }}>
                <Text strong style={{ fontSize: 13 }}>{user.name}</Text>
                <Text type="secondary" style={{ fontSize: 11, textTransform: 'uppercase' }}>{user.role}</Text>
              </div>
              <DownOutlined style={{ fontSize: 10 }} />
            </Space>
          </Dropdown>
        ) : (
          <Link href="/login">
            <Button type="primary" size="small">Login</Button>
          </Link>
        )}
      </div>
    </Header>
  );
}
