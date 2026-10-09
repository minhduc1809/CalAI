import React, { useEffect, useState, useMemo } from 'react';
import {
  Dumbbell,
  Plus,
  Trash2,
  Eye,
  Edit,
  Clock,
  Flame,
  Activity,
  Layers,
} from 'lucide-react';
import { workoutsApi, CreateWorkoutPayload, UpdateWorkoutPayload } from '../api/workouts.api';
import { usersApi } from '../api/users.api';
import { Workout, WorkoutCategory, User } from '../types';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDate } from '../utils/formatters';
import { toast } from 'sonner';

export const WorkoutsPage: React.FC = () => {
  const [workouts, setWorkouts] = useState<Workout[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [selectedUserFilter, setSelectedUserFilter] = useState('ALL');
  const [selectedCatFilter, setSelectedCatFilter] = useState('ALL');

  // Modals
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [selectedWorkout, setSelectedWorkout] = useState<Workout | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Form states
  const [formUserId, setFormUserId] = useState('');
  const [formName, setFormName] = useState('');
  const [formCategory, setFormCategory] = useState<WorkoutCategory>('STRENGTH');
  const [formDuration, setFormDuration] = useState(60);
  const [formCalories, setFormCalories] = useState(400);
  const [formRpe, setFormRpe] = useState(8);
  const [formNotes, setFormNotes] = useState('');
  const [formDate, setFormDate] = useState(new Date().toISOString().substring(0, 16));

  const fetchWorkoutsAndUsers = async () => {
    setIsLoading(true);
    try {
      const [woRes, usersRes] = await Promise.all([
        workoutsApi.getWorkouts(),
        usersApi.getUsers(),
      ]);
      setWorkouts(woRes.data);
      setUsers(usersRes.data);
      if (usersRes.data.length > 0 && !formUserId) {
        setFormUserId(usersRes.data[0].id);
      }
    } catch {
      toast.error('Lỗi khi tải danh sách buổi tập');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchWorkoutsAndUsers();
  }, []);

  const filteredWorkouts = useMemo(() => {
    return workouts.filter((w) => {
      const matchUser = selectedUserFilter === 'ALL' || w.userId === selectedUserFilter;
      const matchCat = selectedCatFilter === 'ALL' || w.category === selectedCatFilter;
      return matchUser && matchCat;
    });
  }, [workouts, selectedUserFilter, selectedCatFilter]);

  // Create Workout
  const handleCreateWorkout = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formUserId || !formName.trim()) {
      toast.error('Vui lòng nhập tên buổi tập và chọn người dùng');
      return;
    }

    setActionLoading(true);
    try {
      await workoutsApi.createWorkout({
        userId: formUserId,
        name: formName.trim(),
        category: formCategory,
        date: new Date(formDate).toISOString(),
        durationMinutes: formDuration,
        caloriesBurned: formCalories,
        rpe: formRpe,
        notes: formNotes,
        exercises: [
          {
            exerciseName: 'Barbell Squat',
            order: 1,
            sets: [
              { setNumber: 1, reps: 10, weightKg: 80, rpe: 8 },
              { setNumber: 2, reps: 8, weightKg: 90, rpe: 8.5 },
            ],
          },
        ],
      });
      toast.success('Tạo buổi tập mới thành công');
      setIsCreateOpen(false);
      fetchWorkoutsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi tạo buổi tập');
    } finally {
      setActionLoading(false);
    }
  };

  // Open Edit
  const openEdit = (wo: Workout) => {
    setSelectedWorkout(wo);
    setFormName(wo.name);
    setFormCategory(wo.category);
    setFormDuration(wo.durationMinutes);
    setFormCalories(wo.caloriesBurned || 300);
    setFormRpe(wo.rpe || 7);
    setFormNotes(wo.notes || '');
    setFormDate(new Date(wo.date).toISOString().substring(0, 16));
    setIsEditOpen(true);
  };

  // Submit Edit
  const handleEditWorkout = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedWorkout) return;

    setActionLoading(true);
    try {
      await workoutsApi.updateWorkout(selectedWorkout.id, {
        name: formName,
        category: formCategory,
        durationMinutes: formDuration,
        caloriesBurned: formCalories,
        rpe: formRpe,
        notes: formNotes,
      });
      toast.success('Cập nhật buổi tập thành công');
      setIsEditOpen(false);
      fetchWorkoutsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi cập nhật buổi tập');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete
  const handleDeleteWorkout = async () => {
    if (!selectedWorkout) return;
    setActionLoading(true);
    try {
      await workoutsApi.deleteWorkout(selectedWorkout.id);
      toast.success('Đã xóa buổi tập thành công');
      setIsDeleteOpen(false);
      fetchWorkoutsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi xóa buổi tập');
    } finally {
      setActionLoading(false);
    }
  };

  const categoryBadges: Record<string, 'indigo' | 'cyan' | 'rose' | 'amber' | 'emerald'> = {
    STRENGTH: 'indigo',
    CARDIO: 'cyan',
    RUNNING: 'rose',
    CYCLING: 'amber',
    HIIT: 'emerald',
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Quản lý Tập luyện (Workouts)
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Nhật ký bài tập, hiệp tập (sets/reps/kg) và đánh giá độ gắng sức RPE
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => {
            setFormName('');
            setIsCreateOpen(true);
          }}
        >
          Thêm buổi tập
        </Button>
      </div>

      {/* Filter Bar */}
      <div className="p-4 rounded-2xl glass-panel flex flex-wrap items-center gap-3">
        <select
          value={selectedUserFilter}
          onChange={(e) => setSelectedUserFilter(e.target.value)}
          className="bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-slate-300 focus:outline-none focus:border-emerald-500"
        >
          <option value="ALL">Mọi Người dùng</option>
          {users.map((u) => (
            <option key={u.id} value={u.id}>
              {u.name || u.username} (@{u.username})
            </option>
          ))}
        </select>

        <select
          value={selectedCatFilter}
          onChange={(e) => setSelectedCatFilter(e.target.value)}
          className="bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-slate-300 focus:outline-none focus:border-emerald-500"
        >
          <option value="ALL">Mọi Danh mục tập</option>
          <option value="STRENGTH">Gym / Kháng lực (STRENGTH)</option>
          <option value="CARDIO">Tim mạch (CARDIO)</option>
          <option value="RUNNING">Chạy bộ (RUNNING)</option>
          <option value="CYCLING">Đạp xe (CYCLING)</option>
          <option value="HIIT">Cường độ cao (HIIT)</option>
        </select>
      </div>

      {/* Table */}
      <div className="overflow-hidden rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50/80 dark:bg-slate-900/50 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-100 dark:border-white/5 uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3.5">Người dùng</th>
                <th className="px-5 py-3.5">Tên buổi tập</th>
                <th className="px-5 py-3.5">Danh mục</th>
                <th className="px-5 py-3.5">Thời lượng</th>
                <th className="px-5 py-3.5">Calo tiêu hao</th>
                <th className="px-5 py-3.5">RPE</th>
                <th className="px-5 py-3.5">Ngày tập</th>
                <th className="px-5 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {filteredWorkouts.length === 0 ? (
                <tr>
                  <td colSpan={8}>
                    <EmptyState
                      title="Không có buổi tập nào"
                      description="Chưa có dữ liệu tập luyện phù hợp với bộ lọc."
                    />
                  </td>
                </tr>
              ) : (
                filteredWorkouts.map((wo) => (
                  <tr
                    key={wo.id}
                    className="hover:bg-slate-50/50 dark:hover:bg-white/[0.02] transition-colors"
                  >
                    <td className="px-5 py-3.5">
                      <p className="font-bold text-slate-900 dark:text-white">
                        {wo.user?.name || wo.user?.username || wo.userId}
                      </p>
                      <p className="text-[11px] text-slate-400">@{wo.user?.username || 'user'}</p>
                    </td>

                    <td className="px-5 py-3.5">
                      <p className="font-bold text-slate-900 dark:text-white">{wo.name}</p>
                      {wo.exercises && (
                        <p className="text-[11px] text-slate-400">{wo.exercises.length} bài tập con</p>
                      )}
                    </td>

                    <td className="px-5 py-3.5">
                      <Badge variant={categoryBadges[wo.category] || 'slate'}>
                        {wo.category}
                      </Badge>
                    </td>

                    <td className="px-5 py-3.5 font-semibold text-slate-700 dark:text-slate-300">
                      {wo.durationMinutes} phút
                    </td>

                    <td className="px-5 py-3.5 font-bold text-amber-500">
                      {wo.caloriesBurned || 0} kcal
                    </td>

                    <td className="px-5 py-3.5">
                      <span className="px-2 py-0.5 rounded-md font-bold text-[11px] bg-slate-100 dark:bg-white/10 text-slate-800 dark:text-slate-200">
                        {wo.rpe ? `${wo.rpe}/10` : '—'}
                      </span>
                    </td>

                    <td className="px-5 py-3.5 text-slate-500">
                      {formatDate(wo.date)}
                    </td>

                    <td className="px-5 py-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => {
                            setSelectedWorkout(wo);
                            setIsDetailOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-emerald-500 hover:bg-emerald-500/10 transition-colors"
                          title="Xem bài tập & hiệp tập"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => openEdit(wo)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-500 hover:bg-cyan-500/10 transition-colors"
                          title="Sửa buổi tập"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => {
                            setSelectedWorkout(wo);
                            setIsDeleteOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
                          title="Xóa buổi tập"
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

      {/* DETAIL MODAL */}
      {selectedWorkout && (
        <Modal
          isOpen={isDetailOpen}
          onClose={() => setIsDetailOpen(false)}
          title={`Chi tiết Buổi tập: ${selectedWorkout.name}`}
          maxWidth="lg"
          footer={
            <Button variant="secondary" onClick={() => setIsDetailOpen(false)}>
              Đóng
            </Button>
          }
        >
          <div className="space-y-4">
            <div className="grid grid-cols-3 gap-2.5 p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 text-center">
              <div>
                <p className="text-[10px] text-slate-400 font-semibold uppercase">Thời lượng</p>
                <p className="text-sm font-bold text-cyan-500">{selectedWorkout.durationMinutes} phút</p>
              </div>
              <div>
                <p className="text-[10px] text-slate-400 font-semibold uppercase">Calo đốt cháy</p>
                <p className="text-sm font-bold text-amber-500">{selectedWorkout.caloriesBurned || 0} kcal</p>
              </div>
              <div>
                <p className="text-[10px] text-slate-400 font-semibold uppercase">RPE Đánh giá</p>
                <p className="text-sm font-bold text-emerald-500">{selectedWorkout.rpe || '—'}/10</p>
              </div>
            </div>

            {selectedWorkout.notes && (
              <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 text-xs text-slate-600 dark:text-slate-300">
                <span className="font-bold text-slate-400 block mb-0.5">Ghi chú của người tập:</span>
                {selectedWorkout.notes}
              </div>
            )}

            <div>
              <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
                Bài tập & Hiệp tập (Sets Breakdown)
              </h4>
              <div className="space-y-3">
                {selectedWorkout.exercises?.map((ex, idx) => (
                  <div
                    key={ex.id || idx}
                    className="p-3.5 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/50 dark:border-white/5 space-y-2 text-xs"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-slate-900 dark:text-white">
                        {idx + 1}. {ex.exerciseName}
                      </span>
                      <span className="text-[11px] text-slate-400 font-medium">
                        {ex.sets.length} hiệp
                      </span>
                    </div>

                    <div className="grid grid-cols-4 gap-2 pt-1">
                      {ex.sets.map((s) => (
                        <div
                          key={s.id}
                          className="p-2 rounded-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-white/10 text-center"
                        >
                          <p className="text-[10px] text-slate-400 font-semibold">Set #{s.setNumber}</p>
                          <p className="font-bold text-slate-900 dark:text-white mt-0.5">
                            {s.weightKg ? `${s.weightKg}kg × ` : ''}{s.reps} reps
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </Modal>
      )}

      {/* CREATE & EDIT MODAL */}
      <Modal
        isOpen={isCreateOpen || isEditOpen}
        onClose={() => {
          setIsCreateOpen(false);
          setIsEditOpen(false);
        }}
        title={isCreateOpen ? 'Thêm Buổi tập Mới' : 'Sửa Buổi tập'}
        maxWidth="md"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => {
                setIsCreateOpen(false);
                setIsEditOpen(false);
              }}
            >
              Hủy
            </Button>
            <Button
              variant="primary"
              onClick={isCreateOpen ? handleCreateWorkout : handleEditWorkout}
              isLoading={actionLoading}
            >
              {isCreateOpen ? 'Tạo buổi tập' : 'Lưu thay đổi'}
            </Button>
          </>
        }
      >
        <div className="space-y-3.5">
          {isCreateOpen && (
            <Select
              label="Người dùng"
              value={formUserId}
              onChange={(e) => setFormUserId(e.target.value)}
              options={users.map((u) => ({
                value: u.id,
                label: `${u.name || u.username} (@${u.username})`,
              }))}
            />
          )}

          <Input
            label="Tên buổi tập"
            placeholder="vd: Leg Day - Thân dưới & Bắp chân"
            value={formName}
            onChange={(e) => setFormName(e.target.value)}
            required
          />

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Danh mục"
              value={formCategory}
              onChange={(e) => setFormCategory(e.target.value as WorkoutCategory)}
              options={[
                { value: 'STRENGTH', label: 'Gym / Kháng lực' },
                { value: 'CARDIO', label: 'Cardio' },
                { value: 'RUNNING', label: 'Chạy bộ' },
                { value: 'CYCLING', label: 'Đạp xe' },
                { value: 'HIIT', label: 'HIIT' },
              ]}
            />
            <Input
              label="Thời lượng (phút)"
              type="number"
              value={formDuration}
              onChange={(e) => setFormDuration(Number(e.target.value))}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Calo đốt cháy (kcal)"
              type="number"
              value={formCalories}
              onChange={(e) => setFormCalories(Number(e.target.value))}
            />
            <Input
              label="Đánh giá RPE (1 - 10)"
              type="number"
              min={1}
              max={10}
              value={formRpe}
              onChange={(e) => setFormRpe(Number(e.target.value))}
            />
          </div>

          <Input
            label="Ghi chú buổi tập"
            placeholder="Cảm giác cơ bắp, mức tạ..."
            value={formNotes}
            onChange={(e) => setFormNotes(e.target.value)}
          />
        </div>
      </Modal>

      {/* DELETE CONFIRM */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDeleteWorkout}
        title="Xóa buổi tập"
        message="Bạn có chắc chắn muốn xóa bản ghi buổi tập này khỏi hệ thống?"
        isLoading={actionLoading}
      />
    </div>
  );
};
