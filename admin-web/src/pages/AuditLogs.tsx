import React, { useEffect, useState } from 'react';
import {
  Shield,
  Search,
  Filter,
  CheckCircle2,
  XCircle,
  Gift,
  KeyRound,
  AlertTriangle,
  Copy,
  Clock,
  Eye,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  Code2,
} from 'lucide-react';
import { auditLogsApi, AuditLogQueryParams } from '../api/audit-logs.api';
import { AdminAuditLog, AuditLogAction } from '../types';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDateTimeVn } from '../utils/formatters';
import { toast } from 'sonner';

export const AuditLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<AdminAuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [actionFilter, setActionFilter] = useState<string>('ALL');
  const [targetTypeFilter, setTargetTypeFilter] = useState<string>('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);

  // Detail Modal
  const [selectedLog, setSelectedLog] = useState<AdminAuditLog | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  const fetchLogs = async (targetPage = page) => {
    setIsLoading(true);
    try {
      const res = await auditLogsApi.getAuditLogs({
        page: targetPage,
        limit: 15,
        action: actionFilter,
        targetType: targetTypeFilter,
      });
      setLogs(res.data);
      setPage(res.meta.page);
      setTotalPages(res.meta.totalPages);
      setTotalRecords(res.meta.total);
    } catch (err: any) {
      toast.error('Lỗi khi tải nhật ký kiểm toán');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    fetchLogs(1);
  }, [actionFilter, targetTypeFilter]);

  const handleRefresh = () => {
    setIsRefreshing(true);
    fetchLogs(page).then(() => toast.success('Đã làm mới nhật ký kiểm toán'));
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
  };

  const renderActionBadge = (action: AuditLogAction) => {
    switch (action) {
      case 'APPROVE_PAYMENT':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 inline-flex items-center gap-1.5">
            <CheckCircle2 className="w-3 h-3" />
            APPROVE_PAYMENT
          </span>
        );
      case 'GRANT_PREMIUM':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-purple-500/15 text-purple-400 border border-purple-500/30 inline-flex items-center gap-1.5">
            <Gift className="w-3 h-3" />
            GRANT_PREMIUM
          </span>
        );
      case 'REJECT_PAYMENT':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30 inline-flex items-center gap-1.5">
            <XCircle className="w-3 h-3" />
            REJECT_PAYMENT
          </span>
        );
      case 'REVOKE_PREMIUM':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30 inline-flex items-center gap-1.5">
            <AlertTriangle className="w-3 h-3" />
            REVOKE_PREMIUM
          </span>
        );
      case 'LOGIN':
      default:
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-cyan-500/15 text-cyan-400 border border-cyan-500/30 inline-flex items-center gap-1.5">
            <KeyRound className="w-3 h-3" />
            {action}
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
            <span className="p-2 rounded-xl bg-cyan-500/15 text-cyan-400 border border-cyan-500/30">
              <Shield className="w-5 h-5" />
            </span>
            <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">
              Nhật Ký Kiểm Toán Bất Biến (Audit Logs)
            </h1>
          </div>
          <p className="text-xs text-slate-400 max-w-2xl">
            Lưu vết toàn bộ thao tác vận hành của đội ngũ Quản trị viên (Admin) theo chuẩn kiểm toán <b className="text-slate-300">BR-17.4</b>. Dữ liệu ghi nhận tự động và không thể chỉnh sửa.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            disabled={isRefreshing}
            className="px-3.5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 text-xs font-bold transition-all flex items-center gap-2 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-cyan-400' : ''}`} />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="p-4 rounded-2xl bg-[#1E293B]/60 border border-slate-700/60 flex flex-wrap items-center justify-between gap-4 text-xs">
        {/* Action Filters */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 sm:pb-0">
          {[
            { key: 'ALL', label: 'Tất cả thao tác' },
            { key: 'APPROVE_PAYMENT', label: 'Duyệt đơn VietQR' },
            { key: 'GRANT_PREMIUM', label: 'Cấp gói bù' },
            { key: 'REJECT_PAYMENT', label: 'Từ chối đơn' },
            { key: 'REVOKE_PREMIUM', label: 'Thu hồi gói' },
            { key: 'LOGIN', label: 'Đăng nhập' },
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActionFilter(tab.key)}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all shrink-0 ${
                actionFilter === tab.key
                  ? 'bg-cyan-500 text-white shadow-glow'
                  : 'bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700/60'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Target Type Filter */}
        <div className="flex items-center gap-2">
          <span className="text-slate-400 font-semibold">Đối tượng:</span>
          <select
            value={targetTypeFilter}
            onChange={(e) => setTargetTypeFilter(e.target.value)}
            className="px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-white focus:outline-none focus:border-cyan-500 font-bold"
          >
            <option value="ALL">Tất cả đối tượng</option>
            <option value="PaymentOrder">PaymentOrder (Đơn hàng)</option>
            <option value="ManualGrant">ManualGrant (Gói bù)</option>
            <option value="User">User (Người dùng)</option>
            <option value="AdminSession">AdminSession (Phiên làm việc)</option>
          </select>
        </div>
      </div>

      {/* Audit Logs Table */}
      <div className="rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg overflow-hidden">
        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center gap-3">
            <div className="w-8 h-8 rounded-full border-2 border-cyan-500 border-t-transparent animate-spin" />
            <span className="text-xs text-slate-400">Đang tải nhật ký kiểm toán...</span>
          </div>
        ) : logs.length === 0 ? (
          <div className="py-16 text-center">
            <EmptyState
              title="Không có nhật ký kiểm toán phù hợp"
              description="Thử thay đổi bộ lọc thao tác hoặc đối tượng tác động."
            />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-[11px] uppercase font-bold text-slate-400 bg-slate-900/60 border-b border-slate-700/80">
                <tr>
                  <th className="py-3.5 px-4">Thời Gian (GMT+7)</th>
                  <th className="py-3.5 px-4">Quản Trị Viên (Admin)</th>
                  <th className="py-3.5 px-4">Thao Tác</th>
                  <th className="py-3.5 px-4">Đối Tượng Tác Động</th>
                  <th className="py-3.5 px-4">Lý Do / Ghi Chú</th>
                  <th className="py-3.5 px-4">Địa Chỉ IP</th>
                  <th className="py-3.5 px-4 text-right">Chi Tiết</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3.5 px-4 text-slate-300 font-medium">
                      {formatDateTimeVn(log.createdAt)}
                    </td>

                    <td className="py-3.5 px-4">
                      <p className="font-bold text-slate-200">{log.adminEmail}</p>
                      <span className="text-[10px] text-slate-500 font-mono">
                        ID: {log.adminId.slice(0, 8)}...
                      </span>
                    </td>

                    <td className="py-3.5 px-4">
                      {renderActionBadge(log.action)}
                    </td>

                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded-lg bg-slate-800 text-[11px] font-bold text-slate-300">
                        {log.targetType}
                      </span>
                      <div className="flex items-center gap-1 text-[11px] text-slate-400 font-mono mt-0.5">
                        <span className="truncate max-w-[120px]">{log.targetId}</span>
                        <button
                          onClick={() => copyToClipboard(log.targetId, 'Target ID')}
                          className="text-slate-500 hover:text-slate-300"
                          title="Copy ID"
                        >
                          <Copy className="w-3 h-3" />
                        </button>
                      </div>
                    </td>

                    <td className="py-3.5 px-4 max-w-xs">
                      <p className="text-slate-300 line-clamp-2" title={log.reason || '—'}>
                        {log.reason || <span className="text-slate-500 italic">Không kèm lý do</span>}
                      </p>
                    </td>

                    <td className="py-3.5 px-4 font-mono text-[11px] text-slate-400">
                      {log.ipAddress || '—'}
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => {
                          setSelectedLog(log);
                          setIsDetailOpen(true);
                        }}
                        className="p-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
                        title="Xem JSON Before/After"
                      >
                        <Code2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        <div className="p-4 bg-slate-900/60 border-t border-slate-700/80 flex items-center justify-between text-xs text-slate-400">
          <span>
            Hiển thị {logs.length} / tổng {totalRecords} bản ghi kiểm toán
          </span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => fetchLogs(page - 1)}
              disabled={page <= 1}
              className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 disabled:opacity-30 disabled:pointer-events-none"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-bold text-slate-200">
              Trang {page} / {totalPages}
            </span>
            <button
              onClick={() => fetchLogs(page + 1)}
              disabled={page >= totalPages}
              className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 disabled:opacity-30 disabled:pointer-events-none"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* JSON Before/After Inspector Modal */}
      <Modal
        isOpen={isDetailOpen}
        onClose={() => setIsDetailOpen(false)}
        title="Chi Tiết Bản Ghi Kiểm Toán Bất Biến"
        maxWidth="lg"
        footer={
          <Button variant="primary" onClick={() => setIsDetailOpen(false)}>
            Đóng
          </Button>
        }
      >
        {selectedLog && (
          <div className="space-y-4 py-2 text-xs">
            <div className="grid grid-cols-2 gap-3 p-3.5 rounded-2xl bg-slate-900 border border-slate-800">
              <div>
                <span className="text-slate-500">Mã kiểm toán:</span>
                <p className="font-mono text-cyan-400 font-bold">{selectedLog.id}</p>
              </div>
              <div>
                <span className="text-slate-500">Thao tác:</span>
                <p className="font-bold text-white mt-0.5">{selectedLog.action}</p>
              </div>
              <div>
                <span className="text-slate-500">Admin thực hiện:</span>
                <p className="font-semibold text-slate-200">{selectedLog.adminEmail}</p>
              </div>
              <div>
                <span className="text-slate-500">Thời gian ghi nhận:</span>
                <p className="text-slate-300">{formatDateTimeVn(selectedLog.createdAt)}</p>
              </div>
            </div>

            {selectedLog.reason && (
              <div className="p-3 rounded-xl bg-slate-900 border border-slate-800">
                <span className="text-slate-500 font-semibold block mb-1">Lý do giải trình:</span>
                <p className="text-slate-200">{selectedLog.reason}</p>
              </div>
            )}

            {/* Before and After JSON Diffs */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <span className="text-slate-400 font-bold block mb-1.5">Trạng thái Trước (Before):</span>
                <pre className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-amber-300 font-mono text-[11px] overflow-x-auto max-h-56">
                  {selectedLog.before ? JSON.stringify(selectedLog.before, null, 2) : '// Không có bản ghi trước'}
                </pre>
              </div>

              <div>
                <span className="text-slate-400 font-bold block mb-1.5">Trạng thái Sau (After):</span>
                <pre className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-emerald-400 font-mono text-[11px] overflow-x-auto max-h-56">
                  {selectedLog.after ? JSON.stringify(selectedLog.after, null, 2) : '// Không có bản ghi sau'}
                </pre>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-slate-900 border border-slate-800 text-[11px] text-slate-400 space-y-1">
              <p>
                <b className="text-slate-300">IP:</b> {selectedLog.ipAddress || '—'}
              </p>
              <p className="truncate">
                <b className="text-slate-300">User Agent:</b> {selectedLog.userAgent || '—'}
              </p>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};
