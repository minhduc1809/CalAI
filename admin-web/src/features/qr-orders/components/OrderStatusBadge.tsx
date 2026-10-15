import { StatusBadge } from '../../../components/data-display/parts';
import type { OrderStatus } from '../types/order.types';

const MAP: Record<OrderStatus, { label: string; tone: 'warning' | 'success' | 'neutral' | 'danger' }> = {
  PENDING: { label: 'Chờ duyệt', tone: 'warning' },
  PAID: { label: 'Đã thanh toán', tone: 'success' },
  EXPIRED: { label: 'Hết hạn', tone: 'neutral' },
  CANCELED: { label: 'Đã huỷ', tone: 'danger' },
};

export const ORDER_STATUS_OPTIONS = (Object.keys(MAP) as OrderStatus[]).map((v) => ({ value: v, label: MAP[v].label }));

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const m = MAP[status];
  return <StatusBadge tone={m.tone}>{m.label}</StatusBadge>;
}
