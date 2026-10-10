"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { ApiError, errorMessage } from "@/lib/api";
import { RegisterPayload, Role } from "@/lib/auth";
import { Alert, Button, Card, Field, Input, Select } from "@/components/ui";

const EMPTY: RegisterPayload = {
  email: "",
  password: "",
  fullName: "",
  studentCode: "",
  phone: "",
  department: "",
  role: "STUDENT",
};

export default function RegisterPage() {
  const { register } = useAuth();
  const router = useRouter();
  const [form, setForm] = useState<RegisterPayload>(EMPTY);
  const [error, setError] = useState<ApiError | string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const update = (key: keyof RegisterPayload) => (value: string) => setForm((prev) => ({ ...prev, [key]: value }));
  const fieldError = (field: string) => (error instanceof ApiError ? error.fieldMessage(field) : undefined);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(form);
      router.push("/rooms");
    } catch (err) {
      setError(err instanceof ApiError ? err : errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg py-10">
      <Card>
        <h1 className="text-xl font-semibold text-slate-900">สมัครสมาชิก</h1>
        <form onSubmit={handleSubmit} className="mt-6 grid gap-4 sm:grid-cols-2">
          {error && (
            <div className="sm:col-span-2">
              <Alert tone="error">{typeof error === "string" ? error : error.message}</Alert>
            </div>
          )}
          <div className="sm:col-span-2">
            <Field label="อีเมล" error={fieldError("email")}>
              <Input type="email" required value={form.email} onChange={(e) => update("email")(e.target.value)} />
            </Field>
          </div>
          <div className="sm:col-span-2">
            <Field label="รหัสผ่าน (อย่างน้อย 8 ตัวอักษร)" error={fieldError("password")}>
              <Input
                type="password"
                required
                minLength={8}
                value={form.password}
                onChange={(e) => update("password")(e.target.value)}
              />
            </Field>
          </div>
          <div className="sm:col-span-2">
            <Field label="ชื่อ-นามสกุล" error={fieldError("fullName")}>
              <Input required value={form.fullName} onChange={(e) => update("fullName")(e.target.value)} />
            </Field>
          </div>
          <Field label="บทบาท">
            <Select value={form.role} onChange={(e) => update("role")(e.target.value as Role)}>
              <option value="STUDENT">นักศึกษา</option>
              <option value="LECTURER">อาจารย์</option>
            </Select>
          </Field>
          <Field label="รหัสนักศึกษา" error={fieldError("studentCode")}>
            <Input value={form.studentCode} onChange={(e) => update("studentCode")(e.target.value)} />
          </Field>
          <Field label="เบอร์โทร" error={fieldError("phone")}>
            <Input value={form.phone} onChange={(e) => update("phone")(e.target.value)} />
          </Field>
          <Field label="สาขา / หน่วยงาน">
            <Input value={form.department} onChange={(e) => update("department")(e.target.value)} />
          </Field>
          <div className="sm:col-span-2">
            <Button type="submit" className="w-full" disabled={submitting}>
              {submitting ? "กำลังสมัคร..." : "สมัครสมาชิก"}
            </Button>
          </div>
        </form>
        <p className="mt-4 text-center text-sm text-slate-500">
          มีบัญชีแล้ว?{" "}
          <Link href="/login" className="font-medium text-indigo-600 hover:underline">
            เข้าสู่ระบบ
          </Link>
        </p>
      </Card>
    </div>
  );
}
