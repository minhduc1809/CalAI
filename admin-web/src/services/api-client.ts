import { API_PREFIX, REQUEST_TIMEOUT_MS } from '../constants';
import { authStore } from './auth';

/** Lỗi API chuẩn hoá: thông báo thân thiện, mã nghiệp vụ và danh sách lỗi theo trường (class-validator). */
export class ApiError extends Error {
  constructor(message: string, public status: number, public code?: string, public messages: string[] = []) {
    super(message);
  }
}

export type Query = Record<string, string | number | boolean | undefined | null>;

export const buildQuery = (params?: Query): string => {
  if (!params) return '';
  const p = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) if (v !== undefined && v !== null && v !== '') p.set(k, String(v));
  const s = p.toString();
  return s ? `?${s}` : '';
};

interface Options {
  method?: string;
  query?: Query;
  body?: unknown;
  signal?: AbortSignal;
  /** Trả về Blob thay vì JSON (tải file). */
  blob?: boolean;
}

/** Điểm gọi API duy nhất: base URL từ môi trường, timeout, huỷ request, xử lý 401, chuẩn hoá lỗi. */
export async function request<T>(path: string, opts: Options = {}): Promise<T> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  opts.signal?.addEventListener('abort', () => controller.abort());
  const token = authStore.getToken();
  let res: Response;
  try {
    res = await fetch(`${API_PREFIX}${path}${buildQuery(opts.query)}`, {
      method: opts.method ?? 'GET',
      headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      body: opts.body === undefined ? undefined : JSON.stringify(opts.body),
      signal: controller.signal,
    });
  } catch (e) {
    if (opts.signal?.aborted) throw e; // người dùng đổi filter: request cũ bị huỷ, không phải lỗi
    throw new ApiError('Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại.', 0);
  } finally {
    clearTimeout(timer);
  }
  if (opts.blob && res.ok) return (await res.blob()) as T;
  let json: any = null;
  try {
    json = await res.json();
  } catch {
    // thân rỗng
  }
  if (res.status === 401 && token) authStore.expire();
  if (!res.ok || json?.success === false) {
    const raw = json?.message ?? json?.error ?? `Lỗi ${res.status}`;
    const msgs: string[] = Array.isArray(raw) ? raw.map(String) : [String(raw)];
    throw new ApiError(msgs.join('\n'), res.status, json?.code, msgs);
  }
  return (json && typeof json === 'object' && 'data' in json ? json.data : json) as T;
}

export const api = {
  get: <T>(path: string, query?: Query, signal?: AbortSignal) => request<T>(path, { query, signal }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body: body ?? {} }),
  patch: <T>(path: string, body: unknown) => request<T>(path, { method: 'PATCH', body }),
  delete: <T>(path: string, body?: unknown) => request<T>(path, { method: 'DELETE', body: body ?? {} }),
  blob: (path: string, query?: Query) => request<Blob>(path, { query, blob: true }),
};
