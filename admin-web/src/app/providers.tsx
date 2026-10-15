import { ReactNode } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HashRouter } from 'react-router-dom';
import { ToastProvider } from '../components/ui/Toast';
import { ApiError } from '../services/api-client';

/** Dữ liệu từ máy chủ do React Query quản lý: huỷ request cũ khi đổi bộ lọc, cache ngắn, không retry lỗi 4xx. */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 15_000,
      refetchOnWindowFocus: false,
      retry: (count, error) => !(error instanceof ApiError && error.status >= 400 && error.status < 500) && count < 1,
    },
  },
});

export function AppProviders({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <HashRouter>{children}</HashRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
}

export { queryClient };
