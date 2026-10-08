"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { errorMessage } from "@/lib/api";
import { formatDateTime } from "@/lib/format";
import { Room, roomApi, RoomClosure, ROOM_STATUS_LABELS } from "@/lib/rooms";
import { Alert, Badge, Button, Card, PageHeader, Spinner } from "@/components/ui";

export default function RoomDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const [room, setRoom] = useState<Room | null>(null);
  const [closures, setClosures] = useState<RoomClosure[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    roomApi
      .get(Number(id))
      .then(setRoom)
      .catch((err) => setError(errorMessage(err)));
  }, [id]);

  useEffect(() => {
    if (!user) return;
    roomApi
      .closures(Number(id))
      .then((items) => setClosures(items.filter((item) => new Date(item.endTime) > new Date())))
      .catch(() => undefined);
  }, [id, user]);

  if (error) return <Alert tone="error">{error}</Alert>;
  if (!room) return <Spinner />;

  return (
    <div className="mx-auto max-w-3xl">
      <Link href="/rooms" className="text-sm text-indigo-600 hover:underline">
        ← กลับไปรายการห้อง
      </Link>
      <div className="mt-3">
        <PageHeader
          title={`${room.code} ${room.name}`}
          subtitle={`ชั้น ${room.floor} · ${room.roomType.name}`}
          actions={
            room.status === "ACTIVE" ? (
              <Link href={user ? `/rooms/${room.id}/book` : `/login?next=/rooms/${room.id}/book`}>
                <Button>จองห้องนี้</Button>
              </Link>
            ) : (
              <Badge className="bg-amber-50 text-amber-700">{ROOM_STATUS_LABELS[room.status]}</Badge>
            )
          }
        />
      </div>
      <div className="grid gap-4 sm:grid-cols-3">
        <Card>
          <p className="text-sm text-slate-500">ความจุ</p>
          <p className="text-2xl font-semibold text-slate-900">{room.capacity} คน</p>
        </Card>
        <Card className="sm:col-span-2">
          <p className="text-sm text-slate-500">รายละเอียด</p>
          <p className="text-slate-800">{room.description || room.roomType.description || "-"}</p>
        </Card>
      </div>
      <Card className="mt-4">
        <h2 className="font-semibold text-slate-900">อุปกรณ์ในห้อง</h2>
        {room.equipment.length === 0 ? (
          <p className="mt-2 text-sm text-slate-500">ไม่มีข้อมูลอุปกรณ์</p>
        ) : (
          <ul className="mt-3 divide-y divide-slate-100">
            {room.equipment.map((item) => (
              <li key={item.equipmentId} className="flex justify-between py-2 text-sm">
                <span>{item.name}</span>
                <span className="text-slate-500">{item.quantity} ชิ้น</span>
              </li>
            ))}
          </ul>
        )}
      </Card>
      {closures.length > 0 && (
        <Card className="mt-4">
          <h2 className="font-semibold text-slate-900">ช่วงปิดห้อง</h2>
          <ul className="mt-3 space-y-2 text-sm">
            {closures.map((closure) => (
              <li key={closure.id} className="rounded-lg bg-amber-50 px-3 py-2 text-amber-800">
                {formatDateTime(closure.startTime)} - {formatDateTime(closure.endTime)} · {closure.reason}
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
}
