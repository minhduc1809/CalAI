import { apiClient } from './client';
import { OverviewStats, PaginatedResponse, ApiUsageLog, AiMessage } from '../types';
import { mockOverviewStats, mockAiUsageLogs, mockAiMessages } from '../utils/mockData';

export const statsApi = {
  async getOverview(): Promise<OverviewStats> {
    try {
      const res = await apiClient.get<{ data: OverviewStats }>('/admin/stats/overview');
      return res.data.data;
    } catch {
      return mockOverviewStats;
    }
  },

  async getAiUsageLogs(params?: {
    userId?: string;
    feature?: string;
    page?: number;
    limit?: number;
  }): Promise<PaginatedResponse<ApiUsageLog>> {
    try {
      const res = await apiClient.get('/admin/stats/ai-usage', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...mockAiUsageLogs];
      if (params?.feature) {
        filtered = filtered.filter((l) => l.feature === params.feature);
      }
      if (params?.userId) {
        filtered = filtered.filter((l) => l.userId === params.userId);
      }
      return {
        data: filtered,
        meta: {
          total: filtered.length,
          page: params?.page || 1,
          limit: params?.limit || 20,
          totalPages: 1,
        },
      };
    }
  },

  async getAiMessages(params?: {
    userId?: string;
    search?: string;
    page?: number;
    limit?: number;
  }): Promise<PaginatedResponse<AiMessage>> {
    try {
      const res = await apiClient.get('/admin/stats/ai-messages', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...mockAiMessages];
      if (params?.search) {
        filtered = filtered.filter((m) =>
          m.content.toLowerCase().includes(params.search!.toLowerCase())
        );
      }
      return {
        data: filtered,
        meta: {
          total: filtered.length,
          page: params?.page || 1,
          limit: params?.limit || 20,
          totalPages: 1,
        },
      };
    }
  },
};
