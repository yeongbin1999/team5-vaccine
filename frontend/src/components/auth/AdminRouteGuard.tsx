"use client";
import { useAdminAuthStore } from '@/features/auth/adminAuthStore';
import { useEffect } from 'react';

export function AdminRouteGuard({ children }: { children: React.ReactNode }) {
  const isAuthChecked = useAdminAuthStore(state => state.isAuthChecked);
  const checkAuth = useAdminAuthStore(state => state.checkAuth);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  if (!isAuthChecked) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600">권한을 확인하는 중...</p>
        </div>
      </div>
    );
  }

  // 인증 체크가 끝났으면 children(로그인 폼 또는 관리자 페이지)을 그대로 렌더
  return <>{children}</>;
} 