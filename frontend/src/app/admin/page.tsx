'use client';
import { useState, useEffect } from 'react';
import Image from 'next/image';
import { useAdminAuthStore } from '../../features/auth/adminAuthStore';
import ProductManagement from '@/components/admin/ProductManagement';

export default function AdminPage() {
  const isAuthenticated = useAdminAuthStore(state => state.isAuthenticated);
  const login = useAdminAuthStore(state => state.login);
  const logout = useAdminAuthStore(state => state.logout);
  const checkAuth = useAdminAuthStore(state => state.checkAuth);
  const [selectedMenu, setSelectedMenu] = useState('main');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const success = await login(username, password);
      if (!success) {
        setError('로그인에 실패했습니다.');
      }
    } catch (err) {
      setError((err as Error).message || '로그인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex h-screen">
      {/* 왼쪽: 로그인 or 메뉴 */}
      <div className="w-1/3 flex flex-col justify-center items-center bg-white">
        <div className="w-full max-w-md">
          <h1 className="text-2xl font-bold mb-8 text-gray-900 flex items-center gap-2">
            <Image
              src="/coffee.jpeg"
              alt="로고"
              width={36}
              height={36}
              className="inline-block"
            />
            Grids & Circles CAFE <span className="font-normal">(관리자용)</span>
          </h1>
          {!isAuthenticated ? (
            <form onSubmit={handleSubmit}>
              <input
                type="text"
                placeholder="아이디"
                className="w-full mb-3 px-3 py-2 border border-gray-700 rounded placeholder-gray-700 text-gray-900"
                value={username}
                onChange={e => setUsername(e.target.value)}
              />
              <div className="relative mb-3">
                <input
                  type="password"
                  placeholder="비밀번호"
                  className="w-full px-3 py-2 border border-gray-700 rounded placeholder-gray-700 text-gray-900"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                />
              </div>
              <button
                type="submit"
                className="w-full bg-black text-white py-2 rounded transition active:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                disabled={loading}
              >
                {loading ? '로그인 중...' : '로그인'}
              </button>
              {error && (
                <div className="text-red-500 mt-2 text-center">{error}</div>
              )}
            </form>
          ) : (
            <div className="flex flex-col gap-4">
              <button
                className={`w-full bg-black text-white py-2 rounded text-lg font-semibold transition active:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 ${selectedMenu === 'product' ? 'ring-2 ring-blue-500' : ''}`}
                onClick={() => setSelectedMenu('product')}
              >
                상품관리
              </button>
              <button
                className={`w-full bg-black text-white py-2 rounded text-lg font-semibold transition active:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 ${selectedMenu === 'order' ? 'ring-2 ring-blue-500' : ''}`}
                onClick={() => setSelectedMenu('order')}
              >
                주문관리
              </button>
              <button
                className={`w-full bg-black text-white py-2 rounded text-lg font-semibold transition active:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 ${selectedMenu === 'user' ? 'ring-2 ring-blue-500' : ''}`}
                onClick={() => setSelectedMenu('user')}
              >
                사용자 관리
              </button>
              <button
                className={`w-full bg-black text-white py-2 rounded text-lg font-semibold transition active:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 ${selectedMenu === 'category' ? 'ring-2 ring-blue-500' : ''}`}
                onClick={() => setSelectedMenu('category')}
              >
                카테고리 관리
              </button>
              <button
                className="w-full bg-red-500 text-white py-2 rounded text-lg font-semibold transition hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-400"
                onClick={logout}
              >
                로그아웃
              </button>
            </div>
          )}
        </div>
      </div>
      {/* 오른쪽: 고정 카페 사진 + 관리 컴포넌트 영역 */}
      <div className="w-2/3 h-full relative flex items-center justify-center">
        {/* 배경 이미지 */}
        <Image
          src="/bg.png"
          alt="카페 내부"
          layout="fill"
          objectFit="cover"
          className="z-0 opacity-40"
          priority
        />
        {/* 관리 컴포넌트 */}
        <div className="relative z-10 w-full flex justify-center items-center">
          <div className="bg-white bg-opacity-90 rounded-xl shadow-lg p-8 w-full max-w-4xl">
            {isAuthenticated && selectedMenu === 'product' && (
              <ProductManagement />
            )}
            {isAuthenticated && selectedMenu === 'order' && (
              <div>주문관리 컴포넌트</div>
            )}
            {isAuthenticated && selectedMenu === 'user' && (
              <div>사용자 관리 컴포넌트</div>
            )}
            {isAuthenticated && selectedMenu === 'category' && (
              <div>카테고리 관리 컴포넌트</div>
            )}
            {(!isAuthenticated || selectedMenu === 'main') && (
              <div className="w-full h-full" />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
