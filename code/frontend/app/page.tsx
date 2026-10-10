"use client";

import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { isStaff } from "@/lib/auth";
import { Button, Card } from "@/components/ui";

const STEPS = [
  { title: "ค้นหาห้องว่าง", text: "เลือกวัน เวลา จำนวนคน และอุปกรณ์ที่ต้องใช้" },
  { title: "ส่งคำขอจอง", text: "ระบบตรวจกฎตามบทบาทและเวลาชนให้อัตโนมัติ" },
  { title: "รับแจ้งเตือนและ check-in", text: "เมื่ออนุมัติแล้ว check-in ได้ 15 นาทีก่อนถึงหลังเวลาเริ่ม" },
];

export default function HomePage() {
  const { user } = useAuth();

  return (
    <div className="space-y-10">
      <section className="rounded-2xl bg-gradient-to-br from-indigo-600 to-indigo-800 px-8 py-12 text-white">
        <p className="text-sm text-indigo-200">อาคารวิทยวิภาส (SC09) · วิทยาลัยการคอมพิวเตอร์ มข.</p>
        <h1 className="mt-2 text-3xl font-semibold sm:text-4xl">จองห้องเรียน ห้องแล็บ และห้องประชุม</h1>
        <p className="mt-3 max-w-xl text-indigo-100">
          ค้นหาห้องว่างตามวัน เวลา ความจุ และอุปกรณ์ ส่งคำขอจอง แล้วติดตามสถานะได้ในที่เดียว
        </p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link href="/rooms">
            <Button variant="inverse">ค้นหาห้อง</Button>
          </Link>
          {user ? (
            <Link href={isStaff(user) ? "/admin/approvals" : "/bookings"}>
              <Button variant="secondary">{isStaff(user) ? "คิวรออนุมัติ" : "การจองของฉัน"}</Button>
            </Link>
          ) : (
            <Link href="/login">
              <Button variant="secondary">เข้าสู่ระบบ</Button>
            </Link>
          )}
        </div>
      </section>
      <section className="grid gap-4 sm:grid-cols-3">
        {STEPS.map((step, index) => (
          <Card key={step.title}>
            <span className="text-sm font-semibold text-indigo-600">ขั้นที่ {index + 1}</span>
            <h2 className="mt-1 font-semibold text-slate-900">{step.title}</h2>
            <p className="mt-1 text-sm text-slate-500">{step.text}</p>
          </Card>
        ))}
      </section>
    </div>
  );
}
