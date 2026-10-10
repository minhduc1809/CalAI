import { apiClient } from './client';

export interface LoginPayload {
  usernameOrEmail: string;
  password: string;
}

export interface AdminUser {
  id: string;
  username: string;
  email: string;
  name: string;
  role: 'ADMIN' | 'USER';
  avatar?: string | null;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  admin: AdminUser;
}

export const authApi = {
  async login(payload: LoginPayload): Promise<LoginResponse> {
    const rawInput = (payload.usernameOrEmail || '').trim();
    // In NestJS, AdminLoginDto requires email
    const email = rawInput.includes('@') ? rawInput : `${rawInput}@calai.com`;

    try {
      const res = await apiClient.post('/admin/auth/login', {
        email,
        password: payload.password,
      });

      const resData = res.data?.data || res.data;
      const rawUser = resData.admin || resData.user;
      const accessToken = resData.accessToken;
      const refreshToken = resData.refreshToken;

      if (!accessToken) {
        throw new Error('Không nhận được mã xác thực accessToken');
      }

      if (rawUser && rawUser.role !== 'ADMIN') {
        throw new Error('Chỉ quản trị viên (ADMIN) mới có quyền truy cập');
      }

      const adminUser: AdminUser = {
        id: rawUser?.id || 'admin-id',
        username: rawUser?.username || rawInput,
        email: rawUser?.email || email,
        name: rawUser?.name || 'Administrator',
        role: rawUser?.role || 'ADMIN',
        avatar: rawUser?.avatar || null,
      };

      return {
        accessToken,
        refreshToken,
        admin: adminUser,
      };
    } catch (err: any) {
      if (err.response?.data?.message) {
        const msg = Array.isArray(err.response.data.message)
          ? err.response.data.message.join(', ')
          : err.response.data.message;
        throw new Error(msg);
      }
      throw err;
    }
  },

  async getProfile(): Promise<AdminUser> {
    const res = await apiClient.get('/admin/auth/me');
    const data = res.data?.data || res.data?.admin || res.data;
    return {
      id: data.id,
      username: data.username || 'admin',
      email: data.email || 'admin@calai.com',
      name: data.name || 'System Administrator',
      role: data.role || 'ADMIN',
      avatar: data.avatar || null,
    };
  },

  async logout(): Promise<void> {
    try {
      await apiClient.post('/admin/auth/logout');
    } catch {
      // Ignore network errors on logout
    } finally {
      localStorage.removeItem('calai_admin_token');
      localStorage.removeItem('calai_admin_user');
      localStorage.removeItem('calai_admin_refresh_token');
    }
  },
};
