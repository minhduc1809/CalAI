import { apiClient } from './client';
import { WeightLog, PaginatedResponse } from '../types';
import { mockWeightLogs } from '../utils/mockData';

export interface WeightLogQueryParams {
  userId?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  limit?: number;
}

export interface CreateWeightLogPayload {
  userId: string;
  weightKg: number;
  date: string;
  notes?: string;
}

export interface UpdateWeightLogPayload {
  weightKg?: number;
  date?: string;
  notes?: string;
}

let localWeightLogs = [...mockWeightLogs];

export const weightLogsApi = {
  async getWeightLogs(params?: WeightLogQueryParams): Promise<PaginatedResponse<WeightLog>> {
    try {
      const res = await apiClient.get('/admin/weight-logs', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localWeightLogs];
      if (params?.userId) filtered = filtered.filter((w) => w.userId === params.userId);
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

  async createWeightLog(payload: CreateWeightLogPayload): Promise<WeightLog> {
    try {
      const res = await apiClient.post('/admin/weight-logs', payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const newLog: WeightLog = {
          id: `wl_${Date.now()}`,
          userId: payload.userId,
          weightKg: payload.weightKg,
          date: payload.date,
          notes: payload.notes,
          createdAt: new Date().toISOString(),
        };
        localWeightLogs.unshift(newLog);
        return newLog;
      }
      throw err;
    }
  },

  async updateWeightLog(id: string, payload: UpdateWeightLogPayload): Promise<WeightLog> {
    try {
      const res = await apiClient.patch(`/admin/weight-logs/${id}`, payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localWeightLogs = localWeightLogs.map((w) => (w.id === id ? { ...w, ...payload } : w));
        return localWeightLogs.find((w) => w.id === id)!;
      }
      throw err;
    }
  },

  async deleteWeightLog(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/weight-logs/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localWeightLogs = localWeightLogs.filter((w) => w.id !== id);
        return { message: 'Xóa bản ghi cân nặng thành công' };
      }
      throw err;
    }
  },
};
