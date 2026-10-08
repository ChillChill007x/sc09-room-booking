"use client";

import { FormEvent, useEffect, useState } from "react";
import { errorMessage } from "@/lib/api";
import { STAFF_ROLES } from "@/lib/auth";
import { BOOKING_STATUS_LABELS, BookingStatus } from "@/lib/bookings";
import { toDateInput } from "@/lib/format";
import { RoomUsage, statsApi, StatsSummary } from "@/lib/stats";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Button, Card, EmptyState, Field, Input, PageHeader, Spinner } from "@/components/ui";

export default function DashboardPage() {
  return (
    <RequireAuth roles={STAFF_ROLES}>
      <PageHeader title="แดชบอร์ด" subtitle="ภาพรวมการจองและการใช้ห้อง" />
      <Summary />
      <Usage />
    </RequireAuth>
  );
}

function Summary() {
  const [summary, setSummary] = useState<StatsSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    statsApi.summary().then(setSummary).catch((err) => setError(errorMessage(err)));
  }, []);

  if (error) return <Alert tone="error">{error}</Alert>;
  if (!summary) return <Spinner />;

  const tiles = [
    { label: "การจองทั้งหมด", value: summary.totalBookings },
    { label: "การจองวันนี้", value: summary.todayBookings },
    { label: "รออนุมัติ", value: summary.pendingApprovals },
    { label: "จำนวนห้อง", value: summary.totalRooms },
    { label: "ผู้ใช้", value: summary.totalUsers },
  ];
  const max = Math.max(1, ...Object.values(summary.bookingsByStatus));

  return (
    <>
      <div className="grid gap-4 sm:grid-cols-3 lg:grid-cols-5">
        {tiles.map((tile) => (
          <Card key={tile.label}>
            <p className="text-xs text-slate-500">{tile.label}</p>
            <p className="mt-1 text-2xl font-semibold text-slate-900">{tile.value.toLocaleString("th-TH")}</p>
          </Card>
        ))}
      </div>
      <Card className="mt-4">
        <h2 className="mb-4 font-semibold text-slate-900">จำนวนการจองแยกตามสถานะ</h2>
        <div className="space-y-2">
          {(Object.entries(summary.bookingsByStatus) as [BookingStatus, number][]).map(([status, count]) => (
            <div key={status} className="grid grid-cols-[8rem_1fr_3rem] items-center gap-3 text-sm">
              <span className="text-slate-600">{BOOKING_STATUS_LABELS[status]}</span>
              <div className="h-3 rounded-full bg-slate-100">
                <div className="h-3 rounded-full bg-indigo-500" style={{ width: `${(count / max) * 100}%` }} />
              </div>
              <span className="text-right tabular-nums text-slate-700">{count}</span>
            </div>
          ))}
        </div>
      </Card>
    </>
  );
}

function monthRange() {
  const today = new Date();
  return {
    from: toDateInput(new Date(today.getFullYear(), today.getMonth(), 1)),
    to: toDateInput(new Date(today.getFullYear(), today.getMonth() + 1, 0)),
  };
}

function Usage() {
  const [range, setRange] = useState(monthRange);
  const [usage, setUsage] = useState<RoomUsage[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = (from: string, to: string) =>
    statsApi
      .roomUsage(from, to)
      .then((items) => {
        setUsage(items);
        setError(null);
      })
      .catch((err) => setError(errorMessage(err)));

  useEffect(() => {
    const initial = monthRange();
    load(initial.from, initial.to);
  }, []);

  const submit = (event: FormEvent) => {
    event.preventDefault();
    load(range.from, range.to);
  };

  const max = Math.max(1, ...(usage ?? []).map((item) => item.totalHours));

  return (
    <Card className="mt-4">
      <form onSubmit={submit} className="mb-4 flex flex-wrap items-end gap-3">
        <h2 className="mr-auto font-semibold text-slate-900">ชั่วโมงใช้งานต่อห้อง</h2>
        <Field label="ตั้งแต่">
          <Input type="date" value={range.from} onChange={(e) => setRange({ ...range, from: e.target.value })} />
        </Field>
        <Field label="ถึง">
          <Input type="date" value={range.to} onChange={(e) => setRange({ ...range, to: e.target.value })} />
        </Field>
        <Button type="submit">ดูสถิติ</Button>
      </form>
      {error && <Alert tone="error">{error}</Alert>}
      {!usage ? (
        <Spinner />
      ) : usage.length === 0 ? (
        <EmptyState>ไม่มีการใช้ห้องในช่วงนี้</EmptyState>
      ) : (
        <div className="space-y-2">
          {usage.map((item) => (
            <div key={item.roomId} className="grid grid-cols-[7rem_1fr_6rem] items-center gap-3 text-sm">
              <span className="font-medium text-slate-700">{item.roomCode}</span>
              <div className="h-3 rounded-full bg-slate-100">
                <div className="h-3 rounded-full bg-emerald-500" style={{ width: `${(item.totalHours / max) * 100}%` }} />
              </div>
              <span className="text-right tabular-nums text-slate-600">
                {item.totalHours} ชม. ({item.bookingCount})
              </span>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}
