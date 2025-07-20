import { apiClient } from '@/lib/backend/apiV1/client';
import type {
  OrderRequestDTO,
  OrderDetailDTO,
  OrderListDTO,
} from '@/lib/backend/apiV1/api';

// 주문 생성
export const createOrder = async (
  orderData: OrderRequestDTO
): Promise<OrderDetailDTO> => {
  const response = await apiClient.api.createOrder(orderData);
  return response.data;
};

// 내 주문 목록 조회
export const getMyOrders = async (): Promise<OrderListDTO[]> => {
  const response = await apiClient.api.getMyOrders();
  return response.data;
};

// 주문 상세 조회
export const getOrderDetail = async (
  orderId: number
): Promise<OrderDetailDTO> => {
  const response = await apiClient.api.getOrderDetail(orderId);
  return response.data;
};
