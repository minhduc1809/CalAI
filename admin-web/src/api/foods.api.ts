import { apiClient } from './client';
import { CustomFood, FavoriteFood, PaginatedResponse } from '../types';
import { mockCustomFoods, mockFavoriteFoods } from '../utils/mockData';

let localCustomFoods = [...mockCustomFoods];
let localFavoriteFoods = [...mockFavoriteFoods];

export const foodsApi = {
  // Custom foods
  async getCustomFoods(params?: { search?: string; userId?: string; page?: number; limit?: number }): Promise<PaginatedResponse<CustomFood>> {
    try {
      const res = await apiClient.get('/admin/foods/custom', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localCustomFoods];
      if (params?.search) {
        filtered = filtered.filter((f) => f.name.toLowerCase().includes(params.search!.toLowerCase()));
      }
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

  async createCustomFood(data: Partial<CustomFood>): Promise<CustomFood> {
    try {
      const res = await apiClient.post('/admin/foods/custom', data);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const item: CustomFood = {
          id: `cf_${Date.now()}`,
          name: data.name || 'Món ăn mới',
          servingSize: data.servingSize || 100,
          servingUnit: data.servingUnit || 'GRAM',
          calories: data.calories || 200,
          protein: data.protein || 10,
          carb: data.carb || 20,
          fat: data.fat || 5,
          createdAt: new Date().toISOString(),
        };
        localCustomFoods.unshift(item);
        return item;
      }
      throw err;
    }
  },

  async updateCustomFood(id: string, data: Partial<CustomFood>): Promise<CustomFood> {
    try {
      const res = await apiClient.patch(`/admin/foods/custom/${id}`, data);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localCustomFoods = localCustomFoods.map((f) => (f.id === id ? { ...f, ...data } : f));
        return localCustomFoods.find((f) => f.id === id)!;
      }
      throw err;
    }
  },

  async deleteCustomFood(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/foods/custom/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localCustomFoods = localCustomFoods.filter((f) => f.id !== id);
        return { message: 'Xóa món ăn tùy chọn thành công' };
      }
      throw err;
    }
  },

  // Favorite foods
  async getFavoriteFoods(params?: { search?: string; userId?: string; page?: number; limit?: number }): Promise<PaginatedResponse<FavoriteFood>> {
    try {
      const res = await apiClient.get('/admin/foods/favorites', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localFavoriteFoods];
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

  async deleteFavoriteFood(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/foods/favorites/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localFavoriteFoods = localFavoriteFoods.filter((f) => f.id !== id);
        return { message: 'Xóa món ăn yêu thích thành công' };
      }
      throw err;
    }
  },
};
