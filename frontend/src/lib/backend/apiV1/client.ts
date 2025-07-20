import { Api } from './api';
import type {
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosError,
} from 'axios';

// 브라우저 환경에서만 localStorage 사용
const isBrowser = typeof window !== 'undefined';

// API 클라이언트 인스턴스 생성
export const apiClient = new Api({
  baseURL: '', // Next.js 프록시를 통해 요청 (상대 경로 사용)
  withCredentials: true, // 쿠키 기반 인증을 위해 필요
});

// 초기 설정 로깅
console.log('🔧 API 클라이언트 설정:', {
  baseURL: apiClient.instance.defaults.baseURL,
  isBrowser,
  withCredentials: apiClient.instance.defaults.withCredentials,
});

// JWT 토큰을 자동으로 헤더에 추가하는 인터셉터
apiClient.instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 요청 URL 로깅
    console.log('🚀 API 요청:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      baseURL: config.baseURL,
      fullURL: (config.baseURL || '') + (config.url || ''),
      headers: config.headers,
    });

    // 회원가입, 로그인 등은 토큰을 붙이지 않음
    const noAuthPaths = ['/api/v1/auth/signup', '/api/v1/auth/login'];
    if (
      typeof config.url === 'string' &&
      noAuthPaths.some(path => config.url?.includes(path))
    ) {
      return config;
    }

    if (isBrowser) {
      const token = localStorage.getItem('accessToken');

      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  }
);

// 응답 인터셉터 (토큰 만료 시 처리 및 새로운 토큰 저장)
apiClient.instance.interceptors.response.use(
  (response: AxiosResponse) => {
    // 응답 로깅
    console.log('✅ API 응답:', {
      status: response.status,
      url: response.config.url,
      headers: response.headers,
    });

    // 응답에서 새로운 Authorization 헤더가 있으면 저장
    const accessToken = response.headers['authorization']?.replace(
      'Bearer ',
      ''
    );
    if (accessToken && isBrowser) {
      localStorage.setItem('accessToken', accessToken);
    }

    return response;
  },
  async (error: AxiosError) => {
    // 에러 로깅
    console.log('❌ API 에러:', {
      status: error.response?.status,
      url: error.config?.url ?? '',
      baseURL: error.config?.baseURL ?? '',
      fullURL: (error.config?.baseURL || '') + (error.config?.url || ''),
      message: error.message,
      response: error.response?.data,
    });

    // 회원가입, 로그인 등은 토큰 갱신을 시도하지 않음
    const noAuthPaths = ['/api/v1/auth/signup', '/api/v1/auth/login'];
    const isAuthPath =
      typeof error.config?.url === 'string' &&
      noAuthPaths.some(path => error.config?.url?.includes(path));

    if (
      error.response?.status === 401 &&
      isBrowser &&
      typeof window !== 'undefined' &&
      window.location.pathname !== '/login' &&
      !isAuthPath // 인증 관련 API가 아닌 경우에만 토큰 갱신 시도
    ) {
      console.log('🔄 401 에러 발생, 토큰 갱신 시도');
      // 1. refreshToken으로 재발급 시도
      try {
        console.log('🔄 reissue API 호출');
        await apiClient.api.reissue();
        console.log('✅ 토큰 갱신 성공');
        // 새 accessToken이 저장됨 (응답 인터셉터에서)
        // 원래 요청을 재시도
        if (error.config) {
          console.log('🔄 원래 요청 재시도');
          return apiClient.instance.request(error.config);
        }
      } catch (refreshError) {
        console.error('❌ 토큰 갱신 실패:', refreshError);
        // 2. 재발급도 실패하면 로그아웃
        localStorage.removeItem('accessToken');
        console.log('🚪 로그인 페이지로 리다이렉트');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);
