import { Api } from './api';
import type {
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosError,
} from 'axios';

// 브라우저 환경에서만 localStorage 사용
const isBrowser = typeof window !== 'undefined';

// API 클라이언트 인스턴스 생성
const apiClientInstance = new Api({
  baseURL: '', // 명시적으로 빈 문자열로 설정
  withCredentials: true, // 쿠키 기반 인증을 위해 필요
});

// baseURL을 강제로 빈 문자열로 설정
apiClientInstance.instance.defaults.baseURL = '';

export const apiClient = apiClientInstance;

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
let isRefreshing = false;
let refreshPromise: Promise<void> | null = null;
let requestQueue: Array<() => void> = [];

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
      const originalRequest = error.config;

      if (!isRefreshing) {
        isRefreshing = true;
        refreshPromise = apiClient.api
          .reissue()
          .then(() => {
            // accessToken이 localStorage에 저장될 때까지 기다림 (최대 1초)
            return new Promise<void>(resolve => {
              const start = Date.now();
              const check = () => {
                const token = localStorage.getItem('accessToken');
                if (token) {
                  resolve();
                } else if (Date.now() - start > 1000) {
                  // 1초가 지나도 저장 안되면 그냥 resolve (무한루프 방지)
                  resolve();
                } else {
                  setTimeout(check, 10);
                }
              };
              check();
            });
          })
          .then(() => {
            isRefreshing = false;
            refreshPromise = null;
            // 대기 중이던 요청들 재시도
            requestQueue.forEach(cb => cb());
            requestQueue = [];
          })
          .catch(refreshError => {
            console.error('❌ 토큰 갱신 실패:', refreshError);
            isRefreshing = false;
            refreshPromise = null;
            requestQueue = [];
            localStorage.removeItem('accessToken');
            console.log('🚪 로그인 페이지로 리다이렉트');
            window.location.href = '/login';
            throw refreshError;
          });
      }

      // reissue가 끝날 때까지 대기 후 원래 요청 재시도
      return new Promise((resolve, reject) => {
        if (!originalRequest) {
          reject(error);
          return;
        }
        requestQueue.push(() => {
          // 토큰 갱신 후 Authorization 헤더를 다시 세팅
          if (isBrowser) {
            const token = localStorage.getItem('accessToken');
            if (token && originalRequest.headers) {
              originalRequest.headers['Authorization'] = `Bearer ${token}`;
            }
          }
          apiClient.instance
            .request(originalRequest)
            .then(resolve)
            .catch(reject);
        });
      });
    }
    return Promise.reject(error);
  }
);
