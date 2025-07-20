import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // 공개 경로 (권한 검증 불필요)
  const publicPaths = ['/', '/login', '/signup', '/shop', '/product'];
  const isPublicPath = publicPaths.some(path => pathname.startsWith(path));

  // 보호된 경로 (로그인 필요)
  const protectedPaths = ['/profile', '/cart', '/orders'];
  const isProtectedPath = protectedPaths.some(path =>
    pathname.startsWith(path)
  );

  // 관리자 경로 (관리자 권한 필요)
  const adminPaths = ['/admin'];
  const isAdminPath = adminPaths.some(path => pathname.startsWith(path));

  // 클라이언트 사이드에서 인증 상태를 확인하므로 middleware에서는 리다이렉트하지 않음
  // RouteGuard 컴포넌트에서 처리

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
};
