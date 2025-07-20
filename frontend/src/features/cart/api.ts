import { useAuthStore } from '@/features/auth/authStore';
import type { AddCartItemRequest } from '@/lib/backend/apiV1/api';
import { CartItem } from './types';
import { apiClient } from '@/lib/backend/apiV1/client';

function mapCartItemResponse(response: {
  id?: number;
  productId?: number;
  productName?: string;
  unitPrice?: number;
  quantity?: number;
}): CartItem {
  return {
    id: response.id ?? 0, // cartItemId
    productId: response.productId ?? 0, // productId
    name: response.productName ?? '',
    price: response.unitPrice ?? 0,
    image_url: undefined, // CartItemDto doesn't have imageUrl
    quantity: response.quantity ?? 0,
  };
}

// 서버 장바구니 전체 조회
export async function fetchCart(): Promise<CartItem[]> {
  const userId = useAuthStore.getState().user?.id;
  if (!userId) throw new Error('로그인이 필요합니다');
  const res = await apiClient.api.getCart({ userId });
  const cartData = res.data;
  return (cartData.items || []).map(mapCartItemResponse);
}

// 장바구니에 상품 추가
export async function addToCart(
  request: AddCartItemRequest
): Promise<CartItem[]> {
  const userId = useAuthStore.getState().user?.id;
  if (!userId) throw new Error('로그인이 필요합니다');
  await apiClient.api.addItem(
    { userId },
    { productId: request.productId, quantity: request.quantity }
  );
  return fetchCart();
}

// 장바구니 상품 수량 업데이트
export async function updateCartItem({
  itemId,
  quantity,
}: {
  itemId: number;
  quantity: number;
}): Promise<CartItem[]> {
  const userId = useAuthStore.getState().user?.id;
  if (!userId) throw new Error('로그인이 필요합니다');
  await apiClient.api.updateItemQuantity(itemId, { quantity });
  return fetchCart();
}

// 장바구니 상품 삭제
export async function removeFromCart(itemId: number): Promise<CartItem[]> {
  const userId = useAuthStore.getState().user?.id;
  if (!userId) throw new Error('로그인이 필요합니다');
  await apiClient.api.deleteItem(itemId);
  return fetchCart();
}

// 장바구니 비우기
export async function clearCart(): Promise<void> {
  const userId = useAuthStore.getState().user?.id;
  if (!userId) throw new Error('로그인이 필요합니다');
  await apiClient.api.clearCart({ userId });
}
