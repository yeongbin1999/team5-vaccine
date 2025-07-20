'use client';

import { LoginForm } from '@/features/auth/components/LoginForm';
import { AuthGuard } from '@/components/auth/AuthGuard';

export default function LoginPage() {
  return (
    <AuthGuard>
      <div className="flex flex-1 items-center justify-center bg-gray-50">
        <LoginForm />
      </div>
    </AuthGuard>
  );
}
