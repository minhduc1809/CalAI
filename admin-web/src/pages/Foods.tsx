import React, { useEffect, useState, useMemo } from 'react';
import {
  Apple,
  Heart,
  Plus,
  Trash2,
  Edit,
  Search,
  Flame,
} from 'lucide-react';
import { foodsApi } from '../api/foods.api';
import { CustomFood, FavoriteFood } from '../types';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState } from '../components/ui/EmptyState';
import { formatCalories } from '../utils/formatters';
import { toast } from 'sonner';

export const FoodsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'custom' | 'favorite'>('custom');
  const [customFoods, setCustomFoods] = useState<CustomFood[]>([]);
  const [favoriteFoods, setFavoriteFoods] = useState<FavoriteFood[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  // Modals
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<{ id: string; type: 'custom' | 'favorite' } | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Form state
  const [editingFood, setEditingFood] = useState<CustomFood | null>(null);
  const [foodForm, setFoodForm] = useState<Partial<CustomFood>>({
    name: '',
    servingSize: 100,
    servingUnit: 'GRAM',
    calories: 150,
    protein: 10,
    carb: 20,
    fat: 3,
  });

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const [customRes, favRes] = await Promise.all([
        foodsApi.getCustomFoods(),
        foodsApi.getFavoriteFoods(),
      ]);
      setCustomFoods(customRes.data);
      setFavoriteFoods(favRes.data);
    } catch {
      toast.error('Lỗi khi tải cơ sở dữ liệu món ăn');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const filteredCustom = useMemo(() => {
    return customFoods.filter((f) =>
      f.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [customFoods, searchTerm]);

  const filteredFavorites = useMemo(() => {
    return favoriteFoods.filter((f) =>
      f.foodName.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [favoriteFoods, searchTerm]);

  // Save Custom Food
  const handleSaveFood = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!foodForm.name) {
      toast.error('Vui lòng nhập tên món ăn');
      return;
    }
    setActionLoading(true);
    try {
      if (editingFood) {
        await foodsApi.updateCustomFood(editingFood.id, foodForm);
        toast.success('Cập nhật món ăn thành công');
      } else {
        await foodsApi.createCustomFood(foodForm);
        toast.success('Thêm món ăn mới thành công');
      }
      setIsModalOpen(false);
      fetchData();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi lưu món ăn');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete
  const handleDelete = async () => {
    if (!selectedItem) return;
    setActionLoading(true);
    try {
      if (selectedItem.type === 'custom') {
        await foodsApi.deleteCustomFood(selectedItem.id);
        toast.success('Đã xóa món ăn tùy chỉnh');
      } else {
        await foodsApi.deleteFavoriteFood(selectedItem.id);
        toast.success('Đã xóa khỏi danh sách yêu thích');
      }
      setIsDeleteOpen(false);
      fetchData();
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi xóa món ăn');
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
            Cơ sở dữ liệu Món ăn (Foods)
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Quản trị các món ăn tùy chỉnh do người dùng tạo và danh mục món ăn ưa thích
          </p>
        </div>

        {activeTab === 'custom' && (
          <Button
            variant="primary"
            size="sm"
            leftIcon={<Plus className="w-4 h-4" />}
            onClick={() => {
              setEditingFood(null);
              setFoodForm({
                name: '',
                servingSize: 100,
                servingUnit: 'GRAM',
                calories: 150,
                protein: 10,
                carb: 20,
                fat: 3,
              });
              setIsModalOpen(true);
            }}
          >
            Thêm món ăn
          </Button>
        )}
      </div>

      {/* Tabs & Search */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-2 p-1.5 rounded-2xl bg-slate-100 dark:bg-slate-900/60 border border-slate-200/60 dark:border-white/5 w-fit">
          <button
            onClick={() => setActiveTab('custom')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'custom'
                ? 'bg-white dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 shadow-sm border border-slate-200/60 dark:border-emerald-500/30'
                : 'text-slate-500 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <Apple className="w-4 h-4" />
            <span>Món Tùy chỉnh (Custom Foods)</span>
          </button>

          <button
            onClick={() => setActiveTab('favorite')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'favorite'
                ? 'bg-white dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 shadow-sm border border-slate-200/60 dark:border-emerald-500/30'
                : 'text-slate-500 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            <Heart className="w-4 h-4" />
            <span>Món Yêu thích (Favorites)</span>
          </button>
        </div>

        <div className="relative w-full sm:w-72">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm kiếm món ăn..."
            className="w-full bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-white/10 rounded-xl pl-9 pr-3.5 py-2 text-xs text-slate-900 dark:text-white focus:outline-none focus:border-emerald-500"
          />
        </div>
      </div>

      {/* TAB 1: CUSTOM FOODS TABLE */}
      {activeTab === 'custom' && (
        <div className="overflow-hidden rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50/80 dark:bg-slate-900/50 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-100 dark:border-white/5 uppercase tracking-wider">
                <tr>
                  <th className="px-5 py-3.5">Tên món ăn</th>
                  <th className="px-5 py-3.5">Khẩu phần</th>
                  <th className="px-5 py-3.5">Calo</th>
                  <th className="px-5 py-3.5">Protein</th>
                  <th className="px-5 py-3.5">Carbs</th>
                  <th className="px-5 py-3.5">Fat</th>
                  <th className="px-5 py-3.5">Người tạo</th>
                  <th className="px-5 py-3.5 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-white/5">
                {filteredCustom.length === 0 ? (
                  <tr>
                    <td colSpan={8}>
                      <EmptyState title="Không tìm thấy món ăn" />
                    </td>
                  </tr>
                ) : (
                  filteredCustom.map((food) => (
                    <tr
                      key={food.id}
                      className="hover:bg-slate-50/50 dark:hover:bg-white/[0.02] transition-colors"
                    >
                      <td className="px-5 py-3.5 font-bold text-slate-900 dark:text-white">
                        {food.name}
                      </td>
                      <td className="px-5 py-3.5 text-slate-500">
                        {food.servingSize} {food.servingUnit}
                      </td>
                      <td className="px-5 py-3.5 font-extrabold text-emerald-600 dark:text-emerald-400">
                        {food.calories} kcal
                      </td>
                      <td className="px-5 py-3.5 font-semibold text-emerald-500">
                        {food.protein} g
                      </td>
                      <td className="px-5 py-3.5 font-semibold text-cyan-500">
                        {food.carb} g
                      </td>
                      <td className="px-5 py-3.5 font-semibold text-amber-500">
                        {food.fat} g
                      </td>
                      <td className="px-5 py-3.5 text-slate-400">
                        @{food.user?.username || 'admin'}
                      </td>
                      <td className="px-5 py-3.5 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => {
                              setEditingFood(food);
                              setFoodForm(food);
                              setIsModalOpen(true);
                            }}
                            className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-500 hover:bg-cyan-500/10 transition-colors"
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => {
                              setSelectedItem({ id: food.id, type: 'custom' });
                              setIsDeleteOpen(true);
                            }}
                            className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
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
      )}

      {/* TAB 2: FAVORITE FOODS TABLE */}
      {activeTab === 'favorite' && (
        <div className="overflow-hidden rounded-2xl border border-slate-200/80 dark:border-white/5 bg-white dark:bg-[#111827]/80 backdrop-blur-md shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50/80 dark:bg-slate-900/50 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-100 dark:border-white/5 uppercase tracking-wider">
                <tr>
                  <th className="px-5 py-3.5">Món ăn yêu thích</th>
                  <th className="px-5 py-3.5">Năng lượng</th>
                  <th className="px-5 py-3.5">Người đánh dấu</th>
                  <th className="px-5 py-3.5 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-white/5">
                {filteredFavorites.length === 0 ? (
                  <tr>
                    <td colSpan={4}>
                      <EmptyState title="Chưa có món ăn yêu thích nào" />
                    </td>
                  </tr>
                ) : (
                  filteredFavorites.map((fav) => (
                    <tr
                      key={fav.id}
                      className="hover:bg-slate-50/50 dark:hover:bg-white/[0.02] transition-colors"
                    >
                      <td className="px-5 py-3.5 font-bold text-slate-900 dark:text-white flex items-center gap-2">
                        <Heart className="w-4 h-4 text-rose-500 fill-rose-500 shrink-0" />
                        <span>{fav.foodName}</span>
                      </td>
                      <td className="px-5 py-3.5 font-bold text-emerald-500">
                        {fav.calories} kcal
                      </td>
                      <td className="px-5 py-3.5 text-slate-400">
                        {fav.user?.name || fav.user?.username || fav.userId}
                      </td>
                      <td className="px-5 py-3.5 text-right">
                        <button
                          onClick={() => {
                            setSelectedItem({ id: fav.id, type: 'favorite' });
                            setIsDeleteOpen(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 transition-colors"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* CUSTOM FOOD MODAL */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingFood ? 'Sửa Món ăn' : 'Tạo Món ăn Tùy chỉnh'}
        maxWidth="md"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsModalOpen(false)}>
              Hủy
            </Button>
            <Button variant="primary" onClick={handleSaveFood} isLoading={actionLoading}>
              Lưu món ăn
            </Button>
          </>
        }
      >
        <div className="space-y-3.5">
          <Input
            label="Tên món ăn"
            value={foodForm.name}
            onChange={(e) => setFoodForm({ ...foodForm, name: e.target.value })}
            placeholder="vd: Cơm chiên dưa bò CalAI"
            required
          />
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Khối lượng khẩu phần"
              type="number"
              value={foodForm.servingSize}
              onChange={(e) => setFoodForm({ ...foodForm, servingSize: Number(e.target.value) })}
            />
            <Select
              label="Đơn vị tính"
              value={foodForm.servingUnit}
              onChange={(e) => setFoodForm({ ...foodForm, servingUnit: e.target.value })}
              options={[
                { value: 'GRAM', label: 'Gram (g)' },
                { value: 'ML', label: 'Mililit (ml)' },
                { value: 'PORTION', label: 'Suất / Phần' },
              ]}
            />
          </div>
          <div className="grid grid-cols-4 gap-2">
            <Input
              label="Calo (kcal)"
              type="number"
              value={foodForm.calories}
              onChange={(e) => setFoodForm({ ...foodForm, calories: Number(e.target.value) })}
            />
            <Input
              label="Protein (g)"
              type="number"
              value={foodForm.protein}
              onChange={(e) => setFoodForm({ ...foodForm, protein: Number(e.target.value) })}
            />
            <Input
              label="Carbs (g)"
              type="number"
              value={foodForm.carb}
              onChange={(e) => setFoodForm({ ...foodForm, carb: Number(e.target.value) })}
            />
            <Input
              label="Fat (g)"
              type="number"
              value={foodForm.fat}
              onChange={(e) => setFoodForm({ ...foodForm, fat: Number(e.target.value) })}
            />
          </div>
        </div>
      </Modal>

      {/* DELETE CONFIRM */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleDelete}
        title="Xóa món ăn"
        message="Bạn có chắc chắn muốn xóa mục này khỏi cơ sở dữ liệu?"
        isLoading={actionLoading}
      />
    </div>
  );
};
