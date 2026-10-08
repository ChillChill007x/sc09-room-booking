import { http, PageResponse } from "./api";

export type NotificationType =
  | "BOOKING_CREATED"
  | "BOOKING_APPROVED"
  | "BOOKING_REJECTED"
  | "BOOKING_CANCELLED"
  | "BOOKING_NO_SHOW";

export interface AppNotification {
  id: number;
  bookingId: number | null;
  type: NotificationType;
  message: string;
  read: boolean;
  createdAt: string;
}

/** แจ้งให้กระดิ่งใน navbar โหลดจำนวนใหม่ทันทีหลังอ่านแจ้งเตือน */
export const NOTIFICATIONS_CHANGED = "sc09:notifications-changed";

export function announceNotificationsChanged() {
  window.dispatchEvent(new Event(NOTIFICATIONS_CHANGED));
}

export const notificationApi = {
  mine: (page: number, size = 20) =>
    http.get<PageResponse<AppNotification>>("/api/v1/users/me/notifications", { page, size, sort: "createdAt,desc" }),
  unreadCount: () => http.get<{ count: number }>("/api/v1/users/me/notifications/unread-count"),
  markRead: (id: number) => http.patch<AppNotification>(`/api/v1/notifications/${id}/read`),
  markAllRead: () => http.patch<{ count: number }>("/api/v1/users/me/notifications/read-all"),
  remove: (id: number) => http.delete(`/api/v1/notifications/${id}`),
};
