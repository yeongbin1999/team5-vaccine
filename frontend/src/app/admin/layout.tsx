import { RouteGuard } from '@/components/auth/RouteGuard';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <RouteGuard requiredRole="ADMIN">{children}</RouteGuard>;
}
