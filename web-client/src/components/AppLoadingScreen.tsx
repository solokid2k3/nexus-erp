"use client";

import React from 'react';
import { Typography } from 'antd';

const { Title, Text } = Typography;

export function AppLoadingScreen() {
  return (
    <div style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      flexDirection: 'column',
      alignItems: 'center', 
      justifyContent: 'center', 
      background: '#f5f5f5' 
    }}>
      <div 
        style={{ 
          width: 64, 
          height: 64, 
          background: '#111', 
          borderRadius: 14, 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center', 
          marginBottom: 24,
          animation: 'pulse-subtle 2s infinite ease-in-out'
        }}
      >
        <span style={{ color: '#fff', fontWeight: 700, fontSize: 32 }}>N</span>
      </div>
      <Title level={4} style={{ marginBottom: 8, letterSpacing: '-0.5px' }}>
        Nexus ERP
      </Title>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div style={{ 
          height: 4, 
          width: 64, 
          borderRadius: 2, 
          overflow: 'hidden',
          background: '#e5e7eb' 
        }}>
          <div className="animate-shimmer" style={{ height: '100%', width: '100%' }} />
        </div>
        <Text type="secondary" style={{ fontSize: 13 }}>Loading workspace...</Text>
      </div>
    </div>
  );
}
