'use client';

import { useCartStore } from '@/features/cart/cartStore';
import Image from 'next/image';
import React, { useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { flushCartSync } from '@/features/cart/cartStore';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchCart,
  updateCartItem,
  removeFromCart,
  clearCart,
} from '@/features/cart/api';
import { useAuthStore } from '@/features/auth/authStore';
import { createOrder } from '@/features/orders/api';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { apiClient } from '@/lib/backend/apiV1/client';
import { mapToProduct } from '@/features/product/api';

function CartSyncOnRouteChange() {
  const pathname = usePathname();
  const prevPath = React.useRef(pathname);
  React.useEffect(() => {
    if (prevPath.current !== pathname) {
      if (flushCartSync) flushCartSync();
      prevPath.current = pathname;
    }
  }, [pathname]);
  return null;
}

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

// 모든 상품 재고 정보를 한 번에 가져오는 훅
function useProductsStock(productIds: number[]) {
  const uniqueProductIds = [...new Set(productIds)].filter(id => id > 0);

  return useQuery({
    queryKey: ['products-stock', uniqueProductIds],
    queryFn: async () => {
      const stockData: Record<number, any> = {};
      await Promise.all(
        uniqueProductIds.map(async productId => {
          try {
            const res = await apiClient.api.getProductById(productId);
            stockData[productId] = mapToProduct(res.data);
          } catch (error) {
            console.error(`Failed to fetch product ${productId}:`, error);
            stockData[productId] = null;
          }
        })
      );
      return stockData;
    },
    enabled: uniqueProductIds.length > 0,
    staleTime: 1000 * 60 * 5, // 5분간 캐시
  });
}

export function CartPage() {
  const user = useAuthStore(state => state.user);
  const items = useCartQuery();
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const updateQuantityZustand = useCartStore(state => state.updateQuantity);
  const removeItemZustand = useCartStore(state => state.removeItem);
  const getTotalPrice = useCartStore(state => state.getTotalPrice);
  const queryClient = useQueryClient();
  const router = useRouter();

  // 모든 상품의 재고 정보 가져오기
  const productIds = items.map(item => item.productId);
  const { data: productsStock } = useProductsStock(productIds);

  // 각 아이템의 입력 값 상태 관리
  const [inputValues, setInputValues] = useState<Record<number, string>>({});

  // React Query mutation for updateQuantity
  const updateQuantityMutation = useMutation({
    mutationFn: async (vars: { itemId: number; quantity: number }) =>
      updateCartItem(vars),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart', user?.id] });
    },
    onError: (error: any) => {
      if (error?.response?.status === 400) {
        toast.error('재고가 부족합니다.');
      } else {
        toast.error('수량 변경에 실패했습니다.');
      }
    },
  });

  // React Query mutation for removeItem
  const removeItemMutation = useMutation({
    mutationFn: async (itemId: number) => removeFromCart(itemId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart', user?.id] });
    },
  });

  // React Query mutation for createOrder
  const createOrderMutation = useMutation({
    mutationFn: async (orderData: any) => createOrder(orderData),
    onSuccess: async () => {
      // 주문 생성 성공 후 장바구니 비우기
      try {
        if (isAuthenticated) {
          await clearCart();
        } else {
          clearCartZustand();
        }
        queryClient.invalidateQueries({ queryKey: ['cart', user?.id] });
        toast.success('주문이 성공적으로 생성되었습니다!');
        router.push('/orders');
      } catch (error) {
        console.error('Cart clear error:', error);
        // 장바구니 비우기 실패해도 주문은 성공했으므로 주문 페이지로 이동
        toast.success('주문이 성공적으로 생성되었습니다!');
        router.push('/orders');
      }
    },
    onError: (error: any) => {
      console.error('Order creation error:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);

      if (error.response?.status === 500) {
        toast.error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      } else if (error.response?.status === 400) {
        toast.error('잘못된 요청입니다. 입력 정보를 확인해주세요.');
      } else {
        toast.error('주문에 실패했습니다. 다시 시도해주세요.');
      }
    },
  });

  const handleUpdateQuantity = (
    itemId: number,
    quantity: number,
    productId: number
  ) => {
    // 재고 확인
    const productStock = productsStock?.[productId];
    if (productStock && quantity > productStock.stock) {
      toast.error('재고 수량을 초과할 수 없습니다.');
      return;
    }

    if (isAuthenticated) {
      updateQuantityMutation.mutate({ itemId, quantity });
    } else {
      updateQuantityZustand(itemId, quantity);
    }
  };

  const handleRemoveItem = (itemId: number) => {
    if (isAuthenticated) {
      removeItemMutation.mutate(itemId);
    } else {
      removeItemZustand(itemId);
    }
  };

  const handleCheckout = () => {
    if (!isAuthenticated) {
      toast.error('로그인이 필요합니다.');
      router.push('/login');
      return;
    }

    if (!user?.id) {
      toast.error('사용자 정보를 가져올 수 없습니다. 다시 로그인해주세요.');
      router.push('/login');
      return;
    }

    if (items.length === 0) {
      toast.error('장바구니가 비어있습니다.');
      return;
    }

    // 임시로 주문 생성 기능 비활성화 (백엔드 데이터베이스 이슈)
    toast.error(
      '주문 생성 기능이 일시적으로 비활성화되었습니다. 관리자에게 문의해주세요.'
    );
    return;

    // 주문 데이터 생성 (상태는 백엔드에서 자동 설정)
    const orderData = {
      userId: user?.id,
      deliveryId: 1, // 기본 배송 ID 추가
      shippingAddress: user?.address || '주소 정보가 없습니다.',
      items: items.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        unitPrice: item.price,
      })),
    };

    createOrderMutation.mutate(orderData);
  };

  const clearCartZustand = useCartStore(state => state.clearCart);

  const subtotal = items.reduce(
    (total, item) => total + item.price * item.quantity,
    0
  );
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);

  return (
    <div className="flex flex-col md:flex-row gap-8 p-8 min-h-[60vh]">
      <CartSyncOnRouteChange />
      {/* Cart Table */}
      <div className="flex-1">
        <h1 className="text-3xl font-bold ml-8 mb-8">Cart</h1>
        <div className="overflow-x-auto rounded-xl shadow-sm">
          <table className="w-full min-w-[600px] bg-white rounded-xl">
            <thead>
              <tr className="bg-[#FAF4EB] text-gray-700 text-lg">
                <th className="py-4 px-4 text-left">Product</th>
                <th className="py-4 px-6 text-right w-32 min-w-[7rem]">
                  Price
                </th>
                <th className="py-4 px-6 text-center w-28 min-w-[6rem]">
                  Quantity
                </th>
                <th className="py-4 px-4 text-right w-36 min-w-[8rem]">
                  Subtotal
                </th>
                <th className="py-4 px-4 w-12"></th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    className="text-center py-12 text-gray-400 text-xl"
                  >
                    No items in cart.
                  </td>
                </tr>
              ) : (
                items.map(item => {
                  const productStock = productsStock?.[item.productId];
                  const isOutOfStock = productStock?.stock === 0;
                  const isLowStock =
                    productStock?.stock &&
                    productStock.stock <= 5 &&
                    productStock.stock > 0;
                  const isOverStock =
                    productStock?.stock && item.quantity > productStock.stock;

                  return (
                    <tr
                      key={item.id}
                      className={`border-b last:border-b-0 ${isOutOfStock ? 'opacity-60' : ''}`}
                    >
                      <td className="py-4 px-4 flex items-center gap-4">
                        <Image
                          src={
                            !item.image_url || item.image_url.startsWith('http')
                              ? '/coffee.jpeg'
                              : item.image_url
                          }
                          alt={item.name}
                          width={80}
                          height={80}
                          className="rounded-lg bg-gray-100"
                        />
                        <div className="flex flex-col">
                          <span
                            className={`font-medium ${isOutOfStock ? 'text-gray-400' : 'text-gray-700'}`}
                          >
                            {item.name}
                          </span>
                          {/* 재고 상태 표시 */}
                          {productStock && (
                            <div className="mt-1">
                              {isOutOfStock ? (
                                <Badge className="bg-red-100 text-red-700 border border-red-300 px-2 py-0.5 text-xs">
                                  품절
                                </Badge>
                              ) : isLowStock ? (
                                <Badge className="bg-orange-100 text-orange-700 border border-orange-300 px-2 py-0.5 text-xs">
                                  재고부족 ({productStock.stock}개)
                                </Badge>
                              ) : isOverStock ? (
                                <Badge className="bg-red-100 text-red-700 border border-red-300 px-2 py-0.5 text-xs">
                                  재고초과
                                </Badge>
                              ) : (
                                <Badge className="bg-green-100 text-green-700 border border-green-300 px-2 py-0.5 text-xs">
                                  재고있음 ({productStock.stock}개)
                                </Badge>
                              )}
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="py-4 px-6 text-right w-32 min-w-[7rem] text-gray-600 text-lg">
                        ₩ {Number(item.price).toLocaleString()}
                      </td>
                      <td className="py-4 px-6 text-center w-28 min-w-[6rem]">
                        <div className="flex items-center justify-center gap-2">
                          <button
                            className="px-2 py-1 text-lg text-gray-500 hover:text-amber-600 border rounded flex-shrink-0 disabled:opacity-50 disabled:cursor-not-allowed"
                            onClick={() => {
                              const newQuantity = Math.max(
                                1,
                                item.quantity - 1
                              );
                              handleUpdateQuantity(
                                item.id,
                                newQuantity,
                                item.productId
                              );
                              setInputValues(prev => ({
                                ...prev,
                                [item.id]: newQuantity.toString(),
                              }));
                            }}
                            disabled={item.quantity <= 1 || isOutOfStock}
                            aria-label="Decrease"
                          >
                            -
                          </button>
                          <input
                            type="number"
                            value={
                              inputValues[item.id] !== undefined
                                ? inputValues[item.id]
                                : item.quantity.toString()
                            }
                            onChange={e => {
                              const value = e.target.value;
                              setInputValues(prev => ({
                                ...prev,
                                [item.id]: value,
                              }));

                              // 빈 문자열이거나 숫자가 아닌 경우 quantity는 변경하지 않음
                              if (value === '' || isNaN(Number(value))) {
                                return;
                              }

                              let numValue = Number(value);

                              // 1 미만인 경우 1로 설정
                              if (numValue < 1) {
                                numValue = 1;
                              }

                              // 재고 초과 시 재고 수량으로 제한
                              if (
                                productStock?.stock &&
                                numValue > productStock.stock
                              ) {
                                numValue = productStock.stock;
                                setInputValues(prev => ({
                                  ...prev,
                                  [item.id]: numValue.toString(),
                                }));
                                toast.error('재고 수량을 초과할 수 없습니다.');
                              }

                              handleUpdateQuantity(
                                item.id,
                                numValue,
                                item.productId
                              );
                            }}
                            onBlur={e => {
                              let value = Number(e.target.value);

                              // 빈 값이거나 유효하지 않은 값인 경우 1로 설정
                              if (isNaN(value) || value < 1) {
                                value = 1;
                              }

                              // 재고 초과 시 재고 수량으로 제한
                              if (
                                productStock?.stock &&
                                value > productStock.stock
                              ) {
                                value = productStock.stock;
                              }

                              handleUpdateQuantity(
                                item.id,
                                value,
                                item.productId
                              );
                              setInputValues(prev => ({
                                ...prev,
                                [item.id]: value.toString(),
                              }));
                            }}
                            className={`min-w-[3rem] w-16 border rounded text-center py-2 font-mono ${
                              isOutOfStock ? 'bg-gray-100' : ''
                            }`}
                            style={{
                              appearance: 'auto',
                            }}
                            disabled={isOutOfStock}
                          />
                          <button
                            className="px-2 py-1 text-lg text-gray-500 hover:text-amber-600 border rounded flex-shrink-0 disabled:opacity-50 disabled:cursor-not-allowed"
                            onClick={() => {
                              const newQuantity = item.quantity + 1;
                              handleUpdateQuantity(
                                item.id,
                                newQuantity,
                                item.productId
                              );
                              setInputValues(prev => ({
                                ...prev,
                                [item.id]: newQuantity.toString(),
                              }));
                            }}
                            disabled={
                              productStock?.stock
                                ? item.quantity >= productStock.stock
                                : false || isOutOfStock
                            }
                            aria-label="Increase"
                          >
                            +
                          </button>
                        </div>
                      </td>
                      <td className="py-4 px-4 text-right w-36 min-w-[8rem] text-gray-700 text-lg">
                        ₩ {(item.price * item.quantity).toLocaleString()}
                      </td>
                      <td className="py-4 px-4 text-center w-12">
                        <button
                          onClick={() => handleRemoveItem(item.id)}
                          className="text-amber-600 hover:text-red-500 text-2xl"
                        >
                          🗑️
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
      {/* Cart Totals */}
      <div className="w-full md:w-96 bg-[#FAF4EB] rounded-xl p-8 h-fit shadow-sm md:ml-8 md:mt-[56px]">
        <h2 className="text-2xl font-bold mb-6">Cart Totals</h2>
        <div className="space-y-4">
          <div className="flex justify-between text-xl font-bold">
            <span>Total:</span>
            <span
              className={
                mounted && subtotal !== 0 ? 'text-gray-900' : 'text-gray-400'
              }
            >
              ₩ {mounted ? subtotal.toLocaleString() : 0}
            </span>
          </div>
        </div>
        <button
          className="w-full bg-amber-600 text-white py-3 rounded-lg mt-6 hover:bg-amber-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
          onClick={handleCheckout}
          disabled={createOrderMutation.isPending || items.length === 0}
        >
          {createOrderMutation.isPending ? '주문 생성 중...' : '주문'}
        </button>
      </div>
    </div>
  );
}
