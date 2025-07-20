// 스프링부트 API 응답 타입
export interface CartItemResponse {
  id: number;
  productId: number;
  productName: string;
  productPrice: number;
  productImageUrl: string;
  quantity: number;
  createdAt: string;
  updatedAt: string;
}

export interface CartResponse {
  id: number;
  userId: number;
  items: CartItemResponse[];
  totalPrice: number;
  totalQuantity: number;
  createdAt: string;
  updatedAt: string;
}

// 클라이언트에서 사용할 타입
export interface CartItem {
  id: number; // cartItemId
  productId: number;
  name: string;
  price: number;
  image_url?: string;
  quantity: number;
}

// API 요청 타입
export interface AddToCartRequest {
  productId: number;
  quantity: number;
}

export interface UpdateCartItemRequest {
  itemId: number;
  quantity: number;
}
