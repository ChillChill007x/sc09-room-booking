"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { errorMessage } from "@/lib/api";
import { Booking, BOOKING_STATUS_LABELS, bookingApi } from "@/lib/bookings";
import { formatDateTime, formatRange } from "@/lib/format";
import { ACTION_LABELS, AllowedActions, BookingAction, BookingHistory, lifecycleApi } from "@/lib/lifecycle";
import { BookingStatusBadge } from "@/components/BookingStatusBadge";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Button, Card, Input, PageHeader, Spinner } from "@/components/ui";

export default function BookingDetailPage() {
  return (
    <RequireAuth>
      <BookingDetail />
    </RequireAuth>
  );
}

const DANGER_ACTIONS: BookingAction[] = ["REJECT", "CANCEL", "MARK_NO_SHOW"];

function BookingDetail() {
  const { id } = useParams<{ id: string }>();
  const bookingId = Number(id);
  const [booking, setBooking] = useState<Booking | null>(null);
  const [history, setHistory] = useState<BookingHistory[]>([]);
  const [allowed, setAllowed] = useState<AllowedActions | null>(null);
  const [note, setNote] = useState("");
  const [message, setMessage] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(
    () =>
      Promise.all([bookingApi.get(bookingId), lifecycleApi.history(bookingId), lifecycleApi.allowedActions(bookingId)])
        .then(([detail, timeline, actions]) => {
          setBooking(detail);
          setHistory(timeline);
          setAllowed(actions);
        })
        .catch((err) => setMessage({ tone: "error", text: errorMessage(err) })),
    [bookingId],
  );

  useEffect(() => {
    load();
  }, [load]);

  const run = async (action: BookingAction) => {
    if (action === "REJECT" && !note.trim()) {
      setMessage({ tone: "error", text: "กรุณาใส่เหตุผลก่อนปฏิเสธ" });
      return;
    }
    if (DANGER_ACTIONS.includes(action) && !confirm(`ยืนยัน "${ACTION_LABELS[action]}"?`)) return;
    setBusy(true);
    try {
      const updated = await lifecycleApi.changeStatus(bookingId, action, note || undefined);
      setMessage({ tone: "success", text: `สถานะใหม่: ${BOOKING_STATUS_LABELS[updated.status]}` });
      setNote("");
      await load();
    } catch (err) {
      setMessage({ tone: "error", text: errorMessage(err) });
    } finally {
      setBusy(false);
    }
  };

  if (!booking) return message ? <Alert tone="error">{message.text}</Alert> : <Spinner />;

  return (
    <div className="mx-auto max-w-3xl">
      <Link href="/bookings" className="text-sm text-indigo-600 hover:underline">
        ← การจองของฉัน
      </Link>
      <div className="mt-3">
        <PageHeader
          title={`การจอง #${booking.id}`}
          subtitle={`${booking.roomCode} ${booking.roomName}`}
          actions={<BookingStatusBadge status={booking.status} />}
        />
      </div>
      {message && (
        <div className="mb-4">
          <Alert tone={message.tone}>{message.text}</Alert>
        </div>
      )}
      <Card className="grid gap-4 sm:grid-cols-2">
        <Info label="เวลา" value={formatRange(booking.startTime, booking.endTime)} />
        <Info label="ผู้จอง" value={`${booking.userFullName ?? "-"} (${booking.userEmail})`} />
        <Info label="วัตถุประสงค์" value={booking.purpose} />
        <Info label="จำนวนผู้เข้าร่วม" value={`${booking.attendees} คน`} />
      </Card>

      {allowed && allowed.actions.length > 0 && (
        <Card className="mt-4">
          <h2 className="mb-3 font-semibold text-slate-900">ดำเนินการ</h2>
          {(allowed.actions.includes("REJECT") || allowed.actions.includes("CANCEL")) && (
            <Input
              className="mb-3"
              placeholder="หมายเหตุ (จำเป็นเมื่อปฏิเสธ)"
              value={note}
              onChange={(e) => setNote(e.target.value)}
            />
          )}
          <div className="flex flex-wrap gap-2">
            {allowed.actions.map((action) => (
              <Button
                key={action}
                disabled={busy}
                variant={DANGER_ACTIONS.includes(action) ? "danger" : "primary"}
                onClick={() => run(action)}
              >
                {ACTION_LABELS[action]}
              </Button>
            ))}
          </div>
          {allowed.status === "APPROVED" && !allowed.actions.includes("CHECK_IN") && (
            <p className="mt-3 text-xs text-slate-500">check-in ได้ตั้งแต่ 15 นาทีก่อนถึง 15 นาทีหลังเวลาเริ่ม</p>
          )}
        </Card>
      )}

      <Card className="mt-4">
        <h2 className="mb-4 font-semibold text-slate-900">ไทม์ไลน์สถานะ</h2>
        <ol className="relative ml-2 border-l border-slate-200">
          {history.map((item) => (
            <li key={item.id} className="mb-5 ml-5">
              <span className="absolute -left-1.5 mt-1.5 h-3 w-3 rounded-full border-2 border-white bg-indigo-500" />
              <div className="flex flex-wrap items-center gap-2">
                <BookingStatusBadge status={item.toStatus} />
                <span className="text-xs text-slate-500">{formatDateTime(item.changedAt)}</span>
              </div>
              <p className="mt-1 text-sm text-slate-600">
                โดย {item.changedByName}
                {item.note && ` · ${item.note}`}
              </p>
            </li>
          ))}
        </ol>
      </Card>
    </div>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs text-slate-500">{label}</p>
      <p className="text-sm text-slate-900">{value}</p>
    </div>
  );
}
