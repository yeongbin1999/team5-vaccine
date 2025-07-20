'use client';
import { useState, useEffect, useMemo } from 'react';
import { DataTable } from '@/components/ui/data-table';
import type { ColumnDef } from '@tanstack/react-table';
import { apiClient } from '@/lib/backend/apiV1/client';
import type { OrderListDTO, OrderDetailDTO } from '@/lib/backend/apiV1/api';
import { statusColor } from '@/features/orders/statusColor';
import { Button } from '@/components/ui/button';

// Status cell component to avoid React hooks rules violation
function StatusCell({
  order,
  onStatusChange,
}: {
  order: OrderListDTO;
  onStatusChange: () => void;
}) {
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = async (e: React.ChangeEvent<HTMLSelectElement>) => {
    if (!order.orderId) return;
    setUpdating(true);
    setError(null);
    try {
      await apiClient.api.updateOrderStatus(order.orderId, {
        orderId: order.orderId,
        newStatus: e.target.value as OrderDetailDTO['status'],
      });
      onStatusChange();
    } catch {
      setError('상태 변경 실패');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="flex flex-col items-center">
      <select
        className={`px-2 py-1 rounded text-xs font-semibold ${statusColor[order.status || ''] || 'bg-gray-100 text-gray-500'}`}
        value={order.status}
        onChange={handleChange}
        disabled={updating}
      >
        <option value="배송준비중">배송준비중</option>
        <option value="배송중">배송중</option>
        <option value="배송완료">배송완료</option>
        <option value="취소">취소</option>
      </select>
      {error && <span className="text-xs text-red-500 mt-1">{error}</span>}
    </div>
  );
}

const STATUS_OPTIONS = [
  { value: '', label: '전체' },
  { value: '배송준비중', label: '배송준비중' },
  { value: '배송중', label: '배송중' },
  { value: '배송완료', label: '배송완료' },
  { value: '취소', label: '취소' },
];
const PAGE_SIZE = 7;

export default function OrderManagement() {
  const [orders, setOrders] = useState<OrderListDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<OrderDetailDTO | null>(
    null
  );
  const [modalLoading, setModalLoading] = useState(false);
  const [statusUpdateLoading, setStatusUpdateLoading] = useState(false);
  const [statusUpdateError, setStatusUpdateError] = useState<string | null>(
    null
  );
  const [form, setForm] = useState<{
    address: string;
    status: OrderDetailDTO['status'];
  }>({ address: '', status: undefined });

  // Fetch all orders (admin API)
  useEffect(() => {
    setLoading(true);
    setError(null);
    apiClient.api
      .getAllOrders()
      .then(res => setOrders(res.data))
      .catch(() => setError('주문 데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  // Filtered and paginated orders
  const filtered = useMemo(
    () =>
      orders
        .filter(
          o =>
            !search ||
            (o.username || '').toLowerCase().includes(search.toLowerCase())
        )
        .filter(o => !status || o.status === status),
    [orders, search, status]
  );
  const total = filtered.length;
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const paged = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  // Refresh orders function
  const refreshOrders = async () => {
    const res = await apiClient.api.getAllOrders();
    setOrders(res.data);
  };

  // Table columns
  const columns = useMemo<ColumnDef<OrderListDTO, unknown>[]>(
    () => [
      {
        accessorKey: 'orderId',
        header: '주문번호',
        cell: ({ row }) => row.original.orderId,
      },
      {
        accessorKey: 'username',
        header: '주문자',
        cell: ({ row }) => row.original.username,
      },
      {
        accessorKey: 'orderDate',
        header: '주문일시',
        cell: ({ row }) =>
          row.original.orderDate
            ? new Date(row.original.orderDate).toLocaleString('ko-KR')
            : '-',
      },
      {
        accessorKey: 'totalPrice',
        header: '총액',
        cell: ({ row }) =>
          (row.original.totalPrice || 0).toLocaleString() + '원',
      },
      {
        accessorKey: 'status',
        header: '상태',
        cell: ({ row }) => (
          <StatusCell order={row.original} onStatusChange={refreshOrders} />
        ),
      },
      {
        id: 'actions',
        header: '관리',
        cell: ({ row }) => (
          <Button
            size="sm"
            onClick={() => openDetailModal(row.original.orderId)}
          >
            상세
          </Button>
        ),
      },
    ],
    [refreshOrders]
  );

  // Open detail modal
  const openDetailModal = async (orderId?: number) => {
    if (!orderId) return;
    setModalLoading(true);
    setModalOpen(true);
    setStatusUpdateError(null);
    try {
      // 관리자용 상세조회 API 직접 호출
      orderId = 1;
      const res = await apiClient.request({
        path: `/api/v1/orders/${orderId}`,
        method: 'GET',
        format: 'json',
      });
      const data = res.data;
      setSelectedOrder(data);
      setForm({
        address: data.address || '',
        status: data.status,
      });
    } catch {
      setSelectedOrder(null);
      setStatusUpdateError('상세 정보를 불러오지 못했습니다.');
    } finally {
      setModalLoading(false);
    }
  };
  const closeModal = () => {
    setModalOpen(false);
    setSelectedOrder(null);
    setStatusUpdateError(null);
  };

  // Update order status
  const handleOrderUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedOrder?.orderId) return;
    setStatusUpdateLoading(true);
    setStatusUpdateError(null);
    try {
      await apiClient.api.updateOrderStatus(selectedOrder.orderId, {
        orderId: selectedOrder.orderId,
        newStatus: form.status,
      });
      // Refresh orders
      const res = await apiClient.api.getAllOrders();
      setOrders(res.data);
      closeModal();
    } catch {
      setStatusUpdateError('주문 정보 변경에 실패했습니다.');
    } finally {
      setStatusUpdateLoading(false);
    }
  };

  return (
    <div className="w-full">
      <div className="flex flex-col md:flex-row md:items-end gap-2 mb-4">
        <div className="flex gap-2 flex-1">
          <input
            className="border rounded px-3 py-2 text-gray-900 font-semibold w-40"
            placeholder="주문자 검색"
            value={search}
            onChange={e => {
              setSearch(e.target.value);
              setPage(1);
            }}
          />
          <select
            className="border rounded px-3 py-2 text-gray-900 font-semibold"
            value={status}
            onChange={e => {
              setStatus(e.target.value);
              setPage(1);
            }}
          >
            {STATUS_OPTIONS.map(opt => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
      </div>
      {loading ? (
        <div className="text-center py-16 text-gray-500">
          주문 목록을 불러오는 중...
        </div>
      ) : error ? (
        <div className="text-center py-16 text-red-500">{error}</div>
      ) : total === 0 ? (
        <div className="text-center py-16 text-gray-500">
          등록된 주문이 없습니다.
        </div>
      ) : (
        <DataTable columns={columns} data={paged} />
      )}
      {/* 페이지네이션 */}
      {total > 0 && (
        <div className="flex justify-center items-center gap-2 mt-6">
          <Button
            onClick={() => setPage(p => Math.max(1, p - 1))}
            disabled={page === 1}
          >
            이전
          </Button>
          <span className="text-gray-700 font-semibold">
            {page} / {totalPages}
          </span>
          <Button
            onClick={() => setPage(p => Math.min(totalPages, p + 1))}
            disabled={page === totalPages}
          >
            다음
          </Button>
        </div>
      )}
      {/* 상세/수정 모달 */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6 relative">
            <button
              className="absolute top-4 right-4 text-gray-400 hover:text-gray-700"
              onClick={closeModal}
            >
              &times;
            </button>
            {modalLoading ? (
              <div className="text-center py-12">
                상세 정보를 불러오는 중...
              </div>
            ) : selectedOrder ? (
              <form
                onSubmit={handleOrderUpdate}
                className="flex flex-col gap-3"
              >
                <h2 className="text-lg font-bold mb-2">주문 상세/수정</h2>
                <div className="text-gray-700">
                  주문번호: {selectedOrder.orderId}
                </div>
                <div className="text-gray-700">
                  주문자: {selectedOrder.username}
                </div>
                <div className="text-gray-700">
                  주문일시:{' '}
                  {selectedOrder.orderDate
                    ? new Date(selectedOrder.orderDate).toLocaleString('ko-KR')
                    : '-'}
                </div>
                <div className="text-gray-700">
                  총액: {selectedOrder.totalPrice?.toLocaleString()}원
                </div>
                <div className="text-gray-700">
                  배송지:
                  <span className="ml-2">{form.address}</span>
                </div>
                <div className="text-gray-700">
                  상태:
                  <select
                    className="ml-2 border rounded px-2 py-1"
                    value={form.status ?? ''}
                    onChange={e =>
                      setForm(f => ({
                        ...f,
                        status: e.target.value as OrderDetailDTO['status'],
                      }))
                    }
                    required
                  >
                    <option value="" disabled>
                      상태 선택
                    </option>
                    <option value="배송준비중">배송준비중</option>
                    <option value="배송중">배송중</option>
                    <option value="배송완료">배송완료</option>
                    <option value="취소">취소</option>
                  </select>
                </div>
                <div className="mt-2">
                  <h3 className="font-semibold mb-1">주문 상품</h3>
                  <ul className="divide-y">
                    {selectedOrder.items?.map((item, idx) => (
                      <li key={idx} className="py-1 flex justify-between">
                        <span>{item.productName}</span>
                        <span>
                          {item.quantity}개 × {item.unitPrice?.toLocaleString()}
                          원
                        </span>
                      </li>
                    ))}
                  </ul>
                </div>
                {statusUpdateError && (
                  <div className="text-red-500 text-sm">
                    {statusUpdateError}
                  </div>
                )}
                <div className="flex gap-2 mt-2 justify-end">
                  <Button type="submit" disabled={statusUpdateLoading}>
                    저장
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={closeModal}
                    disabled={statusUpdateLoading}
                  >
                    취소
                  </Button>
                </div>
              </form>
            ) : (
              <div className="text-center py-12 text-red-400">
                상세 정보를 불러오지 못했습니다.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
