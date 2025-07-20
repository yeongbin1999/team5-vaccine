import { useEffect, useState, useCallback } from 'react';
import { apiClient } from '@/lib/backend/apiV1/client';
import type { UserResponse, UpdateUserRequest } from '@/lib/backend/apiV1/api';
import { DataTable } from '@/components/ui/data-table';
import { Button } from '@/components/ui/button';
import { useAdminAuthStore } from '@/features/auth/adminAuthStore';

const PAGE_SIZE = 10;

export default function UserManagement() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  // 모달 상태
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserResponse | null>(null);
  const [form, setForm] = useState<UpdateUserRequest>({
    name: '',
    phone: '',
    address: '',
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // 관리자 인증 상태 확인
  const { isAuthenticated, isAuthChecked, user } = useAdminAuthStore();

  // 디버깅을 위한 로그
  useEffect(() => {
    console.log('🔍 UserManagement 상태:', {
      isAuthenticated,
      isAuthChecked,
      user,
      hasToken: !!localStorage.getItem('accessToken'),
      token: localStorage.getItem('accessToken')?.substring(0, 20) + '...',
      tokenFull: localStorage.getItem('accessToken'), // 전체 토큰 확인 (개발용)
    });

    // 토큰이 있다면 JWT 디코딩 시도
    const token = localStorage.getItem('accessToken');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('🔍 JWT 토큰 정보:', {
          role: payload.role,
          email: payload.email,
          exp: new Date(payload.exp * 1000).toLocaleString(),
          iat: new Date(payload.iat * 1000).toLocaleString(),
        });
      } catch (err) {
        console.log('❌ JWT 디코딩 실패:', err);
      }
    }
  }, [isAuthenticated, isAuthChecked, user]);

  // API 연결 테스트 함수
  const testApiConnection = async () => {
    try {
      console.log('🧪 API 연결 테스트 시작');

      // 1. getAllProducts API 테스트 (이미 성공하는 것으로 확인됨)
      const res = await apiClient.api.getAllProducts();
      console.log('✅ getAllProducts API 성공:', res.data);

      // 2. fetch를 사용한 직접 테스트
      console.log('🧪 fetch를 사용한 직접 테스트 시작');
      const fetchRes = await fetch(
        '/api/v1/admin/users?pageable[page]=0&pageable[size]=10',
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem('accessToken') || ''}`,
          },
        }
      );

      if (fetchRes.ok) {
        const data = await fetchRes.json();
        console.log('✅ fetch 직접 테스트 성공:', data);
      } else {
        console.error(
          '❌ fetch 직접 테스트 실패:',
          fetchRes.status,
          fetchRes.statusText
        );
      }
    } catch (err: unknown) {
      console.error('❌ API 테스트 실패:', err);
    }
  };

  // 컴포넌트 마운트 시 API 연결 테스트
  useEffect(() => {
    if (isAuthChecked) {
      testApiConnection();
    }
  }, [isAuthChecked]);

  // 검색 디바운싱
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(1);
    }, 500);

    return () => clearTimeout(timer);
  }, [search]);

  const fetchUsers = useCallback(async () => {
    // 관리자 권한 확인
    if (!isAuthenticated || !isAuthChecked) {
      setError('관리자 권한이 필요합니다.');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      console.log('🔍 사용자 목록 조회 시작:', {
        page: page - 1,
        size: PAGE_SIZE,
        search: debouncedSearch,
        hasToken: !!localStorage.getItem('accessToken'),
        token: localStorage.getItem('accessToken')?.substring(0, 20) + '...',
        isAuthenticated,
        isAuthChecked,
        user,
      });

      // API 요청 전에 현재 설정 확인
      console.log('🔧 API 클라이언트 설정:', {
        baseURL: apiClient.instance.defaults.baseURL,
        withCredentials: apiClient.instance.defaults.withCredentials,
        headers: apiClient.instance.defaults.headers,
      });

      // API 요청 전에 실제 요청 URL 확인
      const requestConfig = {
        pageable: { page: page - 1, size: PAGE_SIZE },
        search: debouncedSearch || undefined,
      };
      console.log('🔍 요청 설정:', requestConfig);

      // axios 인스턴스의 기본 설정 확인
      console.log('🔧 axios 인스턴스 설정:', {
        baseURL: apiClient.instance.defaults.baseURL,
        timeout: apiClient.instance.defaults.timeout,
        headers: apiClient.instance.defaults.headers,
      });

      const res = await apiClient.api.getAllUsers(requestConfig);

      console.log('✅ 사용자 목록 조회 성공:', res.data);
      setUsers(res.data.content || []);
      setTotalPages(res.data.totalPages || 1);
    } catch (err: unknown) {
      console.error('❌ 사용자 목록 조회 실패:', err);

      if (err && typeof err === 'object') {
        const errorObj = err as {
          message?: string;
          code?: string;
          response?: {
            status?: number;
            statusText?: string;
            data?: unknown;
          };
          config?: {
            url?: string;
            method?: string;
            baseURL?: string;
            headers?: unknown;
          };
        };

        console.error('❌ 에러 상세:', {
          message: errorObj.message,
          code: errorObj.code,
          status: errorObj.response?.status,
          statusText: errorObj.response?.statusText,
          data: errorObj.response?.data,
          config: {
            url: errorObj.config?.url,
            method: errorObj.config?.method,
            baseURL: errorObj.config?.baseURL,
            headers: errorObj.config?.headers,
          },
        });

        if (errorObj.message === 'Network Error') {
          setError(
            '서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.'
          );
        } else if (errorObj.response?.status === 401) {
          setError('인증이 만료되었습니다. 다시 로그인해주세요.');
          // 토큰 삭제하고 로그인 페이지로 리다이렉트
          localStorage.removeItem('accessToken');
          window.location.href = '/login';
        } else if (errorObj.response?.status === 403) {
          setError('관리자 권한이 필요합니다.');
        } else if (errorObj.response?.status === 404) {
          setError('API 엔드포인트를 찾을 수 없습니다.');
        } else if (
          errorObj.response?.status &&
          errorObj.response.status >= 500
        ) {
          setError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        } else {
          setError(
            '사용자 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.'
          );
        }
      }
    } finally {
      setLoading(false);
    }
  }, [page, debouncedSearch, isAuthenticated, isAuthChecked, user]);

  useEffect(() => {
    if (isAuthChecked) {
      fetchUsers();
    }
  }, [fetchUsers, isAuthChecked]);

  // 모달 열기 (수정)
  const openEditModal = (user: UserResponse) => {
    setEditingUser(user);
    setForm({
      name: user.name || '',
      phone: user.phone || '',
      address: user.address || '',
    });
    setModalOpen(true);
    setFormError(null);
  };

  // 모달 닫기
  const closeModal = () => {
    setModalOpen(false);
    setEditingUser(null);
    setFormError(null);
  };

  // 폼 입력 핸들러
  const handleFormChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
  };

  // 사용자 정보 수정
  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingUser?.id) return;

    setFormLoading(true);
    setFormError(null);

    try {
      await apiClient.api.updateUser(editingUser.id, form);
      closeModal();
      fetchUsers(); // 목록 새로고침
    } catch (err: unknown) {
      console.error('사용자 정보 수정 실패:', err);
      if (
        err &&
        typeof err === 'object' &&
        'response' in err &&
        err.response &&
        typeof err.response === 'object' &&
        'status' in err.response &&
        err.response.status === 401
      ) {
        setFormError('권한이 없습니다.');
      } else {
        setFormError('사용자 정보 수정에 실패했습니다.');
      }
    } finally {
      setFormLoading(false);
    }
  };

  // 관리자 권한이 없으면 접근 거부
  if (isAuthChecked && !isAuthenticated) {
    return (
      <div className="w-full">
        <div className="text-center py-16">
          <div className="text-red-500 mb-4">관리자 권한이 필요합니다.</div>
          <Button
            onClick={() => (window.location.href = '/login')}
            variant="outline"
          >
            로그인 페이지로 이동
          </Button>
        </div>
      </div>
    );
  }

  const columns = [
    {
      accessorKey: 'id',
      header: 'ID',
      cell: ({ row }: { row: { original: UserResponse } }) => row.original.id,
    },
    {
      accessorKey: 'name',
      header: '이름',
      cell: ({ row }: { row: { original: UserResponse } }) => row.original.name,
    },
    {
      accessorKey: 'email',
      header: '이메일',
      cell: ({ row }: { row: { original: UserResponse } }) =>
        row.original.email,
    },
    {
      accessorKey: 'phone',
      header: '전화번호',
      cell: ({ row }: { row: { original: UserResponse } }) =>
        row.original.phone || '-',
    },
    {
      accessorKey: 'address',
      header: '주소',
      cell: ({ row }: { row: { original: UserResponse } }) =>
        row.original.address || '-',
    },
    {
      accessorKey: 'role',
      header: '권한',
      cell: ({ row }: { row: { original: UserResponse } }) => (
        <span
          className={`px-2 py-1 rounded text-xs font-medium ${
            row.original.role === 'ADMIN'
              ? 'bg-red-100 text-red-800'
              : 'bg-blue-100 text-blue-800'
          }`}
        >
          {row.original.role === 'ADMIN' ? '관리자' : '일반사용자'}
        </span>
      ),
    },
    {
      accessorKey: 'actions',
      header: '관리',
      cell: ({ row }: { row: { original: UserResponse } }) => (
        <Button
          size="sm"
          variant="outline"
          onClick={() => openEditModal(row.original)}
        >
          수정
        </Button>
      ),
    },
  ];

  return (
    <div className="w-full">
      <div className="flex flex-col md:flex-row md:items-end gap-2 mb-4">
        <div className="flex gap-2 flex-1">
          <input
            className="border rounded px-3 py-2 text-gray-900 font-semibold w-40"
            placeholder="이름/이메일 검색"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
      </div>
      {loading ? (
        <div className="text-center py-16 text-gray-500">
          사용자 목록을 불러오는 중...
        </div>
      ) : error ? (
        <div className="text-center py-16 text-red-500">{error}</div>
      ) : users.length === 0 ? (
        <div className="text-center py-16 text-gray-500">
          {debouncedSearch
            ? '검색 결과가 없습니다.'
            : '등록된 사용자가 없습니다.'}
        </div>
      ) : (
        <DataTable columns={columns} data={users} />
      )}
      {/* 페이지네이션 */}
      {users.length > 0 && totalPages > 1 && (
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

      {/* 사용자 수정 모달 */}
      {modalOpen && (
        <>
          <div className="fixed inset-0 z-40 bg-black/30" />
          <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6 relative">
              <h2 className="text-lg font-bold mb-4">사용자 정보 수정</h2>
              <form onSubmit={handleFormSubmit} className="flex flex-col gap-3">
                {/* 이름 */}
                <label className="font-semibold text-sm" htmlFor="modal-name">
                  이름
                </label>
                <input
                  id="modal-name"
                  className="border rounded px-3 py-2"
                  name="name"
                  placeholder="이름을 입력하세요"
                  value={form.name}
                  onChange={handleFormChange}
                  required
                />
                {/* 전화번호 */}
                <label className="font-semibold text-sm" htmlFor="modal-phone">
                  전화번호
                </label>
                <input
                  id="modal-phone"
                  className="border rounded px-3 py-2"
                  name="phone"
                  placeholder="전화번호를 입력하세요"
                  value={form.phone}
                  onChange={handleFormChange}
                />
                {/* 주소 */}
                <label
                  className="font-semibold text-sm"
                  htmlFor="modal-address"
                >
                  주소
                </label>
                <textarea
                  id="modal-address"
                  className="border rounded px-3 py-2"
                  name="address"
                  placeholder="주소를 입력하세요"
                  value={form.address}
                  onChange={handleFormChange}
                  rows={3}
                />
                {formError && (
                  <div className="text-red-500 text-sm">{formError}</div>
                )}
                <div className="flex gap-2 mt-2 justify-end">
                  <Button type="submit" disabled={formLoading}>
                    수정
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
