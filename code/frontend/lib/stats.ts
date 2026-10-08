import { http } from "./api";
import { BookingStatus } from "./bookings";

export interface StatsSummary {
  totalBookings: number;
  todayBookings: number;
  pendingApprovals: number;
  totalRooms: number;
  totalUsers: number;
  bookingsByStatus: Record<BookingStatus, number>;
}

export interface RoomUsage {
  roomId: number;
  roomCode: string;
  roomName: string;
  bookingCount: number;
  totalHours: number;
}

export const statsApi = {
  summary: () => http.get<StatsSummary>("/api/v1/stats/summary"),
  roomUsage: (from: string, to: string) => http.get<RoomUsage[]>("/api/v1/stats/room-usage", { from, to }),
};
