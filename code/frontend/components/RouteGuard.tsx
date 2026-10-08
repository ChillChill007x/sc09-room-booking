"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { Role } from "@/lib/auth";
import { Alert, Spinner } from "./ui";

/**
 * ครอบหน้าที่ต้อง login ก่อน ถ้าระบุ roles จะตรวจ role ด้วย
 * การตรวจสิทธิ์จริงอยู่ที่ backend ส่วนนี้ช่วยให้ผู้ใช้ไม่เห็นหน้าที่ใช้ไม่ได้
 */
export function RequireAuth({ roles, children }: { roles?: Role[]; children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!loading && !user) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
    }
  }, [loading, user, router, pathname]);

  if (loading || !user) {
    return <Spinner label="กำลังตรวจสอบการเข้าสู่ระบบ" />;
  }
  if (roles && !roles.includes(user.role)) {
    return <Alert tone="error">ไม่มีสิทธิ์เข้าถึงหน้านี้</Alert>;
  }
  return <>{children}</>;
}
