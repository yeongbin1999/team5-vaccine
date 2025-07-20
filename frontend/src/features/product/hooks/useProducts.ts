import { useQuery } from '@tanstack/react-query';
import { mapToProduct, ProductApiLike } from '@/features/product/api';
import { Product } from '@/features/product/types';
import { apiClient } from '@/lib/backend/apiV1/client';

// 실제 API 함수
async function fetchProducts(): Promise<Product[]> {
  const res = await apiClient.api.getAllProducts();
  return (res.data || []).map(item => mapToProduct(item as ProductApiLike));
}

// 상품 목록 조회 (react-query)
export function useProducts() {
  return useQuery({
    queryKey: ['products'],
    queryFn: fetchProducts,
    staleTime: 1000 * 60 * 5, // 5분
  });
}

// 개별 상품 조회
export function useProduct(id: string) {
  return useQuery({
    queryKey: ['product', id],
    queryFn: async (): Promise<Product> => {
      const productId = Number(id);
      const res = await apiClient.api.getProductById(productId);
      return mapToProduct(res.data as ProductApiLike);
    },
    enabled: !!id,
    staleTime: 1000 * 60 * 10, // 10분
  });
}
