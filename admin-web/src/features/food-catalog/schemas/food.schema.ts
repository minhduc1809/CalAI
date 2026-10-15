import { ALLERGENS, Food, FoodUnit, SafetyTags, emptyTags } from '../types/food.types';
import type { FoodPayload } from '../api/food.api';

/** Giá trị form (chuỗi cho ô nhập số để giữ nguyên những gì người dùng gõ). */
export interface FoodFormValues extends SafetyTags, Record<string, unknown> {
  name: string; category: string; servingSize: string; calories: string; protein: string; carb: string; fat: string;
  note: string; aliases: string; units: string; dataSource: string; needsReview: boolean;
}

export const defaultFoodValues = (): FoodFormValues => ({
  ...emptyTags(), name: '', category: '', servingSize: '', calories: '', protein: '', carb: '', fat: '',
  note: '', aliases: '', units: '', dataSource: '', needsReview: false,
});

export const toFormValues = (f: Food): FoodFormValues => ({
  allergens: f.allergens, containsMeat: f.containsMeat, containsFish: f.containsFish, containsEgg: f.containsEgg,
  containsDairy: f.containsDairy, containsPork: f.containsPork,
  name: f.name, category: f.category, servingSize: f.servingSize,
  calories: String(f.calories), protein: String(f.protein), carb: String(f.carb), fat: String(f.fat),
  note: f.note ?? '', aliases: f.aliases.join(', '), dataSource: f.dataSource ?? '', needsReview: f.needsReview,
  units: (f.units ?? []).map((u) => `${u.name}=${u.grams}${u.isDefault ? '*' : ''}`).join('\n'),
});

/** "tô nhỏ=350" mỗi dòng; dấu * đánh dấu đơn vị mặc định. Ném lỗi có thông báo nếu dòng sai dạng. */
export function parseUnits(text: string): FoodUnit[] {
  return text.split('\n').map((l) => l.trim()).filter(Boolean).map((line) => {
    const m = line.match(/^(.+?)\s*=\s*([\d.,]+)\s*(\*)?$/);
    if (!m) throw new Error(`Dòng đơn vị phải có dạng "tên=số gram": ${line}`);
    return { name: m[1].trim(), grams: Number(m[2].replace(',', '.')), isDefault: !!m[3] };
  });
}

const LIMITS = { calories: 5000, protein: 500, carb: 500, fat: 500 } as const;
const NUTRIENT_LABEL = { calories: 'Calo', protein: 'Đạm', carb: 'Tinh bột', fat: 'Béo' } as const;

export type FoodErrors = Partial<Record<keyof FoodFormValues, string>>;

export function validateFood(v: FoodFormValues): FoodErrors {
  const e: FoodErrors = {};
  if (!v.name.trim()) e.name = 'Nhập tên món';
  else if (v.name.trim().length > 120) e.name = 'Tên món tối đa 120 ký tự';
  if (!v.category.trim()) e.category = 'Chọn hoặc nhập nhóm món';
  if (!v.servingSize.trim()) e.servingSize = 'Nhập khẩu phần chuẩn, ví dụ "1 tô (500g)"';
  for (const k of ['calories', 'protein', 'carb', 'fat'] as const) {
    const raw = v[k];
    const n = Number(raw);
    if (raw === '' || !Number.isFinite(n)) e[k] = `${NUTRIENT_LABEL[k]} phải là số`;
    else if (n < 0) e[k] = `${NUTRIENT_LABEL[k]} không được âm`;
    else if (n > LIMITS[k]) e[k] = `${NUTRIENT_LABEL[k]} tối đa ${LIMITS[k]}`;
  }
  if (!v.dataSource.trim()) e.dataSource = 'Phải ghi rõ nguồn số liệu';
  try {
    parseUnits(v.units);
  } catch (err) {
    e.units = (err as Error).message;
  }
  if (v.allergens.some((a) => !(ALLERGENS as readonly string[]).includes(a))) e.allergens = 'Mã dị ứng không hợp lệ';
  return e;
}

/** Cảnh báo mềm: calo lệch quá 15% so với 4·đạm + 4·tinh bột + 9·béo (không chặn lưu). */
export function atwarterWarning(v: FoodFormValues, tolerancePct = 15): string | null {
  const [c, p, cb, f] = [v.calories, v.protein, v.carb, v.fat].map(Number);
  if (![c, p, cb, f].every(Number.isFinite)) return null;
  const expected = 4 * p + 4 * cb + 9 * f;
  if (expected <= 0 || c <= 0) return null;
  const diff = (Math.abs(c - expected) / expected) * 100;
  return diff > tolerancePct ? `Calo (${Math.round(c)}) lệch ${Math.round(diff)}% so với tính từ đạm/tinh bột/béo (${Math.round(expected)}). Hãy kiểm tra lại đơn vị và số liệu; vẫn lưu được.` : null;
}

export function toPayload(v: FoodFormValues): FoodPayload {
  return {
    name: v.name.trim(), category: v.category.trim(), servingSize: v.servingSize.trim(),
    calories: Number(v.calories), protein: Number(v.protein), carb: Number(v.carb), fat: Number(v.fat),
    note: v.note.trim() || undefined,
    aliases: v.aliases.split(',').map((a) => a.trim()).filter(Boolean),
    units: parseUnits(v.units),
    allergens: v.allergens, containsMeat: v.containsMeat, containsFish: v.containsFish, containsEgg: v.containsEgg,
    containsDairy: v.containsDairy, containsPork: v.containsPork,
    dataSource: v.dataSource.trim(), needsReview: v.needsReview,
  };
}
