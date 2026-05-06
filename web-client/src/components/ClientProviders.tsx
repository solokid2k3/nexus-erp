"use client";

import React from 'react';
import { AuthProvider } from '@/lib/auth';
import { App } from 'antd';

export function ClientProviders({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider>
      <App>{children}</App>
    </AuthProvider>
  );
}
