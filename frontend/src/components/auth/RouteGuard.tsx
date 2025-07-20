'use client';

import { useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/features/auth/authStore';

interface RouteGuardProps {
  children: React.ReactNode;
  requiredRole?: 'USER' | 'ADMIN';
}

export function RouteGuard({ children, requiredRole }: RouteGuardProps) {
  const { user, isAuthenticated, isAuthChecked } = useAuthStore();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    // 인증 상태 확인이 완료되지 않았으면 대기
    if (!isAuthChecked) return;

    if (!isAuthenticated) {
      const isAdminRoute = pathname.startsWith('/admin');
      if (isAdminRoute) {
        // /admin에서는 리다이렉트하지 않음 (SPA에서 로그인 폼 렌더)
        return;
      }
      // 그 외는 기존대로 로그인 페이지로 리다이렉트
      const loginPath = isAdminRoute ? '/admin/login' : '/login';
      router.push(`${loginPath}?redirect=${pathname}`);
      return;
    }

    if (requiredRole && user?.role !== requiredRole) {
      router.push('/access-denied');
      return;
    }
  }, [
    isAuthenticated,
    isAuthChecked,
    user?.role,
    requiredRole,
    router,
    pathname,
  ]);

  // 로딩 상태 (인증 상태 확인 중)
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

  // 로그인 필요 (인증 상태 확인 완료 후)
  if (!isAuthenticated) {
    const isAdminRoute = pathname.startsWith('/admin');
    if (isAdminRoute) {
      // /admin에서는 리다이렉트하지 않고 children만 렌더
      return <>{children}</>;
    }
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600">로그인이 필요합니다...</p>
        </div>
      </div>
    );
  }

  // 권한 부족
  if (requiredRole && user?.role !== requiredRole) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600">권한을 확인하는 중...</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
