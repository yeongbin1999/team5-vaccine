'use client';

import { useState } from 'react';
import { useAuthStore } from '@/features/auth/authStore';
import { apiClient } from '@/lib/backend/apiV1/client';
import { toast } from 'sonner';

export function ProfilePage() {
  const { user, updateUser } = useAuthStore();
  const [editMode, setEditMode] = useState(false);
  const [showPwModal, setShowPwModal] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [pwForm, setPwForm] = useState({
    current: '',
    newPw: '',
    confirm: '',
  });
  const [pwError, setPwError] = useState('');

  const [form, setForm] = useState({
    name: user?.name || '',
    phone: user?.phone || '',
    address: user?.address || '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    if (!user) return;

    // 폼 유효성 검사
    if (!form.name.trim()) {
      toast.error('이름을 입력해주세요.');
      return;
    }

    if (form.phone && !/^[0-9-]+$/.test(form.phone)) {
      toast.error('전화번호 형식이 올바르지 않습니다.');
      return;
    }

    setIsUpdating(true);
    try {
      console.log('🔧 회원정보 수정 시작:', {
        name: form.name.trim(),
        phone: form.phone.trim() || undefined,
        address: form.address.trim() || undefined,
      });

      // API 호출하여 사용자 정보 업데이트
      const response = await apiClient.api.updateMe({
        name: form.name.trim(),
        phone: form.phone.trim() || undefined,
        address: form.address.trim() || undefined,
      });

      console.log('✅ 회원정보 수정 성공:', response.data);

      // 로컬 상태 업데이트
      updateUser({
        ...user,
        name: form.name.trim(),
        phone: form.phone.trim() || undefined,
        address: form.address.trim() || undefined,
      });

      setEditMode(false);
      toast.success('회원정보가 성공적으로 수정되었습니다.');
    } catch (error: unknown) {
      // 사용자에게는 친화적인 메시지만 표시
      const errorResponse = error as { response?: { status?: number } };
      if (errorResponse.response?.status === 400) {
        toast.error('입력 정보를 확인해주세요.');
      } else if (errorResponse.response?.status === 401) {
        toast.error('다시 로그인해주세요.');
      } else if (errorResponse.response?.status === 500) {
        toast.error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      } else {
        toast.error('회원정보 수정에 실패했습니다. 다시 시도해주세요.');
      }
    } finally {
      setIsUpdating(false);
    }
  };

  const handleCancel = () => {
    setForm({
      name: user?.name || '',
      phone: user?.phone || '',
      address: user?.address || '',
    });
    setEditMode(false);
  };

  // 비밀번호 변경 모달 저장
  const handlePwSave = async () => {
    if (!pwForm.current || !pwForm.newPw || !pwForm.confirm) {
      setPwError('모든 필드를 입력해주세요.');
      return;
    }

    if (pwForm.newPw !== pwForm.confirm) {
      setPwError('새 비밀번호가 일치하지 않습니다.');
      return;
    }

    if (pwForm.newPw.length < 8) {
      setPwError('새 비밀번호는 8자 이상이어야 합니다.');
      return;
    }

    setIsChangingPassword(true);
    try {
      // API 호출하여 비밀번호 변경
      await apiClient.api.changePassword({
        currentPassword: pwForm.current,
        newPassword: pwForm.newPw,
      });

      setShowPwModal(false);
      setPwForm({ current: '', newPw: '', confirm: '' });
      setPwError('');
      toast.success('비밀번호가 성공적으로 변경되었습니다.');
    } catch (error: unknown) {
      // 사용자에게는 친화적인 메시지만 표시
      const errorResponse = error as { response?: { status?: number } };
      if (errorResponse.response?.status === 400) {
        setPwError('현재 비밀번호를 확인해주세요.');
      } else if (errorResponse.response?.status === 401) {
        setPwError('다시 로그인해주세요.');
      } else if (errorResponse.response?.status === 500) {
        setPwError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      } else {
        setPwError('비밀번호 변경에 실패했습니다. 다시 시도해주세요.');
      }
    } finally {
      setIsChangingPassword(false);
    }
  };

  const renderPasswordModal = () => {
    if (!showPwModal) return null;

    return (
      <div
        className="fixed inset-0 z-50 flex items-center justify-center"
        style={{ background: 'rgba(0,0,0,0.24)' }}
      >
        <div className="bg-white rounded-xl shadow-lg p-8 w-full max-w-xs flex flex-col gap-4">
          <h3 className="text-lg font-bold mb-2">비밀번호 변경</h3>
          <input
            type="password"
            name="current"
            value={pwForm.current}
            onChange={e => setPwForm(f => ({ ...f, current: e.target.value }))}
            placeholder="기존 비밀번호"
            className="border rounded px-3 py-2"
          />
          <input
            type="password"
            name="newPw"
            value={pwForm.newPw}
            onChange={e => setPwForm(f => ({ ...f, newPw: e.target.value }))}
            placeholder="새 비밀번호"
            className="border rounded px-3 py-2"
          />
          <input
            type="password"
            name="confirm"
            value={pwForm.confirm}
            onChange={e => setPwForm(f => ({ ...f, confirm: e.target.value }))}
            placeholder="비밀번호 확인"
            className="border rounded px-3 py-2"
          />
          {renderPasswordError()}
          <div className="flex justify-end gap-2 mt-4">
            <button
              className="bg-amber-200 text-amber-900 px-3 py-2 rounded hover:bg-amber-300 text-sm font-semibold disabled:bg-gray-300 disabled:cursor-not-allowed"
              onClick={handlePwSave}
              disabled={
                !pwForm.current ||
                !pwForm.newPw ||
                !pwForm.confirm ||
                !!pwError ||
                isChangingPassword
              }
            >
              {isChangingPassword ? '변경 중...' : '변경'}
            </button>
            <button
              className="bg-gray-200 text-gray-700 px-3 py-2 rounded hover:bg-gray-300 text-sm font-semibold"
              onClick={() => {
                setShowPwModal(false);
                setPwForm({ current: '', newPw: '', confirm: '' });
                setPwError('');
              }}
              disabled={isChangingPassword}
            >
              취소
            </button>
          </div>
        </div>
      </div>
    );
  };

  const renderPasswordError = () => {
    if (!pwError) return null;

    return <span className="text-red-500 text-xs mt-1">{pwError}</span>;
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh]">
      <div className="bg-white rounded-2xl shadow-2xl p-8 min-w-[500px] max-w-2xl w-full">
        <h2 className="text-2xl font-bold mb-8 text-center text-gray-900">
          내 정보
        </h2>
        <div className="space-y-5 mb-12">
          {/* 이메일(아이디) - 항상 읽기 전용 */}
          <div className="flex items-center justify-between gap-4">
            <label className="text-gray-700 font-medium w-24">
              이메일(아이디)
            </label>
            <input
              name="email"
              value={user?.email || ''}
              className="flex-1 border border-gray-200 rounded px-3 py-2 bg-gray-100 text-gray-500 cursor-default"
              type="email"
              readOnly
              disabled
            />
          </div>
          {/* 이름 */}
          <div className="flex items-center justify-between gap-4">
            <label className="text-gray-700 font-medium w-24">이름</label>
            <input
              name="name"
              value={form.name}
              onChange={handleChange}
              className={`flex-1 border border-gray-300 rounded px-3 py-2 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 outline-none transition placeholder:text-gray-400 bg-gray-50 ${!editMode ? 'text-gray-500 cursor-default' : 'text-gray-900'}`}
              type="text"
              readOnly={!editMode}
              disabled={!editMode}
            />
          </div>
          {/* 전화번호 */}
          <div className="flex items-center justify-between gap-4">
            <label className="text-gray-700 font-medium w-24">전화번호</label>
            <input
              name="phone"
              value={form.phone}
              onChange={handleChange}
              className={`flex-1 border border-gray-300 rounded px-3 py-2 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 outline-none transition placeholder:text-gray-400 bg-gray-50 ${!editMode ? 'text-gray-500 cursor-default' : 'text-gray-900'}`}
              type="tel"
              placeholder="010-1234-5678"
              readOnly={!editMode}
              disabled={!editMode}
            />
          </div>
          {/* 기본 배송지 */}
          <div className="flex items-center justify-between gap-4">
            <label className="text-gray-700 font-medium w-24">
              기본 배송지
            </label>
            <input
              name="address"
              value={form.address}
              onChange={handleChange}
              className={`flex-1 border border-gray-300 rounded px-3 py-2 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 outline-none transition placeholder:text-gray-400 bg-gray-50 ${!editMode ? 'text-gray-500 cursor-default' : 'text-gray-900'}`}
              type="text"
              placeholder="기본 배송지를 입력하세요"
              readOnly={!editMode}
              disabled={!editMode}
            />
          </div>
        </div>
        {/* 비밀번호 변경 버튼과 회원정보 수정 버튼을 같은 줄에 오른쪽 정렬로 */}
        <div className="flex justify-end gap-2 mt-6 mb-2">
          <button
            type="button"
            onClick={() => setShowPwModal(true)}
            className="bg-gray-200 text-gray-700 px-4 py-2 rounded hover:bg-gray-300 text-sm font-semibold shadow-sm transition"
          >
            비밀번호 변경
          </button>
          {editMode ? (
            <>
              <button
                onClick={handleSave}
                className="bg-amber-200 text-amber-900 px-4 py-2 rounded hover:bg-amber-300 text-sm font-semibold shadow-sm transition disabled:bg-gray-300 disabled:cursor-not-allowed"
                disabled={isUpdating}
              >
                {isUpdating ? '저장 중...' : '저장'}
              </button>
              <button
                onClick={handleCancel}
                className="bg-gray-200 text-gray-700 px-4 py-2 rounded hover:bg-gray-300 text-sm font-semibold shadow-sm transition"
                disabled={isUpdating}
              >
                취소
              </button>
            </>
          ) : (
            <button
              onClick={() => setEditMode(true)}
              className="bg-amber-200 text-amber-900 px-4 py-2 rounded hover:bg-amber-300 text-sm font-semibold shadow-sm transition"
            >
              회원정보 수정
            </button>
          )}
        </div>
        {/* 비밀번호 변경 모달 */}
        {renderPasswordModal()}
      </div>
    </div>
  );
}
