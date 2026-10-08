"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { errorMessage, PageResponse } from "@/lib/api";
import { Booking, bookingApi } from "@/lib/bookings";
import { formatRange } from "@/lib/format";
import { BookingStatusBadge } from "@/components/BookingStatusBadge";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Button, Card, EmptyState, PageHeader, Pagination, Spinner } from "@/components/ui";

export default function MyBookingsPage() {
  return (
    <RequireAuth>
      <MyBookings />
    </RequireAuth>
  );
}

function MyBookings() {
  const { user } = useAuth();
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<Booking> | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(
    () =>
      bookingApi
        .byUser(user!.id, { page, size: 10, sort: "startTime,desc" })
        .then(setData)
        .catch((err) => setError(errorMessage(err))),
    [user, page],
  );

  useEffect(() => {
    load();
  }, [load]);

  const remove = async (booking: Booking) => {
    if (!confirm(`ลบคำขอจองห้อง ${booking.roomCode}?`)) return;
    try {
      await bookingApi.remove(booking.id);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="การจองของฉัน"
        actions={
          <Link href="/rooms">
            <Button>+ จองห้อง</Button>
          </Link>
        }
      />
      {error && <Alert tone="error">{error}</Alert>}
      {!data ? (
        <Spinner />
      ) : data.content.length === 0 ? (
        <EmptyState>ยังไม่มีการจอง</EmptyState>
      ) : (
        <div className="space-y-3">
          {data.content.map((booking) => (
            <Card key={booking.id} className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-slate-900">{booking.roomCode}</span>
                  <BookingStatusBadge status={booking.status} />
                </div>
                <p className="text-sm text-slate-600">{formatRange(booking.startTime, booking.endTime)}</p>
                <p className="text-sm text-slate-500">
                  {booking.purpose} · {booking.attendees} คน
                </p>
              </div>
              <div className="flex gap-2">
                {booking.status === "PENDING" && (
                  <Button variant="ghost-danger" onClick={() => remove(booking)}>
                    ลบคำขอ
                  </Button>
                )}
                <Link href={`/bookings/${booking.id}`}>
                  <Button variant="secondary">รายละเอียด</Button>
                </Link>
              </div>
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
