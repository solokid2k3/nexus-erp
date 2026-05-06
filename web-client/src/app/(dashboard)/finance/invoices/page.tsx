"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { Invoice } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Input, InputNumber, Select, Button, Space, DatePicker, Popconfirm, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function InvoicesPage() {
  const { data, isLoading, mutate } = useApiSWR<Invoice[]>('/finance/invoices');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const handlePayment = async (invoiceId: string) => {
    try {
      await apiPost(`/finance/invoices/${invoiceId}/payment`, { amount_cents: 0 });
      message.success('Payment recorded');
      mutate();
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : 'Error');
    }
  };

  const columns: ColumnsType<Invoice> = [
    { title: 'Invoice #', dataIndex: 'invoice_number', sorter: (a, b) => a.invoice_number.localeCompare(b.invoice_number) },
    { title: 'Type', dataIndex: 'type', filters: [{ text: 'Receivable', value: 'RECEIVABLE' }, { text: 'Payable', value: 'PAYABLE' }], onFilter: (v, r) => r.type === v },
    { title: 'Entity ID', dataIndex: 'entity_id', render: (v) => v || '-' },
    { title: 'Amount', dataIndex: 'total_amount_cents', render: (v) => `$${(v / 100).toFixed(2)}`, sorter: (a, b) => a.total_amount_cents - b.total_amount_cents },
    { title: 'Due Date', dataIndex: 'due_date', render: (v) => new Date(v).toLocaleDateString() },
    { title: 'Status', dataIndex: 'status', render: (s) => <StatusBadge status={s} /> },
    {
      title: 'Actions', key: 'actions',
      render: (_, r) => {
        const s = r.status.toUpperCase();
        if (s === 'SENT' || s === 'OVERDUE') return <Popconfirm title="Record payment?" onConfirm={() => handlePayment(r.id)}><Button type="primary" size="small">Record Payment</Button></Popconfirm>;
        return <Text type="secondary">—</Text>;
      },
    },
  ];

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      await apiPost('/finance/invoices', {
        type: values.type,
        entity_id: values.entity_id,
        total_amount_cents: values.total_amount_cents,
        due_date: values.due_date ? (values.due_date as { format: (s: string) => string }).format('YYYY-MM-DD') : '',
      });
      message.success('Invoice created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Invoices</Title>
          <Text type="secondary">Manage receivable and payable invoices.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Create Invoice</Button>
      </div>
      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />
      <Modal title="Create Invoice" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }} initialValues={{ type: 'RECEIVABLE' }}>
          <Form.Item name="type" label="Type"><Select options={[{ value: 'RECEIVABLE', label: 'Receivable (AR)' }, { value: 'PAYABLE', label: 'Payable (AP)' }]} /></Form.Item>
          <Form.Item name="entity_id" label="Entity ID" rules={[{ required: true }]}><Input placeholder="Customer or Supplier ID" /></Form.Item>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Form.Item name="total_amount_cents" label="Amount (cents)" rules={[{ required: true }]}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
            <Form.Item name="due_date" label="Due Date" rules={[{ required: true }]}><DatePicker style={{ width: '100%' }} /></Form.Item>
          </div>
          <Space style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Save</Button>
          </Space>
        </Form>
      </Modal>
    </div>
  );
}
