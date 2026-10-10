import { apiClient } from './client';
import { AdminAuditLog, PaginatedResponse } from '../types';

export interface AuditLogQueryParams {
  page?: number;
  limit?: number;
  adminId?: string;
  action?: string;
  targetType?: string;
}

interface AuditLogsResponse {
  items: AdminAuditLog[];
  pagination?: {
    page?: number;
    limit?: number;
    total?: number;
    totalPages?: number;
  };
}

export const auditLogsApi = {
  async getAuditLogs(params?: AuditLogQueryParams): Promise<PaginatedResponse<AdminAuditLog>> {
    const apiParams = { ...params };
    if (apiParams.action === 'ALL') delete apiParams.action;
    if (apiParams.targetType === 'ALL') delete apiParams.targetType;

    const res = await apiClient.get<{ data: AuditLogsResponse }>('/admin/audit-logs', {
      params: apiParams,
    });
    const result = res.data.data;
    const pagination = result.pagination ?? {};

    return {
      data: result.items,
      meta: {
        page: pagination.page ?? params?.page ?? 1,
        limit: pagination.limit ?? params?.limit ?? 20,
        total: pagination.total ?? result.items.length,
        totalPages: pagination.totalPages ?? (result.items.length > 0 ? 1 : 0),
      },
    };
  },
};
