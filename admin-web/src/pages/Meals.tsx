import React, { useEffect, useState, useMemo } from 'react';
import {
  UtensilsCrossed,
  Plus,
  Trash2,
  Eye,
  Edit,
  Search,
  Filter,
  Image as ImageIcon,
  Flame,
  Clock,
  Sparkles,
} from 'lucide-react';
import { mealsApi, CreateMealPayload, UpdateMealPayload } from '../api/meals.api';
import { usersApi } from '../api/users.api';
import { Meal, MealItem, MealType, User } from '../types';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDate, formatCalories, formatGrams } from '../utils/formatters';
import { toast } from 'sonner';

export const MealsPage: React.FC = () => {
  const [meals, setMeals] = useState<Meal[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [selectedUserFilter, setSelectedUserFilter] = useState<string>('ALL');
  const [selectedTypeFilter, setSelectedTypeFilter] = useState<string>('ALL');

  // Modals
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [selectedMeal, setSelectedMeal] = useState<Meal | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Dynamic form state for Create/Edit
  const [formUserId, setFormUserId] = useState('');
  const [formMealType, setFormMealType] = useState<MealType>('LUNCH');
  const [formDate, setFormDate] = useState(new Date().toISOString().substring(0, 16));
  const [formImageUrl, setFormImageUrl] = useState('');
  const [formItems, setFormItems] = useState<Omit<MealItem, 'id' | 'mealId'>[]>([
    { name: '', weight: 100, calories: 150, protein: 10, carb: 20, fat: 3 },
  ]);

  const fetchMealsAndUsers = async () => {
    setIsLoading(true);
    try {
      const [mealsRes, usersRes] = await Promise.all([
        mealsApi.getMeals(),
        usersApi.getUsers(),
      ]);
      setMeals(mealsRes.data);
      setUsers(usersRes.data);
      if (usersRes.data.length > 0 && !formUserId) {
        setFormUserId(usersRes.data[0].id);
      }
    } catch {
      toast.error('Lỗi khi tải dữ liệu bữa ăn');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMealsAndUsers();
  }, []);

  // Filtered Meals
  const filteredMeals = useMemo(() => {
    return meals.filter((m) => {
      const matchUser = selectedUserFilter === 'ALL' || m.userId === selectedUserFilter;
      const matchType = selectedTypeFilter === 'ALL' || m.mealType === selectedTypeFilter;
      return matchUser && matchType;
    });
  }, [meals, selectedUserFilter, selectedTypeFilter]);

  // Dynamic Item Row helpers
  const handleAddItem = () => {
    setFormItems([
      ...formItems,
      { name: '', weight: 100, calories: 100, protein: 5, carb: 15, fat: 2 },
    ]);
  };

  const handleRemoveItem = (index: number) => {
    if (formItems.length === 1) {
      toast.error('Bữa ăn phải có ít nhất 1 món!');
      return;
    }
    setFormItems(formItems.filter((_, idx) => idx !== index));
  };

  const handleItemChange = (index: number, field: string, value: any) => {
    const updated = [...formItems];
    (updated[index] as any)[field] = value;
    setFormItems(updated);
  };

  // Auto total calculation
  const calculatedMacros = useMemo(() => {
    return formItems.reduce(
      (acc, item) => ({
        calories: acc.calories + (Number(item.calories) || 0),
        protein: acc.protein + (Number(item.protein) || 0),
        carb: acc.carb + (Number(item.carb) || 0),
        fat: acc.fat + (Number(item.fat) || 0),
      }),
      { calories: 0, protein: 0, carb: 0, fat: 0 }
    );
  }, [formItems]);

  // Create Meal
  const handleCreateMeal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formUserId) {
      toast.error('Vui lòng chọn người dùng');
      return;
    }
    if (formItems.some((i) => !i.name.trim())) {
      toast.error('Vui lòng nhập tên cho tất cả các món ăn');
      return;
    }

    setActionLoading(true);
    try {
      await mealsApi.createMeal({
        userId: formUserId,
        mealType: formMealType,
        date: new Date(formDate).toISOString(),
        imageUrl: formImageUrl || undefined,
        items: formItems,
      });
      toast.success('Thêm bữa ăn mới thành công');
      setIsCreateOpen(false);
      fetchMealsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi tạo bữa ăn');
    } finally {
      setActionLoading(false);
    }
  };

  // Open Edit Modal
  const openEditModal = (meal: Meal) => {
    setSelectedMeal(meal);
    setFormMealType(meal.mealType);
    setFormDate(new Date(meal.date).toISOString().substring(0, 16));
    setFormImageUrl(meal.imageUrl || '');
    setFormItems(
      meal.items.map((i) => ({
        name: i.name,
        weight: i.weight,
        calories: i.calories,
        protein: i.protein,
        carb: i.carb,
        fat: i.fat,
      }))
    );
    setIsEditOpen(true);
  };

  // Submit Edit
  const handleEditMeal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedMeal) return;

    setActionLoading(true);
    try {
      await mealsApi.updateMeal(selectedMeal.id, {
        mealType: formMealType,
        date: new Date(formDate).toISOString(),
        imageUrl: formImageUrl || undefined,
        items: formItems,
      });
      toast.success('Cập nhật bữa ăn thành công');
      setIsEditOpen(false);
      fetchMealsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi cập nhật bữa ăn');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete Meal
  const handleDeleteMeal = async () => {
    if (!selectedMeal) return;
    setActionLoading(true);
    try {
      await mealsApi.deleteMeal(selectedMeal.id);
      toast.success('Đã xóa bữa ăn thành công');
      setIsDeleteOpen(false);
      fetchMealsAndUsers();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi xóa bữa ăn');
    } finally {
      setActionLoading(false);
    }
  };

  const mealTypeVariants: Record<MealType, { label: string; variant: 'amber' | 'emerald' | 'indigo' | 'purple' }> = {
    BREAKFAST: { label: 'Bữa sáng', variant: 'amber' },
    LUNCH: { label: 'Bữa trưa', variant: 'emerald' },
    DINNER: { label: 'Bữa tối', variant: 'indigo' },
    SNACK: { label: 'Ăn nhẹ', variant: 'purple' },
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Quản lý Bữa ăn (Meals)
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Theo dõi ảnh món ăn, thành phần nguyên liệu và hàm lượng macro tự động
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => {
            setFormItems([{ name: '', weight: 100, calories: 150, protein: 10, carb: 20, fat: 3 }]);
            setIsCreateOpen(true);
          }}
        >
          Thêm bữa ăn
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
          value={selectedTypeFilter}
          onChange={(e) => setSelectedTypeFilter(e.target.value)}
          className="bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-slate-700 dark:text-slate-300 focus:outline-none focus:border-emerald-500"
        >
          <option value="ALL">Mọi Loại bữa ăn</option>
          <option value="BREAKFAST">Bữa sáng (Breakfast)</option>
          <option value="LUNCH">Bữa trưa (Lunch)</option>
          <option value="DINNER">Bữa tối (Dinner)</option>
          <option value="SNACK">Ăn vặt / Nhẹ (Snack)</option>
        </select>
      </div>

      {/* Table */}
      <div className="overflow-hidden rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50/80 dark:bg-slate-900/50 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-100 dark:border-white/5 uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3.5">Ảnh</th>
                <th className="px-5 py-3.5">Người dùng</th>
                <th className="px-5 py-3.5">Loại bữa</th>
                <th className="px-5 py-3.5">Thời gian</th>
                <th className="px-5 py-3.5">Tổng Calo</th>
                <th className="px-5 py-3.5">Macros (P / C / F)</th>
                <th className="px-5 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {filteredMeals.length === 0 ? (
                <tr>
                  <td colSpan={7}>
                    <EmptyState
                      title="Không có bữa ăn nào"
                      description="Hãy chọn loại bữa ăn hoặc người dùng khác."
                    />
                  </td>
                </tr>
              ) : (
                filteredMeals.map((meal) => (
                  <tr
                    key={meal.id}
                    className="hover:bg-slate-50/50 dark:hover:bg-white/[0.02] transition-colors"
                  >
                    <td className="px-5 py-3.5">
                      {meal.imageUrl ? (
                        <img
                          src={meal.imageUrl}
                          alt="Meal"
                          className="w-12 h-12 rounded-xl object-cover ring-1 ring-slate-200 dark:ring-white/10"
                        />
                      ) : (
                        <div className="w-12 h-12 rounded-xl bg-slate-100 dark:bg-white/5 flex items-center justify-center text-slate-400 border border-slate-200/60 dark:border-white/10">
                          <ImageIcon className="w-5 h-5" />
                        </div>
                      )}
                    </td>

                    <td className="px-5 py-3.5">
                      <p className="font-bold text-slate-900 dark:text-white">
                        {meal.user?.name || meal.user?.username || meal.userId}
                      </p>
                      <p className="text-[11px] text-slate-400">@{meal.user?.username || 'user'}</p>
                    </td>

                    <td className="px-5 py-3.5">
                      <Badge variant={mealTypeVariants[meal.mealType]?.variant || 'slate'}>
                        {mealTypeVariants[meal.mealType]?.label || meal.mealType}
                      </Badge>
                    </td>

                    <td className="px-5 py-3.5 text-slate-500">
                      {formatDate(meal.date)}
                    </td>

                    <td className="px-5 py-3.5">
                      <span className="font-extrabold text-emerald-600 dark:text-emerald-400 text-sm">
                        {formatCalories(meal.totalCalories)}
                      </span>
                    </td>

                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-1.5 font-medium text-[11px]">
                        <span className="text-emerald-500">{meal.totalProtein}g P</span>
                        <span className="text-slate-400">/</span>
                        <span className="text-cyan-500">{meal.totalCarb}g C</span>
                        <span className="text-slate-400">/</span>
                        <span className="text-amber-500">{meal.totalFat}g F</span>
                      </div>
                    </td>

                    <td className="px-5 py-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => {
                            setSelectedMeal(meal);
                            setIsDetailOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-emerald-500 hover:bg-emerald-500/10 transition-colors"
                          title="Xem chi tiết các món ăn con"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => openEditModal(meal)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-500 hover:bg-cyan-500/10 transition-colors"
                          title="Sửa bữa ăn"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => {
                            setSelectedMeal(meal);
                            setIsDeleteOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
                          title="Xóa bữa ăn"
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
      {selectedMeal && (
        <Modal
          isOpen={isDetailOpen}
          onClose={() => setIsDetailOpen(false)}
          title={`Chi tiết Bữa ăn #${selectedMeal.id}`}
          maxWidth="lg"
          footer={
            <Button variant="secondary" onClick={() => setIsDetailOpen(false)}>
              Đóng
            </Button>
          }
        >
          <div className="space-y-4">
            {selectedMeal.imageUrl && (
              <div className="w-full h-48 rounded-2xl overflow-hidden border border-slate-100 dark:border-white/10">
                <img
                  src={selectedMeal.imageUrl}
                  alt="Meal preview"
                  className="w-full h-full object-cover"
                />
              </div>
            )}

            <div className="grid grid-cols-4 gap-2.5 p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 text-center">
              <div>
                <p className="text-[10px] text-slate-400 font-semibold uppercase">Calo</p>
                <p className="text-sm font-bold text-emerald-500">{selectedMeal.totalCalories} kcal</p>
              </div>
              <div>
                <p className="text-[10px] text-slate-400 font-semibold uppercase">Protein</p>
                <p className="text-sm font-bold text-emerald-500">{selectedMeal.totalProtein} g</p>
              </div>
              <div>
                <p className="text-[10px] text-slate-400 font-semibold uppercase">Carbs</p>
                <p className="text-sm font-bold text-cyan-500">{selectedMeal.totalCarb} g</p>
              </div>
              <div>
                <p className="text-[10px] text-slate-400 font-semibold uppercase">Fat</p>
                <p className="text-sm font-bold text-amber-500">{selectedMeal.totalFat} g</p>
              </div>
            </div>

            <div>
              <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
                Danh sách Món ăn con ({selectedMeal.items.length} món)
              </h4>
              <div className="space-y-2">
                {selectedMeal.items.map((item, idx) => (
                  <div
                    key={item.id || idx}
                    className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/50 dark:border-white/5 flex items-center justify-between text-xs"
                  >
                    <div>
                      <p className="font-bold text-slate-900 dark:text-white">{item.name}</p>
                      <p className="text-[11px] text-slate-400">{item.weight}g {item.source && `• ${item.source}`}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-slate-800 dark:text-slate-200">{item.calories} kcal</p>
                      <p className="text-[10px] text-slate-400">
                        {item.protein}P / {item.carb}C / {item.fat}F
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </Modal>
      )}

      {/* CREATE & EDIT MODAL (DYNAMIC FORM) */}
      <Modal
        isOpen={isCreateOpen || isEditOpen}
        onClose={() => {
          setIsCreateOpen(false);
          setIsEditOpen(false);
        }}
        title={isCreateOpen ? 'Tạo Bữa ăn Mới' : 'Chỉnh sửa Bữa ăn'}
        maxWidth="xl"
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
              onClick={isCreateOpen ? handleCreateMeal : handleEditMeal}
              isLoading={actionLoading}
            >
              {isCreateOpen ? 'Tạo bữa ăn' : 'Lưu cập nhật'}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
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
            <Select
              label="Loại bữa"
              value={formMealType}
              onChange={(e) => setFormMealType(e.target.value as MealType)}
              options={[
                { value: 'BREAKFAST', label: 'Bữa sáng' },
                { value: 'LUNCH', label: 'Bữa trưa' },
                { value: 'DINNER', label: 'Bữa tối' },
                { value: 'SNACK', label: 'Ăn nhẹ' },
              ]}
            />
            <Input
              label="Thời gian"
              type="datetime-local"
              value={formDate}
              onChange={(e) => setFormDate(e.target.value)}
            />
          </div>

          <Input
            label="Đường dẫn ảnh món ăn (URL)"
            value={formImageUrl}
            onChange={(e) => setFormImageUrl(e.target.value)}
            placeholder="https://..."
          />

          {/* Dynamic Items Builder */}
          <div className="pt-2">
            <div className="flex items-center justify-between mb-2">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                Thành phần các món ăn con
              </label>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                onClick={handleAddItem}
                leftIcon={<Plus className="w-3.5 h-3.5" />}
              >
                Thêm món
              </Button>
            </div>

            <div className="space-y-2.5 max-h-60 overflow-y-auto pr-1">
              {formItems.map((item, idx) => (
                <div
                  key={idx}
                  className="p-3 rounded-xl bg-slate-50 dark:bg-white/5 border border-slate-200/60 dark:border-white/5 grid grid-cols-12 gap-2 items-center text-xs"
                >
                  <div className="col-span-4">
                    <input
                      type="text"
                      placeholder="Tên món (vd: Ức gà áp chảo)"
                      value={item.name}
                      onChange={(e) => handleItemChange(idx, 'name', e.target.value)}
                      className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-white/10 rounded-lg px-2.5 py-1.5 text-xs text-slate-900 dark:text-white"
                      required
                    />
                  </div>
                  <div className="col-span-2">
                    <input
                      type="number"
                      placeholder="Gram"
                      value={item.weight}
                      onChange={(e) => handleItemChange(idx, 'weight', Number(e.target.value))}
                      className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-white/10 rounded-lg px-2 py-1.5 text-xs text-slate-900 dark:text-white"
                    />
                  </div>
                  <div className="col-span-2">
                    <input
                      type="number"
                      placeholder="Kcal"
                      value={item.calories}
                      onChange={(e) => handleItemChange(idx, 'calories', Number(e.target.value))}
                      className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-white/10 rounded-lg px-2 py-1.5 text-xs text-slate-900 dark:text-white"
                    />
                  </div>
                  <div className="col-span-1">
                    <input
                      type="number"
                      placeholder="P"
                      value={item.protein}
                      onChange={(e) => handleItemChange(idx, 'protein', Number(e.target.value))}
                      className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-white/10 rounded-lg px-1.5 py-1.5 text-xs text-slate-900 dark:text-white"
                    />
                  </div>
                  <div className="col-span-1">
                    <input
                      type="number"
                      placeholder="C"
                      value={item.carb}
                      onChange={(e) => handleItemChange(idx, 'carb', Number(e.target.value))}
                      className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-white/10 rounded-lg px-1.5 py-1.5 text-xs text-slate-900 dark:text-white"
                    />
                  </div>
                  <div className="col-span-1">
                    <input
                      type="number"
                      placeholder="F"
                      value={item.fat}
                      onChange={(e) => handleItemChange(idx, 'fat', Number(e.target.value))}
                      className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-white/10 rounded-lg px-1.5 py-1.5 text-xs text-slate-900 dark:text-white"
                    />
                  </div>
                  <div className="col-span-1 text-right">
                    <button
                      type="button"
                      onClick={() => handleRemoveItem(idx)}
                      className="p-1 text-slate-400 hover:text-rose-500 transition-colors"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* Calculated Macros live summary */}
            <div className="mt-3 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-between text-xs">
              <span className="font-bold text-emerald-600 dark:text-emerald-400">
                Tổng cộng tự động:
              </span>
              <div className="flex items-center gap-3 font-semibold text-slate-800 dark:text-slate-200">
                <span>{calculatedMacros.calories} kcal</span>
                <span>• {calculatedMacros.protein}g Protein</span>
                <span>• {calculatedMacros.carb}g Carbs</span>
                <span>• {calculatedMacros.fat}g Fat</span>
              </div>
            </div>
          </div>
        </div>
      </Modal>

      {/* DELETE CONFIRM */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDeleteMeal}
        title="Xóa bữa ăn"
        message="Bạn có chắc chắn muốn xóa bữa ăn này? Dữ liệu dinh dưỡng tương ứng của người dùng sẽ bị xóa."
        isLoading={actionLoading}
      />
    </div>
  );
};
