import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/auth.api';
import { toast } from 'sonner';

export interface AdminUser {
  id: string;
  username: string;
  email: string | null;
  name: string | null;
  role: 'ADMIN';
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
  const [user, setUser] = useState<AdminUser | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('calai_admin_token'));
  const [isLoading, setIsLoading] = useState<boolean>(() =>
    Boolean(localStorage.getItem('calai_admin_token')),
  );

  useEffect(() => {
    if (!token) return;

    let isCurrent = true;
    authApi
      .getProfile()
      .then((adminUser) => {
        if (!isCurrent) return;
        localStorage.setItem('calai_admin_user', JSON.stringify(adminUser));
        setUser(adminUser);
      })
      .catch((error: unknown) => {
        if (!isCurrent) return;
        authApi.logout();
        setToken(null);
        setUser(null);
        const message = error instanceof Error ? error.message : 'Không thể xác thực phiên quản trị';
        toast.error(message);
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false);
      });

    return () => {
      isCurrent = false;
    };
  }, [token]);

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ usernameOrEmail: username, password });
    const adminUser = res.admin;
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
