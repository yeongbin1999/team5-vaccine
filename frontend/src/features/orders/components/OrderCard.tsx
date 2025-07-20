import { statusColor } from '../statusColor';
import { Card } from '@/components/ui/card';
import { Coffee } from 'lucide-react';
import type { OrderListDTO } from '@/lib/backend/apiV1/api';

export function OrderCard({ order }: { order: OrderListDTO }) {
  // 날짜 포맷팅
  const formatDate = (dateString?: string) => {
    if (!dateString) return '날짜 없음';
    return new Date(dateString).toLocaleDateString('ko-KR');
  };

  return (
    <Card className="p-6 bg-white/80 border-amber-100 shadow-lg rounded-2xl h-full flex flex-col justify-between">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-2 mb-3">
        <div className="text-brown-900 font-semibold">
          주문번호: {order.orderId}
        </div>
        <div className="text-brown-700 text-sm">
          {formatDate(order.orderDate)}
        </div>
        <span
          className={`px-3 py-1 rounded-full text-xs font-bold ${statusColor[order.status || ''] || 'bg-gray-100 text-gray-500'}`}
        >
          {order.status || '상태 없음'}
        </span>
      </div>

      {/* 주문 정보 요약 */}
      <div className="text-brown-800 text-base mb-3">
        <div className="flex items-center gap-2 mb-2">
          <Coffee className="w-4 h-4 text-amber-400" />
          <span className="text-sm text-brown-600">
            주문자: {order.username || '알 수 없음'}
          </span>
        </div>
      </div>

      <div className="text-right text-brown-900 font-bold text-lg">
        총액: {(order.totalPrice || 0).toLocaleString()}원
      </div>
    </Card>
  );
}
