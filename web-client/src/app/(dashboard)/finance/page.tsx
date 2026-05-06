"use client";

import React from 'react';
import { useApiSWR } from '@/lib/api';
import { Account, JournalEntry, Invoice, Budget } from '@/lib/types';
import { Card, Statistic, Row, Col, Typography } from 'antd';
import { DashboardSkeleton } from '@/components/DashboardSkeleton';
import { CreditCardOutlined, BookOutlined, FileTextOutlined, PieChartOutlined } from '@ant-design/icons';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Bar } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const { Title: AntTitle, Text } = Typography;

export default function FinanceDashboard() {
  const { data: accounts, isLoading: loadingAccounts } = useApiSWR<Account[]>('/finance/accounts');
  const { data: journals, isLoading: loadingJournals } = useApiSWR<JournalEntry[]>('/finance/journal-entries');
  const { data: invoices, isLoading: loadingInvoices } = useApiSWR<Invoice[]>('/finance/invoices');
  const { data: budgets, isLoading: loadingBudgets } = useApiSWR<Budget[]>('/finance/budgets');

  const isLoading = loadingAccounts || loadingJournals || loadingInvoices || loadingBudgets;

  if (isLoading) {
    return <DashboardSkeleton />;
  }

  const chartData = {
    labels: ['Q1', 'Q2', 'Q3', 'Q4'],
    datasets: [
      { label: 'Revenue', data: [120000, 150000, 140000, 180000], backgroundColor: 'rgba(16, 185, 129, 0.8)', borderRadius: 6 },
      { label: 'Expenses', data: [80000, 90000, 85000, 100000], backgroundColor: 'rgba(239, 68, 68, 0.8)', borderRadius: 6 },
    ],
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <AntTitle level={4} style={{ marginBottom: 4 }}>Finance Dashboard</AntTitle>
        <Text type="secondary">Overview of general ledger, invoices, and budgets.</Text>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Active Accounts" value={accounts?.filter(a => a.is_active).length ?? '-'} prefix={<CreditCardOutlined />} loading={loadingAccounts} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Journal Entries" value={journals?.length ?? '-'} prefix={<BookOutlined />} loading={loadingJournals} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Total Invoices" value={invoices?.length ?? '-'} prefix={<FileTextOutlined />} loading={loadingInvoices} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Active Budgets" value={budgets?.filter(b => b.status === 'ACTIVE').length ?? '-'} prefix={<PieChartOutlined />} loading={loadingBudgets} /></Card>
        </Col>
      </Row>

      <Card title="Quarterly Revenue vs Expenses">
        <div style={{ height: 256 }}>
          <Bar data={chartData} options={{ responsive: true, maintainAspectRatio: false }} />
        </div>
      </Card>
    </div>
  );
}
