import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import './globals.css';
import { QueryProvider } from '@/components/providers/QueryProvider';
import { Toaster } from '@/components/ui/sonner';
import AppShell from '@/components/layout/AppShell';
import { ClientOnlyAuthInit } from '@/components/auth/ClientOnlyAuthInit';

const geistSans = Geist({ variable: '--font-geist-sans', subsets: ['latin'] });
const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  title: 'Grids & Circles',
  description: '엄선된 원두로 만든 프리미엄 커피를 편리하게 주문하세요',
  icons: { icon: '/coffee.jpeg' },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased flex flex-col min-h-screen`}
      >
        <Toaster
          visibleToasts={1}
          toastOptions={{
            className: [
              'flex items-center justify-center text-center',
              'text-base font-bold py-3 px-6 rounded-2xl shadow-2xl z-[99999]',
              'bg-black/90 text-white border-4 border-yellow-400',
            ].join(' '),
            duration: 1500,
          }}
          style={{
            position: 'fixed',
            left: '50%',
            top: '10%',
            transform: 'translate(-50%, -50%)',
            pointerEvents: 'none',
          }}
        />
        <QueryProvider>
          <ClientOnlyAuthInit />
          <AppShell>{children}</AppShell>
        </QueryProvider>
      </body>
    </html>
  );
}
