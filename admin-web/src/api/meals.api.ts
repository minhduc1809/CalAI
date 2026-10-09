import { apiClient } from './client';
import { Meal, MealItem, PaginatedResponse, MealType } from '../types';
import { mockMeals } from '../utils/mockData';

export interface MealQueryParams {
  userId?: string;
  mealType?: MealType;
  startDate?: string;
  endDate?: string;
  page?: number;
  limit?: number;
}

export interface CreateMealPayload {
  userId: string;
  mealType: MealType;
  date: string;
  imageUrl?: string;
  items: Omit<MealItem, 'id' | 'mealId'>[];
}

export interface UpdateMealPayload {
  mealType?: MealType;
  date?: string;
  imageUrl?: string;
  items?: Omit<MealItem, 'id' | 'mealId'>[];
}

let localMeals = [...mockMeals];

export const mealsApi = {
  async getMeals(params?: MealQueryParams): Promise<PaginatedResponse<Meal>> {
    try {
      const res = await apiClient.get('/admin/meals', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localMeals];
      if (params?.userId) filtered = filtered.filter((m) => m.userId === params.userId);
      if (params?.mealType) filtered = filtered.filter((m) => m.mealType === params.mealType);
      return {
        data: filtered,
        meta: {
          total: filtered.length,
          page: params?.page || 1,
          limit: params?.limit || 20,
          totalPages: 1,
        },
      };
    }
  },

  async getMealById(id: string): Promise<Meal> {
    try {
      const res = await apiClient.get(`/admin/meals/${id}`);
      return res.data.data;
    } catch {
      const meal = localMeals.find((m) => m.id === id);
      if (!meal) throw new Error('Không tìm thấy bữa ăn');
      return meal;
    }
  },

  async createMeal(payload: CreateMealPayload): Promise<Meal> {
    try {
      const res = await apiClient.post('/admin/meals', payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const totalCalories = payload.items.reduce((s, i) => s + (Number(i.calories) || 0), 0);
        const totalProtein = payload.items.reduce((s, i) => s + (Number(i.protein) || 0), 0);
        const totalCarb = payload.items.reduce((s, i) => s + (Number(i.carb) || 0), 0);
        const totalFat = payload.items.reduce((s, i) => s + (Number(i.fat) || 0), 0);
        const newMeal: Meal = {
          id: `meal_${Date.now()}`,
          userId: payload.userId,
          mealType: payload.mealType,
          date: payload.date,
          imageUrl: payload.imageUrl || null,
          totalCalories,
          totalProtein,
          totalCarb,
          totalFat,
          createdAt: new Date().toISOString(),
          items: payload.items.map((it, idx) => ({ ...it, id: `item_${Date.now()}_${idx}` })),
        };
        localMeals.unshift(newMeal);
        return newMeal;
      }
      throw err;
    }
  },

  async updateMeal(id: string, payload: UpdateMealPayload): Promise<Meal> {
    try {
      const res = await apiClient.patch(`/admin/meals/${id}`, payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localMeals = localMeals.map((m) => {
          if (m.id !== id) return m;
          const updatedItems = payload.items
            ? payload.items.map((it, idx) => ({ ...it, id: `item_${Date.now()}_${idx}` }))
            : m.items;
          const totalCalories = updatedItems.reduce((s, i) => s + (Number(i.calories) || 0), 0);
          const totalProtein = updatedItems.reduce((s, i) => s + (Number(i.protein) || 0), 0);
          const totalCarb = updatedItems.reduce((s, i) => s + (Number(i.carb) || 0), 0);
          const totalFat = updatedItems.reduce((s, i) => s + (Number(i.fat) || 0), 0);
          return {
            ...m,
            mealType: payload.mealType || m.mealType,
            date: payload.date || m.date,
            imageUrl: payload.imageUrl !== undefined ? payload.imageUrl : m.imageUrl,
            items: updatedItems,
            totalCalories,
            totalProtein,
            totalCarb,
            totalFat,
          };
        });
        return localMeals.find((m) => m.id === id)!;
      }
      throw err;
    }
  },

  async deleteMeal(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/meals/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localMeals = localMeals.filter((m) => m.id !== id);
        return { message: 'Xóa bữa ăn thành công' };
      }
      throw err;
    }
  },
};
