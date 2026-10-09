import { apiClient } from './client';
import { Workout, PaginatedResponse, WorkoutCategory } from '../types';
import { mockWorkouts } from '../utils/mockData';

export interface WorkoutQueryParams {
  userId?: string;
  category?: WorkoutCategory;
  startDate?: string;
  endDate?: string;
  page?: number;
  limit?: number;
}

export interface CreateWorkoutPayload {
  userId: string;
  name: string;
  category: WorkoutCategory;
  date: string;
  durationMinutes: number;
  caloriesBurned?: number;
  rpe?: number;
  notes?: string;
  exercises?: {
    exerciseName: string;
    order: number;
    sets: {
      setNumber: number;
      reps?: number;
      weightKg?: number;
      rpe?: number;
    }[];
  }[];
}

export interface UpdateWorkoutPayload {
  name?: string;
  category?: WorkoutCategory;
  date?: string;
  durationMinutes?: number;
  caloriesBurned?: number;
  rpe?: number;
  notes?: string;
}

let localWorkouts = [...mockWorkouts];

export const workoutsApi = {
  async getWorkouts(params?: WorkoutQueryParams): Promise<PaginatedResponse<Workout>> {
    try {
      const res = await apiClient.get('/admin/workouts', { params });
      return res.data.data || res.data;
    } catch {
      let filtered = [...localWorkouts];
      if (params?.userId) filtered = filtered.filter((w) => w.userId === params.userId);
      if (params?.category) filtered = filtered.filter((w) => w.category === params.category);
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

  async getWorkoutById(id: string): Promise<Workout> {
    try {
      const res = await apiClient.get(`/admin/workouts/${id}`);
      return res.data.data;
    } catch {
      const wo = localWorkouts.find((w) => w.id === id);
      if (!wo) throw new Error('Không tìm thấy buổi tập');
      return wo;
    }
  },

  async createWorkout(payload: CreateWorkoutPayload): Promise<Workout> {
    try {
      const res = await apiClient.post('/admin/workouts', payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        const newWo: Workout = {
          id: `wo_${Date.now()}`,
          userId: payload.userId,
          name: payload.name,
          category: payload.category,
          date: payload.date,
          durationMinutes: payload.durationMinutes,
          caloriesBurned: payload.caloriesBurned || 300,
          rpe: payload.rpe || 7,
          notes: payload.notes || '',
          createdAt: new Date().toISOString(),
          exercises: (payload.exercises || []).map((ex, exIdx) => ({
            id: `ex_${Date.now()}_${exIdx}`,
            exerciseName: ex.exerciseName,
            order: ex.order,
            sets: ex.sets.map((s, sIdx) => ({
              id: `set_${Date.now()}_${sIdx}`,
              ...s,
            })),
          })),
        };
        localWorkouts.unshift(newWo);
        return newWo;
      }
      throw err;
    }
  },

  async updateWorkout(id: string, payload: UpdateWorkoutPayload): Promise<Workout> {
    try {
      const res = await apiClient.patch(`/admin/workouts/${id}`, payload);
      return res.data.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localWorkouts = localWorkouts.map((w) => (w.id === id ? { ...w, ...payload } : w));
        return localWorkouts.find((w) => w.id === id)!;
      }
      throw err;
    }
  },

  async deleteWorkout(id: string): Promise<{ message: string }> {
    try {
      const res = await apiClient.delete(`/admin/workouts/${id}`);
      return res.data.data || res.data;
    } catch (err: any) {
      if (err.code === 'ERR_NETWORK' || !err.response) {
        localWorkouts = localWorkouts.filter((w) => w.id !== id);
        return { message: 'Xóa buổi tập thành công' };
      }
      throw err;
    }
  },
};
