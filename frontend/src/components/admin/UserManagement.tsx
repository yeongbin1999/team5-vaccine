import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/backend/apiV1/client';
import type { UserResponse } from '@/lib/backend/apiV1/api';
import { DataTable } from '@/components/ui/data-table';
import { Button } from '@/components/ui/button';

const PAGE_SIZE = 10;

export default function UserManagement() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.api.getAllUsers({
        pageable: { page: page - 1, size: PAGE_SIZE },
        search: search || undefined,
      });
      setUsers(res.data.content || []);
      setTotalPages(res.data.totalPages || 1);
    } catch (err) {
      setError('사용자 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, search]);

  const columns = [
    {
      accessorKey: 'id',
      header: 'ID',
      cell: ({ row }: any) => row.original.id,
    },
    {
      accessorKey: 'name',
      header: '이름',
      cell: ({ row }: any) => row.original.name,
    },
    {
      accessorKey: 'email',
      header: '이메일',
      cell: ({ row }: any) => row.original.email,
    },
    {
      accessorKey: 'phone',
      header: '전화번호',
      cell: ({ row }: any) => row.original.phone || '-',
    },
    {
      accessorKey: 'address',
      header: '주소',
      cell: ({ row }: any) => row.original.address || '-',
    },
    {
      accessorKey: 'role',
      header: '권한',
      cell: ({ row }: any) => row.original.role,
    },
  ];

  return (
    <div className="w-full">
      <div className="flex flex-col md:flex-row md:items-end gap-2 mb-4">
        <input
          className="border rounded px-3 py-2 text-gray-900 font-semibold w-40"
          placeholder="이름/이메일 검색"
          value={search}
          onChange={e => {
            setSearch(e.target.value);
            setPage(1);
          }}
        />
      </div>
      {loading ? (
        <div className="text-center py-16 text-gray-500">사용자 목록을 불러오는 중...</div>
      ) : error ? (
        <div className="text-center py-16 text-red-500">{error}</div>
      ) : users.length === 0 ? (
        <div className="text-center py-16 text-gray-500">등록된 사용자가 없습니다.</div>
      ) : (
        <DataTable columns={columns} data={users} />
      )}
      {/* 페이지네이션 */}
      {users.length > 0 && (
        <div className="flex justify-center items-center gap-2 mt-6">
          <Button onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>
            이전
          </Button>
          <span className="text-gray-700 font-semibold">
            {page} / {totalPages}
          </span>
          <Button onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page === totalPages}>
            다음
          </Button>
        </div>
      )}
    </div>
  );
} 