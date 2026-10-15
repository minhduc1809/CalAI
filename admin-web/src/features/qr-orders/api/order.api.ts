import { api, Query } from '../../../services/api-client';
import type { Paged, QrOrder, QrOrderDetail } from '../types/order.types';

export const orderApi = {
  list: (q: Query, signal?: AbortSignal) => api.get<Paged<QrOrder>>('/admin/payments/orders', q, signal),
  detail: (id: string) => api.get<QrOrderDetail>(`/admin/payments/orders/${id}`),
  approve: (id: string, input: { password: string; note?: string }) => api.post(`/admin/payments/orders/${id}/approve`, input),
  reject: (id: string, input: { reason: string; password: string }) => api.post(`/admin/payments/orders/${id}/reject`, input),
  setNote: (id: string, note: string) => api.patch(`/admin/payments/orders/${id}/note`, { note }),
};
