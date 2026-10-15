import { useEffect, useSyncExternalStore } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { IDLE_TIMEOUT_MS } from '../../constants';
import { authStore } from '../../services/auth';
import { queryClient } from '../providers';

/** Chặn truy cập khi chưa đăng nhập; tự đăng xuất sau 30 phút không thao tác (BR-17.1). Backend vẫn kiểm quyền mọi API. */
export function RequireAuth() {
  const authed = useSyncExternalStore(authStore.subscribe, authStore.isAuthenticated);
  const location = useLocation();

  useEffect(() => {
    if (!authed) return;
    let timer = window.setTimeout(() => authStore.logout(), IDLE_TIMEOUT_MS);
    const reset = () => {
      window.clearTimeout(timer);
      timer = window.setTimeout(() => authStore.logout(), IDLE_TIMEOUT_MS);
    };
    const events = ['click', 'keydown', 'mousemove', 'touchstart'];
    events.forEach((e) => window.addEventListener(e, reset));
    return () => {
      window.clearTimeout(timer);
      events.forEach((e) => window.removeEventListener(e, reset));
    };
  }, [authed]);

  // Đăng xuất thì xoá cache để dữ liệu của phiên trước không hiện ở phiên sau
  useEffect(() => {
    if (!authed) queryClient.clear();
  }, [authed]);

  if (!authed) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return <Outlet />;
}
