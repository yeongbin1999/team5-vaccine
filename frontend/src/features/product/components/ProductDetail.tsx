'use client';

import { useProduct } from '@/features/product/hooks/useProducts';
import Image from 'next/image';
import { notFound } from 'next/navigation';
import { useCartStore } from '@/features/cart/cartStore';
import React, { useState } from 'react';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addToCart } from '@/features/cart/api';
import { useAuthStore } from '@/features/auth/authStore';
import { toast } from 'sonner';

interface ProductDetailProps {
  id: string;
}

export function ProductDetail({ id }: ProductDetailProps) {
  const { data: product, isLoading, error } = useProduct(id);
  const { addItem } = useCartStore();
  const [quantity, setQuantity] = useState(1);
  const [inputValue, setInputValue] = useState('1'); // 입력 중인 값을 별도로 관리
  const [showToast, setShowToast] = useState(false);
  const queryClient = useQueryClient();
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const addToCartMutation = useMutation({
    mutationFn: async (vars: {
      productId: number;
      quantity: number;
      name: string;
      price: number;
      image_url: string;
    }) => addToCart(vars),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      setShowToast(true);
      setQuantity(1);
      setInputValue('1');
      setTimeout(() => setShowToast(false), 2000);
    },
    onError: (error: any) => {
      // 재고 부족 등의 에러 처리
      if (error?.response?.status === 400) {
        toast.error('재고가 부족합니다.');
      } else {
        toast.error('장바구니 추가에 실패했습니다.');
      }
    },
  });

  const handleDecrease = () => {
    if (quantity > 1) {
      const newQuantity = quantity - 1;
      setQuantity(newQuantity);
      setInputValue(newQuantity.toString());
    }
  };

  const handleIncrease = () => {
    if (product && quantity < product.stock) {
      const newQuantity = quantity + 1;
      setQuantity(newQuantity);
      setInputValue(newQuantity.toString());
    } else if (product && quantity >= product.stock) {
      toast.error('재고 수량을 초과할 수 없습니다.');
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setInputValue(value); // 입력 중인 값은 그대로 저장

    // 빈 문자열이거나 숫자가 아닌 경우 quantity는 변경하지 않음
    if (value === '' || isNaN(Number(value))) {
      return;
    }

    let numValue = Number(value);

    // 재고 초과 시에만 제한
    if (product && numValue > product.stock) {
      numValue = product.stock;
      setInputValue(numValue.toString());
      toast.error('재고 수량을 초과할 수 없습니다.');
    }

    setQuantity(numValue);
  };

  const handleInputBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    let value = Number(e.target.value);

    // 빈 값이거나 유효하지 않은 값인 경우 1로 설정
    if (isNaN(value) || value < 1) {
      value = 1;
    }

    // 재고 초과 시 재고 수량으로 제한
    if (product && value > product.stock) {
      value = product.stock;
    }

    setQuantity(value);
    setInputValue(value.toString());
  };

  const handleInputKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      let value = Number((e.target as HTMLInputElement).value);

      // 빈 값이거나 유효하지 않은 값인 경우 1로 설정
      if (isNaN(value) || value < 1) {
        value = 1;
      }

      // 재고 초과 시 재고 수량으로 제한
      if (product && value > product.stock) {
        value = product.stock;
      }

      setQuantity(value);
      setInputValue(value.toString());
      (e.target as HTMLInputElement).blur();
    }
  };

  const handleAddToCart = () => {
    if (!product) return;

    // 재고 확인
    if (product.stock <= 0) {
      toast.error('현재 재고가 없습니다.');
      return;
    }

    if (quantity > product.stock) {
      toast.error('재고 수량을 초과할 수 없습니다.');
      return;
    }

    if (isAuthenticated) {
      addToCartMutation.mutate({
        productId: product.id,
        quantity,
        name: product.name,
        price: product.price,
        image_url: product.image_url || '/placeholder.png',
      });
    } else {
      addItem(product.id, quantity, {
        name: product.name,
        price: product.price,
        image_url: product.image_url || '/placeholder.png',
      });
      setShowToast(true);
      setQuantity(1);
      setTimeout(() => setShowToast(false), 2000);
    }
  };

  const renderToast = () => {
    if (!showToast) return null;

    return (
      <div className="fixed inset-0 flex items-center justify-center z-50 pointer-events-none">
        <div className="bg-black text-white px-6 py-3 rounded shadow text-lg font-semibold">
          장바구니에 담겼습니다!
        </div>
      </div>
    );
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh] pt-20">
        <div className="text-center">
          <div className="inline-flex items-center space-x-3 text-gray-600">
            <div className="w-6 h-6 border-2 border-amber-600 border-t-transparent rounded-full animate-spin"></div>
            <span className="text-xl font-medium">
              상품 정보를 불러오는 중...
            </span>
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return notFound();
  }

  const isOutOfStock = product.stock <= 0;
  const isLowStock = product.stock <= 5 && product.stock > 0;

  return (
    <div className="bg-white flex flex-1 flex-col">
      {renderToast()}
      <div className="w-full bg-[#FAF4EB] mt-10 pl-10 h-16 flex items-center justify-start text-gray-500 text-base md:text-lg rounded-xl shadow-sm">
        <div className="flex items-center h-full px-30">
          <span className="text-gray-700 font-medium mr-2">Shop</span>
          <span className="text-gray-700 font-medium mx-2">{'>'}</span>
          <span className="text-gray-700 font-medium">{product.name}</span>
        </div>
      </div>

      {/* 메인 컨텐츠 */}
      <div className="flex-1 flex items-center justify-center">
        <div className="max-w-[1100px] w-full mx-auto grid grid-cols-1 md:grid-cols-2 gap-14 items-stretch bg-white rounded-2xl px-4 md:px-12">
          {/* 상품 이미지 */}
          <div className="bg-[#FAF4EB] rounded-xl p-8 flex justify-center items-center min-h-[480px] h-[480px] relative mt-4">
            <Image
              src={
                !product.image_url || product.image_url.startsWith('http')
                  ? '/coffee.jpeg'
                  : product.image_url
              }
              alt={product.name}
              width={400}
              height={600}
              className="object-contain rounded-lg shadow-md"
              priority
            />
          </div>

          {/* 상품 정보 */}
          <div className="flex flex-col justify-between h-[500px] min-h-[500px] self-stretch mt-4 md:pr-16">
            <div>
              <h1 className="text-3xl md:text-4xl font-bold mb-4 mt-8">
                {product.name}
              </h1>
              <div className="text-gray-600 text-xl md:text-2xl mb-4">
                ₩ {Number(product.price).toLocaleString()}
              </div>

              {/* 재고 상태 표시 */}
              <div className="mb-4">
                {isOutOfStock ? (
                  <Badge className="bg-red-100 text-red-700 border border-red-300 px-3 py-1 text-sm font-semibold">
                    품절
                  </Badge>
                ) : isLowStock ? (
                  <Badge className="bg-orange-100 text-orange-700 border border-orange-300 px-3 py-1 text-sm font-semibold">
                    재고 부족 (남은 수량: {product.stock}개)
                  </Badge>
                ) : (
                  <Badge className="bg-green-100 text-green-700 border border-green-300 px-3 py-1 text-sm font-semibold">
                    재고 있음 (남은 수량: {product.stock}개)
                  </Badge>
                )}
              </div>

              {/* 설명 */}
              <p className="text-gray-700 text-base md:text-lg mb-8 leading-relaxed">
                {product.description}
              </p>
            </div>
            {/* 수량/장바구니 */}
            <div className="flex items-center space-x-5 mt-4 mb-8">
              <div className="flex items-center border rounded-lg px-4 py-2 text-lg md:text-xl w-40 justify-between">
                <button
                  className="px-2 py-1 disabled:opacity-50 disabled:cursor-not-allowed"
                  onClick={handleDecrease}
                  disabled={quantity <= 1}
                >
                  -
                </button>
                <input
                  type="number"
                  min={1}
                  max={product.stock}
                  value={inputValue}
                  onChange={handleInputChange}
                  onBlur={handleInputBlur}
                  onKeyDown={handleInputKeyDown}
                  className="px-2 w-20 text-center border-none outline-none bg-transparent appearance-none"
                  style={{
                    MozAppearance: 'textfield',
                    WebkitAppearance: 'none',
                  }}
                  disabled={isOutOfStock}
                />
                <button
                  className="px-2 py-1 disabled:opacity-50 disabled:cursor-not-allowed"
                  onClick={handleIncrease}
                  disabled={quantity >= product.stock}
                >
                  +
                </button>
              </div>
              <button
                className={`flex-1 border rounded-lg py-3 text-lg md:text-xl font-semibold transition ${
                  isOutOfStock
                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    : 'hover:bg-gray-100'
                }`}
                onClick={handleAddToCart}
                disabled={isOutOfStock || addToCartMutation.isPending}
              >
                {addToCartMutation.isPending
                  ? '추가 중...'
                  : isOutOfStock
                    ? '품절'
                    : '장바구니'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
