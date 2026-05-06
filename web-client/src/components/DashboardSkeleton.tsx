import React from 'react';
import { Row, Col, Card } from 'antd';

export function DashboardSkeleton() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }} className="animate-fade-in">
      <div>
        <div className="animate-shimmer" style={{ width: 200, height: 28, borderRadius: 6, marginBottom: 8, background: 'var(--color-surface-strong)' }} />
        <div className="animate-shimmer" style={{ width: 300, height: 20, borderRadius: 6, background: 'var(--color-surface-strong)' }} />
      </div>

      <Row gutter={[16, 16]}>
        {[1, 2, 3, 4].map(key => (
          <Col xs={24} sm={12} lg={6} key={key}>
            <Card>
              <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                <div className="animate-shimmer" style={{ width: 48, height: 48, borderRadius: '50%', background: 'var(--color-surface-strong)' }} />
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
                  <div className="animate-shimmer" style={{ width: '60%', height: 16, borderRadius: 4, background: 'var(--color-surface-strong)' }} />
                  <div className="animate-shimmer" style={{ width: '40%', height: 24, borderRadius: 4, background: 'var(--color-surface-strong)' }} />
                </div>
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      <Card>
        <div className="animate-shimmer" style={{ width: 150, height: 24, borderRadius: 6, marginBottom: 24, background: 'var(--color-surface-strong)' }} />
        <div className="animate-shimmer" style={{ width: '100%', height: 256, borderRadius: 8, background: 'var(--color-surface-strong)' }} />
      </Card>
    </div>
  );
}
