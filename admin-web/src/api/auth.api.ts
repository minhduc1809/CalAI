import { apiClient } from './client';
import { mockUsers } from '../utils/mockData';

export interface LoginPayload {
  username: string;
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
    try {
      const res = await apiClient.post<{ data: LoginResponse }>('/auth/login', payload);
      // Backend TransformInterceptor wraps response in { statusCode, message, data }
      const data = res.data.data || (res.data as any);
      if (data.user?.role !== 'ADMIN') {
        throw new Error('Quyền truy cập bị từ chối: Tài khoản không phải Quản trị viên (ADMIN)');
      }
      return data;
    } catch (err: any) {
      // If server is not reachable and credentials are demo admin, allow demo session
      if (
        (err.code === 'ERR_NETWORK' || err.code === 'ECONNREFUSED' || !err.response) &&
        payload.username === 'admin'
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
      const res = await apiClient.get('/users/profile');
      return res.data.data;
    } catch {
      return mockUsers[0];
    }
  },

  async logout(): Promise<void> {
    try {
      await apiClient.post('/auth/logout');
    } catch {
      // ignore
    }
  },
};
