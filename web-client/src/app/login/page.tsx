"use client";

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';
import { Form, Input, Button, Card, Typography, App, Divider } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;

export default function LoginPage() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login, user } = useAuth();
  const router = useRouter();
  const { message } = App.useApp();

  useEffect(() => {
    if (user) {
      router.push('/hr');
    }
  }, [user, router]);

  const handleSubmit = async (values: { username: string; password: string }) => {
    setIsSubmitting(true);
    try {
      await login(values.username, values.password);
      message.success('Login successful');
      router.push('/hr');
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5', padding: 16 }}>
      <Card style={{ width: '100%', maxWidth: 420, boxShadow: '0 1px 3px rgba(0,0,0,0.08)' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ width: 48, height: 48, background: '#111', borderRadius: 10, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', marginBottom: 16 }}>
            <span style={{ color: '#fff', fontWeight: 700, fontSize: 20 }}>N</span>
          </div>
          <Title level={3} style={{ marginBottom: 4 }}>Sign in to Nexus ERP</Title>
          <Text type="secondary">Enter your credentials to continue</Text>
        </div>

        <Form layout="vertical" onFinish={handleSubmit} requiredMark={false} size="large">
          <Form.Item name="username" label="Username" rules={[{ required: true, message: 'Please enter your username' }]}>
            <Input prefix={<UserOutlined />} placeholder="e.g. admin" />
          </Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true, message: 'Please enter your password' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="••••••••" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" block loading={isSubmitting}>
              Sign In
            </Button>
          </Form.Item>
        </Form>

        <Divider />

        <div>
          <Text type="secondary" style={{ fontSize: 12, fontWeight: 500 }}>Demo Accounts:</Text>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginTop: 8 }}>
            <Card size="small" style={{ background: '#fafafa' }}>
              <Text strong style={{ fontSize: 12, display: 'block' }}>Admin</Text>
              <Text type="secondary" style={{ fontSize: 11 }}>admin / admin123</Text>
            </Card>
            <Card size="small" style={{ background: '#fafafa' }}>
              <Text strong style={{ fontSize: 12, display: 'block' }}>Manager</Text>
              <Text type="secondary" style={{ fontSize: 11 }}>manager / manager123</Text>
            </Card>
          </div>
        </div>
      </Card>
    </div>
  );
}
