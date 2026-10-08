"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { errorMessage, PageResponse } from "@/lib/api";
import { formatDateTime } from "@/lib/format";
import { announceNotificationsChanged, AppNotification, notificationApi } from "@/lib/notifications";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Button, Card, EmptyState, PageHeader, Pagination, Spinner } from "@/components/ui";

export default function NotificationsPage() {
  return (
    <RequireAuth>
      <NotificationList />
    </RequireAuth>
  );
}

function NotificationList() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<AppNotification> | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(
    () =>
      notificationApi
        .mine(page)
        .then(setData)
        .catch((err) => setError(errorMessage(err))),
    [page],
  );

  useEffect(() => {
    load();
  }, [load]);

  const act = async (action: () => Promise<unknown>) => {
    try {
      await action();
      announceNotificationsChanged();
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader
        title="แจ้งเตือน"
        actions={
          <Button variant="secondary" onClick={() => act(notificationApi.markAllRead)}>
            อ่านทั้งหมด
          </Button>
        }
      />
      {error && <Alert tone="error">{error}</Alert>}
      {!data ? (
        <Spinner />
      ) : data.content.length === 0 ? (
        <EmptyState>ยังไม่มีแจ้งเตือน</EmptyState>
      ) : (
        <div className="space-y-2">
          {data.content.map((item) => (
            <Card
              key={item.id}
              className={`flex items-start justify-between gap-3 ${item.read ? "" : "border-indigo-200 bg-indigo-50/40"}`}
            >
              <div>
                <p className={`text-sm ${item.read ? "text-slate-600" : "font-medium text-slate-900"}`}>{item.message}</p>
                <p className="mt-1 text-xs text-slate-400">{formatDateTime(item.createdAt)}</p>
              </div>
              <div className="flex shrink-0 gap-1">
                {item.bookingId && (
                  <Link href={`/bookings/${item.bookingId}`} onClick={() => !item.read && act(() => notificationApi.markRead(item.id))}>
                    <Button variant="ghost">ดู</Button>
                  </Link>
                )}
                {!item.read && (
                  <Button variant="ghost" onClick={() => act(() => notificationApi.markRead(item.id))}>
                    อ่านแล้ว
                  </Button>
                )}
                <Button variant="ghost-danger" onClick={() => act(() => notificationApi.remove(item.id))}>
                  ลบ
                </Button>
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
