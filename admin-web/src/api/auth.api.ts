import { apiClient } from './client';
import { mockUsers } from '../utils/mockData';

export interface LoginPayload {
  usernameOrEmail?: string;
  username?: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  user: {
    id: string;
    username: string;
    email: string | null;
    name: string | null;
    role: string;
    avatar?: string | null;
  };
}

export const authApi = {
  async login(payload: LoginPayload): Promise<LoginResponse> {
    const usernameOrEmail = payload.usernameOrEmail || payload.username || '';
    try {
      let res;
      try {
        res = await apiClient.post('/admin/auth/login', {
          usernameOrEmail,
          password: payload.password,
        });
      } catch (e: any) {
        if (e.response?.status === 404) {
          // Fallback to legacy endpoint if admin prefix is mounted differently
          res = await apiClient.post('/auth/login', {
            username: usernameOrEmail,
            password: payload.password,
          });
        } else {
          throw e;
        }
      }

      const resData = res.data;
      const rawUser = resData.admin || resData.user || resData.data?.admin || resData.data?.user || resData.data;
      const accessToken = resData.accessToken || resData.data?.accessToken;
      const refreshToken = resData.refreshToken || resData.data?.refreshToken;

      if (!rawUser || rawUser.role !== 'ADMIN') {
        throw new Error('Chỉ quản trị viên (ADMIN) mới có quyền truy cập');
      }

      return {
        accessToken,
        refreshToken,
        user: {
          id: rawUser.id,
          username: rawUser.username,
          email: rawUser.email,
          name: rawUser.name,
          role: rawUser.role,
          avatar: rawUser.avatar || null,
        },
      };
    } catch (err: any) {
      // If server is not reachable and credentials are demo admin, allow demo session
      if (
        (err.code === 'ERR_NETWORK' || err.code === 'ECONNREFUSED' || !err.response) &&
        (usernameOrEmail === 'admin' || usernameOrEmail === 'admin@calai.com')
      ) {
        console.warn('Backend server offline. Entering preview admin session with mock data.');
        return {
          accessToken: 'mock_jwt_token_for_admin_preview',
          user: {
            id: mockUsers[0].id,
            username: mockUsers[0].username,
            email: mockUsers[0].email,
            name: mockUsers[0].name,
            role: mockUsers[0].role,
            avatar: mockUsers[0].avatar,
          },
        };
      }
      throw err;
    }
  },

  async getProfile(): Promise<any> {
    try {
      const res = await apiClient.get('/admin/auth/me');
      return res.data.admin || res.data;
    } catch {
      try {
        const res = await apiClient.get('/users/profile');
        return res.data.data || res.data;
      } catch {
        return mockUsers[0];
      }
    }
  },

  async logout(): Promise<void> {
    try {
      await apiClient.post('/admin/auth/logout');
    } catch {
      try {
        await apiClient.post('/auth/logout');
      } catch {
        // ignore
      }
    }
  },
};

