"use client";

import { useEffect, useState } from "react";
import { BookingSlot, bookingApi } from "@/lib/bookings";
import { formatTime } from "@/lib/format";
import { BookingStatusBadge } from "./BookingStatusBadge";

const OPEN_HOUR = 8;
const CLOSE_HOUR = 22;
const HOURS = Array.from({ length: CLOSE_HOUR - OPEN_HOUR }, (_, i) => OPEN_HOUR + i);

/** ตารางเวลาของห้องในวันที่เลือก แสดงช่วงที่ถูกจองแล้วเป็นแถบสี */
export function RoomSchedule({ roomId, date, refreshKey = 0 }: { roomId: number; date: string; refreshKey?: number }) {
  const [slots, setSlots] = useState<BookingSlot[]>([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    let cancelled = false;
    bookingApi
      .roomSchedule(roomId, date)
      .then((items) => {
        if (!cancelled) {
          setSlots(items);
          setError(false);
        }
      })
      .catch(() => !cancelled && setError(true));
    return () => {
      cancelled = true;
    };
  }, [roomId, date, refreshKey]);

  const position = (value: string) => {
    const time = new Date(value);
    const hours = time.getHours() + time.getMinutes() / 60;
    return ((Math.min(Math.max(hours, OPEN_HOUR), CLOSE_HOUR) - OPEN_HOUR) / (CLOSE_HOUR - OPEN_HOUR)) * 100;
  };

  return (
    <div>
      <div className="relative h-12 rounded-lg border border-slate-200 bg-slate-50">
        {HOURS.map((hour) => (
          <div
            key={hour}
            className="absolute top-0 h-full border-l border-slate-200"
            style={{ left: `${((hour - OPEN_HOUR) / (CLOSE_HOUR - OPEN_HOUR)) * 100}%` }}
          />
        ))}
        {slots.map((slot) => (
          <div
            key={slot.id}
            title={`${formatTime(slot.startTime)} - ${formatTime(slot.endTime)}`}
            className={`absolute top-1 bottom-1 rounded ${slot.status === "PENDING" ? "bg-amber-300" : "bg-indigo-400"}`}
            style={{ left: `${position(slot.startTime)}%`, width: `${position(slot.endTime) - position(slot.startTime)}%` }}
          />
        ))}
      </div>
      <div className="mt-1 flex justify-between text-[10px] text-slate-400">
        {HOURS.filter((hour) => hour % 2 === 0).map((hour) => (
          <span key={hour}>{hour}:00</span>
        ))}
        <span>{CLOSE_HOUR}:00</span>
      </div>
      {error ? (
        <p className="mt-2 text-xs text-rose-600">โหลดตารางห้องไม่สำเร็จ</p>
      ) : slots.length === 0 ? (
        <p className="mt-2 text-xs text-slate-500">ยังไม่มีการจองในวันนี้</p>
      ) : (
        <ul className="mt-2 space-y-1 text-xs text-slate-600">
          {slots.map((slot) => (
            <li key={slot.id} className="flex items-center gap-2">
              {formatTime(slot.startTime)} - {formatTime(slot.endTime)} <BookingStatusBadge status={slot.status} />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
