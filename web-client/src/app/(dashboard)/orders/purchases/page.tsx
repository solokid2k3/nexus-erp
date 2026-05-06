"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { PurchaseOrder, Supplier } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Select, Button, Space, DatePicker, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function PurchaseOrdersPage() {
  const { data, isLoading, mutate } = useApiSWR<PurchaseOrder[]>('/orders/purchase');
  const { data: suppliers } = useApiSWR<Supplier[]>('/orders/suppliers');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const columns: ColumnsType<PurchaseOrder> = [
    { title: 'PO #', dataIndex: 'po_number', sorter: (a, b) => a.po_number.localeCompare(b.po_number) },
    { title: 'Supplier', dataIndex: 'supplier_id' },
    { title: 'Total', dataIndex: 'total_amount_cents', render: (v) => `$${(v / 100).toFixed(2)}`, sorter: (a, b) => a.total_amount_cents - b.total_amount_cents },
    { title: 'Delivery Date', dataIndex: 'expected_delivery_date', render: (v) => v ? new Date(v).toLocaleDateString() : '-' },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} /> },
  ];

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      const payload = {
        supplier_id: values.supplier_id,
        expected_delivery_date: values.expected_delivery_date ? (values.expected_delivery_date as { format: (s: string) => string }).format('YYYY-MM-DD') : undefined,
      };
      await apiPost('/orders/purchase', payload);
      message.success('Purchase order created');
      setIsModalOpen(false);
      form.resetFields();
      mutate();
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : 'Error');
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <Title level={4} style={{ marginBottom: 4 }}>Purchase Orders</Title>
          <Text type="secondary">Manage supplier orders.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Create PO</Button>
      </div>

      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />

      <Modal title="Create Purchase Order" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}>
          <Form.Item name="supplier_id" label="Supplier" rules={[{ required: true }]}>
            <Select placeholder="Select a supplier" showSearch optionFilterProp="children">
              {suppliers?.map(s => <Select.Option key={s.id} value={s.id}>{s.company_name}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="expected_delivery_date" label="Expected Delivery Date">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Space style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Create</Button>
          </Space>
        </Form>
      </Modal>
    </div>
  );
}
