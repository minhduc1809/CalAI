import React, { useEffect, useState } from 'react';
import {
  Gift,
  Plus,
  RotateCcw,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Copy,
  Calendar,
  Clock,
  ShieldAlert,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
} from 'lucide-react';
import { billingApi, GrantPayload } from '../api/billing.api';
import { usersApi } from '../api/users.api';
import { AdminManualGrant, User } from '../types';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDateTimeVn } from '../utils/formatters';
import { toast } from 'sonner';

export const GrantsPage: React.FC = () => {
  const [grants, setGrants] = useState<AdminManualGrant[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);

  // Modals
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isRevokeOpen, setIsRevokeOpen] = useState(false);
  const [selectedGrant, setSelectedGrant] = useState<AdminManualGrant | null>(null);

  // Form states
  const [grantForm, setGrantForm] = useState<GrantPayload>({
    userId: '',
    days: 7,
    reason: '',
  });
  const [revokeReason, setRevokeReason] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const fetchGrants = async (targetPage = page) => {
    setIsLoading(true);
    try {
      const [grantsRes, usersRes] = await Promise.all([
        billingApi.getGrants({ page: targetPage, limit: 15 }),
        usersApi.getUsers({ limit: 50 }),
      ]);
      setGrants(grantsRes.data);
      setPage(grantsRes.meta.page);
      setTotalPages(grantsRes.meta.totalPages);
      setTotalRecords(grantsRes.meta.total);
      setUsers(usersRes.data);
    } catch (err: any) {
      toast.error('Lỗi tải danh sách cấp gói bù');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    fetchGrants(1);
  }, []);

  const handleRefresh = () => {
    setIsRefreshing(true);
    fetchGrants(page).then(() => toast.success('Đã làm mới dữ liệu'));
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
  };

  // Create Grant Handler
  const handleCreateGrant = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!grantForm.userId) {
      toast.error('Vui lòng chọn hoặc nhập User ID');
      return;
    }
    if (grantForm.days < 1 || grantForm.days > 90) {
      toast.error('Số ngày cấp bù bắt buộc từ 1 đến 90 ngày (BR-17.3)');
      return;
    }
    if (grantForm.reason.trim().length < 10) {
      toast.error('Lý do giải trình bắt buộc có tối thiểu 10 ký tự');
      return;
    }

    setActionLoading(true);
    try {
      await billingApi.grantPremium({
        userId: grantForm.userId,
        days: Number(grantForm.days),
        reason: grantForm.reason.trim(),
      });
      toast.success(`Cấp thành công ${grantForm.days} ngày Premium!`);
      setIsCreateOpen(false);
      setGrantForm({ userId: '', days: 7, reason: '' });
      fetchGrants(page);
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi cấp gói');
    } finally {
      setActionLoading(false);
    }
  };

  // Revoke Handler
  const handleRevokeGrant = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedGrant) return;
    if (revokeReason.trim().length < 5) {
      toast.error('Lý do thu hồi bắt buộc tối thiểu 5 ký tự');
      return;
    }

    setActionLoading(true);
    try {
      await billingApi.revokeGrant({
        grantId: selectedGrant.id,
        reason: revokeReason.trim(),
      });
      toast.success('Thu hồi gói thành công');
      setIsRevokeOpen(false);
      setSelectedGrant(null);
      setRevokeReason('');
      fetchGrants(page);
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi thu hồi');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="p-2 rounded-xl bg-purple-500/15 text-purple-400 border border-purple-500/30">
              <Gift className="w-5 h-5" />
            </span>
            <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">
              Cấp & Thu Hồi Gói Premium Thủ Công
            </h1>
          </div>
          <p className="text-xs text-slate-400 max-w-2xl">
            Công cụ hỗ trợ khách hàng và đền bù sự cố gián đoạn dịch vụ Vision AI/OCR theo quy chuẩn <b className="text-slate-300">BR-17.3</b> (Giới hạn tối đa 90 ngày & bắt buộc giải trình $\ge 10$ ký tự).
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            disabled={isRefreshing}
            className="px-3.5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 text-xs font-bold transition-all flex items-center gap-2 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-purple-400' : ''}`} />
            <span>Làm mới</span>
          </button>

          <Button
            variant="primary"
            onClick={() => setIsCreateOpen(true)}
            className="flex items-center gap-2 shadow-glow text-xs"
          >
            <Plus className="w-4 h-4" />
            <span>Cấp gói bù mới</span>
          </Button>
        </div>
      </div>

      {/* Grants History Table */}
      <div className="rounded-3xl bg-[#1E293B]/80 dark:bg-[#1E293B]/90 border border-slate-700/80 shadow-lg overflow-hidden">
        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center gap-3">
            <div className="w-8 h-8 rounded-full border-2 border-purple-500 border-t-transparent animate-spin" />
            <span className="text-xs text-slate-400">Đang tải lịch sử cấp gói...</span>
          </div>
        ) : grants.length === 0 ? (
          <div className="py-16 text-center">
            <EmptyState
              title="Chưa có lượt cấp gói bù nào"
              description="Bấm 'Cấp gói bù mới' để thêm ngày Premium cho người dùng."
            />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-[11px] uppercase font-bold text-slate-400 bg-slate-900/60 border-b border-slate-700/80">
                <tr>
                  <th className="py-3.5 px-4">Mã Cấp / ID</th>
                  <th className="py-3.5 px-4">Người Nhận</th>
                  <th className="py-3.5 px-4">Thời Hạn Hiệu Lực</th>
                  <th className="py-3.5 px-4">Lý Do Đền Bù (Kiểm Toán)</th>
                  <th className="py-3.5 px-4">Trạng Thái</th>
                  <th className="py-3.5 px-4 text-right">Thao Tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {grants.map((grant) => {
                  const isRevoked = !!grant.revokedAt;
                  const isExpired = new Date(grant.endsAt).getTime() < Date.now();

                  return (
                    <tr key={grant.id} className="hover:bg-slate-800/40 transition-colors">
                      <td className="py-3.5 px-4 font-mono font-bold text-purple-400">
                        <div className="flex items-center gap-1.5">
                          <span>{grant.id}</span>
                          <button
                            onClick={() => copyToClipboard(grant.id, 'Grant ID')}
                            className="p-1 rounded text-slate-500 hover:text-slate-300"
                            title="Copy Grant ID"
                          >
                            <Copy className="w-3 h-3" />
                          </button>
                        </div>
                        <span className="text-[10px] text-slate-500 font-sans block mt-0.5">
                          Tạo: {formatDateTimeVn(grant.createdAt)}
                        </span>
                      </td>

                      <td className="py-3.5 px-4">
                        <div>
                          <p className="font-bold text-slate-200">
                            {grant.user?.name || grant.user?.username || grant.userId}
                          </p>
                          <p className="text-[11px] text-slate-400 flex items-center gap-1">
                            <span>{grant.user?.email || grant.userId}</span>
                            <button
                              onClick={() => copyToClipboard(grant.user?.email || grant.userId, 'User Email')}
                              className="text-slate-500 hover:text-slate-300"
                              title="Copy email"
                            >
                              <Copy className="w-3 h-3" />
                            </button>
                          </p>
                        </div>
                      </td>

                      <td className="py-3.5 px-4">
                        <div className="space-y-0.5">
                          <p className="text-slate-300">
                            Từ: <span className="font-semibold">{formatDateTimeVn(grant.startsAt).split(' - ')[0]}</span>
                          </p>
                          <p className="text-purple-400 font-bold">
                            Đến: <span>{formatDateTimeVn(grant.endsAt).split(' - ')[0]}</span>
                          </p>
                        </div>
                      </td>

                      <td className="py-3.5 px-4 max-w-xs">
                        <p className="text-slate-200 line-clamp-2" title={grant.reason}>
                          {grant.reason}
                        </p>
                        {grant.revokedReason && (
                          <p className="text-[10px] text-rose-400 mt-1 italic line-clamp-1" title={grant.revokedReason}>
                            Lý do thu hồi: {grant.revokedReason}
                          </p>
                        )}
                      </td>

                      <td className="py-3.5 px-4">
                        {isRevoked ? (
                          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30 inline-flex items-center gap-1">
                            <XCircle className="w-3 h-3" />
                            Đã thu hồi
                          </span>
                        ) : isExpired ? (
                          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-slate-700/60 text-slate-300 border border-slate-600 inline-flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            Đã hết hạn
                          </span>
                        ) : (
                          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 inline-flex items-center gap-1">
                            <CheckCircle2 className="w-3 h-3" />
                            Đang hiệu lực
                          </span>
                        )}
                      </td>

                      <td className="py-3.5 px-4 text-right">
                        {!isRevoked && !isExpired && (
                          <button
                            onClick={() => {
                              setSelectedGrant(grant);
                              setRevokeReason('');
                              setIsRevokeOpen(true);
                            }}
                            className="px-3 py-1.5 rounded-xl bg-rose-500/15 hover:bg-rose-500 text-rose-300 hover:text-white border border-rose-500/30 text-xs font-bold transition-all shadow-sm inline-flex items-center gap-1.5"
                          >
                            <RotateCcw className="w-3.5 h-3.5" />
                            <span>Thu hồi</span>
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        <div className="p-4 bg-slate-900/60 border-t border-slate-700/80 flex items-center justify-between text-xs text-slate-400">
          <span>
            Hiển thị {grants.length} / tổng {totalRecords} lượt cấp
          </span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => fetchGrants(page - 1)}
              disabled={page <= 1}
              className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 disabled:opacity-30 disabled:pointer-events-none"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-bold text-slate-200">
              Trang {page} / {totalPages}
            </span>
            <button
              onClick={() => fetchGrants(page + 1)}
              disabled={page >= totalPages}
              className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 disabled:opacity-30 disabled:pointer-events-none"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Modal Cấp Gói Bù */}
      <Modal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        title="Cấp Gói Premium Thủ Công (Đền Bù)"
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsCreateOpen(false)} disabled={actionLoading}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleCreateGrant} isLoading={actionLoading}>
              Xác Nhận Cấp Gói
            </Button>
          </>
        }
      >
        <form onSubmit={handleCreateGrant} className="space-y-4 py-2 text-xs">
          <div className="p-3.5 rounded-2xl bg-purple-500/10 border border-purple-500/20 text-purple-300 flex items-start gap-2.5">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>
              Theo quy chuẩn <b>BR-17.3</b>, số ngày cấp phải từ <b>1 đến 90 ngày</b> và lý do giải trình đền bù bắt buộc tối thiểu <b>10 ký tự</b> để phục vụ kiểm toán đối soát tài chính.
            </span>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">
              Người Dùng Nhận Gói <span className="text-rose-400">*</span>
            </label>
            <select
              value={grantForm.userId}
              onChange={(e) => setGrantForm({ ...grantForm, userId: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white focus:outline-none focus:border-purple-500"
            >
              <option value="">-- Chọn người dùng --</option>
              {users.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name || u.username} ({u.email || u.id})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">
              Số Ngày Cấp (1 - 90 ngày) <span className="text-rose-400">*</span>
            </label>
            <input
              type="number"
              min={1}
              max={90}
              value={grantForm.days}
              onChange={(e) => setGrantForm({ ...grantForm, days: Number(e.target.value) })}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white focus:outline-none focus:border-purple-500"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">
              Lý Do Giải Trình Đền Bù <span className="text-rose-400">*</span>
            </label>
            <textarea
              rows={3}
              value={grantForm.reason}
              onChange={(e) => setGrantForm({ ...grantForm, reason: e.target.value })}
              placeholder="VD: Đền bù lỗi OCR gián đoạn ngày 09/10/2026..."
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:border-purple-500"
            />
            <p className="text-[11px] text-slate-500 mt-1">
              Tối thiểu 10 ký tự ({grantForm.reason.trim().length}/10)
            </p>
          </div>
        </form>
      </Modal>

      {/* Modal Thu Hồi */}
      <Modal
        isOpen={isRevokeOpen}
        onClose={() => setIsRevokeOpen(false)}
        title="Thu Hồi Gói Premium Đã Cấp"
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsRevokeOpen(false)} disabled={actionLoading}>
              Hủy
            </Button>
            <Button
              variant="danger"
              onClick={handleRevokeGrant}
              isLoading={actionLoading}
              disabled={revokeReason.trim().length < 5}
            >
              Xác Nhận Thu Hồi
            </Button>
          </>
        }
      >
        <div className="space-y-4 py-2 text-xs">
          <div className="p-3.5 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-300 flex items-start gap-2.5">
            <ShieldAlert className="w-4 h-4 shrink-0 mt-0.5" />
            <span>
              Thao tác thu hồi sẽ chấm dứt quyền lợi Premium được cấp thủ công của người dùng này ngay lập tức.
            </span>
          </div>

          <div>
            <p className="font-bold text-slate-300 mb-1">Mã cấp bù:</p>
            <p className="font-mono text-purple-400 font-bold">{selectedGrant?.id}</p>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">
              Lý do thu hồi gói <span className="text-rose-400">*</span>
            </label>
            <textarea
              rows={3}
              value={revokeReason}
              onChange={(e) => setRevokeReason(e.target.value)}
              placeholder="VD: Cấp nhầm tài khoản, thu hồi theo yêu cầu của trưởng nhóm..."
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:border-rose-500"
            />
            <p className="text-[11px] text-slate-500 mt-1">
              Tối thiểu 5 ký tự ({revokeReason.trim().length}/5)
            </p>
          </div>
        </div>
      </Modal>
    </div>
  );
};
