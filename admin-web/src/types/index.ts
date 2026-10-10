export type Role = 'USER' | 'ADMIN';

export type GoalType = 'LOSE_WEIGHT' | 'MAINTAIN' | 'GAIN_WEIGHT';

export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK';

export type WorkoutCategory = 'STRENGTH' | 'CARDIO' | 'RUNNING' | 'CYCLING' | 'HIIT' | 'OTHER';

export type WorkoutLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export type CheckInStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'DISMISSED';

export type Mood = 'GREAT' | 'GOOD' | 'OKAY' | 'BAD';

export interface ApiResponse<T> {
  statusCode: number;
  message?: string;
  data: T;
}

export interface PaginatedResponse<T> {
  data: T[];
  meta: {
    total: number;
    page: number;
    limit: number;
    totalPages: number;
  };
}

export interface User {
  id: string;
  username: string;
  email: string | null;
  name: string | null;
  avatar: string | null;
  role: Role;
  isActive: boolean;
  authProvider: string;
  isEmailVerified: boolean;
  weightKg: number | null;
  targetWeightKg: number | null;
  heightCm: number | null;
  gender?: string | null;
  activityLevel?: string | null;
  goal: GoalType | null;
  targetCalories: number | null;
  targetProtein: number | null;
  targetCarb: number | null;
  targetFat: number | null;
  dailyAiQuota: number;
  purchasedAiQuota: number;
  purchasedChatQuota: number;
  isPremium?: boolean;
  subscriptionState?: AdminUserSubscriptionState | null;
  createdAt: string;
  updatedAt: string;
  _count?: {
    meals: number;
    workoutLogs: number;
    weightLogs: number;
    checkIns: number;
    aiMessages: number;
  };
}

export interface MealItem {
  id: string;
  mealId?: string;
  name: string;
  weight: number;
  calories: number;
  protein: number;
  carb: number;
  fat: number;
  source?: string;
}

export interface Meal {
  id: string;
  userId: string;
  mealType: MealType;
  date: string;
  imageUrl: string | null;
  totalCalories: number;
  totalProtein: number;
  totalCarb: number;
  totalFat: number;
  createdAt: string;
  user?: {
    id: string;
    username: string;
    name: string | null;
    email: string | null;
  };
  items: MealItem[];
}

export interface WorkoutSet {
  id: string;
  workoutExerciseId?: string;
  setNumber: number;
  reps?: number;
  weightKg?: number;
  rpe?: number;
  durationSeconds?: number;
}

export interface WorkoutExercise {
  id: string;
  workoutLogId?: string;
  exerciseName: string;
  order: number;
  notes?: string;
  sets: WorkoutSet[];
}

export interface Workout {
  id: string;
  userId: string;
  name: string;
  category: WorkoutCategory;
  date: string;
  durationMinutes: number;
  caloriesBurned?: number;
  rpe?: number;
  notes?: string;
  createdAt: string;
  user?: {
    id: string;
    username: string;
    name: string | null;
    email: string | null;
  };
  exercises: WorkoutExercise[];
}

export interface WeightLog {
  id: string;
  userId: string;
  weightKg: number;
  date: string;
  notes?: string;
  createdAt: string;
  user?: {
    id: string;
    username: string;
    name: string | null;
    email: string | null;
  };
}

export interface CheckIn {
  id: string;
  userId: string;
  weekNumber: number;
  weekStartDate: string;
  weekEndDate: string;
  currentCalorieTarget: number;
  proposedCalorieTarget: number;
  proposedProteinTarget?: number;
  proposedCarbTarget?: number;
  proposedFatTarget?: number;
  status: CheckInStatus;
  weightAtCheckin?: number;
  mood?: Mood;
  note?: string;
  adminFeedback?: string;
  reviewedAt?: string;
  createdAt: string;
  user?: {
    id: string;
    username: string;
    name: string | null;
    email: string | null;
    weightKg?: number;
    targetCalories?: number;
    targetProtein?: number;
    targetCarb?: number;
    targetFat?: number;
  };
}

export interface DietTemplate {
  id: string;
  name: string;
  goal: GoalType;
  targetCalories: number;
  proteinPercent: number;
  carbPercent: number;
  fatPercent: number;
  description?: string;
  mealsSample?: any;
  createdAt: string;
}

export interface WorkoutTemplate {
  id: string;
  name: string;
  goal: GoalType;
  level: WorkoutLevel;
  daysPerWeek: number;
  description?: string;
  scheduleDetails?: any;
  createdAt: string;
}

export interface CustomFood {
  id: string;
  userId?: string;
  name: string;
  servingSize: number;
  servingUnit: string;
  calories: number;
  protein: number;
  carb: number;
  fat: number;
  barcode?: string;
  createdAt: string;
  user?: {
    id: string;
    username: string;
    name: string | null;
  };
}

export interface FavoriteFood {
  id: string;
  userId: string;
  foodId: string;
  foodName: string;
  calories: number;
  createdAt: string;
  user?: {
    id: string;
    username: string;
    name: string | null;
  };
}

export interface ApiUsageLog {
  id: string;
  userId: string;
  feature: string;
  promptTokens: number;
  outputTokens: number;
  costUsd: number;
  createdAt: string;
  user?: {
    id: string;
    username: string;
    name: string | null;
    email: string | null;
  };
}

export interface AiMessage {
  id: string;
  userId: string;
  sender: 'USER' | 'ASSISTANT';
  content: string;
  tokensUsed?: number;
  createdAt: string;
  user?: {
    id: string;
    username: string;
    name: string | null;
  };
}

export interface OverviewStats {
  users: {
    total: number;
    active: number;
    inactive: number;
    admins: number;
    members: number;
  };
  meals: {
    total: number;
    today: number;
  };
  workouts: {
    total: number;
  };
  checkIns: {
    total: number;
    pending: number;
  };
  ai: {
    totalMessages: number;
    totalApiRequests: number;
    totalPromptTokens: number;
    totalOutputTokens: number;
    totalTokens: number;
    estimatedCostUsd: number;
  };
}

// ==========================================
// Phase 1 Admin Portal Specifications (BR-17)
// ==========================================

export interface AdminDashboardSummary {
  users: {
    total: number;
    newLast7Days: number;
    newLast30Days: number;
  };
  subscriptions: {
    activePremiums: number;
  };
  orders: {
    pending: number;
    paid: number;
  };
  revenue: {
    totalVnd: number;
  };
}

export type PaymentOrderStatus = 'PENDING' | 'PAID' | 'CANCELLED' | 'FAILED' | 'EXPIRED';

export interface AdminPaymentOrder {
  id: string;
  orderCode: string;
  userId: string;
  amount: number;
  currency?: string;
  status: PaymentOrderStatus;
  itemSku: string;
  userNote?: string | null;
  paidAmount?: number;
  paidAt?: string | null;
  createdAt: string;
  user?: {
    email: string;
    name: string;
    username?: string;
  };
}

export interface AdminManualGrant {
  id: string;
  userId: string;
  startsAt: string;
  endsAt: string;
  reason: string;
  revokedAt?: string | null;
  revokedReason?: string | null;
  grantedByAdminId?: string;
  createdAt: string;
  user?: {
    id: string;
    email: string;
    name: string;
    username?: string;
  };
}

export type AuditLogAction =
  | 'LOGIN'
  | 'APPROVE_PAYMENT'
  | 'REJECT_PAYMENT'
  | 'GRANT_PREMIUM'
  | 'REVOKE_PREMIUM'
  | string;

export interface AdminAuditLog {
  id: string;
  adminId: string;
  adminEmail: string;
  action: AuditLogAction;
  targetType: 'User' | 'PaymentOrder' | 'ManualGrant' | string;
  targetId: string;
  before?: Record<string, any> | null;
  after?: Record<string, any> | null;
  reason?: string | null;
  ipAddress?: string | null;
  userAgent?: string | null;
  createdAt: string;
}

export interface AdminUserSubscriptionState {
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | string;
  expiryTime: string | null;
  productId: string | null;
  autoRenewing: boolean;
}

export interface AdminUserBillingDetails {
  user: {
    id: string;
    email: string;
    username: string;
    name: string;
    isPremium: boolean;
  };
  subscription?: AdminUserSubscriptionState | null;
  orders: AdminPaymentOrder[];
  manualGrants: AdminManualGrant[];
}

