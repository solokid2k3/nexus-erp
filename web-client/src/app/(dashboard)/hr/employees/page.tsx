"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { Employee, Department } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Modal, Form, Input, Select, Button, Space, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function EmployeesPage() {
  const { data, isLoading, mutate } = useApiSWR<Employee[]>('/hr/employees');
  const { data: depts } = useApiSWR<Department[]>('/hr/departments');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const columns: ColumnsType<Employee> = [
    {
      title: 'Name',
      key: 'name',
      render: (_, r) => `${r.first_name} ${r.last_name}`,
      sorter: (a, b) => a.first_name.localeCompare(b.first_name),
    },
    { title: 'Email', dataIndex: 'email', sorter: (a, b) => a.email.localeCompare(b.email) },
    { title: 'Department', dataIndex: 'department_name', render: (v) => v || '-' },
    { title: 'Position', dataIndex: 'position' },
    {
      title: 'Status', dataIndex: 'status',
      render: (status) => <StatusBadge status={status} />,
      filters: [
        { text: 'Active', value: 'ACTIVE' },
        { text: 'Inactive', value: 'INACTIVE' },
        { text: 'Terminated', value: 'TERMINATED' },
      ],
      onFilter: (value, record) => record.status === value,
    },
  ];

  const handleSubmit = async (values: Record<string, string>) => {
    try {
      await apiPost('/hr/employees', values);
      message.success('Employee created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Employees</Title>
          <Text type="secondary">Manage employee records and details.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Add Employee</Button>
      </div>

      <Table
        columns={columns}
        dataSource={data}
        loading={isLoading}
        rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }}
      />

      <Modal title="Add New Employee" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}>
          <Space.Compact style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 0 }}>
            <Form.Item name="first_name" label="First Name" rules={[{ required: true }]} style={{ marginRight: 8 }}>
              <Input />
            </Form.Item>
            <Form.Item name="last_name" label="Last Name" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
          </Space.Compact>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="position" label="Position" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="department_id" label="Department" rules={[{ required: true }]}>
            <Select placeholder="Select a department">
              {depts?.map(d => <Select.Option key={d.id} value={d.id}>{d.name}</Select.Option>)}
            </Select>
          </Form.Item>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Save Employee</Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
}
