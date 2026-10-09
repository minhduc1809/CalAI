import React, { useEffect, useState, useMemo } from 'react';
import {
  FileText,
  UtensilsCrossed,
  Dumbbell,
  Plus,
  Trash2,
  Edit,
  Sparkles,
} from 'lucide-react';
import { templatesApi } from '../api/templates.api';
import { DietTemplate, WorkoutTemplate, GoalType, WorkoutLevel } from '../types';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { toast } from 'sonner';

export const TemplatesPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'diet' | 'workout'>('diet');
  const [dietTemplates, setDietTemplates] = useState<DietTemplate[]>([]);
  const [workoutTemplates, setWorkoutTemplates] = useState<WorkoutTemplate[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modals
  const [isDietModalOpen, setIsDietModalOpen] = useState(false);
  const [isWorkoutModalOpen, setIsWorkoutModalOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState<{ id: string; type: 'diet' | 'workout' } | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Edit / Create States
  const [editingDiet, setEditingDiet] = useState<DietTemplate | null>(null);
  const [dietForm, setDietForm] = useState<Partial<DietTemplate>>({
    name: '',
    goal: 'LOSE_WEIGHT',
    targetCalories: 2000,
    proteinPercent: 30,
    carbPercent: 45,
    fatPercent: 25,
    description: '',
  });

  const [editingWorkout, setEditingWorkout] = useState<WorkoutTemplate | null>(null);
  const [workoutForm, setWorkoutForm] = useState<Partial<WorkoutTemplate>>({
    name: '',
    goal: 'BUILD_MUSCLE' as any,
    level: 'BEGINNER',
    daysPerWeek: 3,
    description: '',
  });

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const [dietRes, woRes] = await Promise.all([
        templatesApi.getDietTemplates(),
        templatesApi.getWorkoutTemplates(),
      ]);
      setDietTemplates(dietRes);
      setWorkoutTemplates(woRes);
    } catch {
      toast.error('Lỗi khi tải mẫu giáo án');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Save Diet Template
  const handleSaveDiet = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!dietForm.name) {
      toast.error('Vui lòng nhập tên thực đơn mẫu');
      return;
    }
    setActionLoading(true);
    try {
      if (editingDiet) {
        await templatesApi.updateDietTemplate(editingDiet.id, dietForm);
        toast.success('Cập nhật mẫu thực đơn thành công');
      } else {
        await templatesApi.createDietTemplate(dietForm);
        toast.success('Tạo mới mẫu thực đơn thành công');
      }
      setIsDietModalOpen(false);
      fetchData();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi lưu mẫu thực đơn');
    } finally {
      setActionLoading(false);
    }
  };

  // Save Workout Template
  const handleSaveWorkout = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!workoutForm.name) {
      toast.error('Vui lòng nhập tên lịch tập mẫu');
      return;
    }
    setActionLoading(true);
    try {
      if (editingWorkout) {
        await templatesApi.updateWorkoutTemplate(editingWorkout.id, workoutForm);
        toast.success('Cập nhật mẫu lịch tập thành công');
      } else {
        await templatesApi.createWorkoutTemplate(workoutForm);
        toast.success('Tạo mới mẫu lịch tập thành công');
      }
      setIsWorkoutModalOpen(false);
      fetchData();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi lưu mẫu lịch tập');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete
  const handleDelete = async () => {
    if (!itemToDelete) return;
    setActionLoading(true);
    try {
      if (itemToDelete.type === 'diet') {
        await templatesApi.deleteDietTemplate(itemToDelete.id);
        toast.success('Đã xóa mẫu thực đơn');
      } else {
        await templatesApi.deleteWorkoutTemplate(itemToDelete.id);
        toast.success('Đã xóa mẫu lịch tập');
      }
      setIsDeleteOpen(false);
      fetchData();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi xóa mẫu');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Mẫu Thực đơn & Lịch tập (Templates)
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Quản trị các giáo án tiêu chuẩn do hệ thống đề xuất tự động cho người dùng
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => {
            if (activeTab === 'diet') {
              setEditingDiet(null);
              setDietForm({
                name: '',
                goal: 'LOSE_WEIGHT',
                targetCalories: 2000,
                proteinPercent: 30,
                carbPercent: 45,
                fatPercent: 25,
                description: '',
              });
              setIsDietModalOpen(true);
            } else {
              setEditingWorkout(null);
              setWorkoutForm({
                name: '',
                goal: 'BUILD_MUSCLE' as any,
                level: 'BEGINNER',
                daysPerWeek: 3,
                description: '',
              });
              setIsWorkoutModalOpen(true);
            }
          }}
        >
          {activeTab === 'diet' ? 'Tạo Mẫu Thực đơn' : 'Tạo Mẫu Lịch tập'}
        </Button>
      </div>

      {/* Tabs Switcher */}
      <div className="flex items-center gap-2 p-1.5 rounded-2xl bg-slate-100 dark:bg-slate-900/60 border border-slate-200/60 dark:border-white/5 w-fit">
        <button
          onClick={() => setActiveTab('diet')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'diet'
              ? 'bg-white dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 shadow-sm border border-slate-200/60 dark:border-emerald-500/30'
              : 'text-slate-500 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          <UtensilsCrossed className="w-4 h-4" />
          <span>Mẫu Thực đơn (Diet Plans)</span>
        </button>

        <button
          onClick={() => setActiveTab('workout')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'workout'
              ? 'bg-white dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 shadow-sm border border-slate-200/60 dark:border-emerald-500/30'
              : 'text-slate-500 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          <Dumbbell className="w-4 h-4" />
          <span>Mẫu Lịch tập (Workout Plans)</span>
        </button>
      </div>

      {/* TAB 1: DIET TEMPLATES */}
      {activeTab === 'diet' && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {dietTemplates.length === 0 ? (
            <div className="col-span-full">
              <EmptyState title="Chưa có mẫu thực đơn nào" />
            </div>
          ) : (
            dietTemplates.map((item) => (
              <div
                key={item.id}
                className="p-5 rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md flex flex-col justify-between hover:border-emerald-500/30 transition-all shadow-sm"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <Badge variant={item.goal === 'LOSE_WEIGHT' ? 'emerald' : 'cyan'}>
                      {item.goal}
                    </Badge>
                    <span className="text-base font-extrabold text-emerald-600 dark:text-emerald-400">
                      {item.targetCalories} kcal
                    </span>
                  </div>

                  <h3 className="text-base font-bold text-slate-900 dark:text-white mb-1.5">
                    {item.name}
                  </h3>
                  <p className="text-xs text-slate-500 line-clamp-2 mb-4">
                    {item.description || 'Chưa có mô tả chi tiết'}
                  </p>

                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/50 dark:border-white/5 flex items-center justify-between text-xs font-semibold">
                    <span className="text-emerald-500">{item.proteinPercent}% Protein</span>
                    <span className="text-slate-400">•</span>
                    <span className="text-cyan-500">{item.carbPercent}% Carbs</span>
                    <span className="text-slate-400">•</span>
                    <span className="text-amber-500">{item.fatPercent}% Fat</span>
                  </div>
                </div>

                <div className="pt-4 mt-4 border-t border-slate-100 dark:border-white/5 flex items-center justify-end gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      setEditingDiet(item);
                      setDietForm(item);
                      setIsDietModalOpen(true);
                    }}
                    leftIcon={<Edit className="w-3.5 h-3.5" />}
                  >
                    Sửa
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => {
                      setItemToDelete({ id: item.id, type: 'diet' });
                      setIsDeleteOpen(true);
                    }}
                    leftIcon={<Trash2 className="w-3.5 h-3.5" />}
                  >
                    Xóa
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* TAB 2: WORKOUT TEMPLATES */}
      {activeTab === 'workout' && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {workoutTemplates.length === 0 ? (
            <div className="col-span-full">
              <EmptyState title="Chưa có mẫu lịch tập nào" />
            </div>
          ) : (
            workoutTemplates.map((item) => (
              <div
                key={item.id}
                className="p-5 rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md flex flex-col justify-between hover:border-cyan-500/30 transition-all shadow-sm"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <Badge variant="indigo">{item.level}</Badge>
                    <span className="text-xs font-bold text-slate-500">
                      {item.daysPerWeek} buổi / tuần
                    </span>
                  </div>

                  <h3 className="text-base font-bold text-slate-900 dark:text-white mb-1.5">
                    {item.name}
                  </h3>
                  <p className="text-xs text-slate-500 line-clamp-2 mb-4">
                    {item.description || 'Chưa có mô tả chi tiết'}
                  </p>
                </div>

                <div className="pt-4 mt-4 border-t border-slate-100 dark:border-white/5 flex items-center justify-end gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      setEditingWorkout(item);
                      setWorkoutForm(item);
                      setIsWorkoutModalOpen(true);
                    }}
                    leftIcon={<Edit className="w-3.5 h-3.5" />}
                  >
                    Sửa
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => {
                      setItemToDelete({ id: item.id, type: 'workout' });
                      setIsDeleteOpen(true);
                    }}
                    leftIcon={<Trash2 className="w-3.5 h-3.5" />}
                  >
                    Xóa
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* DIET MODAL */}
      <Modal
        isOpen={isDietModalOpen}
        onClose={() => setIsDietModalOpen(false)}
        title={editingDiet ? 'Sửa Mẫu Thực đơn' : 'Tạo Mẫu Thực đơn Mới'}
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsDietModalOpen(false)}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleSaveDiet} isLoading={actionLoading}>
              Lưu mẫu thực đơn
            </Button>
          </>
        }
      >
        <div className="space-y-3.5">
          <Input
            label="Tên mẫu thực đơn"
            value={dietForm.name}
            onChange={(e) => setDietForm({ ...dietForm, name: e.target.value })}
            placeholder="vd: Thực đơn thâm hụt calo cho nữ"
            required
          />
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Mục tiêu (Goal)"
              value={dietForm.goal}
              onChange={(e) => setDietForm({ ...dietForm, goal: e.target.value as GoalType })}
              options={[
                { value: 'LOSE_WEIGHT', label: 'Giảm mỡ (Lose Weight)' },
                { value: 'GAIN_WEIGHT', label: 'Tăng cân/cơ (Gain Weight)' },
                { value: 'MAINTAIN', label: 'Giữ cân (Maintain)' },
              ]}
            />
            <Input
              label="Target Calo (kcal)"
              type="number"
              value={dietForm.targetCalories}
              onChange={(e) => setDietForm({ ...dietForm, targetCalories: Number(e.target.value) })}
            />
          </div>
          <div className="grid grid-cols-3 gap-2">
            <Input
              label="% Protein"
              type="number"
              value={dietForm.proteinPercent}
              onChange={(e) => setDietForm({ ...dietForm, proteinPercent: Number(e.target.value) })}
            />
            <Input
              label="% Carbs"
              type="number"
              value={dietForm.carbPercent}
              onChange={(e) => setDietForm({ ...dietForm, carbPercent: Number(e.target.value) })}
            />
            <Input
              label="% Fat"
              type="number"
              value={dietForm.fatPercent}
              onChange={(e) => setDietForm({ ...dietForm, fatPercent: Number(e.target.value) })}
            />
          </div>
          <Input
            label="Mô tả thực đơn"
            value={dietForm.description}
            onChange={(e) => setDietForm({ ...dietForm, description: e.target.value })}
          />
        </div>
      </Modal>

      {/* WORKOUT MODAL */}
      <Modal
        isOpen={isWorkoutModalOpen}
        onClose={() => setIsWorkoutModalOpen(false)}
        title={editingWorkout ? 'Sửa Mẫu Lịch tập' : 'Tạo Mẫu Lịch tập Mới'}
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsWorkoutModalOpen(false)}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleSaveWorkout} isLoading={actionLoading}>
              Lưu mẫu lịch tập
            </Button>
          </>
        }
      >
        <div className="space-y-3.5">
          <Input
            label="Tên mẫu lịch tập"
            value={workoutForm.name}
            onChange={(e) => setWorkoutForm({ ...workoutForm, name: e.target.value })}
            placeholder="vd: Push / Pull / Legs 6 Ngày"
            required
          />
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Cấp độ (Level)"
              value={workoutForm.level}
              onChange={(e) => setWorkoutForm({ ...workoutForm, level: e.target.value as WorkoutLevel })}
              options={[
                { value: 'BEGINNER', label: 'Người mới (Beginner)' },
                { value: 'INTERMEDIATE', label: 'Trung cấp (Intermediate)' },
                { value: 'ADVANCED', label: 'Nâng cao (Advanced)' },
              ]}
            />
            <Input
              label="Số ngày tập / tuần"
              type="number"
              value={workoutForm.daysPerWeek}
              onChange={(e) => setWorkoutForm({ ...workoutForm, daysPerWeek: Number(e.target.value) })}
            />
          </div>
          <Input
            label="Mô tả giáo án"
            value={workoutForm.description}
            onChange={(e) => setWorkoutForm({ ...workoutForm, description: e.target.value })}
          />
        </div>
      </Modal>

      {/* DELETE CONFIRM */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDelete}
        title="Xóa mẫu giáo án"
        message="Bạn có chắc chắn muốn xóa mẫu này? Người dùng hiện đang theo mẫu này sẽ không bị ảnh hưởng."
        isLoading={actionLoading}
      />
    </div>
  );
};
