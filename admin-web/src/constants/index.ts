/** Hằng số dùng chung của web quản trị. Không rải số "ma thuật" trong component. */
export const API_BASE = (import.meta.env.VITE_API_URL as string | undefined) ?? '';
export const API_PREFIX = `${API_BASE}/api/v1`;
export const REQUEST_TIMEOUT_MS = 30_000;
/** Tự đăng xuất sau chừng này thời gian không thao tác (admin.idleTimeoutMin). */
export const IDLE_TIMEOUT_MS = 30 * 60 * 1000;
export const TIME_ZONE = 'Asia/Ho_Chi_Minh';
export const PAGE_SIZES = [10, 20, 50, 100] as const;
export const DEFAULT_PAGE_SIZE = 20;
export const SEARCH_DEBOUNCE_MS = 350;
/** Độ dài tối thiểu của lý do cho thao tác nhạy cảm (khớp backend). */
export const MIN_REASON_LENGTH = 10;
export const TOAST_MS = 4500;
