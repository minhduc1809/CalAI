export type OrderStatus = 'PENDING' | 'PAID' | 'EXPIRED' | 'CANCELED';

export interface QrOrder {
  id: string;
  code: string;
  productId: string;
  amount: number;
  status: OrderStatus;
  expiresAt: string;
  paidAt: string | null;
  approvedBy: string | null;
  adminNote: string | null;
  createdAt: string;
  user: { id: string; email: string | null; name: string | null };
}

export interface OrderHistoryItem {
  id: string;
  action: string;
  adminEmail: string | null;
  reason: string | null;
  createdAt: string;
}

export interface QrOrderDetail extends QrOrder {
  pastDue: boolean;
  history: OrderHistoryItem[];
}

export interface Paged<T> {
  items: T[];
  pagination: { total: number; page: number; limit: number; totalPages: number };
}
