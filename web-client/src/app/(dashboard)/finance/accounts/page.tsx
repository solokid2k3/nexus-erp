"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { Account } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Input, Select, Button, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function AccountsPage() {
  const { data, isLoading, mutate } = useApiSWR<Account[]>('/finance/accounts');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const columns: ColumnsType<Account> = [
    { title: 'Account #', dataIndex: 'account_number', sorter: (a, b) => a.account_number.localeCompare(b.account_number) },
    { title: 'Name', dataIndex: 'name', sorter: (a, b) => a.name.localeCompare(b.name) },
    { title: 'Type', dataIndex: 'type',
      filters: [{ text: 'Asset', value: 'ASSET' }, { text: 'Liability', value: 'LIABILITY' }, { text: 'Equity', value: 'EQUITY' }, { text: 'Revenue', value: 'REVENUE' }, { text: 'Expense', value: 'EXPENSE' }],
      onFilter: (value, record) => record.type === value },
    { title: 'Category', dataIndex: 'category' },
    { title: 'Status', key: 'status', render: (_, r) => <StatusBadge status={r.is_active ? 'ACTIVE' : 'INACTIVE'} /> },
  ];

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      await apiPost('/finance/accounts', { ...values, is_active: true });
      message.success('Account created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Chart of Accounts</Title>
          <Text type="secondary">Manage general ledger accounts.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Add Account</Button>
      </div>
      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />
      <Modal title="Add Account" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }} initialValues={{ type: 'ASSET' }}>
          <Form.Item name="account_number" label="Account Number" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Form.Item name="type" label="Type"><Select options={[{ value: 'ASSET', label: 'Asset' }, { value: 'LIABILITY', label: 'Liability' }, { value: 'EQUITY', label: 'Equity' }, { value: 'REVENUE', label: 'Revenue' }, { value: 'EXPENSE', label: 'Expense' }]} /></Form.Item>
            <Form.Item name="category" label="Category" rules={[{ required: true }]}><Input /></Form.Item>
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
