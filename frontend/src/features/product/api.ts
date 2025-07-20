import { apiClient } from '@/lib/backend/apiV1/client';
import { Product } from './types';

// API 응답 데이터 타입 정의
export type ProductApiLike = {
  id?: number;
  name?: string;
  description?: string;
  price?: string | number;
  image_url?: string;
  imageUrl?: string;
  stock?: number;
  created_at?: string;
  createdAt?: string;
  updated_at?: string;
  updatedAt?: string;
  category_id?: number;
  categoryId?: number;
};

export function mapToProduct(item: ProductApiLike): Product {
  return {
    id: item.id ?? 0,
    name: item.name ?? '',
    description: item.description ?? '',
    price:
      typeof item.price === 'string'
        ? parseInt(item.price.replace(/[^0-9]/g, ''))
        : (item.price ?? 0),
    image_url: item.image_url || item.imageUrl || '/placeholder.png',
    stock: item.stock ?? 10,
    created_at: item.created_at || item.createdAt || new Date().toISOString(),
    updated_at: item.updated_at || item.updatedAt || new Date().toISOString(),
    category_id: item.category_id ?? item.categoryId ?? 1,
  };
}

export async function fetchProducts(): Promise<any[]> {
  const res = await apiClient.api.getAllProducts();
  return res.data;
}
