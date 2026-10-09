import React, { useEffect, useState, useMemo } from 'react';
import {
  ClipboardCheck,
  CheckCircle2,
  XCircle,
  Clock,
  Smile,
  Meh,
  Frown,
  ArrowRight,
  TrendingDown,
  Trash2,
  AlertCircle,
  Eye,
  SlidersHorizontal,
} from 'lucide-react';
import { checkInsApi, ReviewCheckInPayload } from '../api/checkins.api';
import { CheckIn, CheckInStatus, Mood } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDateOnly, formatCalories } from '../utils/formatters';
import { toast } from 'sonner';

export const CheckInsPage: React.FC = () => {
  const [checkIns, setCheckIns] = useState<CheckIn[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Review Modal state
  const [isReviewOpen, setIsReviewOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [selectedCheckIn, setSelectedCheckIn] = useState<CheckIn | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Review form states
  const [adjustedCalories, setAdjustedCalories] = useState<number>(2000);
  const [adjustedProtein, setAdjustedProtein] = useState<number>(140);
  const [adjustedCarb, setAdjustedCarb] = useState<number>(200);
  const [adjustedFat, setAdjustedFat] = useState<number>(60);
  const [adminNote, setAdminNote] = useState<string>('');

  const fetchCheckIns = async () => {
    setIsLoading(true);
    try {
      const res = await checkInsApi.getCheckIns();
      setCheckIns(res.data);
    } catch {
      toast.error('Lỗi khi tải danh sách Check-ins');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCheckIns();
  }, []);

  const filteredCheckIns = useMemo(() => {
    return checkIns.filter(
      (c) => statusFilter === 'ALL' || c.status === statusFilter
    );
  }, [checkIns, statusFilter]);

  // Open Review Modal
  const openReviewModal = (chk: CheckIn) => {
    setSelectedCheckIn(chk);
    setAdjustedCalories(chk.proposedCalorieTarget);
    setAdjustedProtein(chk.proposedProteinTarget || chk.user?.targetProtein || 140);
    setAdjustedCarb(chk.proposedCarbTarget || chk.user?.targetCarb || 200);
    setAdjustedFat(chk.proposedFatTarget || chk.user?.targetFat || 60);
    setAdminNote(chk.adminFeedback || '');
    setIsReviewOpen(true);
  };

  // Submit Review (Accept or Decline)
  const handleReviewAction = async (decision: 'ACCEPTED' | 'DECLINED') => {
    if (!selectedCheckIn) return;

    setActionLoading(true);
    try {
      await checkInsApi.reviewCheckIn(selectedCheckIn.id, {
        status: decision,
        proposedCalorieTarget: decision === 'ACCEPTED' ? Number(adjustedCalories) : undefined,
        proposedProteinTarget: decision === 'ACCEPTED' ? Number(adjustedProtein) : undefined,
        proposedCarbTarget: decision === 'ACCEPTED' ? Number(adjustedCarb) : undefined,
        proposedFatTarget: decision === 'ACCEPTED' ? Number(adjustedFat) : undefined,
        note: adminNote.trim() || undefined,
      });

      if (decision === 'ACCEPTED') {
        toast.success(
          `Đã duyệt và áp dụng mục tiêu mới (${adjustedCalories} kcal) vào hồ sơ ${selectedCheckIn.user?.username}!`
        );
      } else {
        toast.warning(`Đã từ chối đề xuất check-in của user ${selectedCheckIn.user?.username}.`);
      }

      setIsReviewOpen(false);
      fetchCheckIns();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi gửi duyệt check-in');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete
  const handleDeleteCheckIn = async () => {
    if (!selectedCheckIn) return;
    setActionLoading(true);
    try {
      await checkInsApi.deleteCheckIn(selectedCheckIn.id);
      toast.success('Đã xóa check-in');
      setIsDeleteOpen(false);
      fetchCheckIns();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi xóa check-in');
    } finally {
      setActionLoading(false);
    }
  };

  const getMoodIcon = (mood?: Mood) => {
    switch (mood) {
      case 'GREAT':
      case 'GOOD':
        return <Smile className="w-4 h-4 text-emerald-500" />;
      case 'OKAY':
        return <Meh className="w-4 h-4 text-amber-500" />;
      case 'BAD':
        return <Frown className="w-4 h-4 text-rose-500" />;
      default:
        return <Smile className="w-4 h-4 text-slate-400" />;
    }
  };

  const statusBadges: Record<CheckInStatus, { variant: 'amber' | 'emerald' | 'rose' | 'slate'; label: string }> = {
    PENDING: { variant: 'amber', label: 'Chờ duyệt' },
    ACCEPTED: { variant: 'emerald', label: 'Đã duyệt & Áp dụng' },
    DECLINED: { variant: 'rose', label: 'Từ chối' },
    DISMISSED: { variant: 'slate', label: 'Bỏ qua' },
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Duyệt Check-in Hàng tuần
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Đánh giá tiến độ tuần qua, tinh chỉnh calo & macro và tự động đồng bộ sang hồ sơ người dùng
          </p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="p-4 rounded-2xl glass-panel flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <label className="text-xs font-semibold text-slate-400">Lọc theo trạng thái:</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-slate-300 focus:outline-none focus:border-emerald-500"
          >
            <option value="ALL">Toàn bộ Check-ins</option>
            <option value="PENDING">Đang chờ duyệt (PENDING)</option>
            <option value="ACCEPTED">Đã duyệt (ACCEPTED)</option>
            <option value="DECLINED">Đã từ chối (DECLINED)</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="overflow-hidden rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50/80 dark:bg-slate-900/50 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-100 dark:border-white/5 uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3.5">Người dùng</th>
                <th className="px-5 py-3.5">Tuần</th>
                <th className="px-5 py-3.5">Calo Tuần trước vs Đề xuất</th>
                <th className="px-5 py-3.5">Cân nặng Check-in</th>
                <th className="px-5 py-3.5">Tâm trạng (Mood)</th>
                <th className="px-5 py-3.5">Trạng thái</th>
                <th className="px-5 py-3.5">Ngày gửi</th>
                <th className="px-5 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {filteredCheckIns.length === 0 ? (
                <tr>
                  <td colSpan={8}>
                    <EmptyState
                      title="Không có Check-in nào"
                      description="Chưa có dữ liệu check-in phù hợp với bộ lọc hiện tại."
                    />
                  </td>
                </tr>
              ) : (
                filteredCheckIns.map((chk) => (
                  <tr
                    key={chk.id}
                    className="hover:bg-slate-50/50 dark:hover:bg-white/[0.02] transition-colors"
                  >
                    <td className="px-5 py-3.5">
                      <p className="font-bold text-slate-900 dark:text-white">
                        {chk.user?.name || chk.user?.username || chk.userId}
                      </p>
                      <p className="text-[11px] text-slate-400">@{chk.user?.username || 'user'}</p>
                    </td>

                    <td className="px-5 py-3.5 font-semibold text-slate-700 dark:text-slate-300">
                      Tuần {chk.weekNumber}
                    </td>

                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-2">
                        <span className="text-slate-500 font-semibold">
                          {chk.currentCalorieTarget}
                        </span>
                        <ArrowRight className="w-3.5 h-3.5 text-emerald-500" />
                        <span className="font-extrabold text-emerald-600 dark:text-emerald-400 text-sm">
                          {chk.proposedCalorieTarget} kcal
                        </span>
                      </div>
                    </td>

                    <td className="px-5 py-3.5">
                      <span className="font-bold text-slate-800 dark:text-slate-200">
                        {chk.weightAtCheckin ? `${chk.weightAtCheckin} kg` : '—'}
                      </span>
                    </td>

                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-1.5 font-medium">
                        {getMoodIcon(chk.mood)}
                        <span>{chk.mood || 'GOOD'}</span>
                      </div>
                    </td>

                    <td className="px-5 py-3.5">
                      <Badge
                        variant={statusBadges[chk.status]?.variant || 'slate'}
                        dot={chk.status === 'PENDING'}
                      >
                        {statusBadges[chk.status]?.label || chk.status}
                      </Badge>
                    </td>

                    <td className="px-5 py-3.5 text-slate-500">
                      {formatDateOnly(chk.createdAt)}
                    </td>

                    <td className="px-5 py-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openReviewModal(chk)}
                          className={`p-1.5 rounded-lg transition-colors ${
                            chk.status === 'PENDING'
                              ? 'text-amber-500 hover:bg-amber-500/10 font-bold'
                              : 'text-slate-400 hover:text-emerald-500 hover:bg-emerald-500/10'
                          }`}
                          title="Review Check-in"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => {
                            setSelectedCheckIn(chk);
                            setIsDeleteOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
                          title="Xóa Check-in"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* DEDICATED REVIEW MODAL */}
      {selectedCheckIn && (
        <Modal
          isOpen={isReviewOpen}
          onClose={() => setIsReviewOpen(false)}
          title={`Duyệt Check-in Tuần ${selectedCheckIn.weekNumber} — @${selectedCheckIn.user?.username}`}
          description="So sánh dữ liệu tuần qua và tinh chỉnh mục tiêu calo/macro cho tuần kế tiếp"
          maxWidth="lg"
          footer={
            <div className="flex items-center justify-between w-full">
              <Button
                variant="danger"
                onClick={() => handleReviewAction('DECLINED')}
                isLoading={actionLoading}
              >
                Từ chối đề xuất
              </Button>
              <div className="flex items-center gap-2">
                <Button variant="ghost" onClick={() => setIsReviewOpen(false)}>
                  Hủy
                </Button>
                <Button
                  variant="primary"
                  onClick={() => handleReviewAction('ACCEPTED')}
                  isLoading={actionLoading}
                  leftIcon={<CheckCircle2 className="w-4 h-4" />}
                >
                  Duyệt & Áp dụng vào Profile
                </Button>
              </div>
            </div>
          }
        >
          <div className="space-y-4.5">
            {/* Comparison Bar */}
            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 grid grid-cols-2 gap-4">
              <div className="border-r border-slate-200 dark:border-white/10 pr-2">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                  Mục tiêu Tuần trước
                </span>
                <p className="text-xl font-extrabold text-slate-800 dark:text-slate-200 mt-1">
                  {selectedCheckIn.currentCalorieTarget} kcal
                </p>
                <p className="text-xs text-slate-500 mt-0.5">
                  Cân nặng lúc check-in: <span className="font-bold">{selectedCheckIn.weightAtCheckin || '—'} kg</span>
                </p>
              </div>

              <div>
                <span className="text-[10px] font-bold text-emerald-500 uppercase tracking-wider">
                  Đề xuất Tuần kế tiếp
                </span>
                <p className="text-xl font-extrabold text-emerald-500 mt-1">
                  {selectedCheckIn.proposedCalorieTarget} kcal
                </p>
                <p className="text-xs text-slate-500 mt-0.5">
                  Chênh lệch: <span className="font-bold text-emerald-500">{selectedCheckIn.proposedCalorieTarget - selectedCheckIn.currentCalorieTarget} kcal</span>
                </p>
              </div>
            </div>

            {/* User Note & Mood */}
            {selectedCheckIn.note && (
              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 text-xs text-slate-600 dark:text-slate-300">
                <span className="font-bold text-slate-400 block mb-1">
                  Cảm nhận & Ghi chú của người dùng:
                </span>
                <p className="italic">"{selectedCheckIn.note}"</p>
              </div>
            )}

            {/* Admin Override Inputs */}
            <div>
              <div className="flex items-center gap-2 mb-2">
                <SlidersHorizontal className="w-4 h-4 text-emerald-500" />
                <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                  Điều chỉnh Calo & Macro (Tự động lưu vào Profile)
                </h4>
              </div>

              <div className="grid grid-cols-4 gap-2.5">
                <Input
                  label="Target Calo"
                  type="number"
                  value={adjustedCalories}
                  onChange={(e) => setAdjustedCalories(Number(e.target.value))}
                />
                <Input
                  label="Protein (g)"
                  type="number"
                  value={adjustedProtein}
                  onChange={(e) => setAdjustedProtein(Number(e.target.value))}
                />
                <Input
                  label="Carbs (g)"
                  type="number"
                  value={adjustedCarb}
                  onChange={(e) => setAdjustedCarb(Number(e.target.value))}
                />
                <Input
                  label="Fat (g)"
                  type="number"
                  value={adjustedFat}
                  onChange={(e) => setAdjustedFat(Number(e.target.value))}
                />
              </div>
            </div>

            {/* Admin Feedback Message */}
            <Input
              label="Lời nhắn / Nhận xét của Admin gửi người dùng"
              placeholder="vd: Bạn tập rất tốt! Duy trì mức thâm hụt 100 kcal này trong 7 ngày tới nhé."
              value={adminNote}
              onChange={(e) => setAdminNote(e.target.value)}
            />
          </div>
        </Modal>
      )}

      {/* DELETE CONFIRM */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDeleteCheckIn}
        title="Xóa Check-in"
        message="Bạn có chắc chắn muốn xóa bản ghi check-in này?"
        isLoading={actionLoading}
      />
    </div>
  );
};
