"use client";

import { FormEvent, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { errorMessage } from "@/lib/api";
import { authApi, ProfilePayload, ROLE_LABELS, User } from "@/lib/auth";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Button, Card, Field, Input, PageHeader } from "@/components/ui";

export default function ProfilePage() {
  return (
    <RequireAuth>
      <ProfileForm />
    </RequireAuth>
  );
}

function toPayload(user: User): ProfilePayload {
  return {
    fullName: user.fullName ?? "",
    studentCode: user.studentCode ?? "",
    phone: user.phone ?? "",
    department: user.department ?? "",
  };
}

function ProfileForm() {
  const { user, setUser } = useAuth();
  const [form, setForm] = useState<ProfilePayload>(() => toPayload(user!));
  const [message, setMessage] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [saving, setSaving] = useState(false);

  const update = (key: keyof ProfilePayload, value: string) => setForm((prev) => ({ ...prev, [key]: value }));

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      const updated = await authApi.updateProfile(form);
      setUser(updated);
      setMessage({ tone: "success", text: "บันทึกข้อมูลแล้ว" });
    } catch (err) {
      setMessage({ tone: "error", text: errorMessage(err) });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl">
      <PageHeader title="ข้อมูลส่วนตัว" subtitle={`${user!.email} · ${ROLE_LABELS[user!.role]}`} />
      <Card>
        <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-2">
          {message && (
            <div className="sm:col-span-2">
              <Alert tone={message.tone}>{message.text}</Alert>
            </div>
          )}
          <div className="sm:col-span-2">
            <Field label="ชื่อ-นามสกุล">
              <Input required value={form.fullName} onChange={(e) => update("fullName", e.target.value)} />
            </Field>
          </div>
          <Field label="รหัสนักศึกษา">
            <Input value={form.studentCode} onChange={(e) => update("studentCode", e.target.value)} />
          </Field>
          <Field label="เบอร์โทร">
            <Input value={form.phone} onChange={(e) => update("phone", e.target.value)} />
          </Field>
          <div className="sm:col-span-2">
            <Field label="สาขา / หน่วยงาน">
              <Input value={form.department} onChange={(e) => update("department", e.target.value)} />
            </Field>
          </div>
          <div className="sm:col-span-2 flex justify-end">
            <Button type="submit" disabled={saving}>
              {saving ? "กำลังบันทึก..." : "บันทึก"}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
