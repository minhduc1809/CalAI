import { api, Query } from '../../../services/api-client';
import type { Food, FoodCategory, FoodList, FoodProgress, FoodUnit, SafetyTags } from '../types/food.types';
import type { ImportPreview } from '../../../components/import-export/ImportWizard';

export interface FoodPayload extends SafetyTags {
  name: string; category: string; servingSize: string; calories: number; protein: number; carb: number; fat: number;
  note?: string; aliases: string[]; units: FoodUnit[]; dataSource: string; needsReview: boolean;
}

export const foodApi = {
  list: (q: Query, signal?: AbortSignal) => api.get<FoodList>('/admin/foods', q, signal),
  categories: () => api.get<FoodCategory[]>('/admin/foods/categories'),
  progress: () => api.get<FoodProgress>('/admin/foods/progress'),
  create: (p: FoodPayload) => api.post<{ food: Food; warnings: string[] }>('/admin/foods', p),
  update: (id: string, p: Partial<FoodPayload>) => api.patch<{ food: Food; warnings: string[] }>(`/admin/foods/${id}`, p),
  hide: (id: string) => api.delete(`/admin/foods/${id}`),
  show: (id: string) => api.patch(`/admin/foods/${id}`, { isActive: true }),
  importPreview: (csv: string) => api.post<ImportPreview>('/admin/foods/import', { csv }),
  importCommit: (importId: string) => api.post<{ created: number }>(`/admin/foods/import/${importId}/commit`),
  history: (id: string) => api.get<{ items: { id: string; action: string; adminEmail: string | null; reason: string | null; createdAt: string }[] }>('/admin/audit-logs', { targetType: 'FoodCatalog', targetId: id, limit: 20 }),
};
