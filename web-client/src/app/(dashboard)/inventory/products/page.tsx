"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { Product, Category } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Input, Select, InputNumber, Button, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function ProductsPage() {
  const { data, isLoading, mutate } = useApiSWR<Product[]>('/inventory/products');
  const { data: categories } = useApiSWR<Category[]>('/inventory/categories');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const columns: ColumnsType<Product> = [
    { title: 'SKU', dataIndex: 'sku', sorter: (a, b) => a.sku.localeCompare(b.sku) },
    { title: 'Name', dataIndex: 'name', sorter: (a, b) => a.name.localeCompare(b.name) },
    { title: 'Brand', dataIndex: 'brand', render: (v) => v || '-' },
    { title: 'UOM', dataIndex: 'unit_of_measure' },
    { title: 'Price', dataIndex: 'selling_price_cents', render: (v) => `$${(v / 100).toFixed(2)}`, sorter: (a, b) => a.selling_price_cents - b.selling_price_cents },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} />,
      filters: [{ text: 'Active', value: 'ACTIVE' }, { text: 'Inactive', value: 'INACTIVE' }],
      onFilter: (value, record) => record.status === value },
  ];

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      await apiPost('/inventory/products', values);
      message.success('Product created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Products</Title>
          <Text type="secondary">Manage product catalog.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Add Product</Button>
      </div>

      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />

      <Modal title="Add New Product" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}
          initialValues={{ unit_of_measure: 'PCS', unit_cost_cents: 0, selling_price_cents: 0 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Form.Item name="sku" label="SKU" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
          </div>
          <Form.Item name="category_id" label="Category" rules={[{ required: true }]}>
            <Select placeholder="Select a category">
              {categories?.map(c => <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>)}
            </Select>
          </Form.Item>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Form.Item name="unit_cost_cents" label="Cost (cents)"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
            <Form.Item name="selling_price_cents" label="Price (cents)"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Save Product</Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
}
