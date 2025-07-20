import { RouteGuard } from '@/components/auth/RouteGuard';

export default function ProfileLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <RouteGuard requiredRole="USER">{children}</RouteGuard>;
}
