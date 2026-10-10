/** แปลงวันเวลาจาก backend (LocalDateTime แบบ ISO ไม่มี timezone) เป็นข้อความภาษาไทย */

const dateTimeFormat = new Intl.DateTimeFormat("th-TH", {
  day: "numeric",
  month: "short",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

const dateFormat = new Intl.DateTimeFormat("th-TH", { day: "numeric", month: "short", year: "numeric" });

const timeFormat = new Intl.DateTimeFormat("th-TH", { hour: "2-digit", minute: "2-digit" });

export function formatDateTime(value?: string | null): string {
  return value ? dateTimeFormat.format(new Date(value)) : "-";
}

export function formatDate(value?: string | null): string {
  return value ? dateFormat.format(new Date(value)) : "-";
}

export function formatTime(value?: string | null): string {
  return value ? timeFormat.format(new Date(value)) : "-";
}

export function formatRange(start: string, end: string): string {
  return `${formatDate(start)} ${formatTime(start)} - ${formatTime(end)}`;
}

const pad = (n: number) => String(n).padStart(2, "0");

/** yyyy-MM-dd ตามเวลาเครื่อง ใช้กับ input type="date" */
export function toDateInput(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** yyyy-MM-ddTHH:mm ใช้กับ input type="datetime-local" */
export function toDateTimeInput(date: Date): string {
  return `${toDateInput(date)}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

/** เติมวินาทีให้ค่าจาก datetime-local ก่อนส่งให้ backend */
export function toApiDateTime(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}
