import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Gift,
  Plus,
  RefreshCw,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Copy,
  Check,
  Calendar,
  Clock,
  Sparkles,
  Info,
  ChevronLeft,
  ChevronRight,
  ShieldAlert,
} from 'lucide-react';
import { billingApi } from '../api/billing.api';
import { AdminManualGrant } from '../types';
import { Modal } from '../components/ui/Modal';
import { Button } from '../components/ui/Button';
import { formatDateTimeVn } from '../utils/formatters';
import { toast } from 'sonner';

export const GrantsPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [grants, setGrants] = useState<AdminManualGrant[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);
  const [copiedId, setCopiedId] = useState<string | null>(null);

  // Grant Modal State
  const [isGrantModalOpen, setIsGrantModalOpen] = useState(false);
  const [userIdInput, setUserIdInput] = useState('');
  const [daysInput, setDaysInput] = useState<number>(14);
  const [reasonInput, setReasonInput] = useState('');
  const [isSubmittingGrant, setIsSubmittingGrant] = useState(false);

  // Revoke Modal State
  const [grantToRevoke, setGrantToRevoke] = useState<AdminManualGrant | null>(null);
  const [revokeReason, setRevokeReason] = useState('');
  const [isSubmittingRevoke, setIsSubmittingRevoke] = useState(false);

  // Auto-open modal if navigated from users page with ?userId=...
  useEffect(() => {
    const qUserId = searchParams.get('userId');
    if (qUserId) {
      setUserIdInput(qUserId);
      setIsGrantModalOpen(true);
    }
  }, [searchParams]);

  const fetchGrants = async () => {
    setIsLoading(true);
    try {
      const res = await billingApi.getGrants({ page, limit: 15 });
      setGrants(res.data);
      setTotalPages(res.meta.totalPages || 1);
      setTotalRecords(res.meta.total || 0);
    } catch {
      toast.error('Lỗi khi tải danh sách cấp gói Premium');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchGrants();
  }, [page]);

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(text);
    toast.success(`Đã sao chép ${label}: ${text}`);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleCreateGrant = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userIdInput.trim()) {
      toast.error('Vui lòng nhập User ID');
      return;
    }
    if (daysInput < 1 || daysInput > 90) {
      toast.error('Số ngày cấp bù phải từ 1 đến 90 ngày (BR-17.3)');
      return;
    }
    if (reasonInput.trim().length < 10) {
      toast.error('Lý do giải trình bắt buộc tối thiểu 10 ký tự');
      return;
    }

    setIsSubmittingGrant(true);
    try {
      await billingApi.grantPremium({
        userId: userIdInput.trim(),
        days: Number(daysInput),
        reason: reasonInput.trim(),
      });
      toast.success(`Cấp ${daysInput} ngày Premium thành công cho User`);
      setIsGrantModalOpen(false);
      setUserIdInput('');
      setDaysInput(14);
      setReasonInput('');
      fetchGrants();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi cấp Premium thủ công');
    } finally {
      setIsSubmittingGrant(false);
    }
  };

  const handleConfirmRevoke = async () => {
    if (!grantToRevoke) return;
    if (revokeReason.trim().length < 5) {
      toast.error('Lý do thu hồi bắt buộc tối thiểu 5 ký tự');
      return;
    }

    setIsSubmittingRevoke(true);
    try {
      await billingApi.revokeGrant({
        grantId: grantToRevoke.id,
        reason: revokeReason.trim(),
      });
      toast.success('Thu hồi quyền Premium thành công');
      setGrantToRevoke(null);
      setRevokeReason('');
      fetchGrants();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi thu hồi gói');
    } finally {
      setIsSubmittingRevoke(false);
    }
  };

  const dayPresets = [7, 14, 30, 60, 90];

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-[#334155]">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-[#F8FAFC] flex items-center gap-2.5">
            <Gift className="w-6 h-6 text-emerald-400" />
            Cấp & Thu Hồi Gói Premium Thủ Công
          </h1>
          <p className="text-xs text-[#94A3B8] mt-1">
            Chính sách đền bù sự cố, chăm sóc khách hàng VIP & hỗ trợ kỹ thuật (BR-17.3 - Tối đa 90 ngày/lần)
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <Button
            variant="ghost"
            size="sm"
            onClick={fetchGrants}
            isLoading={isLoading}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
            className="border border-[#334155] text-xs text-[#F8FAFC]"
          >
            Làm mới
          </Button>

          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsGrantModalOpen(true)}
            leftIcon={<Plus className="w-4 h-4" />}
            className="bg-[#10B981] hover:bg-[#059669] text-white font-bold text-xs shadow-glow"
          >
            Cấp Gói Mới
          </Button>
        </div>
      </div>

      {/* Policy Information Card */}
      <div className="p-4 rounded-2xl bg-[#1E293B] border border-[#334155] flex flex-col md:flex-row items-start md:items-center justify-between gap-4 text-xs shadow-sm">
        <div className="flex items-start gap-3">
          <div className="p-2 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20 shrink-0">
            <Sparkles className="w-4.5 h-4.5" />
          </div>
          <div>
            <h4 className="font-bold text-[#F8FAFC]">Quy Chuẩn Cấp Gói Thủ Công (BR-17.3 Policy)</h4>
            <p className="text-[#94A3B8] mt-0.5 leading-relaxed">
              Thời hạn cấp từ 1 đến 90 ngày. Bắt buộc nhập giải trình tối thiểu 10 ký tự phục vụ đối soát kiểm toán độc lập. Nếu khách hàng đang có gói còn hạn, hệ thống sẽ tự động cộng dồn ngày tiếp nối.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <span className="font-mono text-[11px] font-bold px-2.5 py-1 rounded-lg bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
            Max 90 ngày
          </span>
          <span className="font-mono text-[11px] font-bold px-2.5 py-1 rounded-lg bg-indigo-500/15 text-indigo-400 border border-indigo-500/30">
            Audit-Logged
          </span>
        </div>
      </div>

      {/* Grants Table */}
      <div className="bg-[#1E293B] border border-[#334155] rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-[#334155] bg-[#0F172A]/80 text-[#94A3B8] uppercase text-[10px] tracking-wider font-semibold">
                <th className="py-3.5 px-4">User ID / Khách Hàng</th>
                <th className="py-3.5 px-4">Thời Hạn Hiệu Lực</th>
                <th className="py-3.5 px-4">Lý Do Cấp Bù (Giải Trình)</th>
                <th className="py-3.5 px-4">Trạng Thái</th>
                <th className="py-3.5 px-4">Admin Cấp</th>
                <th className="py-3.5 px-4 text-right">Tác Nghiệp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#334155]">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[#94A3B8]">
                    <div className="inline-block w-6 h-6 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin mb-2" />
                    <p>Đang tải danh sách gói đã cấp...</p>
                  </td>
                </tr>
              ) : grants.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[#94A3B8]">
                    <p className="text-sm font-semibold text-[#F8FAFC]">Chưa có lượt cấp gói bù nào</p>
                    <p className="text-xs mt-1 text-[#94A3B8]">Bấm "Cấp Gói Mới" để tạo lượt tặng/đền bù ngày Premium</p>
                  </td>
                </tr>
              ) : (
                grants.map((grant) => {
                  const isRevoked = Boolean(grant.revokedAt);
                  const isExpired = new Date(grant.endsAt) < new Date();

                  return (
                    <tr key={grant.id} className="hover:bg-[#0F172A]/40 transition-colors">
                      {/* User ID */}
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-1.5">
                          <span className="font-mono font-bold text-[#F8FAFC]">
                            {grant.userId}
                          </span>
                          <button
                            onClick={() => copyToClipboard(grant.userId, 'User ID')}
                            className="p-1 rounded text-[#94A3B8] hover:text-[#F8FAFC] transition-colors"
                            title="Sao chép User ID"
                          >
                            {copiedId === grant.userId ? (
                              <Check className="w-3 h-3 text-emerald-400" />
                            ) : (
                              <Copy className="w-3 h-3" />
                            )}
                          </button>
                        </div>
                        {grant.user && (
                          <p className="text-[11px] text-[#94A3B8] mt-0.5">
                            {grant.user.name || grant.user.email}
                          </p>
                        )}
                      </td>

                      {/* Thời hạn */}
                      <td className="py-3.5 px-4 font-mono text-[11px]">
                        <div className="text-emerald-400 font-semibold">
                          Từ: {formatDateTimeVn(grant.startsAt).split(' - ')[1]}
                        </div>
                        <div className="text-[#94A3B8]">
                          Đến: {formatDateTimeVn(grant.endsAt).split(' - ')[1]}
                        </div>
                      </td>

                      {/* Lý do */}
                      <td className="py-3.5 px-4 max-w-xs">
                        <p className="text-[#F8FAFC] font-medium leading-relaxed">
                          {grant.reason}
                        </p>
                        {grant.revokedReason && (
                          <p className="text-[10px] text-red-400 italic mt-0.5">
                            Lý do thu hồi: {grant.revokedReason}
                          </p>
                        )}
                      </td>

                      {/* Trạng thái */}
                      <td className="py-3.5 px-4">
                        {isRevoked ? (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-500/15 text-red-400 border border-red-500/30">
                            <XCircle className="w-3 h-3" />
                            ĐÃ THU HỒI
                          </span>
                        ) : isExpired ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-800 text-[#94A3B8] border border-[#334155]">
                            ĐÃ HẾT HẠN
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                            <CheckCircle2 className="w-3 h-3" />
                            ĐANG HIỆU LỰC
                          </span>
                        )}
                      </td>

                      {/* Admin cấp */}
                      <td className="py-3.5 px-4 font-mono text-[11px] text-[#94A3B8]">
                        {grant.grantedByAdminId ? `${grant.grantedByAdminId.slice(0, 8)}...` : 'System Admin'}
                      </td>

                      {/* Tác nghiệp */}
                      <td className="py-3.5 px-4 text-right">
                        {!isRevoked && !isExpired && (
                          <button
                            onClick={() => {
                              setGrantToRevoke(grant);
                              setRevokeReason('');
                            }}
                            className="px-2.5 py-1 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 font-semibold text-xs transition-colors"
                          >
                            Thu hồi
                          </button>
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
            Hiển thị <span className="font-bold text-[#F8FAFC]">{grants.length}</span> /{' '}
            <span className="font-bold text-[#F8FAFC]">{totalRecords}</span> bản ghi
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

      {/* CREATE GRANT MODAL */}
      <Modal
        isOpen={isGrantModalOpen}
        onClose={() => setIsGrantModalOpen(false)}
        title="Cấp Gói Premium Thủ Công (BR-17.3)"
        maxWidth="md"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => setIsGrantModalOpen(false)}
              disabled={isSubmittingGrant}
            >
              Hủy bỏ
            </Button>
            <Button
              variant="primary"
              onClick={handleCreateGrant}
              isLoading={isSubmittingGrant}
              className="bg-[#10B981] hover:bg-[#059669] text-white font-bold"
            >
              Xác nhận Cấp Gói
            </Button>
          </>
        }
      >
        <form onSubmit={handleCreateGrant} className="space-y-4 py-2 text-xs">
          <div>
            <label className="block font-semibold text-[#F8FAFC] mb-1">
              User ID người nhận: <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={userIdInput}
              onChange={(e) => setUserIdInput(e.target.value)}
              placeholder="VD: u-001 hoặc UUID..."
              className="w-full bg-[#0F172A] border border-[#334155] rounded-xl px-3 py-2 text-xs text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-emerald-500 font-mono"
              required
            />
          </div>

          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="font-semibold text-[#F8FAFC]">
                Số ngày cấp bù (1 đến 90 ngày): <span className="text-red-400">*</span>
              </label>
              <span className="font-mono font-bold text-emerald-400 text-sm">
                {daysInput} ngày
              </span>
            </div>

            <input
              type="range"
              min="1"
              max="90"
              value={daysInput}
              onChange={(e) => setDaysInput(Number(e.target.value))}
              className="w-full accent-emerald-500 cursor-pointer"
            />

            {/* Presets */}
            <div className="flex items-center gap-1.5 mt-2">
              <span className="text-[10px] text-[#94A3B8]">Mốc nhanh:</span>
              {dayPresets.map((d) => (
                <button
                  type="button"
                  key={d}
                  onClick={() => setDaysInput(d)}
                  className={`px-2 py-0.5 rounded-lg text-[10px] font-mono font-bold border transition-colors ${
                    daysInput === d
                      ? 'bg-emerald-500 text-white border-emerald-500'
                      : 'bg-[#0F172A] text-[#94A3B8] border-[#334155] hover:text-[#F8FAFC]'
                  }`}
                >
                  +{d}d
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block font-semibold text-[#F8FAFC] mb-1">
              Lý do giải trình (Bắt buộc $\ge 10$ ký tự): <span className="text-red-400">*</span>
            </label>
            <textarea
              value={reasonInput}
              onChange={(e) => setReasonInput(e.target.value)}
              placeholder="VD: Đền bù lỗi OCR gián đoạn ngày 09/10/2026..."
              rows={3}
              className="w-full bg-[#0F172A] border border-[#334155] rounded-xl p-3 text-xs text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-emerald-500"
              required
            />
            <p className="text-[10px] text-[#94A3B8] mt-1 text-right">
              {reasonInput.trim().length}/10 ký tự tối thiểu
            </p>
          </div>
        </form>
      </Modal>

      {/* REVOKE GRANT MODAL */}
      <Modal
        isOpen={!!grantToRevoke}
        onClose={() => setGrantToRevoke(null)}
        title="Thu Hồi Gói Premium Đã Cấp (BR-17.3)"
        maxWidth="md"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => setGrantToRevoke(null)}
              disabled={isSubmittingRevoke}
            >
              Hủy bỏ
            </Button>
            <Button
              variant="danger"
              onClick={handleConfirmRevoke}
              isLoading={isSubmittingRevoke}
              disabled={revokeReason.trim().length < 5}
            >
              Xác nhận Thu hồi
            </Button>
          </>
        }
      >
        <div className="space-y-4 py-2 text-xs">
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-300">
            Bạn đang chuẩn bị thu hồi gói Premium của User ID{' '}
            <span className="font-mono font-bold">{grantToRevoke?.userId}</span>. Quyền truy cập Premium được cấp thủ công trước đó sẽ bị vô hiệu hóa ngay lập tức.
          </div>

          <div>
            <label className="block font-semibold text-[#F8FAFC] mb-1">
              Lý do thu hồi giải trình (Bắt buộc $\ge 5$ ký tự): <span className="text-red-400">*</span>
            </label>
            <textarea
              value={revokeReason}
              onChange={(e) => setRevokeReason(e.target.value)}
              placeholder="VD: Cấp nhầm tài khoản, thu hồi theo yêu cầu..."
              rows={3}
              className="w-full bg-[#0F172A] border border-[#334155] rounded-xl p-3 text-xs text-[#F8FAFC] placeholder:text-[#94A3B8]/60 focus:outline-none focus:border-red-500"
            />
            <p className="text-[10px] text-[#94A3B8] mt-1 text-right">
              {revokeReason.trim().length}/5 ký tự tối thiểu
            </p>
          </div>
        </div>
      </Modal>
    </div>
  );
};
