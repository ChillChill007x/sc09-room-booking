"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { errorMessage } from "@/lib/api";
import { formatDateTime, toApiDateTime } from "@/lib/format";
import { roomApi, RoomClosure } from "@/lib/rooms";
import { Alert, Button, Field, Input } from "./ui";

/** ฟอร์มปิดห้องเพื่อซ่อมบำรุง พร้อมรายการช่วงปิดที่มีอยู่ */
export function RoomClosureForm({ roomId }: { roomId: number }) {
  const [closures, setClosures] = useState<RoomClosure[]>([]);
  const [form, setForm] = useState({ startTime: "", endTime: "", reason: "" });
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    roomApi.closures(roomId).then(setClosures).catch((err) => setError(errorMessage(err)));
  }, [roomId]);

  useEffect(() => {
    load();
  }, [load]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      await roomApi.addClosure(roomId, {
        startTime: toApiDateTime(form.startTime),
        endTime: toApiDateTime(form.endTime),
        reason: form.reason,
      });
      setForm({ startTime: "", endTime: "", reason: "" });
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const remove = async (closureId: number) => {
    try {
      await roomApi.removeClosure(roomId, closureId);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="space-y-3">
      <form onSubmit={submit} className="grid gap-3 sm:grid-cols-4">
        <Field label="ปิดตั้งแต่">
          <Input
            type="datetime-local"
            required
            value={form.startTime}
            onChange={(e) => setForm({ ...form, startTime: e.target.value })}
          />
        </Field>
        <Field label="ถึง">
          <Input
            type="datetime-local"
            required
            value={form.endTime}
            onChange={(e) => setForm({ ...form, endTime: e.target.value })}
          />
        </Field>
        <Field label="เหตุผล">
          <Input required value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} />
        </Field>
        <div className="flex items-end">
          <Button type="submit" className="w-full">
            เพิ่มช่วงปิด
          </Button>
        </div>
      </form>
      {error && <Alert tone="error">{error}</Alert>}
      {closures.length > 0 && (
        <ul className="divide-y divide-slate-100 text-sm">
          {closures.map((closure) => (
            <li key={closure.id} className="flex items-center justify-between py-2">
              <span>
                {formatDateTime(closure.startTime)} - {formatDateTime(closure.endTime)} · {closure.reason}
              </span>
              <Button variant="ghost-danger" onClick={() => remove(closure.id)}>
                ลบ
              </Button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
