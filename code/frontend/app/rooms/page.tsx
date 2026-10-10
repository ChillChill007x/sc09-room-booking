"use client";

import { FormEvent, useEffect, useState } from "react";
import { errorMessage, PageResponse } from "@/lib/api";
import { toApiDateTime, toDateTimeInput } from "@/lib/format";
import { Equipment, equipmentApi, Room, roomApi, RoomSearchParams, RoomType, roomTypeApi } from "@/lib/rooms";
import { RoomCard } from "@/components/RoomCard";
import { Alert, Button, Card, EmptyState, Field, Input, PageHeader, Pagination, Select, Spinner } from "@/components/ui";

const SORT_OPTIONS = [
  { value: "code,asc", label: "รหัสห้อง" },
  { value: "capacity,desc", label: "ความจุมากไปน้อย" },
  { value: "capacity,asc", label: "ความจุน้อยไปมาก" },
  { value: "floor,asc", label: "ชั้น" },
];

// อาคาร SC09 มี 6 ชั้น (ข้อมูลห้องใน V2__rooms.sql มีชั้น 1, 2, 4, 5, 6)
const BUILDING_FLOORS = [1, 2, 3, 4, 5, 6];

type Mode = "browse" | "available";

export default function RoomsPage() {
  const [mode, setMode] = useState<Mode>("browse");
  const [roomTypes, setRoomTypes] = useState<RoomType[]>([]);
  const [equipment, setEquipment] = useState<Equipment[]>([]);

  useEffect(() => {
    Promise.all([roomTypeApi.list(), equipmentApi.list()])
      .then(([types, items]) => {
        setRoomTypes(types);
        setEquipment(items);
      })
      .catch(() => undefined);
  }, []);

  return (
    <div>
      <PageHeader title="ห้องในอาคาร SC09" subtitle="ค้นหา กรอง และตรวจสอบห้องว่าง" />
      <div className="mb-4 inline-flex rounded-lg border border-slate-200 bg-white p-1 text-sm">
        {(["browse", "available"] as Mode[]).map((value) => (
          <button
            key={value}
            onClick={() => setMode(value)}
            className={`rounded-md px-4 py-1.5 ${mode === value ? "bg-indigo-600 text-white" : "text-slate-600"}`}
          >
            {value === "browse" ? "รายการห้อง" : "ค้นหาห้องว่าง"}
          </button>
        ))}
      </div>
      {mode === "browse" ? (
        <BrowseRooms roomTypes={roomTypes} equipment={equipment} />
      ) : (
        <AvailableRooms equipment={equipment} />
      )}
    </div>
  );
}

function BrowseRooms({ roomTypes, equipment }: { roomTypes: RoomType[]; equipment: Equipment[] }) {
  const [filters, setFilters] = useState<RoomSearchParams>({ page: 0, size: 9, sort: "code,asc" });
  const [data, setData] = useState<PageResponse<Room> | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    roomApi
      .search(filters)
      .then((result) => {
        if (!cancelled) {
          setData(result);
          setError(null);
        }
      })
      .catch((err) => !cancelled && setError(errorMessage(err)));
    return () => {
      cancelled = true;
    };
  }, [filters]);

  const change = (patch: Partial<RoomSearchParams>) => setFilters((prev) => ({ ...prev, ...patch, page: 0 }));
  const toNumber = (value: string) => (value === "" ? "" : Number(value));

  return (
    <>
      <Card className="mb-4 grid gap-3 sm:grid-cols-3 lg:grid-cols-6">
        <div className="sm:col-span-2">
          <Field label="ค้นหา">
            <Input placeholder="รหัสหรือชื่อห้อง" onChange={(e) => change({ keyword: e.target.value })} />
          </Field>
        </div>
        <Field label="ชั้น">
          <Select onChange={(e) => change({ floor: toNumber(e.target.value) })}>
            <option value="">ทุกชั้น</option>
            {BUILDING_FLOORS.map((floor) => (
              <option key={floor} value={floor}>
                ชั้น {floor}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="ประเภท">
          <Select onChange={(e) => change({ typeId: toNumber(e.target.value) })}>
            <option value="">ทุกประเภท</option>
            {roomTypes.map((type) => (
              <option key={type.id} value={type.id}>
                {type.name}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="อุปกรณ์">
          <Select onChange={(e) => change({ equipmentId: toNumber(e.target.value) })}>
            <option value="">ทั้งหมด</option>
            {equipment.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="เรียงตาม">
          <Select value={filters.sort} onChange={(e) => change({ sort: e.target.value })}>
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="ความจุขั้นต่ำ">
          <Input type="number" min={1} onChange={(e) => change({ minCapacity: toNumber(e.target.value) })} />
        </Field>
      </Card>
      {error && <Alert tone="error">{error}</Alert>}
      {!data ? (
        <Spinner />
      ) : data.content.length === 0 ? (
        <EmptyState>ไม่พบห้องตามเงื่อนไข</EmptyState>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.content.map((room) => (
            <RoomCard key={room.id} room={room} />
          ))}
        </div>
      )}
      {data && (
        <Pagination
          page={data.page}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          onChange={(page) => setFilters((prev) => ({ ...prev, page }))}
        />
      )}
    </>
  );
}

function defaultRange() {
  const start = new Date();
  start.setDate(start.getDate() + 1);
  start.setHours(9, 0, 0, 0);
  const end = new Date(start);
  end.setHours(11);
  return { start: toDateTimeInput(start), end: toDateTimeInput(end) };
}

function AvailableRooms({ equipment }: { equipment: Equipment[] }) {
  const [range, setRange] = useState(defaultRange);
  const [minCapacity, setMinCapacity] = useState("");
  const [selected, setSelected] = useState<number[]>([]);
  const [rooms, setRooms] = useState<Room[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const toggle = (id: number) =>
    setSelected((prev) => (prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]));

  const search = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      setRooms(
        await roomApi.available({
          start: toApiDateTime(range.start),
          end: toApiDateTime(range.end),
          minCapacity: minCapacity === "" ? "" : Number(minCapacity),
          equipmentIds: selected,
        }),
      );
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Card className="mb-4">
        <form onSubmit={search} className="grid gap-3 sm:grid-cols-4">
          <Field label="เริ่ม">
            <Input
              type="datetime-local"
              required
              value={range.start}
              onChange={(e) => setRange({ ...range, start: e.target.value })}
            />
          </Field>
          <Field label="สิ้นสุด">
            <Input
              type="datetime-local"
              required
              value={range.end}
              onChange={(e) => setRange({ ...range, end: e.target.value })}
            />
          </Field>
          <Field label="จำนวนคน">
            <Input type="number" min={1} value={minCapacity} onChange={(e) => setMinCapacity(e.target.value)} />
          </Field>
          <div className="flex items-end">
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "กำลังค้นหา..." : "ค้นหาห้องว่าง"}
            </Button>
          </div>
          <div className="sm:col-span-4 flex flex-wrap gap-2">
            {equipment.map((item) => (
              <label
                key={item.id}
                className={`cursor-pointer rounded-full border px-3 py-1 text-xs ${
                  selected.includes(item.id)
                    ? "border-indigo-500 bg-indigo-50 text-indigo-700"
                    : "border-slate-300 text-slate-600"
                }`}
              >
                <input type="checkbox" className="hidden" checked={selected.includes(item.id)} onChange={() => toggle(item.id)} />
                {item.name}
              </label>
            ))}
          </div>
        </form>
      </Card>
      {error && <Alert tone="error">{error}</Alert>}
      {rooms &&
        (rooms.length === 0 ? (
          <EmptyState>ไม่มีห้องว่างในช่วงเวลานี้</EmptyState>
        ) : (
          <>
            <p className="mb-3 text-sm text-slate-500">ว่าง {rooms.length} ห้อง</p>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {rooms.map((room) => (
                <RoomCard key={room.id} room={room} />
              ))}
            </div>
          </>
        ))}
    </>
  );
}
