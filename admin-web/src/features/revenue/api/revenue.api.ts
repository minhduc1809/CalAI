import { api } from '../../../services/api-client';

export interface RevenueSummary {
  range: { from: string; to: string; days: number };
  group: 'day' | 'month';
  totals: { revenueVnd: number; paidOrders: number; averageOrderVnd: number | null; expiredUnpaid: number; canceled: number; pending: number; unpaidRatePct: number | null };
  premium: { newPayers: number; renewals: number; churned: number };
  series: { period: string; revenueVnd: number; orders: number }[];
  byProduct: { productId: string; orders: number; revenueVnd: number }[];
  googlePlay: { purchases: number; note: string };
}

export interface PaidOrder { id: string; code: string; productId: string; amount: number; paidAt: string; approvedBy: string | null; user: { email: string | null } }
export interface PaidOrderList { items: PaidOrder[]; pagination: { total: number; page: number; limit: number; totalPages: number } }

export const revenueApi = {
  summary: (from: string, to: string, group: string, signal?: AbortSignal) => api.get<RevenueSummary>('/admin/revenue/summary', { from, to, group }, signal),
  orders: (from: string, to: string, page: number, limit: number, signal?: AbortSignal) => api.get<PaidOrderList>('/admin/revenue/orders', { from, to, page, limit }, signal),
};
