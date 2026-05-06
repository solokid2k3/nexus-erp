"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { Customer } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Input, Button, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function CustomersPage() {
  const { data, isLoading, mutate } = useApiSWR<Customer[]>('/orders/customers');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const columns: ColumnsType<Customer> = [
    { title: 'Code', dataIndex: 'code', sorter: (a, b) => a.code.localeCompare(b.code) },
    { title: 'Company', dataIndex: 'company_name', sorter: (a, b) => a.company_name.localeCompare(b.company_name) },
    { title: 'Contact', dataIndex: 'contact_name', render: (v) => v || '-' },
    { title: 'Email', dataIndex: 'email', render: (v) => v || '-' },
    { title: 'City', dataIndex: 'billing_city', render: (v) => v || '-' },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} /> },
  ];

  const handleSubmit = async (values: Record<string, string>) => {
    try {
      await apiPost('/orders/customers', values);
      message.success('Customer created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Customers</Title>
          <Text type="secondary">Manage customer accounts.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Add Customer</Button>
      </div>

      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />

      <Modal title="Add Customer" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}>
          <Form.Item name="company_name" label="Company Name" rules={[{ required: true }]}><Input /></Form.Item>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Form.Item name="contact_name" label="Contact Name"><Input /></Form.Item>
            <Form.Item name="phone" label="Phone"><Input /></Form.Item>
          </div>
          <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}><Input /></Form.Item>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Form.Item name="billing_city" label="City"><Input /></Form.Item>
            <Form.Item name="billing_country" label="Country"><Input /></Form.Item>
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
