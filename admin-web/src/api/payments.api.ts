import { apiClient } from './client';
import { AdminPaymentOrder, PaginatedResponse, PaymentOrderStatus } from '../types';
import { mockPaymentOrders } from '../utils/mockData';

export interface PaymentOrderQueryParams {
  page?: number;
  limit?: number;
  status?: PaymentOrderStatus | 'ALL' | string;
  search?: string;
}

// In-memory mock storage
let localOrders: AdminPaymentOrder[] = [...mockPaymentOrders];

export const paymentsApi = {
  async getOrders(params?: PaymentOrderQueryParams): Promise<PaginatedResponse<AdminPaymentOrder>> {
    try {
      const apiParams: any = { ...params };
      if (apiParams.status === 'ALL') {
        delete apiParams.status;
      }
      const res = await apiClient.get('/admin/payments/orders', { params: apiParams });
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
      let filtered = [...localOrders];

      if (params?.status && params.status !== 'ALL') {
        filtered = filtered.filter((o) => o.status === params.status);
      }

      if (params?.search) {
        const query = params.search.toLowerCase();
        filtered = filtered.filter(
          (o) =>
            o.orderCode.toLowerCase().includes(query) ||
            o.userId.toLowerCase().includes(query) ||
            (o.user?.email && o.user.email.toLowerCase().includes(query)) ||
            (o.user?.name && o.user.name.toLowerCase().includes(query))
        );
      }

      const page = params?.page || 1;
      const limit = params?.limit || 20;
      const start = (page - 1) * limit;
      const paged = filtered.slice(start, start + limit);

      return {
        data: paged,
        meta: {
          page,
          limit,
          total: filtered.length,
          totalPages: Math.ceil(filtered.length / limit) || 1,
        },
      };
    }
  },

  async approveOrder(id: string): Promise<AdminPaymentOrder> {
    try {
      const res = await apiClient.post(`/admin/payments/orders/${id}/approve`);
      return res.data.order || res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const idx = localOrders.findIndex((o) => o.id === id);
        if (idx !== -1) {
          localOrders[idx] = {
            ...localOrders[idx],
            status: 'PAID',
            paidAmount: localOrders[idx].amount,
            paidAt: new Date().toISOString(),
          };
          return localOrders[idx];
        }
      }
      throw err;
    }
  },

  async rejectOrder(id: string, reason: string): Promise<AdminPaymentOrder> {
    try {
      const res = await apiClient.post(`/admin/payments/orders/${id}/reject`, { reason });
      return res.data.order || res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const idx = localOrders.findIndex((o) => o.id === id);
        if (idx !== -1) {
          localOrders[idx] = {
            ...localOrders[idx],
            status: 'CANCELLED',
            userNote: `${localOrders[idx].userNote ? localOrders[idx].userNote + ' | ' : ''}Từ chối: ${reason}`,
          };
          return localOrders[idx];
        }
      }
      throw err;
    }
  },
};
