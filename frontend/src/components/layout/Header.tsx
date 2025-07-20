'use client';

import Link from 'next/link';
import Image from 'next/image';
import {
  ShoppingCart,
  Home as HomeIcon,
  Store,
  ClipboardList,
} from 'lucide-react';
import { useCartStore } from '@/features/cart/cartStore';
import { ProfileDropdown } from './ProfileDropdown';
import React, { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchCart } from '@/features/cart/api';
import { useAuthStore } from '@/features/auth/authStore';

function useCartQuery() {
  const user = useAuthStore(state => state.user);
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const items = useCartStore(state => state.items);
  const query = useQuery({
    queryKey: ['cart', user?.id],
    queryFn: fetchCart,
    enabled: !!user?.id && isAuthenticated,
    staleTime: 1000 * 60,
  });
  // 로그인: 서버 데이터, 비로그인: zustand
  return isAuthenticated && query.data ? query.data : items;
}

export function Header() {
  const items = useCartQuery();
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);
  const totalCount = items.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <header className="bg-white shadow-sm border-b border-gray-200 fixed top-0 left-0 right-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* 로고 */}
          <Link href="/" className="flex items-center space-x-2">
            <Image
              src="/coffee.jpeg"
              alt="로고"
              width={40}
              height={40}
              className="object-contain rounded-lg"
            />
            <span className="text-xl font-bold text-gray-900">
              Grids & Circles CAFE
            </span>
          </Link>

          {/* 네비게이션 아이콘 통합 */}
          <nav className="flex items-center space-x-2 ml-auto">
            <Link
              href="/"
              className="p-2 text-gray-600 hover:text-gray-900 transition-colors rounded-full"
              aria-label="Home"
            >
              <HomeIcon className="w-5 h-5" />
            </Link>
            <Link
              href="/shop"
              className="p-2 text-gray-600 hover:text-gray-900 transition-colors rounded-full"
              aria-label="Shop"
            >
              <Store className="w-5 h-5" />
            </Link>
            <Link
              href="/orders"
              className="p-2 text-gray-600 hover:text-gray-900 transition-colors rounded-full"
              aria-label="Orders"
            >
              <ClipboardList className="w-5 h-5" />
            </Link>
            <Link
              href="/cart"
              className="p-2 text-gray-600 hover:text-gray-900 transition-colors rounded-full relative"
              aria-label="Cart"
            >
              <ShoppingCart className="w-5 h-5" />
              <span
                className={`absolute -top-1 -right-1 text-white text-xs rounded-full flex items-center justify-center min-w-[1.25rem] h-5 px-1 font-semibold ${
                  !mounted
                    ? 'bg-gray-400'
                    : totalCount > 0
                      ? 'bg-red-500'
                      : 'bg-gray-400'
                }`}
              >
                {!mounted ? 0 : totalCount}
              </span>
            </Link>
            {/* 프로필 드롭다운 */}
            <ProfileDropdown />
          </nav>
        </div>
      </div>
    </header>
  );
}
