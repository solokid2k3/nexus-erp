"use client";

import React from 'react';
import { Tag } from 'antd';

interface StatusBadgeProps {
  status: string;
}

const colorMap: Record<string, string> = {
  ACTIVE: 'green',
  COMPLETED: 'green',
  APPROVED: 'green',
  PAID: 'green',
  DELIVERED: 'green',
  PROCESSED: 'green',
  PENDING: 'orange',
  PROCESSING: 'blue',
  IN_PROGRESS: 'blue',
  TERMINATED: 'red',
  REJECTED: 'red',
  CANCELLED: 'red',
  FAILED: 'red',
  OVERDUE: 'red',
  DRAFT: 'default',
  ON_HOLD: 'default',
  INACTIVE: 'default',
};

export function StatusBadge({ status }: StatusBadgeProps) {
  const color = colorMap[status.toUpperCase()] || 'default';
  return <Tag color={color}>{status}</Tag>;
}
