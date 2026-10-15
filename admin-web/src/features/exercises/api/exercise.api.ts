import { api, Query } from '../../../services/api-client';
import type { ImportPreview } from '../../../components/import-export/ImportWizard';

export interface Exercise {
  id: string; name: string; genderTarget: string; level: string; targetMuscle: string; equipment: string; source: string; isActive: boolean;
  data: {
    sets: number; repsOrDuration: string; restSeconds: number; caloriesBurnedEstimate: number; imageUrl?: string;
    instructions: { preparation: string; execution: string; commonMistakes: string; breathing: string };
  };
}
export interface ExerciseList { total: number; page: number; limit: number; items: Exercise[] }

export interface ExercisePayload {
  name: string; genderTarget: string; level: string; targetMuscle: string; equipment: string;
  sets: number; repsOrDuration: string; restSeconds: number; caloriesBurnedEstimate: number; imageUrl?: string;
  instructions: { preparation: string; execution: string; commonMistakes: string; breathing: string };
}

export const exerciseApi = {
  list: (q: Query, signal?: AbortSignal) => api.get<ExerciseList>('/admin/exercises', q, signal),
  create: (p: ExercisePayload) => api.post('/admin/exercises', p),
  update: (id: string, p: Partial<ExercisePayload>) => api.patch(`/admin/exercises/${id}`, p),
  hide: (id: string) => api.delete(`/admin/exercises/${id}`),
  show: (id: string) => api.patch(`/admin/exercises/${id}`, { isActive: true }),
  importPreview: (csv: string) => api.post<ImportPreview>('/admin/exercises/import', { csv }),
  importCommit: (importId: string) => api.post<{ created: number }>(`/admin/exercises/import/${importId}/commit`),
};

export const LEVELS = [{ value: 'BEGINNER', label: 'Dễ' }, { value: 'INTERMEDIATE', label: 'Vừa' }, { value: 'ADVANCED', label: 'Nâng cao' }];
export const EQUIPMENT = [
  { value: 'NO_EQUIPMENT', label: 'Không dụng cụ' }, { value: 'DUMBBELL', label: 'Tạ đơn' },
  { value: 'GYM_MACHINE', label: 'Máy tập' }, { value: 'RESISTANCE_BAND', label: 'Dây kháng lực' },
];
export const GENDERS = [{ value: 'ALL', label: 'Tất cả' }, { value: 'MALE', label: 'Nam' }, { value: 'FEMALE', label: 'Nữ' }];
export const labelOf = (opts: { value: string; label: string }[], v: string) => opts.find((o) => o.value === v)?.label ?? v;
