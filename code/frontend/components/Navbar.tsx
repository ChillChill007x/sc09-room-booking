"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { isStaff, ROLE_LABELS } from "@/lib/auth";
import { NotificationBell } from "./NotificationBell";
import { Button } from "./ui";

interface NavLink {
  href: string;
  label: string;
}

const USER_LINKS: NavLink[] = [
  { href: "/rooms", label: "ห้อง" },
  { href: "/schedule", label: "ตารางห้อง" },
  { href: "/bookings", label: "การจองของฉัน" },
];

const HELP_LINK: NavLink = { href: "/help", label: "คู่มือ" };

const STAFF_LINKS: NavLink[] = [
  { href: "/admin/dashboard", label: "แดชบอร์ด" },
  { href: "/admin/approvals", label: "คิวอนุมัติ" },
  { href: "/admin/rooms", label: "จัดการห้อง" },
  { href: "/admin/equipment", label: "อุปกรณ์" },
  { href: "/admin/users", label: "ผู้ใช้" },
];

export function Navbar() {
  const { user, loading, logout } = useAuth();
  const pathname = usePathname();
  const [open, setOpen] = useState(false);

  const links = user
    ? [...USER_LINKS, ...(isStaff(user) ? STAFF_LINKS : []), HELP_LINK]
    : [{ href: "/rooms", label: "ห้อง" }, HELP_LINK];
  const linkClass = (href: string) =>
    `rounded-md px-3 py-2 text-sm ${
      pathname === href || pathname.startsWith(`${href}/`)
        ? "bg-indigo-50 font-medium text-indigo-700"
        : "text-slate-600 hover:bg-slate-100"
    }`;

  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
        <Link href="/" className="flex items-center gap-2 font-semibold text-slate-900">
          <span className="rounded-md bg-indigo-600 px-2 py-1 text-xs font-bold text-white">SC09</span>
          <span className="hidden sm:inline">ระบบจองห้อง</span>
        </Link>
        <nav className="hidden flex-1 flex-wrap gap-1 md:flex">
          {links.map((link) => (
            <Link key={link.href} href={link.href} className={linkClass(link.href)}>
              {link.label}
            </Link>
          ))}
        </nav>
        <div className="flex items-center gap-2">
          {!loading && user && (
            <>
              <NotificationBell />
              <Link href="/profile" className="hidden text-right text-xs leading-tight sm:block">
                <span className="block font-medium text-slate-800">{user.fullName ?? user.email}</span>
                <span className="text-slate-500">{ROLE_LABELS[user.role]}</span>
              </Link>
              <Button variant="secondary" onClick={logout}>
                ออกจากระบบ
              </Button>
            </>
          )}
          {!loading && !user && (
            <Link href="/login">
              <Button>เข้าสู่ระบบ</Button>
            </Link>
          )}
          <button className="rounded-md p-2 text-slate-600 md:hidden" onClick={() => setOpen(!open)} aria-label="เมนู">
            ☰
          </button>
        </div>
      </div>
      {open && (
        <nav className="flex flex-col gap-1 border-t border-slate-100 px-4 py-2 md:hidden">
          {links.map((link) => (
            <Link key={link.href} href={link.href} className={linkClass(link.href)} onClick={() => setOpen(false)}>
              {link.label}
            </Link>
          ))}
        </nav>
      )}
    </header>
  );
}
