import { apiClient } from './client';
import { AdminPaymentOrder, PaginatedResponse, PaymentOrderStatus } from '../types';
import { mockPaymentOrders } from '../utils/mockData';

export interface PaymentOrderQueryParams {
  page?: number;
  limit?: number;
  status?: PaymentOrderStatus | 'ALL' | string;
  search?: string;
}

// In-memory mock storage fallback
let localOrders: AdminPaymentOrder[] = [...mockPaymentOrders];

function normalizeOrder(o: any): AdminPaymentOrder {
  const rawStatus = (o.status || 'PENDING').toUpperCase();
  // Backend uses CANCELED (single L) — keep as-is
  const status: PaymentOrderStatus = rawStatus as PaymentOrderStatus;

  return {
    id: o.id,
    orderCode: o.orderCode || o.code || (o.id ? o.id.slice(0, 8).toUpperCase() : 'ORDER'),
    userId: o.userId,
    amount: Number(o.amount || 0),
    currency: o.currency || 'VND',
    status,
    itemSku: o.itemSku || o.productId || 'premium_monthly',
    userNote: o.userNote || null,
    paidAmount: o.paidAmount !== undefined ? o.paidAmount : (status === 'PAID' ? Number(o.amount || 0) : 0),
    paidAt: o.paidAt || null,
    createdAt: o.createdAt || new Date().toISOString(),
    user: o.user
      ? {
          email: o.user.email || '',
          name: o.user.name || '',
          username: o.user.username,
        }
      : undefined,
  };
}

/** Extract a readable error message from axios error response */
function extractErrorMessage(err: any): string {
  const data = err.response?.data;
  if (!data) return err.message || 'Lỗi không xác định';
  if (typeof data.message === 'string') return data.message;
  if (Array.isArray(data.message)) return data.message.join(', ');
  if (data.code === 'ORDER_CANCELED') return 'Đơn đã bị hủy, không thể duyệt';
  if (data.code === 'ORDER_TOO_OLD') return 'Đơn đã quá 7 ngày, không thể kích hoạt';
  if (data.code === 'UNKNOWN_PRODUCT') return 'Gói sản phẩm không hợp lệ trong đơn hàng này';
  return data.error || err.message || 'Lỗi không xác định';
}

export const paymentsApi = {
  async getOrders(params?: PaymentOrderQueryParams): Promise<PaginatedResponse<AdminPaymentOrder>> {
    try {
      const apiParams: any = { ...params };
      if (apiParams.status === 'ALL') {
        delete apiParams.status;
      }
      const res = await apiClient.get('/admin/payments/orders', { params: apiParams });
      const raw = res.data?.data || res.data;
      const rawItems = Array.isArray(raw) ? raw : (raw?.items || raw?.data || []);
      const pagination = raw?.pagination || res.data?.pagination || res.data?.meta || {
        page: params?.page || 1,
        limit: params?.limit || 20,
        total: rawItems.length,
        totalPages: 1,
      };

      const normalizedItems = rawItems.map(normalizeOrder);

      return {
        data: normalizedItems,
        meta: {
          page: Number(pagination.page || 1),
          limit: Number(pagination.limit || 20),
          total: Number(pagination.total || normalizedItems.length),
          totalPages: Number(pagination.totalPages || 1),
        },
      };
    } catch (err: any) {
      // Network error only → fall back to mock data
      if (err.code === 'ERR_NETWORK' || !err.response) {
        let filtered = [...localOrders];

        if (params?.status && params.status !== 'ALL') {
          const filterStatus = params.status;
          filtered = filtered.filter(
            (o) =>
              o.status === filterStatus ||
              // Match both spelling variants of canceled/cancelled
              (filterStatus === 'CANCELED' && o.status === 'CANCELLED') ||
              (filterStatus === 'CANCELLED' && o.status === 'CANCELED')
          );
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
      throw err;
    }
  },

  async approveOrder(id: string): Promise<AdminPaymentOrder> {
    try {
      const res = await apiClient.post(`/admin/payments/orders/${id}/approve`);
      const raw = res.data?.order || res.data?.data || res.data;
      return normalizeOrder(raw);
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        // Mock fallback for dev without backend
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
      // Re-throw with a readable Vietnamese error message
      throw new Error(extractErrorMessage(err));
    }
  },

  async rejectOrder(id: string, reason: string): Promise<AdminPaymentOrder> {
    try {
      const res = await apiClient.post(`/admin/payments/orders/${id}/reject`, { reason });
      const raw = res.data?.order || res.data?.data || res.data;
      return normalizeOrder(raw);
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const idx = localOrders.findIndex((o) => o.id === id);
        if (idx !== -1) {
          localOrders[idx] = {
            ...localOrders[idx],
            status: 'CANCELED',
            userNote: `${localOrders[idx].userNote ? localOrders[idx].userNote + ' | ' : ''}Từ chối: ${reason}`,
          };
          return localOrders[idx];
        }
      }
      throw new Error(extractErrorMessage(err));
    }
  },
};
