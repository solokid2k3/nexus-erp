"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { JournalEntry } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Input, InputNumber, Button, Space, DatePicker, Popconfirm, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function JournalsPage() {
  const { data, isLoading, mutate } = useApiSWR<JournalEntry[]>('/finance/journal-entries');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const handleAction = async (id: string, action: 'post' | 'reverse') => {
    try {
      await apiPost(`/finance/journal-entries/${id}/${action}`, {});
      message.success(`Entry ${action}ed`);
      mutate();
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : 'Error');
    }
  };

  const columns: ColumnsType<JournalEntry> = [
    { title: 'Entry #', dataIndex: 'entry_number', sorter: (a, b) => a.entry_number.localeCompare(b.entry_number) },
    { title: 'Date', dataIndex: 'entry_date', render: (v) => new Date(v).toLocaleDateString(), sorter: (a, b) => new Date(a.entry_date).getTime() - new Date(b.entry_date).getTime() },
    { title: 'Memo', dataIndex: 'memo', render: (v) => v || '-' },
    { title: 'Amount', dataIndex: 'total_amount_cents', render: (v) => `$${(v / 100).toFixed(2)}` },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} /> },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => {
        const status = record.status.toUpperCase();
        if (status === 'DRAFT') return <Popconfirm title="Post this entry?" onConfirm={() => handleAction(record.id, 'post')}><Button type="primary" size="small">Post</Button></Popconfirm>;
        if (status === 'POSTED') return <Popconfirm title="Reverse this entry?" onConfirm={() => handleAction(record.id, 'reverse')}><Button size="small" danger>Reverse</Button></Popconfirm>;
        return <Text type="secondary">—</Text>;
      },
    },
  ];

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      const payload = {
        entry_date: values.entry_date ? (values.entry_date as { format: (s: string) => string }).format('YYYY-MM-DD') : '',
        memo: values.memo,
        total_amount_cents: values.total_amount_cents,
      };
      await apiPost('/finance/journal-entries', payload);
      message.success('Journal entry created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Journal Entries</Title>
          <Text type="secondary">View accounting journal transactions.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Create Entry</Button>
      </div>
      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />
      <Modal title="Create Journal Entry" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}>
          <Form.Item name="entry_date" label="Date" rules={[{ required: true }]}><DatePicker style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="memo" label="Memo"><Input placeholder="Description of the entry" /></Form.Item>
          <Form.Item name="total_amount_cents" label="Total Amount (cents)" rules={[{ required: true }]}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Space style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Save</Button>
          </Space>
        </Form>
      </Modal>
    </div>
  );
}
