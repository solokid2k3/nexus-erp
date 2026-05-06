"use client";

import React from 'react';
import { useApiSWR, apiPost } from '@/lib/api';
import { LeaveRequest } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Button, Space, Popconfirm, Typography, App } from 'antd';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function LeavePage() {
  const { data, isLoading, mutate } = useApiSWR<LeaveRequest[]>('/hr/leave/requests');
  const { message } = App.useApp();

  const handleAction = async (id: string, action: 'approve' | 'reject') => {
    try {
      await apiPost(`/hr/leave/${id}/${action}`, {});
      message.success(`Leave request ${action}d`);
      mutate();
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : 'Error');
    }
  };

  const columns: ColumnsType<LeaveRequest> = [
    { title: 'Employee', key: 'employee', render: (_, r) => r.employee_name || r.employee_id },
    { title: 'Type', key: 'type', render: (_, r) => r.leave_type || r.leave_type_id },
    { title: 'Start Date', dataIndex: 'start_date', render: (v) => new Date(v).toLocaleDateString(), sorter: (a, b) => new Date(a.start_date).getTime() - new Date(b.start_date).getTime() },
    { title: 'End Date', dataIndex: 'end_date', render: (v) => new Date(v).toLocaleDateString() },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} />,
      filters: [{ text: 'Pending', value: 'PENDING' }, { text: 'Approved', value: 'APPROVED' }, { text: 'Rejected', value: 'REJECTED' }],
      onFilter: (value, record) => record.status === value },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => {
        if (record.status === 'PENDING') {
          return (
            <Space>
              <Popconfirm title="Approve this request?" onConfirm={() => handleAction(record.id, 'approve')}>
                <Button type="primary" size="small">Approve</Button>
              </Popconfirm>
              <Popconfirm title="Reject this request?" onConfirm={() => handleAction(record.id, 'reject')}>
                <Button size="small" danger>Reject</Button>
              </Popconfirm>
            </Space>
          );
        }
        return <Text type="secondary">—</Text>;
      },
    },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <Title level={4} style={{ marginBottom: 4 }}>Leave Requests</Title>
        <Text type="secondary">Review and manage time off.</Text>
      </div>

      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />
    </div>
  );
}
