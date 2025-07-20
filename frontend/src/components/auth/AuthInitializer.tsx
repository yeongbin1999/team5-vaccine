'use client';

import { useEffect } from 'react';
import { usePathname } from 'next/navigation';
import { useAuthStore } from '@/features/auth/authStore';

export function AuthInitializer() {
  const pathname = usePathname();

  useEffect(() => {
    // 회원가입/로그인 페이지에서는 인증 상태 확인을 하지 않음
    if (pathname === '/signup' || pathname === '/login') {
      return;
    }

    // 앱 시작 시 자동으로 인증 상태 확인
    useAuthStore.getState().checkAuth();
  }, [pathname]);

  // 이 컴포넌트는 UI를 렌더링하지 않음
  return null;
}
