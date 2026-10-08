"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { NOTIFICATIONS_CHANGED, notificationApi } from "@/lib/notifications";

const POLL_MS = 30_000;

/** กระดิ่งแจ้งเตือนพร้อมจำนวนที่ยังไม่อ่าน โหลดใหม่ทุก 30 วินาที */
export function NotificationBell() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    let cancelled = false;
    const refresh = () =>
      notificationApi
        .unreadCount()
        .then((result) => !cancelled && setCount(result.count))
        .catch(() => undefined);
    refresh();
    const timer = window.setInterval(refresh, POLL_MS);
    window.addEventListener(NOTIFICATIONS_CHANGED, refresh);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
      window.removeEventListener(NOTIFICATIONS_CHANGED, refresh);
    };
  }, []);

  return (
    <Link href="/notifications" className="relative rounded-full p-2 text-slate-600 hover:bg-slate-100" aria-label="แจ้งเตือน">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
        <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
      {count > 0 && (
        <span className="absolute -right-0.5 -top-0.5 min-w-5 rounded-full bg-rose-600 px-1 text-center text-[10px] font-semibold leading-5 text-white">
          {count > 99 ? "99+" : count}
        </span>
      )}
    </Link>
  );
}
