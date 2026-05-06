"use client";

import React, { useSyncExternalStore, useRef, useEffect, useState } from 'react';
import { loadingStore } from '@/lib/loadingStore';

/**
 * Global API loading bar — shows a smooth animated progress bar at the top
 * of the viewport whenever any apiFetch call is in flight.
 */
export function GlobalLoadingBar() {
  const activeCount = useSyncExternalStore(
    loadingStore.subscribe,
    loadingStore.getSnapshot,
    () => 0, // SSR snapshot
  );

  const isLoading = activeCount > 0;

  // Track progress percentage for the animation
  const [progress, setProgress] = useState(0);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (isLoading) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setProgress(10);

      // Trickle: slowly increase progress to simulate progress
      intervalRef.current = setInterval(() => {
        setProgress((prev) => {
          if (prev >= 90) return prev;
          return prev + (90 - prev) * 0.1;
        });
      }, 300);
    } else {
      // Complete: jump to 100% then fade out
      setProgress(100);

      const timeout = setTimeout(() => {
        setProgress(0);
      }, 400);

      return () => clearTimeout(timeout);
    }

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isLoading]);

  if (progress === 0) return null;

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        height: 3,
        zIndex: 9999,
        pointerEvents: 'none',
      }}
    >
      <div
        style={{
          height: '100%',
          width: `${progress}%`,
          background: 'linear-gradient(90deg, #111 0%, #444 50%, #111 100%)',
          transition: progress === 100
            ? 'width 200ms ease-out, opacity 400ms ease-out 200ms'
            : 'width 300ms cubic-bezier(0.25, 1, 0.5, 1)',
          opacity: progress === 100 ? 0 : 1,
          boxShadow: '0 0 8px rgba(0, 0, 0, 0.15)',
        }}
      />
    </div>
  );
}
