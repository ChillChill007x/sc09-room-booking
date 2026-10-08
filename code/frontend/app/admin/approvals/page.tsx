"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { errorMessage, PageResponse } from "@/lib/api";
import { STAFF_ROLES } from "@/lib/auth";
import { Booking, bookingApi } from "@/lib/bookings";
import { formatRange } from "@/lib/format";
import { lifecycleApi } from "@/lib/lifecycle";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Button, Card, EmptyState, Input, PageHeader, Pagination, Spinner } from "@/components/ui";

export default function ApprovalsPage() {
  return (
    <RequireAuth roles={STAFF_ROLES}>
      <ApprovalQueue />
    </RequireAuth>
  );
}

function ApprovalQueue() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<Booking> | null>(null);
  const [message, setMessage] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [rejecting, setRejecting] = useState<number | null>(null);
  const [reason, setReason] = useState("");

  const load = useCallback(
    () =>
      bookingApi
        .list({ status: "PENDING", page, size: 10, sort: "startTime,asc" })
        .then(setData)
        .catch((err) => setMessage({ tone: "error", text: errorMessage(err) })),
    [page],
  );

  useEffect(() => {
    load();
  }, [load]);

  const approve = async (booking: Booking) => {
    try {
      await lifecycleApi.changeStatus(booking.id, "APPROVE");
      setMessage({ tone: "success", text: `อนุมัติการจองห้อง ${booking.roomCode} แล้ว` });
      await load();
    } catch (err) {
      setMessage({ tone: "error", text: errorMessage(err) });
    }
  };

  const reject = async (booking: Booking) => {
    try {
      await lifecycleApi.changeStatus(booking.id, "REJECT", reason);
      setMessage({ tone: "success", text: `ปฏิเสธการจองห้อง ${booking.roomCode} แล้ว` });
      setRejecting(null);
      setReason("");
      await load();
    } catch (err) {
      setMessage({ tone: "error", text: errorMessage(err) });
    }
  };

  return (
    <div>
      <PageHeader title="คิวรออนุมัติ" subtitle="คำขอจองที่เรียงตามเวลาใช้ห้อง ใกล้ที่สุดก่อน" />
      {message && (
        <div className="mb-4">
          <Alert tone={message.tone}>{message.text}</Alert>
        </div>
      )}
      {!data ? (
        <Spinner />
      ) : data.content.length === 0 ? (
        <EmptyState>ไม่มีคำขอที่รออนุมัติ</EmptyState>
      ) : (
        <div className="space-y-3">
          {data.content.map((booking) => (
            <Card key={booking.id}>
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <Link href={`/bookings/${booking.id}`} className="font-semibold text-slate-900 hover:underline">
                    {booking.roomCode} · {formatRange(booking.startTime, booking.endTime)}
                  </Link>
                  <p className="text-sm text-slate-600">
                    {booking.userFullName} ({booking.userEmail})
                  </p>
                  <p className="text-sm text-slate-500">
                    {booking.purpose} · {booking.attendees} คน
                  </p>
                </div>
                <div className="flex gap-2">
                  <Button onClick={() => approve(booking)}>อนุมัติ</Button>
                  <Button variant="danger" onClick={() => setRejecting(rejecting === booking.id ? null : booking.id)}>
                    ปฏิเสธ
                  </Button>
                </div>
              </div>
              {rejecting === booking.id && (
                <div className="mt-3 flex gap-2">
                  <Input placeholder="เหตุผลที่ปฏิเสธ (จำเป็น)" value={reason} onChange={(e) => setReason(e.target.value)} />
                  <Button variant="danger" disabled={!reason.trim()} onClick={() => reject(booking)}>
                    ยืนยันปฏิเสธ
                  </Button>
                </div>
              )}
            </Card>
          ))}
        </div>
      )}
      {data && (
        <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />
      )}
    </div>
  );
}
