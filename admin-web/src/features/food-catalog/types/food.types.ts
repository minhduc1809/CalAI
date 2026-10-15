export interface SafetyTags {
  allergens: string[];
  containsMeat: boolean;
  containsFish: boolean;
  containsEgg: boolean;
  containsDairy: boolean;
  containsPork: boolean;
}

export interface FoodUnit { name: string; grams: number; isDefault?: boolean }

export interface Food extends SafetyTags {
  id: string;
  name: string;
  category: string;
  servingSize: string;
  calories: number;
  protein: number;
  carb: number;
  fat: number;
  note: string | null;
  aliases: string[];
  source: 'OFFICIAL' | 'COMMUNITY' | string;
  dataSource: string | null;
  isActive: boolean;
  safetyTagged: boolean;
  needsReview: boolean;
  units: FoodUnit[] | null;
  createdAt: string;
}

export interface FoodList { total: number; page: number; limit: number; items: Food[] }
export interface FoodCategory { category: string; count: number }
export interface FoodProgress { active: number; tagged: number; needsReview: number; targets: { demo: number; release: number } }

export const ALLERGENS = ['DAIRY', 'EGG', 'FISH', 'GLUTEN', 'PEANUT', 'SESAME', 'SHELLFISH', 'SOY', 'TREE_NUT'] as const;
export const ALLERGEN_LABEL: Record<string, string> = {
  DAIRY: 'Sữa', EGG: 'Trứng', FISH: 'Cá', GLUTEN: 'Gluten', PEANUT: 'Đậu phộng',
  SESAME: 'Mè', SHELLFISH: 'Hải sản có vỏ', SOY: 'Đậu nành', TREE_NUT: 'Hạt cây',
};
export const DIET_FLAGS = [
  ['containsMeat', 'Có thịt'],
  ['containsFish', 'Có cá'],
  ['containsEgg', 'Có trứng'],
  ['containsDairy', 'Có sữa'],
  ['containsPork', 'Có thịt heo'],
] as const;

export const emptyTags = (): SafetyTags => ({
  allergens: [], containsMeat: false, containsFish: false, containsEgg: false, containsDairy: false, containsPork: false,
});
