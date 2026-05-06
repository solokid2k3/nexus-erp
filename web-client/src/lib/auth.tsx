"use client";

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { API_BASE } from './api';
import { User } from './types';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem('access_token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.exp * 1000 > Date.now()) {
          // eslint-disable-next-line react-hooks/set-state-in-effect
          setUser({
            id: payload.sub,
            name: payload.name,
            email: payload.email,
            role: payload.role,
          });
        } else {
          localStorage.removeItem('access_token');
          localStorage.removeItem('refresh_token');
        }
      } catch (e) {
        console.error('Failed to parse token', e);
        localStorage.removeItem('access_token');
      }
    }
    setLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await res.json();
    
    if (!res.ok) {
      throw new Error(data.error || 'Login failed');
    }

    localStorage.setItem('access_token', data.access_token);
    localStorage.setItem('refresh_token', data.refresh_token);

    const payload = JSON.parse(atob(data.access_token.split('.')[1]));
    setUser({
      id: payload.sub,
      name: payload.name,
      email: payload.email,
      role: payload.role,
    });
  };

  const logout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    setUser(null);
    router.push('/login');
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
