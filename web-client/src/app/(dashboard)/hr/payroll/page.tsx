"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { PayrollRun } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Button, Space, Typography, App, DatePicker, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function PayrollPage() {
  const { data, isLoading, mutate } = useApiSWR<PayrollRun[]>('/hr/payroll');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const handleAction = async (runId: string, action: 'calculate' | 'approve' | 'process') => {
    try {
      await apiPost(`/hr/payroll/${runId}/${action}`, {});
      message.success(`Payroll ${action}d`);
      mutate();
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : 'Error');
    }
  };

  const columns: ColumnsType<PayrollRun> = [
    { title: 'Run Number', dataIndex: 'run_number', sorter: (a, b) => a.run_number.localeCompare(b.run_number) },
    { title: 'Period Start', dataIndex: 'period_start', render: (v) => new Date(v).toLocaleDateString() },
    { title: 'Period End', dataIndex: 'period_end', render: (v) => new Date(v).toLocaleDateString() },
    { title: 'Total Amount', dataIndex: 'total_amount_cents', render: (v) => v ? `$${(v / 100).toFixed(2)}` : '-' },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} /> },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => {
        const status = record.status.toUpperCase();
        if (status === 'DRAFT') {
          return <Popconfirm title="Calculate payroll?" onConfirm={() => handleAction(record.id, 'calculate')}><Button type="primary" size="small">Calculate</Button></Popconfirm>;
        }
        if (status === 'CALCULATED') {
          return <Popconfirm title="Approve payroll?" onConfirm={() => handleAction(record.id, 'approve')}><Button type="primary" size="small">Approve</Button></Popconfirm>;
        }
        if (status === 'APPROVED') {
          return <Popconfirm title="Process payroll?" onConfirm={() => handleAction(record.id, 'process')}><Button type="primary" size="small">Process</Button></Popconfirm>;
        }
        return <Text type="secondary">—</Text>;
      },
    },
  ];

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      const payload = {
        period_start: values.period_start ? (values.period_start as { format: (s: string) => string }).format('YYYY-MM-DD') : '',
        period_end: values.period_end ? (values.period_end as { format: (s: string) => string }).format('YYYY-MM-DD') : '',
      };
      await apiPost('/hr/payroll', payload);
      message.success('Payroll run created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Payroll Runs</Title>
          <Text type="secondary">Manage salary disbursements.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Create Payroll Run</Button>
      </div>

      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />

      <Modal title="Create Payroll Run" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}>
          <Form.Item name="period_start" label="Period Start" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="period_end" label="Period End" rules={[{ required: true }]}>
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
