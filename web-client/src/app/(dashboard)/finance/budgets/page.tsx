"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { Budget } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Input, InputNumber, Button, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function BudgetsPage() {
  const { data, isLoading, mutate } = useApiSWR<Budget[]>('/finance/budgets');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const columns: ColumnsType<Budget> = [
    { title: 'Name', dataIndex: 'name', sorter: (a, b) => a.name.localeCompare(b.name) },
    { title: 'Fiscal Year', dataIndex: 'year' },
    { title: 'Department', dataIndex: 'department_id', render: (v) => v || 'All' },
    { title: 'Total Budget', dataIndex: 'total_amount_cents', render: (v) => `$${(v / 100).toFixed(2)}`, sorter: (a, b) => a.total_amount_cents - b.total_amount_cents },
    { title: 'Status', dataIndex: 'status', render: (s) => <StatusBadge status={s} /> },
  ];

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      await apiPost('/finance/budgets', values);
      message.success('Budget created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Budgets</Title>
          <Text type="secondary">Manage financial budgets.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Create Budget</Button>
      </div>
      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />
      <Modal title="Create Budget" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}
          initialValues={{ year: new Date().getFullYear().toString(), total_amount_cents: 0 }}>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Form.Item name="year" label="Fiscal Year" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item name="total_amount_cents" label="Total (cents)"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Save</Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
}
