"use client";

import React from 'react';
import { useApiSWR } from '@/lib/api';
import { Attendance } from '@/lib/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

export default function AttendancePage() {
  const { data, isLoading } = useApiSWR<Attendance[]>('/hr/attendance');

  const columns: ColumnsType<Attendance> = [
    { title: 'Employee', key: 'employee', render: (_, r) => r.employee_name || r.employee_id },
    { title: 'Date', dataIndex: 'date', render: (v) => v ? new Date(v).toLocaleDateString() : '-', sorter: (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime() },
    { title: 'Clock In', dataIndex: 'clock_in', render: (v) => v || '-' },
    { title: 'Clock Out', dataIndex: 'clock_out', render: (v) => v || '-' },
    { title: 'Status', dataIndex: 'status', render: (status) => <StatusBadge status={status} />,
      filters: [{ text: 'Present', value: 'PRESENT' }, { text: 'Absent', value: 'ABSENT' }, { text: 'Late', value: 'LATE' }],
      onFilter: (value, record) => record.status === value },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <Title level={4} style={{ marginBottom: 4 }}>Attendance</Title>
        <Text type="secondary">Track employee clock in/out times.</Text>
      </div>
      <Table columns={columns} dataSource={data} loading={isLoading} rowKey="id"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `${total} records` }} />
    </div>
  );
}
