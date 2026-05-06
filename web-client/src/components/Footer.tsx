"use client";

import React from 'react';
import { Layout, Typography } from 'antd';

const { Footer: AntFooter } = Layout;
const { Text } = Typography;

export function Footer() {
  return (
    <AntFooter
      style={{
        background: '#101010',
        padding: '16px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 16, height: 16, background: '#fff', borderRadius: 3 }} />
        <Text strong style={{ color: '#fff', fontSize: 13 }}>Nexus ERP</Text>
      </div>
      <Text style={{ color: '#71717a', fontSize: 12 }}>
        &copy; {new Date().getFullYear()} Nexus ERP. All rights reserved.
      </Text>
    </AntFooter>
  );
}
