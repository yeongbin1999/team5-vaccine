'use client';
import { usePathname } from 'next/navigation';
import { Header } from './Header';
import { Footer } from './Footer';

export default function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isAdmin = pathname.startsWith('/admin');
  return (
    <>
      {!isAdmin && <Header />}
      <main className={!isAdmin ? 'pt-16 flex-1 flex flex-col' : ''}>
        {children}
      </main>
      {!isAdmin && <Footer />}
    </>
  );
}
