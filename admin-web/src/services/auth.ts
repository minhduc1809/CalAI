/**
 * Phiên đăng nhập admin. Hiện token nằm trong sessionStorage (đóng tab là mất).
 * Kế hoạch an toàn hơn (cần đổi cả backend): refresh token trong cookie HttpOnly + Secure + SameSite=Strict, access token chỉ
 * giữ trong bộ nhớ, thêm chống CSRF và CORS đúng tên miền. Được ghi ở mục "Việc cần xử lý tiếp" của báo cáo.
 */
export interface AdminInfo {
  id: string;
  email: string;
  name?: string | null;
  role: string;
}

const TOKEN_KEY = 'nw_admin_token';
const ADMIN_KEY = 'nw_admin_info';
type Listener = () => void;
const listeners = new Set<Listener>();
const notify = () => listeners.forEach((l) => l());

export const authStore = {
  getToken: () => sessionStorage.getItem(TOKEN_KEY),
  getAdmin: (): AdminInfo | null => {
    try {
      return JSON.parse(sessionStorage.getItem(ADMIN_KEY) ?? 'null');
    } catch {
      return null;
    }
  },
  isAuthenticated: () => !!sessionStorage.getItem(TOKEN_KEY),
  login(token: string, admin: AdminInfo) {
    sessionStorage.setItem(TOKEN_KEY, token);
    sessionStorage.setItem(ADMIN_KEY, JSON.stringify(admin));
    notify();
  },
  logout() {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(ADMIN_KEY);
    notify();
  },
  /** Gọi khi máy chủ trả 401: xoá phiên để giao diện về màn đăng nhập. */
  expire() {
    authStore.logout();
  },
  subscribe(l: Listener) {
    listeners.add(l);
    return () => listeners.delete(l);
  },
};
