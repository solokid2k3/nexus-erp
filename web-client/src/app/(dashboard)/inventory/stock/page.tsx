"use client";

import React from 'react';
import { useApiSWR } from '@/lib/api';
import { Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

interface StockAlert {
  product_id: string;
  product_name?: string;
  sku?: string;
  warehouse_id: string;
  warehouse_name?: string;
  quantity_on_hand: number;
  reorder_point: number;
  severity?: string;
}

export default function StockAlertsPage() {
  const { data: rawData, isLoading } = useApiSWR<StockAlert[] | Record<string, unknown>>('/inventory/stock/alerts');
  const data = Array.isArray(rawData) ? rawData : [];

  const columns: ColumnsType<StockAlert> = [
    { title: 'Product', key: 'product', render: (_, r) => r.product_name || r.product_id },
    { title: 'SKU', dataIndex: 'sku', render: (v) => v || '-' },
    { title: 'Warehouse', key: 'warehouse', render: (_, r) => r.warehouse_name || r.warehouse_id },
    {
      title: 'On Hand', dataIndex: 'quantity_on_hand',
      render: (v) => <Text strong type={v === 0 ? 'danger' : undefined}>{v}</Text>,
      sorter: (a, b) => a.quantity_on_hand - b.quantity_on_hand,
    },
    { title: 'Reorder Point', dataIndex: 'reorder_point' },
    {
      title: 'Severity', key: 'severity',
      render: (_, r) => {
        const severity = r.severity || (r.quantity_on_hand === 0 ? 'CRITICAL' : 'WARNING');
        return <Tag color={severity === 'CRITICAL' ? 'red' : 'orange'}>{severity}</Tag>;
      },
      filters: [{ text: 'Critical', value: 'CRITICAL' }, { text: 'Warning', value: 'WARNING' }],
      onFilter: (value, record) => (record.severity || (record.quantity_on_hand === 0 ? 'CRITICAL' : 'WARNING')) === value,
    },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <Title level={4} style={{ marginBottom: 4 }}>Stock Alerts</Title>
        <Text type="secondary">Products below minimum stock threshold.</Text>
      </div>
      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="product_id"
        locale={{ emptyText: 'No low-stock alerts — all products are above threshold.' }}
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} alerts` }} />
    </div>
  );
}
