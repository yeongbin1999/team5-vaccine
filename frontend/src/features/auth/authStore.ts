import { create } from 'zustand';
import { apiClient } from '@/lib/backend/apiV1/client';
import type { LoginRequest, SignupRequest } from '@/lib/backend/apiV1/api';
import { useCartStore } from '@/features/cart/cartStore';

// 브라우저 환경에서만 localStorage 사용
const isBrowser = typeof window !== 'undefined';

export interface User {
  id: number;
  email: string;
  name: string;
  phone?: string;
  address?: string;
  role?: 'USER' | 'ADMIN';
}

interface AuthStore {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isAuthChecked: boolean; // 인증 상태 확인 완료 여부
  isCartSyncing: boolean; // 장바구니 병합 중 여부
  setCartSyncing: (syncing: boolean) => void;

  // 로그인
  login: (email: string, password: string) => Promise<User | null>;

  // 회원가입
  register: (
    email: string,
    password: string,
    name: string,
    phone?: string,
    address?: string
  ) => Promise<void>;

  // 로그아웃
  logout: () => void;

  // 사용자 정보 업데이트
  updateUser: (user: User) => void;

  // 인증 상태 확인
  checkAuth: () => Promise<void>;
}

export const useAuthStore = create<AuthStore>(set => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  isAuthChecked: false,
  isCartSyncing: false,
  setCartSyncing: (syncing: boolean) => set({ isCartSyncing: syncing }),

  login: async function (
    this: AuthStore,
    email: string,
    password: string
  ): Promise<User | null> {
    set({ isLoading: true });

    try {
      const loginData: LoginRequest = { email, password };
      const response = await apiClient.api.login(loginData);

      // Authorization 헤더에서 토큰 추출
      const authHeader =
        response.headers['authorization'] ||
        response.headers['Authorization'] ||
        response.headers['AUTHORIZATION'];

      if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.substring(7);
        if (isBrowser) {
          localStorage.setItem('accessToken', token);
        }
        console.log('✅ 토큰 저장 완료');
      } else {
        console.error('❌ Authorization 헤더를 찾을 수 없습니다');
      }

      // 로그인 성공 후 user 정보 fetch
      if (isBrowser) {
        await useAuthStore.getState().checkAuth();
        const user = (useAuthStore.getState() as AuthStore).user;
        set({ isLoading: false });
        return user;
      }
      set({ isLoading: false });
      return null;
    } catch (error: unknown) {
      set({ isLoading: false });
      console.error('로그인 에러:', error);

      // 백엔드에서 반환한 에러 메시지 처리
      if (error && typeof error === 'object' && 'response' in error) {
        const errorResponse = error as {
          response?: { data?: { message?: string } };
        };
        if (errorResponse.response?.data?.message) {
          throw new Error(errorResponse.response.data.message);
        }
      }
      if (error instanceof Error) {
        throw new Error(`로그인에 실패했습니다: ${error.message}`);
      } else {
        throw new Error('로그인에 실패했습니다.');
      }
    }
  },

  register: async (email: string, password: string, name: string) => {
    set({ isLoading: true });

    try {
      const signupData: SignupRequest = { email, password, name };
      await apiClient.api.signup(signupData);

      set({ isLoading: false });
      console.log('✅ 회원가입 성공');
    } catch (error: unknown) {
      set({ isLoading: false });
      console.error('회원가입 에러:', error);

      // 백엔드에서 반환한 에러 메시지 처리
      if (error && typeof error === 'object' && 'response' in error) {
        const errorResponse = error as {
          response?: { data?: { message?: string } };
        };
        if (errorResponse.response?.data?.message) {
          throw new Error(errorResponse.response.data.message);
        }
      }
      if (error instanceof Error) {
        throw new Error(`회원가입에 실패했습니다: ${error.message}`);
      } else {
        throw new Error('회원가입에 실패했습니다.');
      }
    }
  },

  logout: async () => {
    try {
      await apiClient.api.logout();
      console.log('✅ 로그아웃 성공');
    } catch (error: unknown) {
      console.error('로그아웃 에러:', error);
    } finally {
      // 로컬 상태 초기화
      if (isBrowser) {
        // 서버 items를 sessionStorage(cart-storage)에 저장
        const items = useCartStore.getState().items;
        sessionStorage.setItem(
          'cart-storage',
          JSON.stringify({ state: { items } })
        );
        localStorage.removeItem('accessToken');
      }
      useCartStore.setState({ items: [] });
      set({ user: null, isAuthenticated: false });
    }
  },

  updateUser: (user: User) => {
    set({ user });
  },

  checkAuth: async () => {
    try {
      if (!isBrowser) return;

      const token = localStorage.getItem('accessToken');
      console.log('🔍 checkAuth 시작:', { hasToken: !!token });

      if (!token) {
        console.log('❌ 토큰이 없음');
        set({ user: null, isAuthenticated: false, isAuthChecked: true });
        return;
      }

      console.log('🔍 getMe API 호출 시작');
      const userResponse = await apiClient.api.getMe();
      const userData = userResponse.data;
      console.log('✅ getMe API 성공:', userData);

      const user: User = {
        id: userData.id || 0,
        email: userData.email || '',
        name: userData.name || '',
        phone: userData.phone,
        address: userData.address,
        role: userData.role,
      };

      console.log('✅ 인증 상태 설정:', { user, isAuthenticated: true });
      set({ user, isAuthenticated: true, isAuthChecked: true });
    } catch (error: unknown) {
      console.error('❌ 인증 확인 에러:', error);
      console.error('에러 상세:', {
        status: (error as { response?: { status?: number } })?.response?.status,
        data: (error as { response?: { data?: unknown } })?.response?.data,
        message: (error as { message?: string })?.message,
      });

      if (isBrowser) {
        console.log('🗑️ 토큰 삭제');
        localStorage.removeItem('accessToken');
      }
      set({ user: null, isAuthenticated: false, isAuthChecked: true });
    }
  },
}));
