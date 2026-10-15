import { api } from '../../../services/api-client';
import type { FunnelResponse, OverviewResponse, PendingWork, RetentionResponse, TimeSeriesResponse } from '../types/dashboard.types';

interface Range { from: string; to: string }

export const dashboardApi = {
  pendingWork: (signal?: AbortSignal) => api.get<PendingWork>('/admin/dashboard/pending', undefined, signal),
  overview: (r: Range, signal?: AbortSignal) => api.get<OverviewResponse>('/admin/dashboard/overview', { ...r }, signal),
  timeseries: (r: Range, signal?: AbortSignal) => api.get<TimeSeriesResponse>('/admin/dashboard/timeseries', { ...r }, signal),
  funnel: (r: Range, signal?: AbortSignal) => api.get<FunnelResponse>('/admin/dashboard/funnel', { ...r }, signal),
  retention: (r: Range, signal?: AbortSignal) => api.get<RetentionResponse>('/admin/dashboard/retention', { ...r }, signal),
};
