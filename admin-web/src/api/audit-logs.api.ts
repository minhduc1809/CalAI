import { apiClient } from './client';
import { AdminAuditLog, PaginatedResponse } from '../types';
import { mockAuditLogs } from '../utils/mockData';

export interface AuditLogQueryParams {
  page?: number;
  limit?: number;
  adminId?: string;
  action?: string;
  targetType?: string;
}

// In-memory mock storage
let localLogs: AdminAuditLog[] = [...mockAuditLogs];

export const auditLogsApi = {
  async getAuditLogs(params?: AuditLogQueryParams): Promise<PaginatedResponse<AdminAuditLog>> {
    try {
      const apiParams: any = { ...params };
      if (apiParams.action === 'ALL') delete apiParams.action;
      if (apiParams.targetType === 'ALL') delete apiParams.targetType;

      const res = await apiClient.get('/admin/audit-logs', { params: apiParams });
      const data = res.data.data || res.data;
      const pagination = res.data.pagination || res.data.meta || {
        page: params?.page || 1,
        limit: params?.limit || 20,
        total: Array.isArray(data) ? data.length : 0,
        totalPages: 1,
      };
      return {
        data: Array.isArray(data) ? data : [],
        meta: pagination,
      };
    } catch {
      let filtered = [...localLogs];

      if (params?.action && params.action !== 'ALL') {
        filtered = filtered.filter((l) => l.action === params.action);
      }
      if (params?.targetType && params.targetType !== 'ALL') {
        filtered = filtered.filter((l) => l.targetType === params.targetType);
      }
      if (params?.adminId) {
        filtered = filtered.filter((l) => l.adminId === params.adminId);
      }

      const page = params?.page || 1;
      const limit = params?.limit || 20;
      const start = (page - 1) * limit;

      return {
        data: filtered.slice(start, start + limit),
        meta: {
          page,
          limit,
          total: filtered.length,
          totalPages: Math.ceil(filtered.length / limit) || 1,
        },
      };
    }
  },
};
