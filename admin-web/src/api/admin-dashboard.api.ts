import { apiClient } from './client';
import { AdminDashboardSummary } from '../types';
import { mockDashboardSummary } from '../utils/mockData';

export const adminDashboardApi = {
  async getSummary(): Promise<AdminDashboardSummary> {
    try {
      let res;
      try {
        res = await apiClient.get<{ message?: string; data: any }>('/admin/dashboard');
      } catch (err: any) {
        if (err.response?.status === 404) {
          res = await apiClient.get<{ message?: string; data: any }>('/admin/dashboard/summary');
        } else {
          throw err;
        }
      }

      const raw = res.data?.data || res.data;
      if (!raw || !raw.users) {
        return mockDashboardSummary;
      }

      return {
        users: {
          total: Number(raw.users?.total ?? 0),
          newLast7Days: Number(raw.users?.newLast7Days ?? 0),
          newLast30Days: Number(raw.users?.newLast30Days ?? 0),
        },
        subscriptions: {
          activePremiums: Number(raw.subscriptions?.activePremiums ?? 0),
        },
        orders: {
          pending: Number(raw.orders?.pending ?? 0),
          paid: Number(raw.orders?.paid ?? 0),
        },
        revenue: {
          totalVnd: Number(raw.revenue?.totalVnd ?? raw.orders?.totalRevenueVnd ?? 0),
        },
      };
    } catch {
      return mockDashboardSummary;
    }
  },
};
