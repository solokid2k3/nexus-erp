"use client";

import React from 'react';
import { useApiSWR } from '@/lib/api';
import { Employee, Department, LeaveRequest, PayrollRun } from '@/lib/types';
import { Card, Statistic, Row, Col, Typography } from 'antd';
import { DashboardSkeleton } from '@/components/DashboardSkeleton';
import { TeamOutlined, BankOutlined, CalendarOutlined, DollarOutlined } from '@ant-design/icons';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Bar } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const { Title: AntTitle, Text } = Typography;

export default function HRDashboard() {
  const { data: employees, isLoading: loadingEmp } = useApiSWR<Employee[]>('/hr/employees');
  const { data: depts, isLoading: loadingDept } = useApiSWR<Department[]>('/hr/departments');
  const { data: leaves, isLoading: loadingLeaves } = useApiSWR<LeaveRequest[]>('/hr/leave/requests');
  const { data: payroll, isLoading: loadingPayroll } = useApiSWR<PayrollRun[]>('/hr/payroll');

  const isLoading = loadingEmp || loadingDept || loadingLeaves || loadingPayroll;

  if (isLoading) {
    return <DashboardSkeleton />;
  }

  const pendingLeaves = leaves?.filter(l => l.status === 'PENDING').length || 0;

  const chartData = {
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
    datasets: [{
      label: 'Attendance Rate (%)',
      data: [95, 96, 94, 98, 92],
      backgroundColor: 'rgba(59, 130, 246, 0.8)',
      borderRadius: 6,
    }],
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <AntTitle level={4} style={{ marginBottom: 4 }}>HR Dashboard</AntTitle>
        <Text type="secondary">Overview of human resources metrics.</Text>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Total Employees" value={employees?.length ?? '-'} prefix={<TeamOutlined />} loading={loadingEmp} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Departments" value={depts?.length ?? '-'} prefix={<BankOutlined />} loading={loadingDept} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Pending Leaves" value={pendingLeaves} prefix={<CalendarOutlined />} loading={loadingLeaves} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Payroll Runs" value={payroll?.length ?? '-'} prefix={<DollarOutlined />} loading={loadingPayroll} /></Card>
        </Col>
      </Row>

      <Card title="Weekly Attendance">
        <div style={{ height: 256 }}>
          <Bar data={chartData} options={{ responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, max: 100 } } }} />
        </div>
      </Card>
    </div>
  );
}
