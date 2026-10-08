"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { errorMessage, PageResponse } from "@/lib/api";
import { STAFF_ROLES } from "@/lib/auth";
import {
  Equipment,
  equipmentApi,
  Room,
  roomApi,
  RoomPayload,
  RoomStatus,
  ROOM_STATUS_LABELS,
  RoomType,
  roomTypeApi,
} from "@/lib/rooms";
import { RequireAuth } from "@/components/RouteGuard";
import { RoomClosureForm } from "@/components/RoomClosureForm";
import { Alert, Badge, Button, Card, Field, Input, PageHeader, Pagination, Select, Spinner } from "@/components/ui";

export default function AdminRoomsPage() {
  return (
    <RequireAuth roles={STAFF_ROLES}>
      <RoomManagement />
    </RequireAuth>
  );
}

type Panel = { kind: "form"; room: Room | null } | { kind: "equipment"; room: Room } | { kind: "closures"; room: Room };

function RoomManagement() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<Room> | null>(null);
  const [roomTypes, setRoomTypes] = useState<RoomType[]>([]);
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [panel, setPanel] = useState<Panel | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(
    () =>
      roomApi
        .search({ page, size: 10, sort: "code,asc" })
        .then((result) => {
          // ลบห้องสุดท้ายของหน้าสุดท้ายแล้วหน้านี้จะว่าง ให้ถอยไปหน้าสุดท้ายที่ยังมีข้อมูล (useEffect จะโหลดให้ใหม่)
          if (result.content.length === 0 && page > 0) {
            setPage(Math.max(result.totalPages - 1, 0));
            return;
          }
          setData(result);
        })
        .catch((err) => setError(errorMessage(err))),
    [page],
  );

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    Promise.all([roomTypeApi.list(), equipmentApi.list()])
      .then(([types, items]) => {
        setRoomTypes(types);
        setEquipment(items);
      })
      .catch((err) => setError(errorMessage(err)));
  }, []);

  const remove = async (room: Room) => {
    if (!confirm(`ลบห้อง ${room.code}? (ห้องจะถูกเปลี่ยนเป็นเลิกใช้งาน)`)) return;
    try {
      await roomApi.remove(room.id);
      setError(null);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const done = async () => {
    setPanel(null);
    await load();
  };

  return (
    <div>
      <PageHeader
        title="จัดการห้อง"
        subtitle="เพิ่ม แก้ไข ลบห้อง กำหนดอุปกรณ์ และช่วงปิดห้อง"
        actions={<Button onClick={() => setPanel({ kind: "form", room: null })}>+ เพิ่มห้อง</Button>}
      />
      {error && <Alert tone="error">{error}</Alert>}
      {panel?.kind === "form" && (
        <RoomForm room={panel.room} roomTypes={roomTypes} onCancel={() => setPanel(null)} onSaved={done} />
      )}
      {panel?.kind === "equipment" && (
        <EquipmentEditor room={panel.room} equipment={equipment} onCancel={() => setPanel(null)} onSaved={done} />
      )}
      {panel?.kind === "closures" && (
        <Card className="mb-4">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="font-semibold">ช่วงปิดห้อง {panel.room.code}</h2>
            <Button variant="ghost" onClick={() => setPanel(null)}>
              ปิด
            </Button>
          </div>
          <RoomClosureForm roomId={panel.room.id} />
        </Card>
      )}
      {!data ? (
        <Spinner />
      ) : (
        <Card className="overflow-x-auto p-0">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="px-4 py-3">รหัส</th>
                <th className="px-4 py-3">ชื่อ</th>
                <th className="px-4 py-3">ชั้น</th>
                <th className="px-4 py-3">ความจุ</th>
                <th className="px-4 py-3">สถานะ</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data.content.map((room) => (
                <tr key={room.id}>
                  <td className="px-4 py-3 font-medium">{room.code}</td>
                  <td className="px-4 py-3">{room.name}</td>
                  <td className="px-4 py-3">{room.floor}</td>
                  <td className="px-4 py-3">{room.capacity}</td>
                  <td className="px-4 py-3">
                    <Badge className={room.status === "ACTIVE" ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-700"}>
                      {ROOM_STATUS_LABELS[room.status]}
                    </Badge>
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right">
                    <Button variant="ghost" onClick={() => setPanel({ kind: "form", room })}>
                      แก้ไข
                    </Button>
                    <Button variant="ghost" onClick={() => setPanel({ kind: "equipment", room })}>
                      อุปกรณ์
                    </Button>
                    <Button variant="ghost" onClick={() => setPanel({ kind: "closures", room })}>
                      ปิดห้อง
                    </Button>
                    <Button variant="ghost-danger" onClick={() => remove(room)}>
                      ลบ
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
      {data && (
        <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />
      )}
    </div>
  );
}

function RoomForm({
  room,
  roomTypes,
  onCancel,
  onSaved,
}: {
  room: Room | null;
  roomTypes: RoomType[];
  onCancel: () => void;
  onSaved: () => void;
}) {
  const [form, setForm] = useState<RoomPayload>({
    code: room?.code ?? "",
    name: room?.name ?? "",
    floor: room?.floor ?? 1,
    capacity: room?.capacity ?? 30,
    description: room?.description ?? "",
    roomTypeId: room?.roomType.id ?? roomTypes[0]?.id ?? 0,
    status: room?.status ?? "ACTIVE",
  });
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    try {
      if (room) {
        await roomApi.update(room.id, form);
      } else {
        await roomApi.create(form);
      }
      onSaved();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <Card className="mb-4">
      <form onSubmit={submit} className="grid gap-3 sm:grid-cols-4">
        <h2 className="sm:col-span-4 font-semibold">{room ? `แก้ไข ${room.code}` : "เพิ่มห้องใหม่"}</h2>
        {error && (
          <div className="sm:col-span-4">
            <Alert tone="error">{error}</Alert>
          </div>
        )}
        <Field label="รหัสห้อง">
          <Input required value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
        </Field>
        <div className="sm:col-span-2">
          <Field label="ชื่อห้อง">
            <Input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </Field>
        </div>
        <Field label="ประเภท">
          <Select value={form.roomTypeId} onChange={(e) => setForm({ ...form, roomTypeId: Number(e.target.value) })}>
            {roomTypes.map((type) => (
              <option key={type.id} value={type.id}>
                {type.name}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="ชั้น">
          <Input
            type="number"
            min={1}
            required
            value={form.floor}
            onChange={(e) => setForm({ ...form, floor: Number(e.target.value) })}
          />
        </Field>
        <Field label="ความจุ">
          <Input
            type="number"
            min={1}
            required
            value={form.capacity}
            onChange={(e) => setForm({ ...form, capacity: Number(e.target.value) })}
          />
        </Field>
        <Field label="สถานะ">
          <Select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value as RoomStatus })}>
            {Object.entries(ROOM_STATUS_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="รายละเอียด">
          <Input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </Field>
        <div className="sm:col-span-4 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onCancel}>
            ยกเลิก
          </Button>
          <Button type="submit">บันทึก</Button>
        </div>
      </form>
    </Card>
  );
}

function EquipmentEditor({
  room,
  equipment,
  onCancel,
  onSaved,
}: {
  room: Room;
  equipment: Equipment[];
  onCancel: () => void;
  onSaved: () => void;
}) {
  const [quantities, setQuantities] = useState<Record<number, number>>(() =>
    Object.fromEntries(room.equipment.map((item) => [item.equipmentId, item.quantity])),
  );
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const items = Object.entries(quantities)
      .filter(([, quantity]) => quantity > 0)
      .map(([equipmentId, quantity]) => ({ equipmentId: Number(equipmentId), quantity }));
    try {
      await roomApi.updateEquipment(room.id, items);
      onSaved();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <Card className="mb-4">
      <form onSubmit={submit}>
        <h2 className="mb-1 font-semibold">อุปกรณ์ในห้อง {room.code}</h2>
        <p className="mb-3 text-sm text-slate-500">ใส่ 0 เพื่อเอาอุปกรณ์ออกจากห้อง</p>
        {error && <Alert tone="error">{error}</Alert>}
        <div className="grid gap-3 sm:grid-cols-4">
          {equipment.map((item) => (
            <Field key={item.id} label={item.name}>
              <Input
                type="number"
                min={0}
                value={quantities[item.id] ?? 0}
                onChange={(e) => setQuantities({ ...quantities, [item.id]: Number(e.target.value) })}
              />
            </Field>
          ))}
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onCancel}>
            ยกเลิก
          </Button>
          <Button type="submit">บันทึกอุปกรณ์</Button>
        </div>
      </form>
    </Card>
  );
}
