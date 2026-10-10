import React, { useEffect, useState } from 'react';
import {
  CreditCard,
  Search,
  CheckCircle2,
  XCircle,
  Copy,
  Check,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { paymentsApi } from '../api/payments.api';
import { AdminPaymentOrder } from '../types';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { Modal } from '../components/ui/Modal';
import { Button } from '../components/ui/Button';
import { formatCurrencyVnd, formatDateTimeVn } from '../utils/formatters';
import { toast } from 'sonner';

export const OrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<AdminPaymentOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [copiedText, setCopiedText] = useState<string | null>(null);

  // Approve Dialog State
  const [orderToApprove, setOrderToApprove] = useState<AdminPaymentOrder | null>(null);
  const [isApproving, setIsApproving] = useState(false);

  // Reject Modal State
  const [orderToReject, setOrderToReject] = useState<AdminPaymentOrder | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [isRejecting, setIsRejecting] = useState(false);

  const fetchOrders = async () => {
    setIsLoading(true);
    try {
      const res = await paymentsApi.getOrders({
        page,
        limit: 15,
        status: statusFilter,
        search: searchTerm.trim() || undefined,
      });
      setOrders(res.data);
      setTotalPages(res.meta.totalPages || 1);
      setTotalRecords(res.meta.total || 0);
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi tải danh sách đơn thanh toán');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [page, statusFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    fetchOrders();
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopiedText(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
    setTimeout(() => setCopiedText(null), 2000);
  };

  const handleConfirmApprove = async () => {
    if (!orderToApprove) return;
    setIsApproving(true);
    try {
      await paymentsApi.approveOrder(orderToApprove.id);
      toast.success(`Đã duyệt đơn ${orderToApprove.orderCode} và kích hoạt gói thành công`);
      setOrderToApprove(null);
      fetchOrders();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi duyệt đơn');
    } finally {
      setIsApproving(false);
    }
  };

  const handleConfirmReject = async () => {
    if (!orderToReject) return;
    if (rejectReason.trim().length < 5) {
      toast.error('Lý do từ chối đơn bắt buộc tối thiểu 5 ký tự');
      return;
    }

    setIsRejecting(true);
    try {
      await paymentsApi.rejectOrder(orderToReject.id, rejectReason.trim());
      toast.success(`Đã từ chối đơn ${orderToReject.orderCode}`);
      setOrderToReject(null);
      setRejectReason('');
      fetchOrders();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi từ chối đơn');
    } finally {
      setIsRejecting(false);
    }
  };

  const statusBadges: Record<string, { label: string; class: string }> = {
    PAID: {
      label: 'ĐÃ THANH TOÁN',
      class: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
    },
    PENDING: {
      label: 'CHỜ DUYỆT',
      class: 'bg-amber-500/15 text-amber-400 border-amber-500/30 animate-pulse',
    },
    CANCELED: {
      label: 'ĐÃ HỦY',
      class: 'bg-slate-700 text-slate-300 border-slate-600',
    },
    CANCELLED: {
      label: 'ĐÃ HỦY',
      class: 'bg-slate-700 text-slate-300 border-slate-600',
    },
    FAILED: {
      label: 'THẤT BẠI',
      class: 'bg-red-500/15 text-red-400 border-red-500/30',
    },
    EXPIRED: {
      label: 'HẾT HẠN',
      class: 'bg-slate-700/60 text-slate-400 border-slate-600',
    },
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-[#334155]">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-[#F8FAFC] flex items-center gap-2.5">
            <CreditCard className="w-6 h-6 text-emerald-400" />
            Vận Hành Đơn Thanh Toán VietQR
          </h1>
          <p className="text-xs text-[#94A3B8] mt-1">
            Tra cứu, kiểm soát gian lận, đối soát sao kê ngân hàng và phê duyệt kích hoạt gói Premium thủ công (BR-17.2)
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={fetchOrders}
            isLoading={isLoading}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
            className="border border-[#334155] text-xs text-[#F8FAFC]"
          >
            Làm mới
          </Button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 rounded-2xl bg-[#1E293B] border border-[#334155] flex flex-col md:flex-row items-center justify-between gap-3 shadow-sm">
        {/* Search */}
        <form onSubmit={handleSearchSubmit} className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-[#94A3B8] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo mã đơn hoặc email người dùng..."
            className="w-full bg-[#0F172A] border border-[#334155] rounded-xl pl-9 pr-4 py-2 text-xs text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all font-mono"
          />
        </form>

        {/* Status Filters */}
        <div className="flex items-center gap-1.5 overflow-x-auto w-full md:w-auto pb-1 md:pb-0">
          {[
            { id: 'ALL', label: 'Tất cả' },
            { id: 'PENDING', label: 'Chờ duyệt' },
            { id: 'PAID', label: 'Đã thanh toán' },
            { id: 'CANCELED', label: 'Đã hủy' },
            { id: 'EXPIRED', label: 'Hết hạn' },
          ].map((st) => (
            <button
              key={st.id}
              onClick={() => {
                setStatusFilter(st.id);
                setPage(1);
              }}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                statusFilter === st.id
                  ? 'bg-emerald-500 text-white shadow-glow'
                  : 'bg-[#0F172A] text-[#94A3B8] hover:text-[#F8FAFC] border border-[#334155]'
              }`}
            >
              {st.label}
            </button>
          ))}
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-[#1E293B] border border-[#334155] rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-[#334155] bg-[#0F172A]/80 text-[#94A3B8] uppercase text-[10px] tracking-wider font-semibold">
                <th className="py-3.5 px-4">Mã Đơn (Order Code)</th>
                <th className="py-3.5 px-4">Người Dùng</th>
                <th className="py-3.5 px-4">Gói SKU</th>
                <th className="py-3.5 px-4">Số Tiền (VND)</th>
                <th className="py-3.5 px-4">Thời Gian Tạo</th>
                <th className="py-3.5 px-4">Trạng Thái</th>
                <th className="py-3.5 px-4 text-right">Tác Nghiệp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#334155]">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#94A3B8]">
                    <div className="inline-block w-6 h-6 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin mb-2" />
                    <p>Đang tải dữ liệu đơn hàng...</p>
                  </td>
                </tr>
              ) : orders.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#94A3B8]">
                    <p className="text-sm font-semibold text-[#F8FAFC]">Không tìm thấy đơn hàng nào</p>
                    <p className="text-xs mt-1 text-[#94A3B8]">Thử thay đổi bộ lọc trạng thái hoặc từ khóa tìm kiếm</p>
                  </td>
                </tr>
              ) : (
                orders.map((order) => {
                  const badge = statusBadges[order.status] || {
                    label: order.status,
                    class: 'bg-slate-700 text-slate-300 border-slate-600',
                  };

                  return (
                    <tr key={order.id} className="hover:bg-[#0F172A]/40 transition-colors">
                      {/* Mã đơn */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2">
                          <span className="font-mono font-bold text-emerald-400 text-xs">
                            {order.orderCode}
                          </span>
                          <button
                            onClick={() => copyToClipboard(order.orderCode, 'mã đơn')}
                            className="p-1 rounded text-[#94A3B8] hover:text-[#F8FAFC] hover:bg-[#334155] transition-colors"
                            title="Sao chép mã đơn"
                          >
                            {copiedText === order.orderCode ? (
                              <Check className="w-3 h-3 text-emerald-400" />
                            ) : (
                              <Copy className="w-3 h-3" />
                            )}
                          </button>
                        </div>
                        {order.userNote && (
                          <p className="text-[10px] text-[#94A3B8] italic mt-0.5 truncate max-w-xs">
                            Ghi chú: {order.userNote}
                          </p>
                        )}
                      </td>

                      {/* Người dùng */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-1.5">
                          <p className="font-semibold text-[#F8FAFC] truncate max-w-[140px]">
                            {order.user?.name || order.user?.email || `User: ${order.userId.slice(0, 8)}`}
                          </p>
                          <button
                            onClick={() => copyToClipboard(order.userId, 'User ID')}
                            className="p-1 rounded text-[#94A3B8] hover:text-[#F8FAFC] transition-colors"
                            title="Sao chép User ID"
                          >
                            {copiedText === order.userId ? (
                              <Check className="w-3 h-3 text-emerald-400" />
                            ) : (
                              <Copy className="w-3 h-3" />
                            )}
                          </button>
                        </div>
                        <p className="text-[11px] text-[#94A3B8] font-mono truncate max-w-[160px]">
                          {order.user?.email || order.userId}
                        </p>
                      </td>

                      {/* Gói SKU */}
                      <td className="py-3.5 px-4 font-mono font-semibold text-[#F8FAFC]">
                        {order.itemSku || '—'}
                      </td>

                      {/* Số tiền */}
                      <td className="py-3.5 px-4 font-mono font-bold text-emerald-400 text-sm">
                        {formatCurrencyVnd(order.amount)}
                      </td>

                      {/* Thời gian tạo */}
                      <td className="py-3.5 px-4 text-[#94A3B8] font-mono text-[11px]">
                        {formatDateTimeVn(order.createdAt)}
                      </td>

                      {/* Trạng thái */}
                      <td className="py-3.5 px-4">
                        <span
                          className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold border ${badge.class}`}
                        >
                          {badge.label}
                        </span>
                      </td>

                      {/* Thao tác */}
                      <td className="py-3.5 px-4 text-right">
                        {order.status === 'PENDING' ? (
                          <div className="flex items-center justify-end gap-1.5">
                            <button
                              onClick={() => setOrderToApprove(order)}
                              className="px-2.5 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-xs shadow-glow transition-all flex items-center gap-1"
                              title="Duyệt đơn nạp tiền thủ công"
                            >
                              <CheckCircle2 className="w-3.5 h-3.5" />
                              <span>Duyệt</span>
                            </button>
                            <button
                              onClick={() => {
                                setOrderToReject(order);
                                setRejectReason('');
                              }}
                              className="px-2.5 py-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 font-bold text-xs transition-colors flex items-center gap-1"
                              title="Từ chối đơn"
                            >
                              <XCircle className="w-3.5 h-3.5" />
                              <span>Từ chối</span>
                            </button>
                          </div>
                        ) : order.status === 'PAID' ? (
                          <span className="text-[11px] text-emerald-400 font-mono">
                            Đã kích hoạt {order.paidAt ? formatDateTimeVn(order.paidAt).split(' - ')[0] : ''}
                          </span>
                        ) : (
                          <span className="text-[11px] text-[#94A3B8]">Đã kết thúc</span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="p-4 border-t border-[#334155] flex items-center justify-between text-xs text-[#94A3B8] bg-[#0F172A]/50">
          <div>
            Hiển thị <span className="font-bold text-[#F8FAFC]">{orders.length}</span> /{' '}
            <span className="font-bold text-[#F8FAFC]">{totalRecords}</span> đơn hàng
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page <= 1}
              className="p-1.5 rounded-lg bg-[#1E293B] border border-[#334155] text-[#F8FAFC] disabled:opacity-40 hover:bg-[#334155] transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-mono px-2">
              Trang <span className="text-[#F8FAFC] font-bold">{page}</span> / {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page >= totalPages}
              className="p-1.5 rounded-lg bg-[#1E293B] border border-[#334155] text-[#F8FAFC] disabled:opacity-40 hover:bg-[#334155] transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* CONFIRM APPROVE MODAL */}
      <ConfirmDialog
        isOpen={!!orderToApprove}
        onClose={() => setOrderToApprove(null)}
        onConfirm={handleConfirmApprove}
        title="Xác nhận Duyệt Đơn Nạp VietQR (BR-17.2)"
        message={`Bạn xác nhận duyệt đơn ${orderToApprove?.orderCode} với số tiền ${formatCurrencyVnd(orderToApprove?.amount)}? Hệ thống sẽ kích hoạt gói Premium tương ứng${orderToApprove?.itemSku ? ` SKU ${orderToApprove.itemSku}` : ''}, tự động cộng dồn ngày và ghi nhận vào Nhật ký kiểm toán.`}
        confirmLabel="Duyệt đơn ngay"
        cancelLabel="Hủy"
        isLoading={isApproving}
        isDestructive={false}
      />

      {/* REJECT MODAL WITH REASON */}
      <Modal
        isOpen={!!orderToReject}
        onClose={() => setOrderToReject(null)}
        title="Từ Chối / Hủy Đơn Thanh Toán"
        maxWidth="md"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => setOrderToReject(null)}
              disabled={isRejecting}
            >
              Hủy bỏ
            </Button>
            <Button
              variant="danger"
              onClick={handleConfirmReject}
              isLoading={isRejecting}
              disabled={rejectReason.trim().length < 5}
            >
              Xác nhận Từ chối
            </Button>
          </>
        }
      >
        <div className="space-y-4 py-2">
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-xs text-red-300">
            Đơn <span className="font-mono font-bold">{orderToReject?.orderCode}</span> sẽ chuyển sang trạng thái <span className="font-bold">CANCELLED</span>. Thao tác này sẽ ghi lại lý do vào Audit Log.
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#F8FAFC] mb-1.5">
              Lý do từ chối (bắt buộc $\ge 5$ ký tự):
            </label>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="VD: Khách chuyển khoản sai số tài khoản hoặc đơn quá hạn 24h..."
              rows={3}
              className="w-full bg-[#0F172A] border border-[#334155] rounded-xl p-3 text-xs text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500 transition-all"
            />
            <p className="text-[10px] text-[#94A3B8] mt-1 text-right">
              {rejectReason.trim().length}/5 ký tự tối thiểu
            </p>
          </div>
        </div>
      </Modal>
    </div>
  );
};
