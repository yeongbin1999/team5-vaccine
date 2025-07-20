'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { Eye, EyeOff } from 'lucide-react';
import { useAuthStore } from '@/features/auth/authStore';
import { useCartHydrated } from '@/features/cart/cartStore';

// JWT 디코드 함수 (jwt-decode 없이)
function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(function (c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        })
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

export function LoginForm() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const router = useRouter();
  const searchParams = useSearchParams();
  const { login } = useAuthStore();
  const hasHydrated = useCartHydrated();
  const modalRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (showSuccessModal && modalRef.current) {
      modalRef.current.focus();
    }
  }, [showSuccessModal]);

  useEffect(() => {
    if (showSuccessModal && buttonRef.current) {
      buttonRef.current.focus();
    }
  }, [showSuccessModal]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsLoading(true);
    setError('');

    // hydration이 끝날 때까지 대기
    if (!hasHydrated) {
      await new Promise(resolve => {
        const interval = setInterval(() => {
          if (hasHydrated) {
            clearInterval(interval);
            resolve(null);
          }
        }, 10);
      });
    }

    try {
      // 1. 로그인 API 직접 호출 (useAuthStore.login 대신)
      const loginData = { email: formData.email, password: formData.password };
      const clientModule = await import('@/lib/backend/apiV1/client');
      const response = await clientModule.apiClient.api.login(loginData);
      const authHeader =
        response.headers['authorization'] ||
        response.headers['Authorization'] ||
        response.headers['AUTHORIZATION'];
      if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.substring(7);
        // JWT 디코드해서 role 확인
        const payload = decodeJwtPayload(token);
        const role =
          payload && typeof payload === 'object' && 'role' in payload
            ? (payload as { role?: string }).role
            : undefined;
        if (role === 'ADMIN') {
          setError(
            '관리자 계정으로는 일반 사용자 페이지에서 로그인할 수 없습니다.'
          );
          localStorage.removeItem('accessToken');
          setIsLoading(false);
          return;
        }
        // USER면 토큰 저장 후 기존 로직 진행
        localStorage.setItem('accessToken', token);
      } else {
        setError('로그인 토큰을 확인할 수 없습니다.');
        setIsLoading(false);
        return;
      }
      // 2. 이후 기존 로그인 후처리 (장바구니 동기화 등)
      await useAuthStore.getState().checkAuth();
      setShowSuccessModal(true);
      setIsLoading(false);
      return;
    } catch (error: unknown) {
      // 사용자에게는 친화적인 메시지만 표시
      if (error instanceof Error) {
        const errorMessage = error.message;

        // 서버에서 반환한 메시지가 있으면 사용
        if (
          errorMessage.includes('이메일') ||
          errorMessage.includes('비밀번호')
        ) {
          setError(errorMessage);
        } else if (
          errorMessage.includes('네트워크') ||
          errorMessage.includes('연결')
        ) {
          setError('네트워크 연결을 확인해주세요.');
        } else if (errorMessage.includes('서버')) {
          setError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        } else {
          setError('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
        }
      } else {
        setError('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
      }
      setIsLoading(false);
    }
  };

  const renderError = () => {
    if (!error) return null;

    return (
      <div className="text-red-500 text-sm text-center bg-red-50 p-3 rounded-md border border-red-200">
        {error}
      </div>
    );
  };

  const renderSuccessModal = () => {
    if (!showSuccessModal) return null;

    return (
      <div
        ref={modalRef}
        tabIndex={-1}
        autoFocus
        className="fixed inset-0 flex items-center justify-center z-50"
        style={{ background: 'rgba(0,0,0,0.24)' }}
        onKeyDown={e => {
          if (e.key === 'Enter') {
            setShowSuccessModal(false);
            const redirect = searchParams.get('redirect');
            if (redirect) {
              router.push(redirect);
            } else {
              router.push('/');
            }
          }
        }}
      >
        <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
          <div className="text-center">
            <div className="text-4xl mb-4">🎉</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              로그인 성공!
            </h3>
            <p className="text-gray-600 mb-6">로그인이 완료되었습니다.</p>
            <button
              ref={buttonRef}
              autoFocus
              onClick={() => {
                setShowSuccessModal(false);
                const redirect = searchParams.get('redirect');
                if (redirect) {
                  router.push(redirect);
                } else {
                  router.push('/');
                }
              }}
              className="w-full bg-gray-900 text-white py-2 px-4 rounded-md hover:bg-gray-800 transition-colors"
            >
              확인
            </button>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="w-full max-w-md p-8 bg-white rounded-lg shadow-md">
      <h2 className="text-2xl font-bold mb-6 text-center">로그인</h2>
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 이메일 */}
        <div>
          <label
            htmlFor="email"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            이메일 <span className="text-red-500">*</span>
          </label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
            placeholder="example@email.com"
          />
        </div>
        {/* 비밀번호 */}
        <div>
          <label
            htmlFor="password"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            비밀번호 <span className="text-red-500">*</span>
          </label>
          <div className="relative">
            <input
              type={showPassword ? 'text' : 'password'}
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 pr-10 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
              placeholder="••••••••"
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showPassword ? (
                <EyeOff className="w-5 h-5" />
              ) : (
                <Eye className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>

        {/* 에러 메시지 */}
        {renderError()}

        {/* 로그인 버튼 */}
        <button
          type="submit"
          disabled={isLoading}
          className="w-full bg-gray-900 text-white py-3 rounded-md hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-900 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isLoading ? '로그인 중...' : '로그인'}
        </button>
        {/* 회원가입 링크 */}
        <div className="text-center">
          <Link
            href="/signup"
            className="text-sm text-gray-600 hover:text-gray-900"
          >
            아직 회원이 아니신가요? 회원가입
          </Link>
        </div>
      </form>

      {/* 성공 모달 */}
      {renderSuccessModal()}
    </div>
  );
}
