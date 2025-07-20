// Enum 타입 정의
export type DeliveryStatus = '배송준비중' | '배송중' | '배송완료';
export type OrdersStatus = '배송준비중' | '배송중' | '배송완료' | '취소';

// 카테고리
export interface Category {
  id: number;
  name: string;
  parent_id?: number;
}

// 상품
export interface Product {
  id: number; // 상품 ID
  name: string; // 상품명
  image_url?: string; // 상품 이미지
  description?: string; // 상품 설명
  price: number; // 가격 (숫자)
  stock: number; // 재고
  category_id?: number; // 카테고리 (옵션)
  created_at: string; // 생성일
  updated_at: string; // 수정일
}
