import { apiClient } from './client';

export interface LoginPayload {
  usernameOrEmail: string;
  password: string;
}

export interface AdminUser {
  id: string;
  username: string;
  email: string;
  name: string | null;
  role: 'ADMIN';
  avatar?: string | null;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  admin: AdminUser;
}

export const authApi = {
  async login(payload: LoginPayload): Promise<LoginResponse> {
    try {
      const res = await apiClient.post('/admin/auth/login', {
        email: payload.usernameOrEmail.trim(),
        password: payload.password,
      });

      const resData = res.data?.data || res.data;
      const rawUser = resData.admin || resData.user;
      const accessToken = resData.accessToken;
      const refreshToken = resData.refreshToken;

      if (!accessToken) {
        throw new Error('Không nhận được mã xác thực accessToken');
      }
      if (!rawUser?.id || !rawUser?.email) {
        throw new Error('Phản hồi đăng nhập không có thông tin quản trị viên hợp lệ');
      }

      if (rawUser.role !== 'ADMIN') {
        throw new Error('Chỉ quản trị viên (ADMIN) mới có quyền truy cập');
      }

      const adminUser: AdminUser = {
        id: rawUser.id,
        username: rawUser.username || rawUser.email.split('@')[0],
        email: rawUser.email,
        name: rawUser.name ?? null,
        role: 'ADMIN',
        avatar: rawUser.avatar ?? null,
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
    if (!data?.id || !data?.email || data.role !== 'ADMIN') {
      throw new Error('Tài khoản hiện tại không có quyền quản trị viên');
    }
    return {
      id: data.id,
      username: data.username || data.email.split('@')[0],
      email: data.email,
      name: data.name ?? null,
      role: 'ADMIN',
      avatar: data.avatar || null,
    };
  },

  logout(): void {
    localStorage.removeItem('calai_admin_token');
    localStorage.removeItem('calai_admin_user');
    localStorage.removeItem('calai_admin_refresh_token');
  },
};
