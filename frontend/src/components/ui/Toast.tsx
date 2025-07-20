'use client';
import { createContext, useContext, useState, useCallback } from 'react';

const ToastContext = createContext<{
  show: (message: string, duration?: number) => void;
} | null>(null);

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toast, setToast] = useState<{ message: string; visible: boolean }>({
    message: '',
    visible: false,
  });
  const show = useCallback((message: string, duration = 2000) => {
    setToast({ message, visible: true });
    setTimeout(() => setToast({ message: '', visible: false }), duration);
  }, []);

  const renderToast = () => {
    if (!toast.visible) return null;

    return (
      <div className="fixed bottom-6 right-6 z-[9999] bg-gray-900 text-white px-4 py-2 rounded shadow-lg animate-fade-in">
        {toast.message}
      </div>
    );
  };

  return (
    <ToastContext.Provider value={{ show }}>
      {children}
      {renderToast()}
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within a ToastProvider');
  return ctx;
}
