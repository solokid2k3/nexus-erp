"use client";

import React, { useEffect, useState } from 'react';
import { usePathname } from 'next/navigation';

export function PageTransition({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setMounted(true);
  }, []);

  if (!mounted) {
    return <div style={{ opacity: 0 }}>{children}</div>;
  }

  return (
    <div key={pathname} className="animate-slide-up" style={{ height: '100%' }}>
      {children}
    </div>
  );
}
