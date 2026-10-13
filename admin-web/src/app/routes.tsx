import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { RequireAuth } from './guards/RequireAuth';
import { AdminLayout } from '../layouts/AdminLayout';
import { LoadingState } from '../components/feedback/States';
import { LoginPage } from '../features/auth/pages/LoginPage';

// Mỗi module nặng tải riêng khi mở lần đầu (giảm kích thước bản tải ban đầu).
const DashboardPage = lazy(() => import('../features/dashboard/pages/DashboardPage'));
const UsersPage = lazy(() => import('../features/users/pages/UsersPage'));
const UserDetailPage = lazy(() => import('../features/users/pages/UserDetailPage'));
const DataRequestsPage = lazy(() => import('../features/data-requests/pages/DataRequestsPage'));
const QrOrdersPage = lazy(() => import('../features/qr-orders/pages/QrOrdersPage'));
const PremiumGrantsPage = lazy(() => import('../features/premium-grants/pages/PremiumGrantsPage'));
const RevenuePage = lazy(() => import('../features/revenue/pages/RevenuePage'));
const FoodCatalogPage = lazy(() => import('../features/food-catalog/pages/FoodCatalogPage'));
const FoodSubmissionsPage = lazy(() => import('../features/food-submissions/pages/FoodSubmissionsPage'));
const ExercisesPage = lazy(() => import('../features/exercises/pages/ExercisesPage'));
const AiMonitoringPage = lazy(() => import('../features/ai-monitoring/pages/AiMonitoringPage'));
const SystemHealthPage = lazy(() => import('../features/system-health/pages/SystemHealthPage'));
const SystemSettingsPage = lazy(() => import('../features/system-settings/pages/SystemSettingsPage'));
const AuditLogsPage = lazy(() => import('../features/audit-logs/pages/AuditLogsPage'));
const SecurityPage = lazy(() => import('../features/security/pages/SecurityPage'));

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AdminLayout />}>
          <Route
            path="*"
            element={
              <Suspense fallback={<LoadingState lines={8} />}>
                <Routes>
                  <Route path="/" element={<DashboardPage />} />
                  <Route path="/users" element={<UsersPage />} />
                  <Route path="/users/:id" element={<UserDetailPage />} />
                  <Route path="/data-requests" element={<DataRequestsPage />} />
                  <Route path="/payments" element={<QrOrdersPage />} />
                  <Route path="/grants" element={<PremiumGrantsPage />} />
                  <Route path="/revenue" element={<RevenuePage />} />
                  <Route path="/foods" element={<FoodCatalogPage />} />
                  <Route path="/submissions" element={<FoodSubmissionsPage />} />
                  <Route path="/exercises" element={<ExercisesPage />} />
                  <Route path="/ai" element={<AiMonitoringPage />} />
                  <Route path="/system-health" element={<SystemHealthPage />} />
                  <Route path="/config" element={<SystemSettingsPage />} />
                  <Route path="/audit" element={<AuditLogsPage />} />
                  <Route path="/security" element={<SecurityPage />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </Suspense>
            }
          />
        </Route>
      </Route>
    </Routes>
  );
}
