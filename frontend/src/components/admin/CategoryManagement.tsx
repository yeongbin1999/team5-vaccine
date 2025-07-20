import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/backend/apiV1/client';
import type {
  CategoryResponseDto,
  CategoryRequestDto,
} from '@/lib/backend/apiV1/api';
import { DataTable } from '@/components/ui/data-table';
import { Button } from '@/components/ui/button';

export default function CategoryManagement() {
  const [categories, setCategories] = useState<CategoryResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] =
    useState<CategoryResponseDto | null>(null);
  const [form, setForm] = useState<{ name: string; parentId?: number }>({
    name: '',
    parentId: undefined,
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const PAGE_SIZE = 10;
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const fetchCategories = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.api.getAllCategories();
      setCategories(res.data);
      setTotalPages(Math.max(1, Math.ceil((res.data.length || 0) / PAGE_SIZE)));
    } catch {
      setError('카테고리 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const openCreateModal = () => {
    setEditingCategory(null);
    setForm({ name: '', parentId: undefined });
    setModalOpen(true);
    setFormError(null);
  };
  const openEditModal = (cat: CategoryResponseDto) => {
    setEditingCategory(cat);
    setForm({ name: cat.name || '', parentId: cat.parentId });
    setModalOpen(true);
    setFormError(null);
  };
  const closeModal = () => {
    setModalOpen(false);
    setEditingCategory(null);
    setFormError(null);
  };
  const handleFormChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setForm(f => ({
      ...f,
      [name]: name === 'parentId' ? (value ? Number(value) : undefined) : value,
    }));
  };
  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError(null);
    try {
      const payload: CategoryRequestDto = {
        name: form.name,
        parentId: form.parentId,
      };
      if (editingCategory) {
        await apiClient.api.updateCategory(editingCategory.id!, payload);
      } else {
        await apiClient.api.createCategory(payload);
      }
      await fetchCategories();
      closeModal();
    } catch {
      setFormError('저장에 실패했습니다.');
    } finally {
      setFormLoading(false);
    }
  };
  const handleDelete = async (cat: CategoryResponseDto) => {
    if (!window.confirm(`정말 "${cat.name}" 카테고리를 삭제하시겠습니까?`))
      return;
    try {
      await apiClient.api.deleteCategory(cat.id!);
      await fetchCategories();
    } catch (err: unknown) {
      // 409 Conflict 처리
      if (
        err &&
        typeof err === 'object' &&
        'response' in err &&
        err.response &&
        typeof err.response === 'object' &&
        'status' in err.response &&
        err.response.status === 409
      ) {
        alert('이 카테고리는 다른 데이터와 연관되어 있어 삭제할 수 없습니다.');
        return;
      }

      alert('삭제에 실패했습니다.');
    }
  };

  const columns = [
    {
      accessorKey: 'id',
      header: 'ID',
      cell: ({ row }: { row: { original: CategoryResponseDto } }) =>
        row.original.id,
    },
    {
      accessorKey: 'name',
      header: '이름',
      cell: ({ row }: { row: { original: CategoryResponseDto } }) =>
        row.original.name,
    },
    {
      accessorKey: 'parentId',
      header: '상위카테고리',
      cell: ({ row }: { row: { original: CategoryResponseDto } }) => {
        const parent = categories.find(c => c.id === row.original.parentId);
        return parent ? parent.name : '-';
      },
    },
    {
      id: 'actions',
      header: '관리',
      cell: ({ row }: { row: { original: CategoryResponseDto } }) => (
        <div className="flex gap-2 justify-center">
          <Button size="sm" onClick={() => openEditModal(row.original)}>
            수정
          </Button>
          <Button
            size="sm"
            variant="destructive"
            onClick={() => handleDelete(row.original)}
          >
            삭제
          </Button>
        </div>
      ),
    },
  ];

  // 페이지네이션 적용된 데이터
  const paged = categories.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  return (
    <div className="w-full">
      <div className="flex justify-between items-end mb-4">
        <div className="font-bold text-lg">카테고리 관리</div>
        <Button onClick={openCreateModal}>카테고리 생성</Button>
      </div>
      {loading ? (
        <div className="text-center py-16 text-gray-500">
          카테고리 목록을 불러오는 중...
        </div>
      ) : error ? (
        <div className="text-center py-16 text-red-500">{error}</div>
      ) : categories.length === 0 ? (
        <div className="text-center py-16 text-gray-500">
          등록된 카테고리가 없습니다.
        </div>
      ) : (
        <DataTable columns={columns} data={paged} />
      )}
      {/* 페이지네이션 */}
      {categories.length > 0 && (
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
      {/* 생성/수정 모달 */}
      {modalOpen && (
        <>
          <div className="fixed inset-0 z-40 bg-black/30" />
          <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6 relative">
              <h2 className="text-lg font-bold mb-4">
                {editingCategory ? '카테고리 수정' : '카테고리 생성'}
              </h2>
              <form onSubmit={handleFormSubmit} className="flex flex-col gap-3">
                <label className="font-semibold text-sm" htmlFor="modal-name">
                  이름
                </label>
                <input
                  id="modal-name"
                  className="border rounded px-3 py-2"
                  name="name"
                  placeholder="카테고리명을 입력하세요"
                  value={form.name}
                  onChange={handleFormChange}
                  required
                />
                <label className="font-semibold text-sm" htmlFor="modal-parent">
                  상위카테고리
                </label>
                <select
                  id="modal-parent"
                  className="border rounded px-3 py-2"
                  name="parentId"
                  value={form.parentId || ''}
                  onChange={handleFormChange}
                >
                  <option value="">없음</option>
                  {categories
                    .filter(
                      c => !editingCategory || c.id !== editingCategory.id
                    )
                    .map(opt => (
                      <option key={opt.id} value={opt.id}>
                        {opt.name}
                      </option>
                    ))}
                </select>
                {formError && (
                  <div className="text-red-500 text-sm">{formError}</div>
                )}
                <div className="flex gap-2 mt-2 justify-end">
                  <Button type="submit" disabled={formLoading}>
                    {editingCategory ? '수정' : '생성'}
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
        </>
      )}
    </div>
  );
}
