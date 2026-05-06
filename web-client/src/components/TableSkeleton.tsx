import React from 'react';

export function TableSkeleton() {
  return (
    <div className="animate-fade-in" style={{ background: '#fff', borderRadius: 8, border: '1px solid #f0f0f0', overflow: 'hidden' }}>
      {/* Header */}
      <div style={{ display: 'flex', background: '#fafafa', padding: '16px 24px', borderBottom: '1px solid #f0f0f0' }}>
        {[1, 2, 3, 4, 5].map(i => (
          <div key={i} style={{ flex: 1 }}>
            <div className="animate-shimmer" style={{ width: '60%', height: 16, borderRadius: 4, background: 'var(--color-surface-strong)' }} />
          </div>
        ))}
      </div>
      
      {/* Rows */}
      {[1, 2, 3, 4, 5].map(row => (
        <div key={row} style={{ display: 'flex', padding: '16px 24px', borderBottom: row === 5 ? 'none' : '1px solid #f0f0f0' }}>
          {[1, 2, 3, 4, 5].map(col => (
            <div key={col} style={{ flex: 1 }}>
              <div className="animate-shimmer" style={{ width: col === 1 ? '40%' : '80%', height: 16, borderRadius: 4, background: 'var(--color-surface-strong)' }} />
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
