"use client";

import React from 'react';
import { useApiSWR } from '@/lib/api';
import { Product, Category, Warehouse } from '@/lib/types';
import { Card, Statistic, Row, Col, Typography } from 'antd';
import { DashboardSkeleton } from '@/components/DashboardSkeleton';
import { TagOutlined, AppstoreOutlined, InboxOutlined, WarningOutlined } from '@ant-design/icons';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend);

const { Title, Text } = Typography;

export default function InventoryDashboard() {
  const { data: products, isLoading: loadingProducts } = useApiSWR<Product[]>('/inventory/products');
  const { data: categories, isLoading: loadingCategories } = useApiSWR<Category[]>('/inventory/categories');
  const { data: warehouses, isLoading: loadingWarehouses } = useApiSWR<Warehouse[]>('/inventory/warehouses');
  const { data: stockAlerts, isLoading: loadingAlerts } = useApiSWR<unknown[]>('/inventory/stock/alerts');

  const isLoading = loadingProducts || loadingCategories || loadingWarehouses || loadingAlerts;

  if (isLoading) {
    return <DashboardSkeleton />;
  }

  const chartData = {
    labels: ['Active Products', 'Inactive Products'],
    datasets: [{
      data: [
        products?.filter(p => p.status === 'ACTIVE').length || 0,
        products?.filter(p => p.status !== 'ACTIVE').length || 0,
      ],
      backgroundColor: ['rgba(16, 185, 129, 0.8)', 'rgba(209, 213, 219, 0.8)'],
      borderWidth: 0,
    }],
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <Title level={4} style={{ marginBottom: 4 }}>Inventory Dashboard</Title>
        <Text type="secondary">Overview of products, stock, and warehouses.</Text>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Products" value={products?.length ?? '-'} prefix={<TagOutlined />} loading={loadingProducts} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Categories" value={categories?.length ?? '-'} prefix={<AppstoreOutlined />} loading={loadingCategories} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Warehouses" value={warehouses?.length ?? '-'} prefix={<InboxOutlined />} loading={loadingWarehouses} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Low Stock Items" value={stockAlerts?.length ?? 0} prefix={<WarningOutlined />} loading={loadingAlerts} /></Card>
        </Col>
      </Row>

      <Card title="Product Status Distribution" style={{ display: 'flex', justifyContent: 'center' }}>
        <div style={{ width: 256, height: 256, margin: '0 auto' }}>
          <Doughnut data={chartData} options={{ maintainAspectRatio: false }} />
        </div>
      </Card>
    </div>
  );
}
