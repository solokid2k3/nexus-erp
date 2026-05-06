import useSWR, { SWRConfiguration } from 'swr';
import { loadingStore } from './loadingStore';

export const API_BASE = '/api/v1';

export async function apiFetch(endpoint: string, options: RequestInit = {}) {
  const token = typeof window !== 'undefined' ? localStorage.getItem('access_token') : null;
  
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  loadingStore.start();

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        window.location.href = '/login';
      }
      throw new Error('Unauthorized');
    }

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.error || 'An error occurred');
    }

    return data;
  } finally {
    loadingStore.end();
  }
}

export function useApiSWR<T>(endpoint: string | null, options?: SWRConfiguration) {
  const fetcher = (url: string) => apiFetch(url);
  const swr = useSWR<T>(endpoint, fetcher, {
    revalidateOnFocus: false,
    shouldRetryOnError: false,
    onError: (err) => {
      if (err.message !== 'Unauthorized') {
        console.error(`Error loading data: ${err.message}`);
      }
    },
    ...options,
  });

  return swr;
}

export async function apiPost<T = unknown>(endpoint: string, body: unknown): Promise<T> {
  return apiFetch(endpoint, { method: 'POST', body: JSON.stringify(body) });
}

export async function apiPut<T = unknown>(endpoint: string, body: unknown): Promise<T> {
  return apiFetch(endpoint, { method: 'PUT', body: JSON.stringify(body) });
}

export async function apiDelete<T = unknown>(endpoint: string): Promise<T> {
  return apiFetch(endpoint, { method: 'DELETE' });
}

