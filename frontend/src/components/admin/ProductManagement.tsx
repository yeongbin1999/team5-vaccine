"use client";
import { useRouter } from 'next/navigation';
import { useState, useEffect, useMemo } from 'react';
import Image from 'next/image';
import { Button } from '@/components/ui/button';
import { Product } from '@/features/product/types';
import { DataTable } from '@/components/ui/data-table';
import type { ColumnDef } from '@tanstack/react-table';

const CATEGORY_OPTIONS = [
  { id: '', name: '전체' },
  { id: 'C001', name: 'BEAN' },
  { id: 'C002', name: 'GOODS' },
  { id: 'C003', name: 'DRINK' },
  { id: 'C004', name: 'FOOD' },
  { id: 'C005', name: 'GIFT' },
];
const STATUS_OPTIONS = [
  { value: '', label: '전체' },
  { value: '판매중', label: '판매중' },
  { value: '품절', label: '품절' },
];
const PAGE_SIZE = 10;

export default function ProductManagement() {
  const router = useRouter();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // 검색/필터/정렬/페이지네이션 상태
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchProducts()
      .then(data => {
        console.log('상품 API 응답:', data);
        setProducts(data); // 혹은 setProducts(data.content) 등
      })
      .catch(() => setError('상품 데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, []);

  // 필터/검색 적용
  const filtered = useMemo(() => products
    .filter(p => !search || p.name.toLowerCase().includes(search.toLowerCase()))
    .filter(p => !category || p.category_id === category)
    .filter(p => {
      if (!status) return true;
      if (status === '판매중') return (p.stock || 0) > 0;
      if (status === '품절') return (p.stock || 0) === 0;
      return true;
    }), [products, search, category, status]);

  // 페이지네이션
  const total = filtered.length;
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const paged = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  // shadcn DataTable columns 정의
  const columns = useMemo<ColumnDef<Product, any>[]>(() => [
    {
      accessorKey: 'image_url',
      header: '이미지',
      cell: ({ row }) => (
        <img src={row.original.image_url || '/coffee.jpeg'} alt={row.original.name} className="w-16 h-16 object-contain mx-auto" />
      ),
    },
    {
      accessorKey: 'name',
      header: '상품명',
      cell: ({ row }) => row.original.name,
    },
    {
      accessorKey: 'category_id',
      header: '카테고리',
      cell: ({ row }) => row.original.category_id || '-',
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
        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
          (row.original.stock || 0) > 0 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
        }`}>
          {(row.original.stock || 0) > 0 ? '판매중' : '품절'}
        </span>
      ),
    },
    {
      accessorKey: 'created_at',
      header: '등록일',
      cell: ({ row }) => row.original.created_at || '-',
    },
    {
      id: 'actions',
      header: '관리',
      cell: ({ row }) => (
        <div className="flex justify-center items-center">
          <button
            className="p-2 hover:bg-blue-50 rounded-full transition-colors"
            onClick={() => {
              const item = row.original;
              const params = new URLSearchParams({
                id: String(item.id || ''),
                name: item.name || '',
                description: item.description || '',
                categoryId: String(item.category_id || 'C001'),
                price: (item.price || 0).toString(),
                stock: (item.stock || 0).toString(),
                image: item.image_url || '',
                createdAt: item.created_at || '',
                updatedAt: item.updated_at || item.created_at || '',
              });
              router.push(`/admin/product/modify?${params.toString()}`);
            }}
            title="수정"
          >
            <Image
              src="/돋보기.png"
              alt="상세보기"
              width={18}
              height={18}
              className="cursor-pointer"
            />
          </button>
        </div>
      ),
    },
  ], [router]);

  return (
    <div className="w-full">
      <div className="flex flex-col md:flex-row md:items-end gap-2 mb-4">
        <div className="flex gap-2 flex-1">
          <input
            className="border rounded px-3 py-2 text-gray-900 font-semibold w-40"
            placeholder="상품명 검색"
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(1); }}
          />
          <select
            className="border rounded px-3 py-2 text-gray-900 font-semibold"
            value={category}
            onChange={e => { setCategory(e.target.value); setPage(1); }}
          >
            {CATEGORY_OPTIONS.map(opt => (
              <option key={opt.id} value={opt.id}>{opt.name}</option>
            ))}
          </select>
          <select
            className="border rounded px-3 py-2 text-gray-900 font-semibold"
            value={status}
            onChange={e => { setStatus(e.target.value); setPage(1); }}
          >
            {STATUS_OPTIONS.map(opt => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>
        <Button onClick={() => router.push('/admin/product/creation')}>상품 생성</Button>
      </div>
      {loading ? (
        <div className="text-center py-16 text-gray-500">상품 목록을 불러오는 중...</div>
      ) : error ? (
        <div className="text-center py-16 text-red-500">{error}</div>
      ) : total === 0 ? (
        <div className="text-center py-16 text-gray-500">등록된 상품이 없습니다.</div>
      ) : (
        <DataTable columns={columns} data={paged} />
      )}
      {/* 페이지네이션 */}
      {total > 0 && (
        <div className="flex justify-center items-center gap-2 mt-6">
          <Button onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>이전</Button>
          <span className="text-gray-700 font-semibold">{page} / {totalPages}</span>
          <Button onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page === totalPages}>다음</Button>
        </div>
      )}
    </div>
  );
} 