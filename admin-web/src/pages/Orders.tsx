import React, { useEffect, useState, useMemo } from 'react';
import {
  CreditCard,
  Search,
  Filter,
  Check,
  X,
  Copy,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  RefreshCw,
  Eye,
  ShieldCheck,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { paymentsApi, PaymentOrderQueryParams } from '../api/payments.api';
import { AdminPaymentOrder, PaymentOrderStatus } from '../types';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatCurrencyVnd, formatDateTimeVn, formatNumber } from '../utils/formatters';
import { toast } from 'sonner';

export const OrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<AdminPaymentOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);

  // Modals state
  const [selectedOrder, setSelectedOrder] = useState<AdminPaymentOrder | null>(null);
  const [isApproveOpen, setIsApproveOpen] = useState(false);
  const [isRejectOpen, setIsRejectOpen] = useState(false);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const fetchOrders = async (targetPage = page) => {
    setIsLoading(true);
    try {
      const res = await paymentsApi.getOrders({
        page: targetPage,
        limit: 15,
        status: statusFilter,
        search,
      });
      setOrders(res.data);
      setPage(res.meta.page);
      setTotalPages(res.meta.totalPages);
      setTotalRecords(res.meta.total);
    } catch (err: any) {
      toast.error('Lỗi khi tải danh sách đơn thanh toán VietQR');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    fetchOrders(1);
  }, [statusFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchOrders(1);
  };

  const handleRefresh = () => {
    setIsRefreshing(true);
    fetchOrders(page).then(() => toast.success('Đã làm mới danh sách đơn'));
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
  };

  // Quick stats
  const pendingCount = useMemo(
    () => orders.filter((o) => o.status === 'PENDING').length,
    [orders]
  );
  const paidCount = useMemo(
    () => orders.filter((o) => o.status === 'PAID').length,
    [orders]
  );

  // Approve Handler
  const handleApprove = async () => {
    if (!selectedOrder) return;
    setActionLoading(true);
    try {
      await paymentsApi.approveOrder(selectedOrder.id);
      toast.success(`Đã duyệt đơn ${selectedOrder.orderCode} & kích hoạt gói Premium!`);
      setIsApproveOpen(false);
      setSelectedOrder(null);
      fetchOrders(page);
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi duyệt đơn');
    } finally {
      setActionLoading(false);
    }
  };

  // Reject Handler
  const handleReject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedOrder) return;
    if (rejectReason.trim().length < 5) {
      toast.error('Lý do từ chối phải có tối thiểu 5 ký tự');
      return;
    }
    setActionLoading(true);
    try {
      await paymentsApi.rejectOrder(selectedOrder.id, rejectReason.trim());
      toast.success(`Đã từ chối đơn ${selectedOrder.orderCode}`);
      setIsRejectOpen(false);
      setSelectedOrder(null);
      setRejectReason('');
      fetchOrders(page);
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi từ chối đơn');
    } finally {
      setActionLoading(false);
    }
  };

  const renderStatusBadge = (status: PaymentOrderStatus) => {
    switch (status) {
      case 'PAID':
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 inline-flex items-center gap-1.5">
            <CheckCircle2 className="w-3.5 h-3.5" />
            PAID
          </span>
        );
      case 'PENDING':
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30 inline-flex items-center gap-1.5 animate-pulse">
            <Clock className="w-3.5 h-3.5" />
            PENDING
          </span>
        );
      case 'CANCELLED':
      case 'EXPIRED':
      case 'FAILED':
      default:
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-slate-700/60 text-slate-300 border border-slate-600 inline-flex items-center gap-1.5">
            <XCircle className="w-3.5 h-3.5" />
            {status}
          </span>
        );
    }
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="p-2 rounded-xl bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
              <CreditCard className="w-5 h-5" />
            </span>
            <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">
              Quản Lý Đơn Thanh Toán VietQR
            </h1>
          </div>
          <p className="text-xs text-slate-400 max-w-xl">
            Theo dõi, tra cứu và duyệt các giao dịch nạp tiền qua mã QR động (VietQR). Kích hoạt gói tức thì hoặc đối soát thủ công theo <b className="text-slate-300">BR-17.2</b>.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            disabled={isRefreshing}
            className="px-3.5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 text-xs font-bold transition-all flex items-center gap-2 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-emerald-400' : ''}`} />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      {/* Filter Tabs & Search Controls */}
      <div className="p-4 sm:p-5 rounded-2xl bg-[#1E293B]/60 border border-slate-700/60 flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Status Tabs */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 md:pb-0">
          {[
            { key: 'ALL', label: 'Tất cả đơn' },
            { key: 'PENDING', label: 'Chờ duyệt', badge: pendingCount > 0 ? pendingCount : undefined },
            { key: 'PAID', label: 'Đã nhận tiền' },
            { key: 'CANCELLED', label: 'Đã hủy / Quá hạn' },
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setStatusFilter(tab.key)}
              className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all shrink-0 flex items-center gap-2 ${
                statusFilter === tab.key
                  ? 'bg-emerald-500 text-white shadow-glow'
                  : 'bg-slate-800/80 hover:bg-slate-700 text-slate-300 border border-slate-700/60'
              }`}
            >
              <span>{tab.label}</span>
              {tab.badge !== undefined && (
                <span className="px-1.5 py-0.2 rounded-full text-[10px] font-extrabold bg-amber-400 text-slate-900">
                  {tab.badge}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearchSubmit} className="flex items-center gap-2 min-w-[280px]">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Tìm mã đơn, User ID, Email..."
              className="w-full pl-9 pr-3 py-2 bg-slate-900/80 border border-slate-700 rounded-xl text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <Button type="submit" variant="primary" className="text-xs shrink-0">
            Tìm
          </Button>
        </form>
      </div>

      {/* Orders Table */}
      <div className="rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg overflow-hidden">
        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center gap-3">
            <div className="w-8 h-8 rounded-full border-2 border-emerald-500 border-t-transparent animate-spin" />
            <span className="text-xs text-slate-400">Đang tải danh sách đơn thanh toán...</span>
          </div>
        ) : orders.length === 0 ? (
          <div className="py-16 text-center">
            <EmptyState
              title="Không tìm thấy đơn thanh toán nào"
              description="Thử đổi bộ lọc trạng thái hoặc từ khóa tìm kiếm mã đơn VietQR."
            />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-[11px] uppercase font-bold text-slate-400 bg-slate-900/60 border-b border-slate-700/80">
                <tr>
                  <th className="py-3.5 px-4">Mã Đơn VietQR</th>
                  <th className="py-3.5 px-4">Khách Hàng & Liên Hệ</th>
                  <th className="py-3.5 px-4">Gói Đăng Ký</th>
                  <th className="py-3.5 px-4">Số Tiền (VND)</th>
                  <th className="py-3.5 px-4">Thời Gian Tạo</th>
                  <th className="py-3.5 px-4">Trạng Thái</th>
                  <th className="py-3.5 px-4 text-right">Thao Tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {orders.map((order) => (
                  <tr key={order.id} className="hover:bg-slate-800/40 transition-colors">
                    {/* Mã Đơn */}
                    <td className="py-3.5 px-4 font-mono font-bold text-emerald-400">
                      <div className="flex items-center gap-1.5">
                        <span>{order.orderCode}</span>
                        <button
                          onClick={() => copyToClipboard(order.orderCode, 'Mã đơn')}
                          className="p-1 rounded text-slate-500 hover:text-slate-300 hover:bg-slate-700 transition-colors"
                          title="Sao chép mã đơn"
                        >
                          <Copy className="w-3.5 h-3.5" />
                        </button>
                      </div>
                      <span className="text-[10px] text-slate-500 font-sans block mt-0.5">
                        ID: {order.id.slice(0, 8)}...
                      </span>
                    </td>

                    {/* Khách hàng */}
                    <td className="py-3.5 px-4">
                      <div>
                        <p className="font-bold text-slate-200">{order.user?.name || 'Khách hàng'}</p>
                        <div className="flex items-center gap-1.5 text-[11px] text-slate-400 mt-0.5">
                          <span>{order.user?.email || order.userId}</span>
                          <button
                            onClick={() => copyToClipboard(order.user?.email || order.userId, 'Email/ID')}
                            className="text-slate-500 hover:text-slate-300"
                            title="Sao chép email"
                          >
                            <Copy className="w-3 h-3" />
                          </button>
                        </div>
                      </div>
                    </td>

                    {/* Gói nạp */}
                    <td className="py-3.5 px-4">
                      <span className="px-2.5 py-1 rounded-lg bg-slate-800 border border-slate-700 text-slate-300 text-[11px] font-bold uppercase">
                        {order.itemSku.replace('premium_', '')}
                      </span>
                      {order.userNote && (
                        <p className="text-[10px] text-slate-500 italic mt-1 max-w-[180px] truncate" title={order.userNote}>
                          Note: {order.userNote}
                        </p>
                      )}
                    </td>

                    {/* Số tiền */}
                    <td className="py-3.5 px-4 font-extrabold text-white text-sm">
                      {formatCurrencyVnd(order.amount)}
                      {order.paidAmount ? (
                        <span className="text-[10px] text-emerald-400 block font-normal">
                          Đã trả: {formatCurrencyVnd(order.paidAmount)}
                        </span>
                      ) : null}
                    </td>

                    {/* Thời gian */}
                    <td className="py-3.5 px-4 text-slate-400">
                      <p>{formatDateTimeVn(order.createdAt)}</p>
                      {order.paidAt && (
                        <p className="text-[10px] text-emerald-400 mt-0.5">
                          Duyệt: {formatDateTimeVn(order.paidAt).split(' - ')[0]}
                        </p>
                      )}
                    </td>

                    {/* Trạng thái */}
                    <td className="py-3.5 px-4">
                      {renderStatusBadge(order.status)}
                    </td>

                    {/* Hành động */}
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        {order.status === 'PENDING' ? (
                          <>
                            <button
                              onClick={() => {
                                setSelectedOrder(order);
                                setIsApproveOpen(true);
                              }}
                              className="px-3 py-1.5 rounded-xl bg-emerald-500/20 hover:bg-emerald-500 text-emerald-300 hover:text-white border border-emerald-500/30 text-xs font-bold transition-all shadow-sm flex items-center gap-1.5"
                              title="Duyệt và kích hoạt gói"
                            >
                              <Check className="w-3.5 h-3.5" />
                              <span>Duyệt</span>
                            </button>
                            <button
                              onClick={() => {
                                setSelectedOrder(order);
                                setRejectReason('');
                                setIsRejectOpen(true);
                              }}
                              className="px-3 py-1.5 rounded-xl bg-rose-500/20 hover:bg-rose-500 text-rose-300 hover:text-white border border-rose-500/30 text-xs font-bold transition-all shadow-sm flex items-center gap-1.5"
                              title="Từ chối đơn"
                            >
                              <X className="w-3.5 h-3.5" />
                              <span>Từ chối</span>
                            </button>
                          </>
                        ) : (
                          <button
                            onClick={() => {
                              setSelectedOrder(order);
                              setIsDetailOpen(true);
                            }}
                            className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-bold transition-all flex items-center gap-1.5"
                          >
                            <Eye className="w-3.5 h-3.5" />
                            <span>Chi tiết</span>
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Bar */}
        <div className="p-4 bg-slate-900/60 border-t border-slate-700/80 flex items-center justify-between text-xs text-slate-400">
          <span>
            Hiển thị {orders.length} / tổng {totalRecords} đơn
          </span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => fetchOrders(page - 1)}
              disabled={page <= 1}
              className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 disabled:opacity-30 disabled:pointer-events-none transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-bold text-slate-200">
              Trang {page} / {totalPages}
            </span>
            <button
              onClick={() => fetchOrders(page + 1)}
              disabled={page >= totalPages}
              className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 disabled:opacity-30 disabled:pointer-events-none transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Confirm Approve Modal */}
      <ConfirmDialog
        isOpen={isApproveOpen}
        onClose={() => setIsApproveOpen(false)}
        onConfirm={handleApprove}
        title="Xác Nhận Duyệt Đơn & Kích Hoạt Gói"
        message={`Bạn có chắc chắn muốn duyệt đơn ${selectedOrder?.orderCode} (${formatCurrencyVnd(
          selectedOrder?.amount
        )}) cho người dùng ${selectedOrder?.user?.name || selectedOrder?.userId}? Hệ thống sẽ tự động kích hoạt gói Premium và ghi nhận nhật ký kiểm toán.`}
        confirmLabel="Duyệt Kích Hoạt"
        isDestructive={false}
        isLoading={actionLoading}
      />

      {/* Reject Modal with Mandatory Reason (>= 5 chars) */}
      <Modal
        isOpen={isRejectOpen}
        onClose={() => setIsRejectOpen(false)}
        title="Từ Chối Đơn Thanh Toán"
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsRejectOpen(false)} disabled={actionLoading}>
              Hủy
            </Button>
            <Button
              variant="danger"
              onClick={handleReject}
              isLoading={actionLoading}
              disabled={rejectReason.trim().length < 5}
            >
              Xác Nhận Từ Chối
            </Button>
          </>
        }
      >
        <div className="space-y-4 py-2">
          <div className="p-3.5 rounded-2xl bg-amber-500/10 border border-amber-500/20 text-xs text-amber-300 flex items-start gap-2.5">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>
              Theo quy chuẩn <b>BR-17.2</b>, mọi hành động từ chối đơn nạp đều bắt buộc giải trình lý do (tối thiểu 5 ký tự) phục vụ đối soát kiểm toán và bảo vệ quyền lợi người dùng.
            </span>
          </div>

          <div>
            <p className="text-xs font-bold text-slate-300 mb-1">Mã đơn:</p>
            <p className="font-mono text-sm text-emerald-400 font-bold">{selectedOrder?.orderCode}</p>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">
              Lý do từ chối đơn <span className="text-rose-400">*</span>
            </label>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="VD: Khách chuyển khoản sai số tài khoản hoặc đơn quá hạn 24h..."
              rows={3}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-rose-500"
            />
            <p className="text-[11px] text-slate-500 mt-1">Tối thiểu 5 ký tự ({rejectReason.trim().length}/5)</p>
          </div>
        </div>
      </Modal>

      {/* Order Detail Modal */}
      <Modal
        isOpen={isDetailOpen}
        onClose={() => setIsDetailOpen(false)}
        title="Chi Tiết Đơn Hàng VietQR"
        maxWidth="md"
        footer={
          <Button variant="primary" onClick={() => setIsDetailOpen(false)}>
            Đóng
          </Button>
        }
      >
        {selectedOrder && (
          <div className="space-y-3.5 py-2 text-xs">
            <div className="flex items-center justify-between p-3 rounded-2xl bg-slate-900 border border-slate-800">
              <span className="text-slate-400">Mã đơn hàng:</span>
              <span className="font-mono font-bold text-emerald-400 text-sm">{selectedOrder.orderCode}</span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-2xl bg-slate-900 border border-slate-800">
              <span className="text-slate-400">Số tiền:</span>
              <span className="font-extrabold text-white text-sm">{formatCurrencyVnd(selectedOrder.amount)}</span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-2xl bg-slate-900 border border-slate-800">
              <span className="text-slate-400">Trạng thái:</span>
              <span>{renderStatusBadge(selectedOrder.status)}</span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-2xl bg-slate-900 border border-slate-800">
              <span className="text-slate-400">Người nạp:</span>
              <span className="font-bold text-slate-200">
                {selectedOrder.user?.name} ({selectedOrder.user?.email || selectedOrder.userId})
              </span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-2xl bg-slate-900 border border-slate-800">
              <span className="text-slate-400">Gói dịch vụ:</span>
              <span className="font-bold text-slate-200 uppercase">{selectedOrder.itemSku}</span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-2xl bg-slate-900 border border-slate-800">
              <span className="text-slate-400">Thời gian tạo:</span>
              <span className="text-slate-300">{formatDateTimeVn(selectedOrder.createdAt)}</span>
            </div>

            {selectedOrder.userNote && (
              <div className="p-3 rounded-2xl bg-slate-900 border border-slate-800">
                <span className="text-slate-400 block mb-1">Ghi chú:</span>
                <p className="text-slate-200 italic">{selectedOrder.userNote}</p>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};
