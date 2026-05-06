"use client";

import React from 'react';
import { useApiSWR } from '@/lib/api';
import { SalesOrder, PurchaseOrder, Customer, Supplier } from '@/lib/types';
import { Card, Statistic, Row, Col, Typography } from 'antd';
import { DashboardSkeleton } from '@/components/DashboardSkeleton';
import { ShoppingCartOutlined, ShoppingOutlined, UserOutlined, SolutionOutlined } from '@ant-design/icons';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const { Title: AntTitle, Text } = Typography;

export default function OrdersDashboard() {
  const { data: sales, isLoading: loadingSales } = useApiSWR<SalesOrder[]>('/orders/sales');
  const { data: purchases, isLoading: loadingPurchases } = useApiSWR<PurchaseOrder[]>('/orders/purchase');
  const { data: customers, isLoading: loadingCustomers } = useApiSWR<Customer[]>('/orders/customers');
  const { data: suppliers, isLoading: loadingSuppliers } = useApiSWR<Supplier[]>('/orders/suppliers');

  const isLoading = loadingSales || loadingPurchases || loadingCustomers || loadingSuppliers;

  if (isLoading) {
    return <DashboardSkeleton />;
  }

  const chartData = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [
      { label: 'Sales Revenue', data: [12000, 19000, 15000, 25000, 22000, 30000], borderColor: 'rgba(59, 130, 246, 1)', tension: 0.4 },
      { label: 'Purchase Costs', data: [8000, 12000, 10000, 18000, 15000, 20000], borderColor: 'rgba(239, 68, 68, 1)', tension: 0.4 },
    ],
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <AntTitle level={4} style={{ marginBottom: 4 }}>Orders Dashboard</AntTitle>
        <Text type="secondary">Overview of sales, purchases, and partners.</Text>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Sales Orders" value={sales?.length ?? '-'} prefix={<ShoppingCartOutlined />} loading={loadingSales} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Purchase Orders" value={purchases?.length ?? '-'} prefix={<ShoppingOutlined />} loading={loadingPurchases} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Customers" value={customers?.length ?? '-'} prefix={<UserOutlined />} loading={loadingCustomers} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Suppliers" value={suppliers?.length ?? '-'} prefix={<SolutionOutlined />} loading={loadingSuppliers} /></Card>
        </Col>
      </Row>

      <Card title="Financial Trends">
        <div style={{ height: 256 }}>
          <Line data={chartData} options={{ responsive: true, maintainAspectRatio: false }} />
        </div>
      </Card>
    </div>
  );
}
