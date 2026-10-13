import { api, Query } from '../../../services/api-client';
import type { SafetyTags } from '../../food-catalog/types/food.types';

export interface DuplicateFood {
  name: string; category: string; servingSize: string; calories: number; protein: number; carb: number; fat: number; isActive: boolean;
}

export interface Submission {
  id: string; name: string; servingSize: string; calories: number; protein: number; carb: number; fat: number;
  barcode: string | null; note: string | null; status: 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELED';
  possibleDuplicate: boolean; duplicateOfName: string | null; duplicateOf: DuplicateFood | null; rejectReason: string | null;
  createdAt: string; user: { id: string; email: string | null; username: string };
}

export interface SubmissionList { total: number; page: number; limit: number; items: Submission[] }

export interface ApproveInput extends SafetyTags {
  category: string; name?: string; servingSize?: string; calories?: number; protein?: number; carb?: number; fat?: number; aliases?: string[];
}

export const submissionApi = {
  list: (q: Query, signal?: AbortSignal) => api.get<SubmissionList>('/admin/food-submissions', q, signal),
  approve: (id: string, input: ApproveInput) => api.post<{ warnings: string[] }>(`/admin/food-submissions/${id}/approve`, input),
  reject: (id: string, reason: string) => api.post(`/admin/food-submissions/${id}/reject`, { reason }),
};
