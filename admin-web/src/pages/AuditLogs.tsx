import React, { useEffect, useState } from 'react';
import {
  Shield,
  RefreshCw,
  CheckCircle2,
  XCircle,
  Gift,
  KeyRound,
  Eye,
  Copy,
  Check,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { auditLogsApi } from '../api/audit-logs.api';
import { AdminAuditLog } from '../types';
import { Modal } from '../components/ui/Modal';
import { Button } from '../components/ui/Button';
import { formatDateTimeVn } from '../utils/formatters';
import { toast } from 'sonner';

export const AuditLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<AdminAuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [actionFilter, setActionFilter] = useState('ALL');
  const [targetFilter, setTargetFilter] = useState('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [copiedId, setCopiedId] = useState<string | null>(null);

  // Inspector Modal State
  const [selectedLog, setSelectedLog] = useState<AdminAuditLog | null>(null);

  const fetchLogs = async () => {
    setIsLoading(true);
    try {
      const res = await auditLogsApi.getAuditLogs({
        page,
        limit: 15,
        action: actionFilter !== 'ALL' ? actionFilter : undefined,
        targetType: targetFilter !== 'ALL' ? targetFilter : undefined,
      });
      setLogs(res.data);
      setTotalPages(res.meta.totalPages || 1);
      setTotalRecords(res.meta.total || 0);
    } catch {
      toast.error('Lỗi khi tải nhật ký kiểm toán');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [page, actionFilter, targetFilter]);

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const actionConfig: Record<string, { label: string; class: string; icon: any }> = {
    APPROVE_PAYMENT: {
      label: 'DUYỆT THANH TOÁN',
      class: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
      icon: CheckCircle2,
    },
    REJECT_PAYMENT: {
      label: 'TỪ CHỐI ĐƠN',
      class: 'bg-red-500/15 text-red-400 border-red-500/30',
      icon: XCircle,
    },
    GRANT_PREMIUM: {
      label: 'CẤP GÓI BÙ',
      class: 'bg-purple-500/15 text-purple-400 border-purple-500/30',
      icon: Gift,
    },
    REVOKE_PREMIUM: {
      label: 'THU HỒI GÓI',
      class: 'bg-red-500/15 text-red-400 border-red-500/30',
      icon: XCircle,
    },
    LOGIN: {
      label: 'ĐĂNG NHẬP ADMIN',
      class: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
      icon: KeyRound,
    },
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-[#334155]">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-[#F8FAFC] flex items-center gap-2.5">
            <Shield className="w-6 h-6 text-emerald-400" />
            Nhật Ký Kiểm Toán Bất Biến (Audit Trail)
          </h1>
          <p className="text-xs text-[#94A3B8] mt-1">
            Ghi nhận tự động (Append-Only) mọi thao tác duyệt đơn, cấp gói bù và đăng nhập hệ thống của Quản trị viên (BR-17)
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={fetchLogs}
            isLoading={isLoading}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
            className="border border-[#334155] text-xs text-[#F8FAFC]"
          >
            Làm mới
          </Button>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="p-4 rounded-2xl bg-[#1E293B] border border-[#334155] flex flex-col md:flex-row items-center justify-between gap-3 shadow-sm">
        {/* Action Filter */}
        <div className="flex items-center gap-1.5 overflow-x-auto w-full md:w-auto">
          <span className="text-[10px] uppercase font-bold text-[#94A3B8] px-1">Thao tác:</span>
          {[
            { id: 'ALL', label: 'Tất cả' },
            { id: 'APPROVE_PAYMENT', label: 'Duyệt đơn' },
            { id: 'REJECT_PAYMENT', label: 'Từ chối đơn' },
            { id: 'GRANT_PREMIUM', label: 'Cấp gói bù' },
            { id: 'REVOKE_PREMIUM', label: 'Thu hồi gói' },
            { id: 'LOGIN', label: 'Đăng nhập' },
          ].map((act) => (
            <button
              key={act.id}
              onClick={() => {
                setActionFilter(act.id);
                setPage(1);
              }}
              className={`px-2.5 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                actionFilter === act.id
                  ? 'bg-emerald-500 text-white shadow-sm'
                  : 'bg-[#0F172A] text-[#94A3B8] hover:text-[#F8FAFC] border border-[#334155]'
              }`}
            >
              {act.label}
            </button>
          ))}
        </div>

        {/* Target Filter */}
        <div className="flex items-center gap-1 bg-[#0F172A] p-1 rounded-xl border border-[#334155] text-xs self-start md:self-auto">
          <span className="text-[10px] uppercase font-bold text-[#94A3B8] px-2">Đối tượng:</span>
          {['ALL', 'PaymentOrder', 'User', 'ManualGrant'].map((tgt) => (
            <button
              key={tgt}
              onClick={() => {
                setTargetFilter(tgt);
                setPage(1);
              }}
              className={`px-2 py-1 rounded-lg font-semibold text-xs transition-all ${
                targetFilter === tgt
                  ? 'bg-emerald-500 text-white'
                  : 'text-[#94A3B8] hover:text-[#F8FAFC]'
              }`}
            >
              {tgt === 'ALL' ? 'Tất cả' : tgt}
            </button>
          ))}
        </div>
      </div>

      {/* Logs Table */}
      <div className="bg-[#1E293B] border border-[#334155] rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-[#334155] bg-[#0F172A]/80 text-[#94A3B8] uppercase text-[10px] tracking-wider font-semibold">
                <th className="py-3.5 px-4">Thời Gian (Timestamp)</th>
                <th className="py-3.5 px-4">Admin Thực Hiện</th>
                <th className="py-3.5 px-4">Loại Hành Động</th>
                <th className="py-3.5 px-4">Đối Tượng Tác Động</th>
                <th className="py-3.5 px-4">Giải Trình / Lý Do</th>
                <th className="py-3.5 px-4">Địa Chỉ IP</th>
                <th className="py-3.5 px-4 text-right">Chi Tiết Diff</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#334155]">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#94A3B8]">
                    <div className="inline-block w-6 h-6 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin mb-2" />
                    <p>Đang tải nhật ký kiểm toán...</p>
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#94A3B8]">
                    <p className="text-sm font-semibold text-[#F8FAFC]">Không có bản ghi nhật ký nào</p>
                    <p className="text-xs mt-1 text-[#94A3B8]">Chưa ghi nhận hoạt động nào khớp với bộ lọc</p>
                  </td>
                </tr>
              ) : (
                logs.map((log) => {
                  const cfg = actionConfig[log.action] || {
                    label: log.action,
                    class: 'bg-slate-700 text-slate-300 border-slate-600',
                    icon: Shield,
                  };
                  const Icon = cfg.icon;

                  return (
                    <tr key={log.id} className="hover:bg-[#0F172A]/40 transition-colors">
                      {/* Timestamp */}
                      <td className="py-3.5 px-4 font-mono text-[11px] text-[#94A3B8] whitespace-nowrap">
                        {formatDateTimeVn(log.createdAt)}
                      </td>

                      {/* Admin email */}
                      <td className="py-3.5 px-4 font-mono text-[11px] text-[#F8FAFC]">
                        {log.adminEmail || '—'}
                      </td>

                      {/* Loại hành động */}
                      <td className="py-3.5 px-4">
                        <span
                          className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-bold border ${cfg.class}`}
                        >
                          <Icon className="w-3 h-3" />
                          {cfg.label}
                        </span>
                      </td>

                      {/* Đối tượng tác động */}
                      <td className="py-3.5 px-4 font-mono text-[11px]">
                        <span className="text-[#F8FAFC] font-semibold">{log.targetType}</span>
                        {log.targetId && (
                          <div className="flex items-center gap-1 text-[#94A3B8] mt-0.5">
                            <span>ID: {log.targetId.slice(0, 10)}...</span>
                            <button
                              onClick={() => {
                                if (log.targetId) copyToClipboard(log.targetId, 'Target ID');
                              }}
                              className="p-0.5 hover:text-[#F8FAFC]"
                              title="Copy Target ID"
                            >
                              {copiedId === log.targetId ? (
                                <Check className="w-2.5 h-2.5 text-emerald-400" />
                              ) : (
                                <Copy className="w-2.5 h-2.5" />
                              )}
                            </button>
                          </div>
                        )}
                      </td>

                      {/* Lý do */}
                      <td className="py-3.5 px-4 text-[#94A3B8] max-w-xs truncate">
                        {log.reason || '—'}
                      </td>

                      {/* IP */}
                      <td className="py-3.5 px-4 font-mono text-[10px] text-[#94A3B8]">
                        {log.ipAddress || '127.0.0.1'}
                      </td>

                      {/* Chi tiết Diff */}
                      <td className="py-3.5 px-4 text-right">
                        <button
                          onClick={() => setSelectedLog(log)}
                          className="px-2.5 py-1 rounded-lg bg-[#0F172A] hover:bg-[#334155] text-emerald-400 border border-[#334155] font-semibold text-xs transition-colors inline-flex items-center gap-1"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          <span>Chi tiết</span>
                        </button>
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
            Hiển thị <span className="font-bold text-[#F8FAFC]">{logs.length}</span> /{' '}
            <span className="font-bold text-[#F8FAFC]">{totalRecords}</span> bản ghi kiểm toán
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

      {/* AUDIT LOG DETAILS / STATE DIFF INSPECTOR MODAL */}
      <Modal
        isOpen={!!selectedLog}
        onClose={() => setSelectedLog(null)}
        title="Chi Tiết Bản Ghi Kiểm Toán (Audit Inspector)"
        maxWidth="lg"
        footer={
          <Button variant="ghost" onClick={() => setSelectedLog(null)}>
            Đóng
          </Button>
        }
      >
        {selectedLog && (
          <div className="space-y-4 py-1 text-xs">
            {/* Meta Summary */}
            <div className="grid grid-cols-2 gap-3 p-3.5 rounded-xl bg-[#0F172A] border border-[#334155]">
              <div>
                <span className="text-[#94A3B8]">Mã Log ID:</span>
                <p className="font-mono text-[#F8FAFC] font-bold mt-0.5">{selectedLog.id}</p>
              </div>
              <div>
                <span className="text-[#94A3B8]">Thời gian thực hiện:</span>
                <p className="font-mono text-emerald-400 font-bold mt-0.5">
                  {formatDateTimeVn(selectedLog.createdAt)}
                </p>
              </div>
              <div>
                <span className="text-[#94A3B8]">Admin Email:</span>
                <p className="font-mono text-[#F8FAFC] mt-0.5">{selectedLog.adminEmail || '—'}</p>
              </div>
              <div>
                <span className="text-[#94A3B8]">Loại Thao Tác:</span>
                <p className="font-bold text-emerald-400 mt-0.5">{selectedLog.action}</p>
              </div>
              <div>
                <span className="text-[#94A3B8]">Đối Tượng / ID:</span>
                <p className="font-mono text-[#F8FAFC] mt-0.5">
                  {selectedLog.targetType} • {selectedLog.targetId || '—'}
                </p>
              </div>
              <div>
                <span className="text-[#94A3B8]">Địa chỉ IP:</span>
                <p className="font-mono text-[#F8FAFC] mt-0.5">{selectedLog.ipAddress || '127.0.0.1'}</p>
              </div>
            </div>

            {/* Reason */}
            {selectedLog.reason && (
              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#334155]">
                <span className="text-[#94A3B8] font-semibold">Lý do giải trình nghiệp vụ:</span>
                <p className="text-[#F8FAFC] mt-1 leading-relaxed">{selectedLog.reason}</p>
              </div>
            )}

            {/* State Diffs (Before & After) */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {/* Before State */}
              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#334155] space-y-1.5">
                <span className="font-bold text-amber-400 uppercase text-[10px] tracking-wider">
                  Trạng Thái Trước (Before)
                </span>
                <pre className="p-2.5 rounded-lg bg-[#0B0F17] text-slate-300 font-mono text-[11px] overflow-x-auto max-h-40">
                  {selectedLog.before
                    ? JSON.stringify(selectedLog.before, null, 2)
                    : 'null (Không áp dụng)'}
                </pre>
              </div>

              {/* After State */}
              <div className="p-3 rounded-xl bg-[#0F172A] border border-[#334155] space-y-1.5">
                <span className="font-bold text-emerald-400 uppercase text-[10px] tracking-wider">
                  Trạng Thái Sau (After)
                </span>
                <pre className="p-2.5 rounded-lg bg-[#0B0F17] text-emerald-300 font-mono text-[11px] overflow-x-auto max-h-40">
                  {selectedLog.after
                    ? JSON.stringify(selectedLog.after, null, 2)
                    : 'null (Không áp dụng)'}
                </pre>
              </div>
            </div>

            {/* User Agent */}
            {selectedLog.userAgent && (
              <div className="p-2.5 rounded-xl bg-[#0F172A] border border-[#334155] text-[10px] text-[#94A3B8] font-mono truncate">
                User Agent: {selectedLog.userAgent}
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};
