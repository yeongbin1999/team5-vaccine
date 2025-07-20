'use client';

import { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { Eye, EyeOff, ArrowLeft } from 'lucide-react';
import { useAuthStore } from '@/features/auth/authStore';

export function SignupForm() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    name: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const { register } = useAuthStore();
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    // 프론트엔드 검증
    if (formData.password.length < 8) {
      setError('비밀번호는 8자 이상이어야 합니다.');
      setIsLoading(false);
      return;
    }

    if (formData.password.length > 20) {
      setError('비밀번호는 20자 이하여야 합니다.');
      setIsLoading(false);
      return;
    }

    try {
      await register(formData.email, formData.password, formData.name);
      // 회원가입 성공 시 모달 표시
      setShowSuccessModal(true);
      setIsLoading(false);
    } catch (error: unknown) {
      // 사용자에게는 친화적인 메시지만 표시
      if (error instanceof Error) {
        const errorMessage = error.message;

        // 서버에서 반환한 메시지가 있으면 사용
        if (errorMessage.includes('이메일') || errorMessage.includes('중복')) {
          setError(errorMessage);
        } else if (errorMessage.includes('비밀번호')) {
          setError(errorMessage);
        } else if (
          errorMessage.includes('네트워크') ||
          errorMessage.includes('연결')
        ) {
          setError('네트워크 연결을 확인해주세요.');
        } else if (errorMessage.includes('서버')) {
          setError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        } else {
          setError('회원가입에 실패했습니다. 입력 정보를 확인해주세요.');
        }
      } else {
        setError('회원가입에 실패했습니다. 입력 정보를 확인해주세요.');
      }
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
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
            window.location.href = '/login';
          }
        }}
      >
        <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
          <div className="text-center">
            <div className="text-4xl mb-4">🎉</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              회원가입 성공!
            </h3>
            <p className="text-gray-600 mb-6">
              회원가입이 완료되었습니다.
              <br />
              로그인 페이지로 이동합니다.
            </p>
            <button
              ref={buttonRef}
              autoFocus
              onClick={() => {
                setShowSuccessModal(false);
                window.location.href = '/login';
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
    <div className="w-full">
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

        {/* 이름 */}
        <div>
          <label
            htmlFor="name"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            이름 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
            placeholder="홍길동"
          />
        </div>

        {/* 에러 메시지 */}
        {renderError()}

        {/* 회원가입 버튼 */}
        <button
          type="submit"
          disabled={isLoading}
          className="w-full bg-gray-900 text-white py-3 rounded-md hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-900 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isLoading ? '회원가입 중...' : '회원가입'}
        </button>

        {/* 로그인 링크 */}
        <div className="text-center">
          {' '}
          <Link
            href="/login"
            className="text-sm text-gray-600 hover:text-gray-900"
          >
            이미 계정이 있으신가요? 로그인하기
          </Link>
        </div>

        {/* 홈으로 돌아가기 */}
        <div className="text-center">
          {' '}
          <Link
            href="/"
            className="inline-flex items-center text-sm text-gray-600 hover:text-gray-900"
          >
            <ArrowLeft className="w-4 h-4 mr-1" />
            홈으로 돌아가기
          </Link>
        </div>
      </form>

      {/* 성공 모달 */}
      {renderSuccessModal()}
    </div>
  );
}
