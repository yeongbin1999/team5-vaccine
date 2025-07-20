import { create } from 'zustand';
import { apiClient } from '@/lib/backend/apiV1/client';
// 타입 단언으로 api 프로퍼티가 있다고 명시
const typedApiClient = apiClient as typeof apiClient & {
  api: import('@/lib/backend/apiV1/api').Api<unknown>['api'];
};
import { Api } from '@/lib/backend/apiV1/api';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  role?: string;
}

interface AdminAuthStore {
  isAuthenticated: boolean;
  isAuthChecked: boolean;
  user: { role: string } | null;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  checkAuth: () => void;
}

export const useAdminAuthStore = create<AdminAuthStore>(set => ({
  isAuthenticated: false,
  isAuthChecked: false,
  user: null,
  login: async (username, password) => {
    try {
      const response = await typedApiClient.api.login({
        email: username,
        password,
      });
      const authHeader = response.headers['authorization'];
      if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.substring(7);
        localStorage.setItem('accessToken', token);
        // JWT 파싱
        const decoded: JwtPayload = jwtDecode(token);
        const role = decoded.role || '';
        if (role === 'ADMIN') {
          set({ isAuthenticated: true, isAuthChecked: true, user: { role } });
          return true;
        } else {
          localStorage.removeItem('accessToken');
          set({ isAuthenticated: false, isAuthChecked: true, user: null });
          return false;
        }
      }
      set({ isAuthenticated: false, isAuthChecked: true, user: null });
      return false;
    } catch (e) {
      set({ isAuthenticated: false, isAuthChecked: true, user: null });
      return false;
    }
  },
  logout: () => {
    localStorage.removeItem('accessToken');
    set({ isAuthenticated: false, isAuthChecked: true, user: null });
  },
  checkAuth: () => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        set({ isAuthenticated: false, isAuthChecked: true, user: null });
        return;
      }
      try {
        const decoded: JwtPayload = jwtDecode(token);
        const role = decoded.role || '';
        if (role === 'ADMIN') {
          set({ isAuthenticated: true, isAuthChecked: true, user: { role } });
        } else {
          localStorage.removeItem('accessToken');
          set({ isAuthenticated: false, isAuthChecked: true, user: null });
        }
      } catch (e) {
        localStorage.removeItem('accessToken');
        set({ isAuthenticated: false, isAuthChecked: true, user: null });
      }
    }
  },
}));
