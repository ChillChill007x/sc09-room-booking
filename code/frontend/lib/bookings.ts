import { http, PageResponse } from "./api";

export type BookingStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED" | "CHECKED_IN" | "COMPLETED" | "NO_SHOW";

export const BOOKING_STATUS_LABELS: Record<BookingStatus, string> = {
  PENDING: "รออนุมัติ",
  APPROVED: "อนุมัติแล้ว",
  REJECTED: "ถูกปฏิเสธ",
  CANCELLED: "ยกเลิก",
  CHECKED_IN: "กำลังใช้ห้อง",
  COMPLETED: "เสร็จสิ้น",
  NO_SHOW: "ไม่มาใช้ห้อง",
};

export interface Booking {
  id: number;
  roomId: number;
  roomCode: string;
  roomName: string;
  userId: number;
  userEmail: string;
  userFullName: string | null;
  startTime: string;
  endTime: string;
  purpose: string;
  attendees: number;
  status: BookingStatus;
  createdAt: string;
  updatedAt: string;
}

export interface BookingSlot {
  id: number;
  startTime: string;
  endTime: string;
  status: BookingStatus;
}

export interface BookingPayload {
  roomId: number;
  startTime: string;
  endTime: string;
  purpose: string;
  attendees: number;
}

export interface BookingFilter {
  page?: number;
  size?: number;
  sort?: string;
  status?: BookingStatus | "";
  roomId?: number | "";
  from?: string;
  to?: string;
}

export const bookingApi = {
  create: (payload: BookingPayload) => http.post<Booking>("/api/v1/bookings", payload),
  get: (id: number) => http.get<Booking>(`/api/v1/bookings/${id}`),
  update: (id: number, payload: BookingPayload) => http.put<Booking>(`/api/v1/bookings/${id}`, payload),
  remove: (id: number) => http.delete(`/api/v1/bookings/${id}`),
  list: (filter: BookingFilter) => http.get<PageResponse<Booking>>("/api/v1/bookings", { ...filter }),
  byUser: (userId: number, params: { page?: number; size?: number; sort?: string }) =>
    http.get<PageResponse<Booking>>(`/api/v1/users/${userId}/bookings`, params),
  roomSchedule: (roomId: number, date: string) =>
    http.get<BookingSlot[]>(`/api/v1/rooms/${roomId}/bookings`, { date }),
};
