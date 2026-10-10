import { apiClient } from './client';
import { AdminDashboardSummary } from '../types';

interface DashboardSummaryResponse {
  users: {
    total: number;
    newLast7Days: number;
    newLast30Days: number;
  };
  subscriptions: {
    activePremiums: number;
  };
  orders: {
    pending: number;
    paid: number;
    totalRevenueVnd?: number;
  };
  revenue?: {
    totalVnd?: number;
  };
}

export const adminDashboardApi = {
  async getSummary(): Promise<AdminDashboardSummary> {
    const res = await apiClient.get<{ data?: DashboardSummaryResponse } & DashboardSummaryResponse>(
      '/admin/dashboard',
    );
    const raw = res.data.data ?? res.data;

    const metrics = [
      raw.users?.total,
      raw.users?.newLast7Days,
      raw.users?.newLast30Days,
      raw.subscriptions?.activePremiums,
      raw.orders?.pending,
      raw.orders?.paid,
      raw.revenue?.totalVnd ?? raw.orders?.totalRevenueVnd,
    ];
    if (
      !raw.users ||
      !raw.subscriptions ||
      !raw.orders ||
      metrics.some((value) => value === undefined || !Number.isFinite(Number(value)))
    ) {
      throw new Error('Phản hồi thống kê dashboard không đúng định dạng');
    }

    return {
      users: {
        total: Number(raw.users.total),
        newLast7Days: Number(raw.users.newLast7Days),
        newLast30Days: Number(raw.users.newLast30Days),
      },
      subscriptions: {
        activePremiums: Number(raw.subscriptions.activePremiums),
      },
      orders: {
        pending: Number(raw.orders.pending),
        paid: Number(raw.orders.paid),
      },
      revenue: {
        totalVnd: Number(raw.revenue?.totalVnd ?? raw.orders.totalRevenueVnd),
      },
    };
  },
};
