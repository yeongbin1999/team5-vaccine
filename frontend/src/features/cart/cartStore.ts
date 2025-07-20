import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { CartItem } from './types';
import {
  fetchCart,
  addToCart,
  updateCartItem as apiUpdateCartItem,
  removeFromCart as apiRemoveFromCart,
  clearCart as apiClearCart,
} from './api';
import { useAuthStore } from '../auth/authStore';
import { queryClient } from '@/components/providers/QueryProvider';

interface CartStore {
  items: CartItem[];
  isLoading: boolean;
  error: string | null;

  fetch: () => Promise<void>;
  addItem: (
    productId: number,
    quantity: number,
    productInfo: Omit<CartItem, 'id' | 'quantity' | 'productId'>
  ) => Promise<void>;
  updateQuantity: (itemId: number, quantity: number) => void;
  removeItem: (itemId: number) => Promise<void>;
  clearCart: () => Promise<void>;

  getTotalCount: () => number;
  getTotalPrice: () => number;
  _hasHydrated: boolean;
}

// flushCartSync를 export
export let flushCartSync: (() => Promise<void>) | null = null;

export const useCartStore = create<CartStore>()(
  persist(
    (set, get) => {
      let syncTimeout: NodeJS.Timeout | null = null;
      let lastSyncItems: CartItem[] = [];
      let pendingSync: (() => Promise<void>) | null = null;

      // flushCartSync: debounce된 서버 동기화를 즉시 실행
      flushCartSync = async () => {
        if (pendingSync) {
          await pendingSync();
          pendingSync = null;
        }
      };

      // beforeunload에서 flushCartSync 실행
      if (typeof window !== 'undefined') {
        window.addEventListener('beforeunload', flushCartSync);
      }

      return {
        items: [],
        isLoading: false,
        error: null,
        _hasHydrated: false,

        fetch: async () => {
          set({ isLoading: true, error: null });
          try {
            const items = await fetchCart();
            set({ items, isLoading: false });
            lastSyncItems = items;
          } catch (e: unknown) {
            const error = e as { message?: string };
            const msg =
              error.message && error.message.includes('로그인이 필요합니다')
                ? '오류가 발생했습니다. 다시 시도해 주세요.'
                : error.message || '장바구니 조회 실패';
            set({ error: msg, isLoading: false });
          }
        },

        addItem: async (
          productId: number,
          quantity: number,
          productInfo: Omit<CartItem, 'id' | 'quantity' | 'productId'>
        ) => {
          const userId = useAuthStore.getState().user?.id;
          if (!userId) {
            // 비로그인: 로컬 상태만 변경 (id는 productId, productId도 명시)
            set(state => {
              const existingItem = state.items.find(
                i => i.productId === productId
              );
              if (existingItem) {
                return {
                  ...state,
                  items: state.items.map(i =>
                    i.productId === productId
                      ? { ...i, quantity: i.quantity + quantity }
                      : i
                  ),
                };
              }
              return {
                ...state,
                items: [
                  ...state.items,
                  {
                    id: productId, // 게스트는 id=productId
                    productId,
                    quantity,
                    ...productInfo,
                  },
                ],
              };
            });
            return;
          }
          set({ isLoading: true, error: null });
          try {
            const items = await addToCart({
              productId,
              quantity,
            });
            set({ items, isLoading: false });
            lastSyncItems = items;
          } catch (e: unknown) {
            const error = e as { message?: string };
            const msg =
              error.message && error.message.includes('로그인이 필요합니다')
                ? '오류가 발생했습니다. 다시 시도해 주세요.'
                : error.message || '장바구니 추가 실패';
            set({ error: msg, isLoading: false });
          }
        },

        updateQuantity: (itemId: number, quantity: number) => {
          const userId = useAuthStore.getState().user?.id;
          if (!userId) {
            // 비로그인: 로컬 상태만 변경
            set(state => ({
              items: state.items.map(i =>
                i.id === itemId ? { ...i, quantity } : i
              ),
            }));
            return;
          }
          // debounce 서버 동기화
          if (syncTimeout) clearTimeout(syncTimeout);
          pendingSync = async () => {
            try {
              const item = get().items.find(i => i.id === itemId);
              if (
                item &&
                item.quantity !==
                  (lastSyncItems.find(i => i.id === itemId)?.quantity ?? 0)
              ) {
                const items = await apiUpdateCartItem({
                  itemId,
                  quantity: item.quantity,
                });
                set({ items });
                lastSyncItems = items;
              }
            } catch (e: unknown) {
              const error = e as { message?: string };
              const msg =
                error.message && error.message.includes('로그인이 필요합니다')
                  ? '오류가 발생했습니다. 다시 시도해 주세요.'
                  : error.message || '수량 변경 실패';
              set({ error: msg });
            }
          };
          syncTimeout = setTimeout(async () => {
            if (pendingSync) {
              await pendingSync();
              pendingSync = null;
            }
          }, 500);
        },

        removeItem: async (itemId: number) => {
          const userId = useAuthStore.getState().user?.id;
          if (!userId) {
            // 비로그인: 로컬 상태만 변경
            set(state => ({
              items: state.items.filter(i => i.id !== itemId),
            }));
            return;
          }
          set({ isLoading: true, error: null });
          try {
            const items = await apiRemoveFromCart(itemId);
            set({ items, isLoading: false });
            lastSyncItems = items;
          } catch (e: unknown) {
            const error = e as { message?: string };
            const msg =
              error.message && error.message.includes('로그인이 필요합니다')
                ? '오류가 발생했습니다. 다시 시도해 주세요.'
                : error.message || '삭제 실패';
            set({ error: msg, isLoading: false });
          }
        },

        clearCart: async () => {
          const userId = useAuthStore.getState().user?.id;
          if (!userId) {
            // 비로그인: 로컬 상태만 변경
            set({ items: [] });
            return;
          }
          set({ isLoading: true, error: null });
          try {
            await apiClearCart();
            set({ items: [], isLoading: false });
            lastSyncItems = [];
          } catch (e: unknown) {
            const error = e as { message?: string };
            const msg =
              error.message && error.message.includes('로그인이 필요합니다')
                ? '오류가 발생했습니다. 다시 시도해 주세요.'
                : error.message || '장바구니 비우기 실패';
            set({ error: msg, isLoading: false });
          }
        },

        getTotalCount: () => {
          return get().items.reduce((sum, item) => sum + item.quantity, 0);
        },

        getTotalPrice: () => {
          return get().items.reduce(
            (sum, item) => sum + item.price * item.quantity,
            0
          );
        },
      };
    },
    {
      name: 'cart-storage',
      partialize: state => ({ items: state.items }),
      storage: createJSONStorage(() => sessionStorage),
      onRehydrateStorage: () => state => {
        if (state) state._hasHydrated = true;
      },
    }
  )
);

export function useCartHydrated() {
  return useCartStore(state => state._hasHydrated);
}

/**
 * 로그인 후 장바구니 동기화: sessionStorage(비로그인)와 서버 장바구니를 병합(수량 합치기)
 * 1. 서버 장바구니 fetch
 * 2. sessionStorage 장바구니 fetch
 * 3. 두 장바구니 병합(같은 상품은 수량 합치기)
 * 4. 서버에 없는 상품은 서버에 추가
 * 5. zustand store 갱신
 */
export async function syncCartOnLogin() {
  // 1. 로그인 직전, 게스트(세션) 장바구니를 먼저 백업
  let guestCart: CartItem[] = [];
  try {
    const raw = sessionStorage.getItem('cart-storage');
    if (raw) {
      const parsed = JSON.parse(raw);
      guestCart = parsed.state?.items || [];
    }
  } catch {
    guestCart = [];
  }

  // 2. 서버 장바구니 fetch
  let serverCart: CartItem[] = [];
  try {
    serverCart = await fetchCart();
  } catch {
    serverCart = [];
  }

  // 병합 전 상태 로그
  console.log('🟡 [syncCartOnLogin] 서버 장바구니:', serverCart);
  console.log('🟠 [syncCartOnLogin] 게스트(세션) 장바구니:', guestCart);

  // 3. 서버 장바구니를 Map(productId → {id, quantity})로 만듦
  const serverCartMap = new Map<number, { id: number; quantity: number }>();
  serverCart.forEach(item => {
    serverCartMap.set(Number(item.productId), {
      id: item.id,
      quantity: item.quantity,
    });
  });

  // 4. 게스트 장바구니의 각 상품에 대해 병합
  for (const guestItem of guestCart) {
    const pid = Number(guestItem.productId);
    if (isNaN(pid)) continue;
    const serverItem = serverCartMap.get(pid);
    if (serverItem) {
      // 서버에 있으면 수량 합산
      await apiUpdateCartItem({
        itemId: serverItem.id,
        quantity: serverItem.quantity + guestItem.quantity,
      });
    } else {
      // 서버에 없으면 새로 추가
      await addToCart({
        productId: pid,
        quantity: guestItem.quantity,
      });
    }
  }

  // 5. 병합 후 서버 장바구니 fetch로 상태 동기화
  const mergedCart = await fetchCart();
  useCartStore.setState({ items: mergedCart });
  sessionStorage.setItem(
    'cart-storage',
    JSON.stringify({ state: { items: mergedCart } })
  );

  // 6. react-query cart 쿼리 invalidate (화면 즉시 갱신)
  const userId = useAuthStore.getState().user?.id;
  if (userId) {
    queryClient.invalidateQueries({ queryKey: ['cart', userId] });
  }
}
