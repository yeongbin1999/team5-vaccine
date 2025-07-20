'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useEffect } from 'react';
import { useCartStore } from '@/features/cart/cartStore';
import { useAuthStore } from '@/features/auth/authStore';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 5, // 5분
      gcTime: 100 * 60 * 10, // 10분
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export function QueryProvider({ children }: { children: React.ReactNode }) {
  // use the exported singleton queryClient

  const fetchCart = useCartStore(state => state.fetch);
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);

  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
    }
  }, [fetchCart, isAuthenticated]);

  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}
