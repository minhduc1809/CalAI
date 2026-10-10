import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/auth.api';

export interface AdminUser {
  id: string;
  username: string;
  email: string | null;
  name: string | null;
  role: string;
  avatar?: string | null;
}

interface AuthContextType {
  user: AdminUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AdminUser | null>(() => {
    const saved = localStorage.getItem('calai_admin_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('calai_admin_token');
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('calai_admin_token');
    const savedUser = localStorage.getItem('calai_admin_user');
    if (savedToken && savedUser) {
      setToken(savedToken);
      try {
        setUser(JSON.parse(savedUser));
      } catch {
        setUser(null);
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ usernameOrEmail: username, password });
    const adminUser = res.admin || (res as any).user;
    if (adminUser.role !== 'ADMIN') {
      throw new Error('Chỉ quản trị viên (ADMIN) mới có quyền truy cập');
    }
    localStorage.setItem('calai_admin_token', res.accessToken);
    if (res.refreshToken) {
      localStorage.setItem('calai_admin_refresh_token', res.refreshToken);
    }
    localStorage.setItem('calai_admin_user', JSON.stringify(adminUser));
    setToken(res.accessToken);
    setUser(adminUser);
  };

  const logout = () => {
    authApi.logout();
    localStorage.removeItem('calai_admin_token');
    localStorage.removeItem('calai_admin_user');
    setToken(null);
    setUser(null);
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
