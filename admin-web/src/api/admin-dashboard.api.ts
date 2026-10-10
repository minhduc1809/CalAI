import { apiClient } from './client';
import { AdminDashboardSummary } from '../types';
import { mockDashboardSummary } from '../utils/mockData';

export const adminDashboardApi = {
  async getSummary(): Promise<AdminDashboardSummary> {
    try {
      const res = await apiClient.get<{ message?: string; data: AdminDashboardSummary }>(
        '/admin/dashboard/summary'
      );
      return res.data.data || res.data;
    } catch {
      return mockDashboardSummary;
    }
  },
};
