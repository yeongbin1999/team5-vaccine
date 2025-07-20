"use client";
import { usePathname } from "next/navigation";
import { AuthInitializer } from "./AuthInitializer";

export function ClientOnlyAuthInit() {
  const pathname = usePathname();
  const isAdmin = pathname.startsWith("/admin");

  if (isAdmin) return null;
  return <AuthInitializer />;
} 