"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { SalesOrder, Customer } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Select, Button, Space, Typography, App, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function SalesOrdersPage() {
  const { data, isLoading, mutate } = useApiSWR<SalesOrder[]>('/orders/sales');
  const { data: customers } = useApiSWR<Customer[]>('/orders/customers');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const handleAction = async (id: string, action: 'approve' | 'ship' | 'deliver') => {
    try {
      await apiPost(`/orders/sales/${id}/${action}`, {});
      message.success(`Order ${action}d`);
      mutate();
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : 'Error');
    }
  };

  const columns: ColumnsType<SalesOrder> = [
    { title: 'Order #', dataIndex: 'order_number', sorter: (a, b) => a.order_number.localeCompare(b.order_number) },
    { title: 'Customer', key: 'customer', render: (_, r) => r.customer_name || r.customer_id },
    { title: 'Total', dataIndex: 'total_amount_cents', render: (v) => `$${(v / 100).toFixed(2)}`, sorter: (a, b) => a.total_amount_cents - b.total_amount_cents },
    { title: 'Date', dataIndex: 'order_date', render: (v) => new Date(v).toLocaleDateString(), sorter: (a, b) => new Date(a.order_date).getTime() - new Date(b.order_date).getTime() },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} />,
      filters: [{ text: 'Draft', value: 'DRAFT' }, { text: 'Approved', value: 'APPROVED' }, { text: 'Shipped', value: 'SHIPPED' }, { text: 'Delivered', value: 'DELIVERED' }],
      onFilter: (value, record) => record.status === value },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => {
        const status = record.status.toUpperCase();
        if (status === 'DRAFT') return <Popconfirm title="Approve?" onConfirm={() => handleAction(record.id, 'approve')}><Button type="primary" size="small">Approve</Button></Popconfirm>;
        if (status === 'APPROVED' || status === 'PROCESSING') return <Popconfirm title="Ship?" onConfirm={() => handleAction(record.id, 'ship')}><Button type="primary" size="small">Ship</Button></Popconfirm>;
        if (status === 'SHIPPED') return <Popconfirm title="Mark delivered?" onConfirm={() => handleAction(record.id, 'deliver')}><Button type="primary" size="small">Deliver</Button></Popconfirm>;
        return <Text type="secondary">—</Text>;
      },
    },
  ];

  const handleSubmit = async (values: Record<string, string>) => {
    try {
      await apiPost('/orders/sales', values);
      message.success('Sales order created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Sales Orders</Title>
          <Text type="secondary">Manage customer orders.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Create Order</Button>
      </div>

      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />

      <Modal title="Create Sales Order" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}>
          <Form.Item name="customer_id" label="Customer" rules={[{ required: true }]}>
            <Select placeholder="Select a customer" showSearch optionFilterProp="children">
              {customers?.map(c => <Select.Option key={c.id} value={c.id}>{c.company_name}</Select.Option>)}
            </Select>
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
