'use client';

import { ProfilePage } from '@/features/auth/components/ProfilePage';

export default function ProfilePageWrapper() {
  return (
    <div className="flex flex-1 h-full">
      {/* 좌측: 프로필 폼 */}
      <div className="w-full lg:w-1/2 bg-white flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          <ProfilePage />
        </div>
      </div>

      {/* 우측: 카페 배경 이미지 */}
      <div className="hidden lg:block lg:w-1/2 relative">
        <div
          className="absolute inset-0 bg-cover bg-center bg-no-repeat"
          style={{ backgroundImage: "url('/bg.png')" }}
        />
      </div>
    </div>
  );
}
