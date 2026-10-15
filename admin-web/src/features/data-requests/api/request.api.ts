import { api, Query } from '../../../services/api-client';

export type RequestType = 'EXPORT' | 'DELETE' | 'COMPLAINT';
export type RequestStatus = 'NEW' | 'IN_PROGRESS' | 'DONE' | 'REJECTED';

export interface DataRequest {
  id: string; email: string; type: RequestType; note: string | null; status: RequestStatus;
  dueAt: string; overdue: boolean; userId: string | null; resultNote: string | null; createdAt: string; handledAt: string | null;
}
export interface DataRequestList { total: number; page: number; limit: number; items: DataRequest[] }

export const requestApi = {
  list: (q: Query, signal?: AbortSignal) => api.get<DataRequestList>('/admin/data-requests', q, signal),
  create: (input: { email: string; type: string; note?: string }) => api.post<{ matchedAccount: boolean }>('/admin/data-requests', input),
  update: (id: string, input: { status: string; resultNote?: string }) => api.patch(`/admin/data-requests/${id}`, input),
};
