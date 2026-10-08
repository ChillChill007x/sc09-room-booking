"use client";

import { FormEvent, useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ApiError, errorMessage } from "@/lib/api";
import { bookingApi } from "@/lib/bookings";
import { toApiDateTime, toDateInput } from "@/lib/format";
import { Room, roomApi } from "@/lib/rooms";
import { RequireAuth } from "@/components/RouteGuard";
import { RoomSchedule } from "@/components/RoomSchedule";
import { Alert, Button, Card, Field, Input, PageHeader, Spinner, Textarea } from "@/components/ui";

export default function BookRoomPage() {
  return (
    <RequireAuth>
      <BookingForm />
    </RequireAuth>
  );
}

function tomorrow(): string {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return toDateInput(date);
}

function BookingForm() {
  const { id } = useParams<{ id: string }>();
  const roomId = Number(id);
  const router = useRouter();
  const [room, setRoom] = useState<Room | null>(null);
  const [date, setDate] = useState(tomorrow);
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("11:00");
  const [purpose, setPurpose] = useState("");
  const [attendees, setAttendees] = useState(1);
  const [error, setError] = useState<ApiError | string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    roomApi.get(roomId).then(setRoom).catch((err) => setError(errorMessage(err)));
  }, [roomId]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const booking = await bookingApi.create({
        roomId,
        startTime: toApiDateTime(`${date}T${startTime}`),
        endTime: toApiDateTime(`${date}T${endTime}`),
        purpose,
        attendees,
      });
      router.push(`/bookings/${booking.id}`);
    } catch (err) {
      setError(err instanceof ApiError ? err : errorMessage(err));
      setRefreshKey((key) => key + 1);
    } finally {
      setSubmitting(false);
    }
  };

  if (!room) return error ? <Alert tone="error">{String(error)}</Alert> : <Spinner />;

  return (
    <div className="mx-auto max-w-2xl">
      <Link href={`/rooms/${room.id}`} className="text-sm text-indigo-600 hover:underline">
        ← {room.code}
      </Link>
      <div className="mt-3">
        <PageHeader title={`จองห้อง ${room.code}`} subtitle={`${room.name} · รับได้ ${room.capacity} คน`} />
      </div>
      <Card className="mb-4">
        <div className="mb-3 flex items-end justify-between gap-3">
          <h2 className="font-semibold text-slate-900">ตารางห้องวันที่เลือก</h2>
          <Input type="date" value={date} onChange={(e) => setDate(e.target.value)} className="max-w-44" />
        </div>
        <RoomSchedule roomId={room.id} date={date} refreshKey={refreshKey} />
      </Card>
      <Card>
        <form onSubmit={submit} className="grid gap-4 sm:grid-cols-3">
          {error && (
            <div className="sm:col-span-3">
              <Alert tone="error">
                {typeof error === "string" ? error : error.message}
                {error instanceof ApiError && error.status === 409 && " กรุณาเลือกช่วงเวลาอื่น"}
              </Alert>
            </div>
          )}
          <Field label="วันที่">
            <Input type="date" required value={date} onChange={(e) => setDate(e.target.value)} />
          </Field>
          <Field label="เวลาเริ่ม">
            <Input type="time" required step={900} value={startTime} onChange={(e) => setStartTime(e.target.value)} />
          </Field>
          <Field label="เวลาสิ้นสุด">
            <Input type="time" required step={900} value={endTime} onChange={(e) => setEndTime(e.target.value)} />
          </Field>
          <div className="sm:col-span-2">
            <Field label="วัตถุประสงค์">
              <Textarea required rows={2} maxLength={300} value={purpose} onChange={(e) => setPurpose(e.target.value)} />
            </Field>
          </div>
          <Field label="จำนวนผู้เข้าร่วม">
            <Input
              type="number"
              min={1}
              max={room.capacity}
              required
              value={attendees}
              onChange={(e) => setAttendees(Number(e.target.value))}
            />
          </Field>
          <div className="sm:col-span-3 flex justify-end">
            <Button type="submit" disabled={submitting}>
              {submitting ? "กำลังส่งคำขอ..." : "ยืนยันการจอง"}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
