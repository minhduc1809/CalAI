import { api, Query } from '../../../services/api-client';

export interface Grant {
  id: string;
  user: { id: string; email: string | null; name: string | null };
  source: 'ADMIN' | 'QR_ORDER' | string;
  orderCode: string | null;
  grantedBy: string | null;
  reason: string;
  startsAt: string;
  endsAt: string;
  revokedAt: string | null;
  status: 'ACTIVE' | 'EXPIRED' | 'REVOKED';
  remainingDays: number;
}

export interface GrantList { items: Grant[]; pagination: { total: number; page: number; limit: number; totalPages: number } }

export const grantApi = {
  list: (q: Query, signal?: AbortSignal) => api.get<GrantList>('/admin/billing/grants', q, signal),
  create: (input: { userId: string; days: number; reason: string; password: string }) => api.post('/admin/billing/grants', input),
  revoke: (id: string, input: { reason: string; password: string }) => api.post(`/admin/billing/grants/${id}/revoke`, input),
};
