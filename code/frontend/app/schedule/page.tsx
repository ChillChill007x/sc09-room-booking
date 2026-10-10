"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { errorMessage } from "@/lib/api";
import { BOOKING_STATUS_LABELS, BookingStatus, CalendarDay, ScheduleSlot, scheduleApi } from "@/lib/bookings";
import { formatTime, toDateInput } from "@/lib/format";
import { Room, roomApi, RoomClosure, ROOM_STATUS_LABELS } from "@/lib/rooms";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Button, Card, EmptyState, PageHeader, Spinner } from "@/components/ui";

const OPEN_HOUR = 8;
const CLOSE_HOUR = 22;
const HOUR_HEIGHT = 48;
const HOURS = Array.from({ length: CLOSE_HOUR - OPEN_HOUR }, (_, i) => OPEN_HOUR + i);
const WEEKDAYS = ["อา.", "จ.", "อ.", "พ.", "พฤ.", "ศ.", "ส."];

const SLOT_STYLES: Partial<Record<BookingStatus, string>> = {
  PENDING: "border-amber-300 bg-amber-100 text-amber-900",
  APPROVED: "border-indigo-300 bg-indigo-100 text-indigo-900",
  CHECKED_IN: "border-sky-300 bg-sky-100 text-sky-900",
  COMPLETED: "border-slate-300 bg-slate-100 text-slate-600",
};

const CLOSURE_STYLE =
  "border-slate-400 bg-[repeating-linear-gradient(45deg,#e2e8f0,#e2e8f0_6px,#cbd5e1_6px,#cbd5e1_12px)] text-slate-700";

const monthLabel = new Intl.DateTimeFormat("th-TH", { month: "long", year: "numeric" });
const dayLabel = new Intl.DateTimeFormat("th-TH", { weekday: "long", day: "numeric", month: "long", year: "numeric" });

/**
 * ผลโหลดที่ผูกกับคีย์ (วันหรือเดือน) ที่ขอไป ถ้าคีย์ไม่ตรงกับที่เลือกอยู่ แปลว่ายังโหลดอยู่
 * ทำให้ไม่แสดงข้อมูลของวันหรือเดือนก่อนหน้าระหว่างโหลดหรือเมื่อโหลดล้มเหลว
 */
interface Loaded<T> {
  key: string;
  data?: T;
  error?: string;
}

export default function SchedulePage() {
  return (
    <RequireAuth>
      <Schedule />
    </RequireAuth>
  );
}

function Schedule() {
  const [selected, setSelected] = useState(() => toDateInput(new Date()));
  const [month, setMonth] = useState(() => firstOfMonth(new Date()));

  const pick = (date: string) => {
    setSelected(date);
    setMonth(firstOfMonth(parseDate(date)));
  };

  return (
    <div>
      <PageHeader title="ตารางการใช้ห้อง" subtitle="ดูว่าวันไหนมีการจอง และห้องไหนมีการจองหรือปิดในแต่ละช่วงเวลา" />
      <div className="grid gap-4 lg:grid-cols-[320px_1fr]">
        <MonthCalendar month={month} selected={selected} onMonthChange={setMonth} onSelect={pick} />
        <DaySchedule date={selected} onSelect={pick} />
      </div>
    </div>
  );
}

function MonthCalendar({
  month,
  selected,
  onMonthChange,
  onSelect,
}: {
  month: Date;
  selected: string;
  onMonthChange: (month: Date) => void;
  onSelect: (date: string) => void;
}) {
  const monthKey = toDateInput(month).slice(0, 7);
  const [loaded, setLoaded] = useState<Loaded<CalendarDay[]> | null>(null);

  useEffect(() => {
    let cancelled = false;
    scheduleApi
      .month(monthKey)
      .then((data) => !cancelled && setLoaded({ key: monthKey, data }))
      .catch((err) => !cancelled && setLoaded({ key: monthKey, error: errorMessage(err) }));
    return () => {
      cancelled = true;
    };
  }, [monthKey]);

  const current = loaded?.key === monthKey ? loaded : null;
  const counts = useMemo(() => new Map((current?.data ?? []).map((day) => [day.date, day.bookings])), [current]);
  const today = toDateInput(new Date());
  const cells = calendarCells(month);
  const shift = (delta: number) => onMonthChange(new Date(month.getFullYear(), month.getMonth() + delta, 1));

  return (
    <Card>
      <div className="mb-3 flex items-center justify-between">
        <Button variant="ghost" onClick={() => shift(-1)} aria-label="เดือนก่อนหน้า">
          ‹
        </Button>
        <h2 className="font-semibold text-slate-900">{monthLabel.format(month)}</h2>
        <Button variant="ghost" onClick={() => shift(1)} aria-label="เดือนถัดไป">
          ›
        </Button>
      </div>
      {current?.error && <Alert tone="error">โหลดจำนวนการจองของเดือนนี้ไม่สำเร็จ: {current.error}</Alert>}
      <div className="grid grid-cols-7 gap-1 text-center text-xs">
        {WEEKDAYS.map((weekday) => (
          <div key={weekday} className="py-1 font-medium text-slate-500">
            {weekday}
          </div>
        ))}
        {cells.map((cell, index) => {
          if (!cell) return <div key={`blank-${index}`} />;
          const count = counts.get(cell) ?? 0;
          const isSelected = cell === selected;
          return (
            <button
              key={cell}
              onClick={() => onSelect(cell)}
              className={`flex h-12 flex-col items-center justify-center rounded-lg border text-sm transition ${
                isSelected
                  ? "border-indigo-600 bg-indigo-600 text-white"
                  : count > 0
                    ? "border-indigo-200 bg-indigo-50 text-slate-900 hover:bg-indigo-100"
                    : "border-transparent text-slate-700 hover:bg-slate-100"
              } ${cell === today && !isSelected ? "ring-2 ring-indigo-300" : ""}`}
            >
              <span>{Number(cell.slice(8))}</span>
              {count > 0 && (
                <span className={`text-[10px] ${isSelected ? "text-indigo-100" : "text-indigo-600"}`}>{count} จอง</span>
              )}
            </button>
          );
        })}
      </div>
      {!current?.error && (
        <p className="mt-3 text-xs text-slate-500">
          {current
            ? "วันที่มีพื้นสีม่วงอ่อนคือวันที่มีการจองแล้ว กดวันที่เพื่อดูตารางทุกห้อง"
            : "กำลังโหลดจำนวนการจองของเดือนนี้..."}
        </p>
      )}
    </Card>
  );
}

function DaySchedule({ date, onSelect }: { date: string; onSelect: (date: string) => void }) {
  const [rooms, setRooms] = useState<Room[] | null>(null);
  const [roomError, setRoomError] = useState<string | null>(null);
  const [closures, setClosures] = useState<RoomClosure[]>([]);
  const [closureError, setClosureError] = useState(false);
  const [loaded, setLoaded] = useState<Loaded<ScheduleSlot[]> | null>(null);

  // ห้องและช่วงปิดห้องโหลดครั้งเดียวตอนเปิดหน้า แล้วกรองตามวันที่ในเบราว์เซอร์
  useEffect(() => {
    roomApi
      .search({ page: 0, size: 100, sort: "code,asc" })
      .then((result) => {
        setRooms(result.content);
        return Promise.allSettled(result.content.map((room) => roomApi.closures(room.id)));
      })
      .then((results) => {
        setClosures(results.flatMap((result) => (result.status === "fulfilled" ? result.value : [])));
        setClosureError(results.some((result) => result.status === "rejected"));
      })
      .catch((err) => setRoomError(errorMessage(err)));
  }, []);

  useEffect(() => {
    let cancelled = false;
    scheduleApi
      .day(date)
      .then((data) => !cancelled && setLoaded({ key: date, data }))
      .catch((err) => !cancelled && setLoaded({ key: date, error: errorMessage(err) }));
    return () => {
      cancelled = true;
    };
  }, [date]);

  const current = loaded?.key === date ? loaded : null;
  const slots = current?.data;

  const slotsByRoom = useMemo(() => groupByRoom(slots ?? []), [slots]);
  const closuresByRoom = useMemo(() => {
    const dayStart = parseDate(date);
    const dayEnd = new Date(dayStart.getFullYear(), dayStart.getMonth(), dayStart.getDate() + 1);
    return groupByRoom(
      closures.filter((closure) => new Date(closure.startTime) < dayEnd && new Date(closure.endTime) > dayStart),
    );
  }, [closures, date]);

  const isToday = date === toDateInput(new Date());
  const shiftDay = (delta: number) => {
    const next = parseDate(date);
    next.setDate(next.getDate() + delta);
    onSelect(toDateInput(next));
  };

  return (
    <Card className="min-w-0">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <h2 className="font-semibold text-slate-900">{dayLabel.format(parseDate(date))}</h2>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => shiftDay(-1)}>
            ‹ วันก่อน
          </Button>
          <Button variant="secondary" onClick={() => onSelect(toDateInput(new Date()))}>
            วันนี้
          </Button>
          <Button variant="secondary" onClick={() => shiftDay(1)}>
            วันถัดไป ›
          </Button>
        </div>
      </div>
      <Legend />
      {roomError && <Alert tone="error">{roomError}</Alert>}
      {closureError && (
        <Alert tone="info">โหลดข้อมูลช่วงปิดห้องไม่ครบ บางห้องอาจปิดอยู่แต่ไม่แสดงในตาราง ระบบจะตรวจอีกครั้งตอนจอง</Alert>
      )}
      {current?.error ? (
        <Alert tone="error">โหลดการจองของวันที่เลือกไม่สำเร็จ: {current.error}</Alert>
      ) : !rooms || !slots ? (
        roomError ? null : <Spinner />
      ) : rooms.length === 0 ? (
        <EmptyState>ยังไม่มีห้องในระบบ</EmptyState>
      ) : (
        <>
          <div className="overflow-x-auto">
            <div className="flex min-w-max">
              <TimeColumn />
              {rooms.map((room) => (
                <RoomColumn
                  key={room.id}
                  room={room}
                  date={date}
                  slots={slotsByRoom.get(room.id) ?? []}
                  closures={closuresByRoom.get(room.id) ?? []}
                />
              ))}
            </div>
          </div>
          {slots.length === 0 && (
            <p className="mt-3 text-sm text-slate-500">ไม่มีรายการจองใน{isToday ? "วันนี้" : "วันที่เลือก"}</p>
          )}
        </>
      )}
    </Card>
  );
}

function TimeColumn() {
  return (
    <div className="sticky left-0 z-10 w-14 shrink-0 bg-white">
      <div className="h-14" />
      {HOURS.map((hour) => (
        <div key={hour} className="pr-2 text-right text-xs text-slate-400" style={{ height: HOUR_HEIGHT }}>
          {String(hour).padStart(2, "0")}:00
        </div>
      ))}
    </div>
  );
}

function RoomColumn({
  room,
  date,
  slots,
  closures,
}: {
  room: Room;
  date: string;
  slots: ScheduleSlot[];
  closures: RoomClosure[];
}) {
  const bookable = room.status === "ACTIVE";
  const top = (value: string) => {
    const time = new Date(value);
    // ช่วงปิดอาจเริ่มก่อนวันนี้หรือจบหลังวันนี้ ให้ตัดให้อยู่ในช่วงเวลาเปิดอาคารของวันที่เลือก
    const hours = toDateInput(time) < date ? OPEN_HOUR : toDateInput(time) > date ? CLOSE_HOUR : time.getHours() + time.getMinutes() / 60;
    return (Math.min(Math.max(hours, OPEN_HOUR), CLOSE_HOUR) - OPEN_HOUR) * HOUR_HEIGHT;
  };
  const height = (start: string, end: string) => Math.max(top(end) - top(start), 14);

  return (
    <div className={`w-28 shrink-0 border-l border-slate-200 ${bookable ? "" : "bg-slate-50"}`}>
      <div className="flex h-14 flex-col items-center justify-center border-b border-slate-200 px-1 text-center">
        <Link href={`/rooms/${room.id}`} className="text-xs font-semibold text-slate-800 hover:text-indigo-600">
          {room.code.replace("SC09-", "")}
        </Link>
        {bookable ? (
          <Link href={`/rooms/${room.id}/book?date=${date}`} className="text-[10px] text-indigo-600 hover:underline">
            จองห้องนี้
          </Link>
        ) : (
          <span className="rounded bg-slate-200 px-1 text-[10px] text-slate-600">{ROOM_STATUS_LABELS[room.status]}</span>
        )}
      </div>
      <div className="relative" style={{ height: HOURS.length * HOUR_HEIGHT }}>
        {HOURS.map((hour) => (
          <div
            key={hour}
            className="absolute inset-x-0 border-t border-slate-100"
            style={{ top: (hour - OPEN_HOUR) * HOUR_HEIGHT }}
          />
        ))}
        {!bookable && (
          <div className="absolute inset-0 flex items-start justify-center pt-2 text-[10px] text-slate-500">
            ไม่เปิดให้จอง
          </div>
        )}
        {closures.map((closure) => (
          <div
            key={`closure-${closure.id}`}
            title={`ปิดห้อง ${formatTime(closure.startTime)} - ${formatTime(closure.endTime)}: ${closure.reason}`}
            className={`absolute inset-x-1 overflow-hidden rounded border px-1 text-[10px] leading-tight ${CLOSURE_STYLE}`}
            style={{ top: top(closure.startTime), height: height(closure.startTime, closure.endTime) }}
          >
            <div className="font-medium">ปิดห้อง</div>
            <div className="truncate">{closure.reason}</div>
          </div>
        ))}
        {slots.map((slot) => (
          <div
            key={slot.bookingId}
            title={`${room.code} ${formatTime(slot.startTime)} - ${formatTime(slot.endTime)} (${BOOKING_STATUS_LABELS[slot.status]})`}
            className={`absolute inset-x-1 overflow-hidden rounded border px-1 text-[10px] leading-tight ${SLOT_STYLES[slot.status] ?? ""}`}
            style={{ top: top(slot.startTime), height: height(slot.startTime, slot.endTime) }}
          >
            <div className="font-medium">
              {formatTime(slot.startTime)}-{formatTime(slot.endTime)}
            </div>
            <div>{BOOKING_STATUS_LABELS[slot.status]}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

function Legend() {
  return (
    <div className="mb-3 flex flex-wrap gap-3 text-xs text-slate-600">
      {(Object.keys(SLOT_STYLES) as BookingStatus[]).map((status) => (
        <span key={status} className="flex items-center gap-1">
          <span className={`h-3 w-3 rounded border ${SLOT_STYLES[status]}`} />
          {BOOKING_STATUS_LABELS[status]}
        </span>
      ))}
      <span className="flex items-center gap-1">
        <span className={`h-3 w-3 rounded border ${CLOSURE_STYLE}`} />
        ปิดห้อง
      </span>
      <span className="text-slate-400">ช่องว่างคือช่วงที่ไม่มีการจองและไม่ได้ปิดห้อง ระบบจะตรวจอีกครั้งตอนจอง</span>
    </div>
  );
}

function groupByRoom<T extends { roomId: number }>(items: T[]): Map<number, T[]> {
  const map = new Map<number, T[]>();
  items.forEach((item) => map.set(item.roomId, [...(map.get(item.roomId) ?? []), item]));
  return map;
}

function firstOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

/** แปลง yyyy-MM-dd เป็นวันที่ตามเวลาเครื่อง (new Date("yyyy-MM-dd") จะได้เวลา UTC ทำให้วันเพี้ยน) */
function parseDate(value: string): Date {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

/** ช่องของปฏิทิน 1 เดือน เริ่มวันอาทิตย์ ช่องก่อนวันที่ 1 เป็น null */
function calendarCells(month: Date): (string | null)[] {
  const blanks = month.getDay();
  const daysInMonth = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate();
  return [
    ...Array.from({ length: blanks }, () => null),
    ...Array.from({ length: daysInMonth }, (_, i) =>
      toDateInput(new Date(month.getFullYear(), month.getMonth(), i + 1)),
    ),
  ];
}
