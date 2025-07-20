'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

interface AuthGuardProps {
  children: React.ReactNode;
}

export function AuthGuard({ children }: AuthGuardProps) {
  const [isClient, setIsClient] = useState(false);
  const [hasToken, setHasToken] = useState(false);
  const router = useRouter();

  useEffect(() => {
    // 클라이언트 사이드에서만 실행
    setIsClient(true);

    const token = localStorage.getItem('accessToken');
    if (token) {
      setHasToken(true);
      // 토큰이 있으면 메인 페이지로 리다이렉트
      router.push('/');
    }
  }, [router]);

  // 서버 사이드 렌더링 중이거나 토큰이 있는 경우 로딩 화면 표시
  if (!isClient || hasToken) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600">
            {!isClient ? '확인하는 중...' : '이미 로그인되어 있습니다...'}
          </p>
        </div>
      </div>
    );
  }

  // 클라이언트 사이드에서 토큰이 없으면 원래 페이지 렌더링
  return <>{children}</>;
}
