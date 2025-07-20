'use client';

import Link from 'next/link';
import { Shield, Home, ArrowLeft } from 'lucide-react';

export default function AccessDeniedPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8 text-center">
        <div className="mb-6">
          <div className="mx-auto w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mb-4">
            <Shield className="w-8 h-8 text-red-600" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">
            접근 권한이 없습니다
          </h1>
          <p className="text-gray-600 mb-6">
            해당 페이지에 접근할 수 있는 권한이 없습니다.
            <br />
            관리자에게 문의하시거나 홈으로 돌아가세요.
          </p>
        </div>

        <div className="space-y-3">
          <Link
            href="/"
            className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
          >
            <Home className="w-4 h-4" />
            홈으로 돌아가기
          </Link>

          <button
            onClick={() => window.history.back()}
            className="w-full bg-gray-200 text-gray-700 py-3 px-4 rounded-lg hover:bg-gray-300 transition-colors flex items-center justify-center gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            이전 페이지로
          </button>
        </div>
      </div>
    </div>
  );
}
