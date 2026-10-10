"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { errorMessage } from "@/lib/api";
import { STAFF_ROLES } from "@/lib/auth";
import { equipmentApi, roomTypeApi } from "@/lib/rooms";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Button, Card, Field, Input, PageHeader, Spinner } from "@/components/ui";

export default function AdminEquipmentPage() {
  return (
    <RequireAuth roles={STAFF_ROLES}>
      <PageHeader title="อุปกรณ์และประเภทห้อง" subtitle="ข้อมูลหลักที่ใช้กับห้องทุกห้อง" />
      <div className="grid gap-6 lg:grid-cols-2">
        <NamedItemManager title="อุปกรณ์" api={equipmentApi} />
        <NamedItemManager title="ประเภทห้อง" api={roomTypeApi} />
      </div>
    </RequireAuth>
  );
}

interface NamedItem {
  id: number;
  name: string;
  description: string | null;
}

interface NamedItemApi<T extends NamedItem> {
  list: () => Promise<T[]>;
  create: (payload: { name: string; description?: string }) => Promise<T>;
  update: (id: number, payload: { name: string; description?: string }) => Promise<T>;
  remove: (id: number) => Promise<void>;
}

/** ใช้ร่วมกันทั้งอุปกรณ์และประเภทห้อง เพราะมีแค่ชื่อกับคำอธิบายเหมือนกัน */
function NamedItemManager<T extends NamedItem>({ title, api }: { title: string; api: NamedItemApi<T> }) {
  const [items, setItems] = useState<T[] | null>(null);
  const [editing, setEditing] = useState<T | null>(null);
  const [form, setForm] = useState({ name: "", description: "" });
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    api.list().then(setItems).catch((err) => setError(errorMessage(err)));
  }, [api]);

  useEffect(() => {
    load();
  }, [load]);

  const startEdit = (item: T) => {
    setEditing(item);
    setForm({ name: item.name, description: item.description ?? "" });
  };

  const reset = () => {
    setEditing(null);
    setForm({ name: "", description: "" });
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      if (editing) {
        await api.update(editing.id, form);
      } else {
        await api.create(form);
      }
      reset();
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const remove = async (item: T) => {
    if (!confirm(`ลบ ${item.name}?`)) return;
    try {
      await api.remove(item.id);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <Card>
      <h2 className="mb-3 font-semibold text-slate-900">{title}</h2>
      <form onSubmit={submit} className="mb-4 grid gap-3 sm:grid-cols-[1fr_1fr_auto]">
        <Field label="ชื่อ">
          <Input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </Field>
        <Field label="คำอธิบาย">
          <Input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </Field>
        <div className="flex items-end gap-2">
          <Button type="submit">{editing ? "บันทึก" : "เพิ่ม"}</Button>
          {editing && (
            <Button type="button" variant="secondary" onClick={reset}>
              ยกเลิก
            </Button>
          )}
        </div>
      </form>
      {error && <Alert tone="error">{error}</Alert>}
      {!items ? (
        <Spinner />
      ) : (
        <ul className="divide-y divide-slate-100 text-sm">
          {items.map((item) => (
            <li key={item.id} className="flex items-center justify-between py-2">
              <div>
                <p className="font-medium text-slate-800">{item.name}</p>
                {item.description && <p className="text-xs text-slate-500">{item.description}</p>}
              </div>
              <div>
                <Button variant="ghost" onClick={() => startEdit(item)}>
                  แก้ไข
                </Button>
                <Button variant="ghost-danger" onClick={() => remove(item)}>
                  ลบ
                </Button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}
