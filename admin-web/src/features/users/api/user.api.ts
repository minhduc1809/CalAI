import { api, Query } from '../../../services/api-client';
import type { UserBilling, UserDetail, UserList } from '../types/user.types';

interface Sensitive { reason: string; password: string }

export const userApi = {
  list: (q: Query, signal?: AbortSignal) => api.get<UserList>('/admin/users', q, signal),
  detail: (id: string, signal?: AbortSignal) => api.get<UserDetail>(`/admin/users/${id}`, undefined, signal),
  billing: (id: string, signal?: AbortSignal) => api.get<UserBilling>(`/admin/users/${id}/billing`, undefined, signal),
  lock: (id: string, s: Sensitive) => api.post(`/admin/users/${id}/lock`, s),
  unlock: (id: string, s: Sensitive) => api.post(`/admin/users/${id}/unlock`, s),
  revokeSessions: (id: string, s: Sensitive) => api.post(`/admin/users/${id}/revoke-sessions`, s),
  grant: (userId: string, days: number, reason: string, password: string) => api.post('/admin/billing/grants', { userId, days, reason, password }),
};
