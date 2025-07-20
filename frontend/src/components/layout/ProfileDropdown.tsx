'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { User, LogIn, UserPlus, LogOut } from 'lucide-react';
import {
  Dropdown,
  DropdownItem,
  DropdownSeparator,
} from '@/components/ui/dropdown';
import { Tooltip } from '@/components/ui/tooltip';
import { useAuthStore } from '@/features/auth/authStore';
import { toast } from 'sonner';

export function ProfileDropdown() {
  const { user, isAuthenticated, logout } = useAuthStore();
  const router = useRouter();

  if (!isAuthenticated) {
    return (
      <Dropdown trigger={<User className="w-5 h-5" />} className="bg-white">
        {() => (
          <>
            <DropdownItem
              onClick={(e, close) => {
                if (close) close();
                setTimeout(() => router.push('/login'), 0);
              }}
            >
              <div className="flex items-center space-x-2">
                <LogIn className="w-4 h-4" />
                <span>로그인</span>
              </div>
            </DropdownItem>
            <DropdownItem
              onClick={(e, close) => {
                if (close) close();
                setTimeout(() => router.push('/signup'), 0);
              }}
            >
              <div className="flex items-center space-x-2">
                <UserPlus className="w-4 h-4" />
                <span>회원가입</span>
              </div>
            </DropdownItem>
          </>
        )}
      </Dropdown>
    );
  }

  return (
    <Dropdown trigger={<User className="w-5 h-5" />} className="bg-white">
      {() => (
        <>
          <div className="px-4 py-2 border-b border-gray-200">
            <Tooltip content={user?.name || ''}>
              <div className="text-sm font-medium text-gray-900 truncate overflow-hidden text-ellipsis whitespace-nowrap">
                {user?.name}
              </div>
            </Tooltip>
            <Tooltip content={user?.email || ''}>
              <div className="text-xs text-gray-500 truncate overflow-hidden text-ellipsis whitespace-nowrap">
                {user?.email}
              </div>
            </Tooltip>
          </div>
          <DropdownItem
            onClick={(e, close) => {
              if (close) close();
              setTimeout(() => router.push('/profile'), 0);
            }}
          >
            <div className="flex items-center space-x-2">
              <User className="w-4 h-4" />
              <span>내 정보</span>
            </div>
          </DropdownItem>
          <DropdownSeparator />
          <DropdownItem
            onClick={async (e, close) => {
              await logout();
              toast('로그아웃 되었습니다');
              if (close) close();
            }}
          >
            <div className="flex items-center space-x-2 text-red-600">
              <LogOut className="w-4 h-4" />
              <span>로그아웃</span>
            </div>
          </DropdownItem>
        </>
      )}
    </Dropdown>
  );
}
