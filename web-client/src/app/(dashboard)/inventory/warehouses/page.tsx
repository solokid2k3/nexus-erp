"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { Warehouse } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Input, Button, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function WarehousesPage() {
  const { data, isLoading, mutate } = useApiSWR<Warehouse[]>('/inventory/warehouses');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const columns: ColumnsType<Warehouse> = [
    { title: 'Code', dataIndex: 'code', sorter: (a, b) => a.code.localeCompare(b.code) },
    { title: 'Name', dataIndex: 'name', sorter: (a, b) => a.name.localeCompare(b.name) },
    { title: 'City', dataIndex: 'city', render: (v) => v || '-' },
    { title: 'Country', dataIndex: 'country', render: (v) => v || '-' },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} /> },
  ];

  const handleSubmit = async (values: Record<string, string>) => {
    try {
      await apiPost('/inventory/warehouses', values);
      message.success('Warehouse created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Warehouses</Title>
          <Text type="secondary">Manage storage locations.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Add Warehouse</Button>
      </div>

      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />

      <Modal title="Add Warehouse" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Form.Item name="code" label="Code" rules={[{ required: true }]}><Input placeholder="e.g. WH-01" /></Form.Item>
            <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input placeholder="e.g. Main Warehouse" /></Form.Item>
          </div>
          <Form.Item name="address" label="Address"><Input placeholder="Full address" /></Form.Item>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Save</Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
}
