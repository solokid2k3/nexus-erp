"use client";

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';
import { Layout } from 'antd';
import { TopNav } from '@/components/TopNav';
import { Sidebar } from '@/components/Sidebar';
import { Footer } from '@/components/Footer';
import { AppLoadingScreen } from '@/components/AppLoadingScreen';
import { PageTransition } from '@/components/PageTransition';
import { GlobalLoadingBar } from '@/components/GlobalLoadingBar';

const { Content } = Layout;

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !user) {
      router.push('/login');
    }
  }, [user, loading, router]);

  if (loading) {
    return <AppLoadingScreen />;
  }

  if (!user) {
    return null;
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <GlobalLoadingBar />
      <TopNav />
      <Layout>
        <Sidebar />
        <Content style={{ padding: 24, background: '#f5f5f5', overflow: 'auto' }}>
          <PageTransition>
            {children}
          </PageTransition>
        </Content>
      </Layout>
      <Footer />
    </Layout>
  );
}
