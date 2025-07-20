import { create } from 'zustand';
import { apiClient } from '@/lib/backend/apiV1/client';

interface AdminAuthStore {
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  checkAuth: () => void;
}

export const useAdminAuthStore = create<AdminAuthStore>(set => ({
  isAuthenticated: false,
  login: async (username, password) => {
    try {
      // 관리자 전용 API가 있으면 아래를 사용:
      // const response = await apiClient.api.adminLogin({ username, password });
      // 임시로 기존 login 사용 (백엔드 구현 필요)
      const response = await apiClient.api.login({ email: username, password });
      const authHeader =
        response.headers['authorization'] ||
        response.headers['Authorization'] ||
        response.headers['AUTHORIZATION'];
      if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.substring(7);
        localStorage.setItem('adminAccessToken', token);
        set({ isAuthenticated: true });
        return true;
      }
      return false;
    } catch (e) {
      set({ isAuthenticated: false });
      return false;
    }
  },
  logout: () => {
    localStorage.removeItem('adminAccessToken');
    set({ isAuthenticated: false });
  },
  checkAuth: () => {
    if (typeof window !== "undefined") {
      const token = localStorage.getItem('adminAccessToken');
      set({ isAuthenticated: !!token });
    }
  },
})); 