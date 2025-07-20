'use client';

import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { useProducts } from '@/features/product/hooks/useProducts';
import Link from 'next/link';

export function ProductGrid() {
  const { data: products, isLoading, error } = useProducts();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh] pt-20">
        <div className="text-center">
          <div className="inline-flex items-center space-x-3 text-gray-600">
            <div className="w-6 h-6 border-2 border-amber-600 border-t-transparent rounded-full animate-spin"></div>
            <span className="text-xl font-medium">상품을 불러오는 중...</span>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-20">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">
          상품을 불러오는데 실패했습니다
        </h2>
        <p className="text-gray-600">잠시 후 다시 시도해주세요.</p>
      </div>
    );
  }

  if (!products) {
    return null;
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
      {products.map(product => {
        const isNew =
          Date.now() - new Date(product.created_at).getTime() <
          7 * 24 * 60 * 60 * 1000;

        const isOutOfStock = product.stock <= 0;
        const isLowStock = product.stock <= 5 && product.stock > 0;

        return (
          <Link
            key={product.id}
            href={`/product/${product.id}`}
            className={`block group ${isOutOfStock ? 'pointer-events-none' : ''}`}
          >
            <Card
              className={`w-full max-w-[285px] h-[400px] overflow-hidden relative transition-all duration-200 cursor-pointer ${
                isOutOfStock
                  ? 'opacity-60 grayscale'
                  : 'hover:shadow-lg hover:scale-[1.02]'
              }`}
            >
              {/* 상단 이미지 */}
              <div
                className="relative h-[220px] bg-cover bg-center rounded-t-xl"
                style={{
                  backgroundImage: `url(${
                    !product.image_url || product.image_url.startsWith('http')
                      ? '/coffee.jpeg'
                      : product.image_url
                  })`,
                }}
              >
                {/* NEW 배지 */}
                {isNew && !isOutOfStock && (
                  <Badge
                    className="
                      absolute top-0 right-5
                      bg-green-100 text-green-700
                      border border-green-300
                      rounded-full
                      px-3 py-1
                      text-xs font-bold
                      shadow-md
                      uppercase
                      tracking-wider
                      z-10
                    "
                  >
                    NEW
                  </Badge>
                )}

                {/* 재고 상태 배지 */}
                {isOutOfStock ? (
                  <Badge
                    className="
                      absolute top-0 left-5
                      bg-red-100 text-red-700
                      border border-red-300
                      rounded-full
                      px-3 py-1
                      text-xs font-bold
                      shadow-md
                      z-10
                    "
                  >
                    품절
                  </Badge>
                ) : isLowStock ? (
                  <Badge
                    className="
                      absolute top-0 left-5
                      bg-orange-100 text-orange-700
                      border border-orange-300
                      rounded-full
                      px-3 py-1
                      text-xs font-bold
                      shadow-md
                      z-10
                    "
                  >
                    재고부족
                  </Badge>
                ) : null}
              </div>

              {/* 하단 정보 */}
              <CardContent className="min-h-[120px] p-4 pr-6 pb-0 flex flex-col justify-between">
                <div className="space-y-3">
                  <h3
                    className={`font-semibold text-2xl leading-[28.8px] line-clamp-2 ${
                      isOutOfStock ? 'text-gray-400' : 'text-gray-800'
                    }`}
                  >
                    {product.name}
                  </h3>

                  {/* 재고 정보 */}
                  <div className="text-sm text-gray-500">
                    {isOutOfStock ? (
                      <span className="text-red-500 font-medium">품절</span>
                    ) : isLowStock ? (
                      <span className="text-orange-500 font-medium">
                        재고: {product.stock}개 남음
                      </span>
                    ) : (
                      <span className="text-green-500 font-medium">
                        재고: {product.stock}개
                      </span>
                    )}
                  </div>
                </div>
                <div
                  className={`text-right text-base font-bold ${
                    isOutOfStock ? 'text-gray-400' : 'text-gray-800'
                  }`}
                >
                  ₩ {product.price.toLocaleString()}
                </div>
              </CardContent>
            </Card>
          </Link>
        );
      })}
    </div>
  );
}
