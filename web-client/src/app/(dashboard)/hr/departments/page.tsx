"use client";

import React, { useState } from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { Department } from '@/lib/types';
import { Table, Modal, Form, Input, Button, Typography, App } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function DepartmentsPage() {
  const { data, isLoading, mutate } = useApiSWR<Department[]>('/hr/departments');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const columns: ColumnsType<Department> = [
    { title: 'Code', dataIndex: 'code', sorter: (a, b) => a.code.localeCompare(b.code) },
    { title: 'Name', dataIndex: 'name', sorter: (a, b) => a.name.localeCompare(b.name) },
    { title: 'Manager ID', dataIndex: 'manager_id', render: (v) => v || '-' },
  ];

  const handleSubmit = async (values: Record<string, string>) => {
    try {
      await apiPost('/hr/departments', values);
      message.success('Department created');
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
          <Title level={4} style={{ marginBottom: 4 }}>Departments</Title>
          <Text type="secondary">Manage organizational departments.</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>Add Department</Button>
      </div>

      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />

      <Modal title="Add Department" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null} destroyOnHidden>
        <Form form={form} layout="vertical" onFinish={handleSubmit} requiredMark={false} style={{ marginTop: 16 }}>
          <Form.Item name="code" label="Code" rules={[{ required: true }]}><Input placeholder="e.g. ENG" /></Form.Item>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input placeholder="e.g. Engineering" /></Form.Item>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <Button onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="primary" htmlType="submit">Save</Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
}
