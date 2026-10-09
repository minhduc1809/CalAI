import { apiClient } from './client';
import { DietTemplate, WorkoutTemplate, GoalType, WorkoutLevel } from '../types';
import { mockDietTemplates, mockWorkoutTemplates } from '../utils/mockData';

let localDietTemplates = [...mockDietTemplates];
let localWorkoutTemplates = [...mockWorkoutTemplates];

export const templatesApi = {
  // --- Diet Templates ---
  async getDietTemplates(goal?: GoalType): Promise<DietTemplate[]> {
    try {
      const res = await apiClient.get('/admin/templates/diet', { params: { goal } });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localDietTemplates];
      if (goal) filtered = filtered.filter((d) => d.goal === goal);
      return filtered;
    }
  },

  async createDietTemplate(data: Partial<DietTemplate>): Promise<DietTemplate> {
    try {
      const res = await apiClient.post('/admin/templates/diet', data);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const item: DietTemplate = {
          id: `dt_${Date.now()}`,
          name: data.name || 'Mẫu thực đơn mới',
          goal: data.goal || 'LOSE_WEIGHT',
          targetCalories: data.targetCalories || 2000,
          proteinPercent: data.proteinPercent || 30,
          carbPercent: data.carbPercent || 45,
          fatPercent: data.fatPercent || 25,
          description: data.description || '',
          createdAt: new Date().toISOString(),
        };
        localDietTemplates.unshift(item);
        return item;
      }
      throw err;
    }
  },

  async updateDietTemplate(id: string, data: Partial<DietTemplate>): Promise<DietTemplate> {
    try {
      const res = await apiClient.patch(`/admin/templates/diet/${id}`, data);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localDietTemplates = localDietTemplates.map((d) => (d.id === id ? { ...d, ...data } : d));
        return localDietTemplates.find((d) => d.id === id)!;
      }
      throw err;
    }
  },

  async deleteDietTemplate(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/templates/diet/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localDietTemplates = localDietTemplates.filter((d) => d.id !== id);
        return { message: 'Xóa mẫu thực đơn thành công' };
      }
      throw err;
    }
  },

  // --- Workout Templates ---
  async getWorkoutTemplates(goal?: GoalType, level?: WorkoutLevel): Promise<WorkoutTemplate[]> {
    try {
      const res = await apiClient.get('/admin/templates/workout', { params: { goal, level } });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localWorkoutTemplates];
      if (goal) filtered = filtered.filter((w) => w.goal === goal);
      if (level) filtered = filtered.filter((w) => w.level === level);
      return filtered;
    }
  },

  async createWorkoutTemplate(data: Partial<WorkoutTemplate>): Promise<WorkoutTemplate> {
    try {
      const res = await apiClient.post('/admin/templates/workout', data);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const item: WorkoutTemplate = {
          id: `wt_${Date.now()}`,
          name: data.name || 'Mẫu lịch tập mới',
          goal: data.goal || ('BUILD_MUSCLE' as any),
          level: data.level || 'BEGINNER',
          daysPerWeek: data.daysPerWeek || 3,
          description: data.description || '',
          createdAt: new Date().toISOString(),
        };
        localWorkoutTemplates.unshift(item);
        return item;
      }
      throw err;
    }
  },

  async updateWorkoutTemplate(id: string, data: Partial<WorkoutTemplate>): Promise<WorkoutTemplate> {
    try {
      const res = await apiClient.patch(`/admin/templates/workout/${id}`, data);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localWorkoutTemplates = localWorkoutTemplates.map((w) => (w.id === id ? { ...w, ...data } : w));
        return localWorkoutTemplates.find((w) => w.id === id)!;
      }
      throw err;
    }
  },

  async deleteWorkoutTemplate(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/templates/workout/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localWorkoutTemplates = localWorkoutTemplates.filter((w) => w.id !== id);
        return { message: 'Xóa mẫu lịch tập thành công' };
      }
      throw err;
    }
  },
};
