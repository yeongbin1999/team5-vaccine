'use client';

import Image from 'next/image';
import { OrderCard } from './components/OrderCard';
import { useQuery } from '@tanstack/react-query';
import { getMyOrders } from './api';
import { useAuthStore } from '@/features/auth/authStore';
// import { toast } from 'sonner';

export default function OrdersPage() {
  const user = useAuthStore(state => state.user);
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const isAuthChecked = useAuthStore(state => state.isAuthChecked);

  const {
    data: orders,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['orders', user?.id],
    queryFn: getMyOrders,
    enabled: !!user?.id && isAuthenticated,
    staleTime: 1000 * 60 * 5, // 5분
  });

  // 인증 상태 확인이 아직 완료되지 않은 경우 (새로고침 시)
  if (!isAuthChecked) {
    return (
      <div className="w-full py-12 px-4">
        <div className="flex flex-col items-center mb-8">
          <Image
            src="/coffee.jpeg"
            alt="커피"
            width={80}
            height={80}
            className="rounded-full shadow-lg mb-2"
          />
          <h1 className="text-3xl font-bold text-brown-900 mb-2">주문 내역</h1>
          <p className="text-brown-500 text-sm">
            그동안 주문하신 내역을 한눈에 확인하세요 ☕️
          </p>
        </div>
        <div className="flex items-center justify-center min-h-[40vh]">
          <div className="text-center">
            <div className="inline-flex items-center space-x-3 text-gray-600">
              <div className="w-6 h-6 border-2 border-amber-600 border-t-transparent rounded-full animate-spin"></div>
              <span className="text-xl font-medium">
                주문 내역을 불러오는 중...
              </span>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // 로그인하지 않은 경우
  if (!isAuthenticated) {
    return (
      <div className="w-full py-12 px-4">
        <div className="flex flex-col items-center mb-8">
          <Image
            src="/coffee.jpeg"
            alt="커피"
            width={80}
            height={80}
            className="rounded-full shadow-lg mb-2"
          />
          <h1 className="text-3xl font-bold text-brown-900 mb-2">주문 내역</h1>
          <p className="text-brown-500 text-sm">
            그동안 주문하신 내역을 한눈에 확인하세요 ☕️
          </p>
        </div>
        <div className="flex items-center justify-center min-h-[40vh]">
          <div className="text-center">
            <div className="inline-flex items-center space-x-3 text-gray-600">
              <div className="w-8 h-8 text-amber-600">🔒</div>
              <span className="text-xl font-medium">로그인이 필요합니다</span>
            </div>
            <p className="text-gray-500 mt-2">
              주문 내역을 확인하려면 로그인해주세요.
            </p>
          </div>
        </div>
      </div>
    );
  }

  // 로그인한 상태에서만 렌더링
  return (
    <div className="w-full py-12 px-4">
      <div className="flex flex-col items-center mb-8">
        <Image
          src="/coffee.jpeg"
          alt="커피"
          width={80}
          height={80}
          className="rounded-full shadow-lg mb-2"
        />
        <h1 className="text-3xl font-bold text-brown-900 mb-2">주문 내역</h1>
        <p className="text-brown-500 text-sm">
          그동안 주문하신 내역을 한눈에 확인하세요 ☕️
        </p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {isLoading ? (
          <div className="col-span-full">
            <div className="flex items-center justify-center min-h-[40vh]">
              <div className="text-center">
                <div className="inline-flex items-center space-x-3 text-gray-600">
                  <div className="w-6 h-6 border-2 border-amber-600 border-t-transparent rounded-full animate-spin"></div>
                  <span className="text-xl font-medium">
                    주문 내역을 불러오는 중...
                  </span>
                </div>
              </div>
            </div>
          </div>
        ) : error ? (
          <div className="text-center text-brown-400 py-12 col-span-full">
            주문 내역을 불러오는데 실패했습니다.
          </div>
        ) : !orders || orders.length === 0 ? (
          <div className="text-center text-brown-400 py-12 col-span-full">
            주문 내역이 없습니다.
          </div>
        ) : (
          orders.map(order => (
            <OrderCard key={order.orderId ?? 0} order={order} />
          ))
        )}
      </div>
    </div>
  );
}
