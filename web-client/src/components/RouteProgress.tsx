"use client";

import React, { useEffect, useState } from 'react';
import { usePathname, useSearchParams } from 'next/navigation';

export function RouteProgress() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [isNavigating, setIsNavigating] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setIsNavigating(true);
    
    // Simulate navigation progress since app router doesn't expose route change events natively
    const timeout = setTimeout(() => {
      setIsNavigating(false);
    }, 400); 

    return () => clearTimeout(timeout);
  }, [pathname, searchParams]);

  if (!isNavigating) return null;

  return (
    <div 
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        height: 3,
        background: 'linear-gradient(90deg, #111, #555, #111)',
        backgroundSize: '200% 100%',
        zIndex: 9999,
        animation: 'shimmer 1s infinite linear, fadeIn 0.2s ease-out'
      }}
    />
  );
}
