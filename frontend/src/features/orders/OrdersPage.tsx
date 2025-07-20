'use client';

import Image from 'next/image';
import { OrderCard } from './components/OrderCard';
import { useQuery } from '@tanstack/react-query';
import { getMyOrders } from './api';
import { useAuthStore } from '@/features/auth/authStore';
import { useState, useEffect } from 'react';
import { getOrderDetail } from './api';
import { useSearchParams } from 'next/navigation';
// import { toast } from 'sonner';

function OrderDetailModal({
  orderId,
  onClose,
}: {
  orderId: number;
  onClose: () => void;
}) {
  const {
    data: detail,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['orderDetail', orderId],
    queryFn: () => getOrderDetail(orderId),
    enabled: !!orderId,
  });

  if (!orderId) return null;
  return (
    <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center">
      <div className="bg-white rounded-xl shadow-lg p-8 max-w-lg w-full relative">
        <button
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-700"
          onClick={onClose}
        >
          &times;
        </button>
        {isLoading ? (
          <div className="text-center py-12">상세 정보를 불러오는 중...</div>
        ) : error ? (
          <div className="text-center py-12 text-red-400">
            상세 정보를 불러오지 못했습니다.
          </div>
        ) : detail ? (
          <div>
            <h2 className="text-2xl font-bold mb-2">주문 상세</h2>
            <div className="mb-2 text-gray-700">주문번호: {detail.orderId}</div>
            <div className="mb-2 text-gray-700">
              주문일시:{' '}
              {detail.orderDate
                ? new Date(detail.orderDate).toLocaleString('ko-KR')
                : '-'}
            </div>
            <div className="mb-2 text-gray-700">상태: {detail.status}</div>
            <div className="mb-2 text-gray-700">
              총액: {detail.totalPrice?.toLocaleString()}원
            </div>
            <div className="mb-2 text-gray-700">배송지: {detail.address}</div>
            <div className="mb-2 text-gray-700">주문자: {detail.username}</div>
            <div className="mt-4">
              <h3 className="font-semibold mb-2">주문 상품</h3>
              <ul className="divide-y">
                {detail.items?.map((item, idx) => (
                  <li key={idx} className="py-2 flex justify-between">
                    <span>{item.productName}</span>
                    <span>
                      {item.quantity}개 × {item.unitPrice?.toLocaleString()}원
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}

export default function OrdersPage() {
  const user = useAuthStore(state => state.user);
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const isAuthChecked = useAuthStore(state => state.isAuthChecked);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const searchParams = useSearchParams();

  // 주문 ID 쿼리 파라미터가 있으면 자동으로 상세 모달 오픈
  useEffect(() => {
    const orderIdParam = searchParams.get('orderId');
    if (orderIdParam) {
      setSelectedOrderId(Number(orderIdParam));
    }
  }, [searchParams]);

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
          orders
            .slice()
            .sort(
              (a, b) =>
                new Date(b.orderDate || 0).getTime() -
                new Date(a.orderDate || 0).getTime()
            )
            .map(order => (
              <OrderCard
                key={order.orderId ?? 0}
                order={order}
                onDetailClick={() => setSelectedOrderId(order.orderId ?? 0)}
              />
            ))
        )}
      </div>
      {selectedOrderId && (
        <OrderDetailModal
          orderId={selectedOrderId}
          onClose={() => setSelectedOrderId(null)}
        />
      )}
    </div>
  );
}
