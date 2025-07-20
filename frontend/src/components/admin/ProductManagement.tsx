'use client';
import { useRouter } from 'next/navigation';
import { useState, useEffect, useMemo, useRef } from 'react';
import Image from 'next/image';
import { Button } from '@/components/ui/button';
import type { ProductWithCategoryName } from '@/features/product/api';
import { DataTable } from '@/components/ui/data-table';
import type { ColumnDef } from '@tanstack/react-table';
import { fetchProducts, fetchCategories } from '@/features/product/api';
import { apiClient } from '@/lib/backend/apiV1/client';

const ALL_CATEGORY_OPTION = { id: 0, name: '전체' };
const STATUS_OPTIONS = [
  { value: '', label: '전체' },
  { value: '판매중', label: '판매중' },
  { value: '품절', label: '품절' },
];
const PAGE_SIZE = 6;

export default function ProductManagement() {
  const router = useRouter();
  const [products, setProducts] = useState<ProductWithCategoryName[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // 검색/필터/정렬/페이지네이션 상태
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState<number>(0);
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);
  const [categories, setCategories] = useState<{ id: number; name: string }[]>([
    ALL_CATEGORY_OPTION,
  ]);

  // 모달 상태 및 폼 상태
  const [modalOpen, setModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] =
    useState<ProductWithCategoryName | null>(null);
  const [form, setForm] = useState({
    name: '',
    description: '',
    price: '',
    stock: '',
    category_id: 0,
    image_url: '',
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 모달 열기 (생성/수정)
  const openCreateModal = () => {
    setEditingProduct(null);
    setForm({
      name: '',
      description: '',
      price: '',
      stock: '',
      category_id: 0,
      image_url: '',
    });
    setModalOpen(true);
    setFormError(null);
  };
  const openEditModal = (product: ProductWithCategoryName) => {
    setEditingProduct(product);
    setForm({
      name: product.name,
      description: product.description || '',
      price: String(product.price),
      stock: String(product.stock),
      category_id: product.category_id || 0,
      image_url: product.image_url || '',
    });
    setModalOpen(true);
    setFormError(null);
  };
  const closeModal = () => {
    setModalOpen(false);
    setEditingProduct(null);
    setFormError(null);
  };

  // 폼 입력 핸들러
  const handleFormChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >
  ) => {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
  };

  // 상품 생성/수정 API 연동
  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError(null);
    try {
      const payload = {
        name: form.name,
        description: form.description,
        price: Number(form.price),
        stock: Number(form.stock),
        categoryId: Number(form.category_id),
        imageUrl: form.image_url,
      };
      if (editingProduct) {
        // 수정
        await apiClient.api.updateProduct(editingProduct.id, payload);
      } else {
        // 생성
        await apiClient.api.createProduct(payload);
      }
      // 성공 시 목록 새로고침
      const [productsData] = await Promise.all([fetchProducts()]);
      setProducts(productsData);
      closeModal();
    } catch (err: unknown) {
      setFormError('저장에 실패했습니다.');
    } finally {
      setFormLoading(false);
    }
  };

  useEffect(() => {
    setLoading(true);
    setError(null);
    Promise.all([fetchProducts(), fetchCategories()])
      .then(([productsData, categoriesData]) => {
        setProducts(productsData);
        setCategories([ALL_CATEGORY_OPTION, ...categoriesData]);
      })
      .catch(() => setError('상품 데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  // 필터/검색 적용
  const filtered = useMemo(
    () =>
      products
        .filter(
          p => !search || p.name.toLowerCase().includes(search.toLowerCase())
        )
        .filter(p => !category || p.category_id === category)
        .filter(p => {
          if (!status) return true;
          if (status === '판매중') return (p.stock || 0) > 0;
          if (status === '품절') return (p.stock || 0) === 0;
          return true;
        }),
    [products, search, category, status]
  );

  // 페이지네이션
  const total = filtered.length;
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const paged = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  // shadcn DataTable columns 정의
  const columns = useMemo<ColumnDef<ProductWithCategoryName, any>[]>(
    () => [
      {
        accessorKey: 'image_url',
        header: '이미지',
        cell: ({ row }) => {
          const url =
            row.original.image_url && row.original.image_url.trim() !== ''
              ? row.original.image_url
              : '/coffee.jpeg';
          return (
            <img
              src={url}
              alt={row.original.name}
              className="w-16 h-16 object-contain mx-auto"
            />
          );
        },
      },
      {
        accessorKey: 'name',
        header: '상품명',
        cell: ({ row }) => row.original.name,
      },
      {
        accessorKey: 'category_id',
        header: '카테고리',
        cell: ({ row }) => {
          if (row.original.category_name) return row.original.category_name;
          const cat = categories.find(c => c.id === row.original.category_id);
          return cat ? cat.name : '-';
        },
      },
      {
        accessorKey: 'price',
        header: '가격',
        cell: ({ row }) => (row.original.price || 0).toLocaleString(),
      },
      {
        accessorKey: 'stock',
        header: '재고',
        cell: ({ row }) => (row.original.stock || 0).toLocaleString(),
      },
      {
        id: 'status',
        header: '상태',
        cell: ({ row }) => (
          <span
            className={`px-3 py-1 rounded-full text-xs font-semibold ${
              (row.original.stock || 0) > 0
                ? 'bg-green-100 text-green-800'
                : 'bg-red-100 text-red-800'
            }`}
          >
            {(row.original.stock || 0) > 0 ? '판매중' : '품절'}
          </span>
        ),
      },
      {
        id: 'actions',
        header: '관리',
        cell: ({ row }) => (
          <div className="flex justify-center items-center gap-2">
            <button
              className="p-1 px-2 text-xs rounded bg-black hover:bg-gray-800 text-white"
              onClick={() => openEditModal(row.original)}
            >
              수정
            </button>
          </div>
        ),
      },
    ],
    [router]
  );

  return (
    <div className="w-full">
      <div className="flex flex-col md:flex-row md:items-end gap-2 mb-4">
        <div className="flex gap-2 flex-1">
          <input
            className="border rounded px-3 py-2 text-gray-900 font-semibold w-40"
            placeholder="상품명 검색"
            value={search}
            onChange={e => {
              setSearch(e.target.value);
              setPage(1);
            }}
          />
          <select
            className="border rounded px-3 py-2 text-gray-900 font-semibold"
            value={category}
            onChange={e => {
              setCategory(Number(e.target.value));
              setPage(1);
            }}
          >
            {categories.map(opt => (
              <option key={opt.id} value={opt.id}>
                {opt.name}
              </option>
            ))}
          </select>
          <select
            className="border rounded px-3 py-2 text-gray-900 font-semibold"
            value={status}
            onChange={e => {
              setStatus(e.target.value);
              setPage(1);
            }}
          >
            {STATUS_OPTIONS.map(opt => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
        <Button onClick={openCreateModal}>상품 생성</Button>
      </div>
      {loading ? (
        <div className="text-center py-16 text-gray-500">
          상품 목록을 불러오는 중...
        </div>
      ) : error ? (
        <div className="text-center py-16 text-red-500">{error}</div>
      ) : total === 0 ? (
        <div className="text-center py-16 text-gray-500">
          등록된 상품이 없습니다.
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={paged}
        />
      )}
      {/* 페이지네이션 */}
      {total > 0 && (
        <div className="flex justify-center items-center gap-2 mt-6">
          <Button
            onClick={() => setPage(p => Math.max(1, p - 1))}
            disabled={page === 1}
          >
            이전
          </Button>
          <span className="text-gray-700 font-semibold">
            {page} / {totalPages}
          </span>
          <Button
            onClick={() => setPage(p => Math.min(totalPages, p + 1))}
            disabled={page === totalPages}
          >
            다음
          </Button>
        </div>
      )}
      {/* 상품 생성/수정 모달 */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6 relative">
            <h2 className="text-lg font-bold mb-4">
              {editingProduct ? '상품 수정' : '상품 생성'}
            </h2>
            <form onSubmit={handleFormSubmit} className="flex flex-col gap-3">
              {/* 상품명 */}
              <label className="font-semibold text-sm" htmlFor="modal-name">
                상품명
              </label>
              <input
                id="modal-name"
                className="border rounded px-3 py-2"
                name="name"
                placeholder="상품명을 입력하세요"
                value={form.name}
                onChange={handleFormChange}
                required
              />
              {/* 설명 */}
              <label
                className="font-semibold text-sm"
                htmlFor="modal-description"
              >
                설명
              </label>
              <textarea
                id="modal-description"
                className="border rounded px-3 py-2"
                name="description"
                placeholder="상품 설명을 입력하세요"
                value={form.description}
                onChange={handleFormChange}
              />
              {/* 가격 */}
              <label className="font-semibold text-sm" htmlFor="modal-price">
                가격
              </label>
              <input
                id="modal-price"
                className="border rounded px-3 py-2"
                name="price"
                type="number"
                placeholder="숫자만 입력"
                value={form.price}
                onChange={handleFormChange}
                required
                min={0}
              />
              {/* 재고 */}
              <label className="font-semibold text-sm" htmlFor="modal-stock">
                재고
              </label>
              <input
                id="modal-stock"
                className="border rounded px-3 py-2"
                name="stock"
                type="number"
                placeholder="숫자만 입력"
                value={form.stock}
                onChange={handleFormChange}
                required
                min={0}
              />
              {/* 카테고리 */}
              <label className="font-semibold text-sm" htmlFor="modal-category">
                카테고리
              </label>
              <select
                id="modal-category"
                className="border rounded px-3 py-2"
                name="category_id"
                value={form.category_id}
                onChange={handleFormChange}
                required
              >
                <option value={0}>카테고리 선택</option>
                {categories
                  .filter(c => c.id !== 0)
                  .map(opt => (
                    <option key={opt.id} value={opt.id}>
                      {opt.name}
                    </option>
                  ))}
              </select>
              {/* 이미지 URL */}
              <label className="font-semibold text-sm" htmlFor="modal-image">
                이미지 URL
              </label>
              <input
                id="modal-image"
                className="border rounded px-3 py-2"
                name="image_url"
                placeholder="이미지 URL을 입력하세요"
                value={form.image_url}
                onChange={handleFormChange}
              />
              {formError && (
                <div className="text-red-500 text-sm">{formError}</div>
              )}
              <div className="flex gap-2 mt-2 justify-end">
                <Button type="submit" disabled={formLoading}>
                  {editingProduct ? '수정' : '생성'}
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={closeModal}
                  disabled={formLoading}
                >
                  취소
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
