import { apiClient } from './client';
import { AdminPaymentOrder, PaginatedResponse, PaymentOrderStatus } from '../types';

export interface PaymentOrderQueryParams {
  page?: number;
  limit?: number;
  status?: PaymentOrderStatus | 'ALL' | string;
  search?: string;
}

interface PaymentOrdersResponse {
  items: Record<string, unknown>[];
  pagination?: {
    page?: number;
    limit?: number;
    total?: number;
    totalPages?: number;
  };
}

function normalizeOrder(order: Record<string, any>): AdminPaymentOrder {
  const status = String(order.status || '').toUpperCase();
  const amount = Number(order.amount);
  const validStatuses: PaymentOrderStatus[] = [
    'PENDING',
    'PAID',
    'CANCELED',
    'CANCELLED',
    'FAILED',
    'EXPIRED',
  ];
  if (
    !order.id ||
    !order.userId ||
    !validStatuses.includes(status as PaymentOrderStatus) ||
    !Number.isFinite(amount) ||
    !(order.orderCode ?? order.code)
  ) {
    throw new Error('Phản hồi đơn thanh toán không đúng định dạng');
  }

  return {
    id: String(order.id),
    orderCode: String(order.orderCode ?? order.code),
    userId: String(order.userId),
    amount,
    currency: order.currency || 'VND',
    status: (status === 'CANCELED' ? 'CANCELED' : status) as PaymentOrderStatus,
    itemSku: order.itemSku ?? order.productId,
    userNote: order.userNote ?? null,
    paidAmount: order.paidAmount === undefined ? undefined : Number(order.paidAmount),
    paidAt: order.paidAt ?? null,
    createdAt: String(order.createdAt ?? ''),
    user: order.user
      ? {
          email: order.user.email ?? '',
          name: order.user.name ?? '',
          username: order.user.username,
        }
      : undefined,
  };
}

function extractErrorMessage(err: any): string {
  const data = err.response?.data;
  if (Array.isArray(data?.message)) return data.message.join(', ');
  if (typeof data?.message === 'string') return data.message;
  if (data?.code === 'ORDER_CANCELED') return 'Đơn đã bị hủy, không thể duyệt';
  if (data?.code === 'ORDER_TOO_OLD') return 'Đơn đã quá 7 ngày, không thể kích hoạt';
  if (data?.code === 'UNKNOWN_PRODUCT') return 'Gói sản phẩm không hợp lệ trong đơn hàng này';
  return data?.error || err.message || 'Lỗi không xác định';
}

export const paymentsApi = {
  async getOrders(params?: PaymentOrderQueryParams): Promise<PaginatedResponse<AdminPaymentOrder>> {
    const apiParams = { ...params };
    if (apiParams.status === 'ALL') {
      delete apiParams.status;
    } else if (apiParams.status === 'CANCELLED') {
      apiParams.status = 'CANCELED';
    }

    try {
      const res = await apiClient.get<{ data: PaymentOrdersResponse }>('/admin/payments/orders', {
        params: apiParams,
      });
      const result = res.data.data;
      const orders = result.items.map(normalizeOrder);
      const pagination = result.pagination ?? {};

      return {
        data: orders,
        meta: {
          page: pagination.page ?? params?.page ?? 1,
          limit: pagination.limit ?? params?.limit ?? 20,
          total: pagination.total ?? orders.length,
          totalPages: pagination.totalPages ?? (orders.length > 0 ? 1 : 0),
        },
      };
    } catch (err: any) {
      throw new Error(extractErrorMessage(err));
    }
  },

  async approveOrder(id: string): Promise<void> {
    try {
      await apiClient.post(`/admin/payments/orders/${encodeURIComponent(id)}/approve`);
    } catch (err: any) {
      throw new Error(extractErrorMessage(err));
    }
  },

  async rejectOrder(id: string, reason: string): Promise<void> {
    try {
      await apiClient.post(`/admin/payments/orders/${encodeURIComponent(id)}/reject`, { reason });
    } catch (err: any) {
      throw new Error(extractErrorMessage(err));
    }
  },
};
