"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { errorMessage, PageResponse } from "@/lib/api";
import { ROLE_LABELS, Role, STAFF_ROLES, UpdateUserPayload, User, userApi } from "@/lib/auth";
import { formatDate } from "@/lib/format";
import { RequireAuth } from "@/components/RouteGuard";
import { Alert, Badge, Button, Card, EmptyState, Field, Input, PageHeader, Pagination, Select, Spinner } from "@/components/ui";

export default function AdminUsersPage() {
  return (
    <RequireAuth roles={STAFF_ROLES}>
      <UserManagement />
    </RequireAuth>
  );
}

function UserManagement() {
  const [page, setPage] = useState(0);
  const [role, setRole] = useState<Role | "">("");
  const [data, setData] = useState<PageResponse<User> | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState<User | null>(null);

  const load = useCallback(
    () =>
      userApi
        .list({ page, size: 10, sort: "id,asc", role })
        .then((result) => {
          setData(result);
          setError(null);
        })
        .catch((err) => setError(errorMessage(err))),
    [page, role],
  );

  useEffect(() => {
    load();
  }, [load]);

  const deactivate = async (user: User) => {
    if (!confirm(`ปิดใช้งานบัญชี ${user.email}?`)) return;
    try {
      await userApi.deactivate(user.id);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader title="จัดการผู้ใช้" subtitle="แก้บทบาท สถานะ และข้อมูลผู้ใช้" />
      <div className="mb-4 flex items-end gap-3">
        <Field label="กรองตามบทบาท">
          <Select
            value={role}
            onChange={(e) => {
              setRole(e.target.value as Role | "");
              setPage(0);
            }}
          >
            <option value="">ทั้งหมด</option>
            {Object.entries(ROLE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
      </div>
      {error && <Alert tone="error">{error}</Alert>}
      {editing && (
        <EditUserForm
          user={editing}
          onClose={() => setEditing(null)}
          onSaved={async () => {
            setEditing(null);
            await load();
          }}
        />
      )}
      {!data ? (
        <Spinner />
      ) : data.content.length === 0 ? (
        <EmptyState>ไม่พบผู้ใช้</EmptyState>
      ) : (
        <Card className="overflow-x-auto p-0">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="px-4 py-3">ชื่อ</th>
                <th className="px-4 py-3">อีเมล</th>
                <th className="px-4 py-3">บทบาท</th>
                <th className="px-4 py-3">สถานะ</th>
                <th className="px-4 py-3">สมัครเมื่อ</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data.content.map((user) => (
                <tr key={user.id}>
                  <td className="px-4 py-3 font-medium text-slate-900">{user.fullName}</td>
                  <td className="px-4 py-3 text-slate-600">{user.email}</td>
                  <td className="px-4 py-3">{ROLE_LABELS[user.role]}</td>
                  <td className="px-4 py-3">
                    <Badge className={user.status === "ACTIVE" ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-500"}>
                      {user.status === "ACTIVE" ? "ใช้งาน" : "ปิดใช้งาน"}
                    </Badge>
                  </td>
                  <td className="px-4 py-3 text-slate-500">{formatDate(user.createdAt)}</td>
                  <td className="px-4 py-3 text-right">
                    <Button variant="ghost" onClick={() => setEditing(user)}>
                      แก้ไข
                    </Button>
                    {user.status === "ACTIVE" && (
                      <Button variant="ghost-danger" onClick={() => deactivate(user)}>
                        ปิดใช้งาน
                      </Button>
                    )}
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

function EditUserForm({ user, onClose, onSaved }: { user: User; onClose: () => void; onSaved: () => void }) {
  const [form, setForm] = useState<UpdateUserPayload>({
    role: user.role,
    status: user.status,
    fullName: user.fullName ?? "",
    studentCode: user.studentCode ?? "",
    phone: user.phone ?? "",
    department: user.department ?? "",
  });
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await userApi.update(user.id, form);
      onSaved();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <Card className="mb-4">
      <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-3">
        <h2 className="sm:col-span-3 font-semibold text-slate-900">แก้ไข {user.email}</h2>
        {error && (
          <div className="sm:col-span-3">
            <Alert tone="error">{error}</Alert>
          </div>
        )}
        <Field label="ชื่อ-นามสกุล">
          <Input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
        </Field>
        <Field label="บทบาท">
          <Select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value as Role })}>
            {Object.entries(ROLE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="สถานะ">
          <Select
            value={form.status}
            onChange={(e) => setForm({ ...form, status: e.target.value as UpdateUserPayload["status"] })}
          >
            <option value="ACTIVE">ใช้งาน</option>
            <option value="INACTIVE">ปิดใช้งาน</option>
          </Select>
        </Field>
        <Field label="รหัสนักศึกษา">
          <Input value={form.studentCode} onChange={(e) => setForm({ ...form, studentCode: e.target.value })} />
        </Field>
        <Field label="เบอร์โทร">
          <Input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </Field>
        <Field label="สาขา / หน่วยงาน">
          <Input value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} />
        </Field>
        <div className="sm:col-span-3 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            ยกเลิก
          </Button>
          <Button type="submit">บันทึก</Button>
        </div>
      </form>
    </Card>
  );
}
