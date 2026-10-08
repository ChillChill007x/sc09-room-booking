import { http } from "./api";
import { Booking, BookingStatus } from "./bookings";

export type BookingAction = "APPROVE" | "REJECT" | "CANCEL" | "CHECK_IN" | "COMPLETE" | "MARK_NO_SHOW";

export const ACTION_LABELS: Record<BookingAction, string> = {
  APPROVE: "อนุมัติ",
  REJECT: "ปฏิเสธ",
  CANCEL: "ยกเลิกการจอง",
  CHECK_IN: "Check-in",
  COMPLETE: "จบการใช้งาน",
  MARK_NO_SHOW: "ตั้งเป็นไม่มาใช้ห้อง",
};

export interface BookingHistory {
  id: number;
  fromStatus: BookingStatus | null;
  toStatus: BookingStatus;
  changedById: number | null;
  changedByName: string;
  note: string | null;
  changedAt: string;
}

export interface AllowedActions {
  bookingId: number;
  status: BookingStatus;
  actions: BookingAction[];
}

export const lifecycleApi = {
  changeStatus: (bookingId: number, action: BookingAction, note?: string) =>
    http.patch<Booking>(`/api/v1/bookings/${bookingId}/status`, { action, note }),
  history: (bookingId: number) => http.get<BookingHistory[]>(`/api/v1/bookings/${bookingId}/history`),
  allowedActions: (bookingId: number) => http.get<AllowedActions>(`/api/v1/bookings/${bookingId}/allowed-actions`),
};
