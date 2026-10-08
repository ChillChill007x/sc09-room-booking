import { http, PageResponse } from "./api";

export type RoomStatus = "ACTIVE" | "MAINTENANCE" | "INACTIVE";

export const ROOM_STATUS_LABELS: Record<RoomStatus, string> = {
  ACTIVE: "เปิดให้จอง",
  MAINTENANCE: "ปิดซ่อมบำรุง",
  INACTIVE: "เลิกใช้งาน",
};

export interface RoomType {
  id: number;
  name: string;
  description: string | null;
}

export interface Equipment {
  id: number;
  name: string;
  description: string | null;
}

export interface RoomEquipment {
  equipmentId: number;
  name: string;
  quantity: number;
}

export interface Room {
  id: number;
  code: string;
  name: string;
  floor: number;
  capacity: number;
  description: string | null;
  status: RoomStatus;
  roomType: RoomType;
  equipment: RoomEquipment[];
}

export interface RoomPayload {
  code: string;
  name: string;
  floor: number;
  capacity: number;
  description?: string;
  roomTypeId: number;
  status: RoomStatus;
}

export interface RoomClosure {
  id: number;
  roomId: number;
  startTime: string;
  endTime: string;
  reason: string;
  createdAt: string;
}

export interface RoomSearchParams {
  page?: number;
  size?: number;
  sort?: string;
  floor?: number | "";
  typeId?: number | "";
  minCapacity?: number | "";
  equipmentId?: number | "";
  keyword?: string;
  status?: RoomStatus | "";
}

export interface AvailableParams {
  start: string;
  end: string;
  minCapacity?: number | "";
  equipmentIds?: number[];
}

export const roomApi = {
  search: (params: RoomSearchParams) => http.get<PageResponse<Room>>("/api/v1/rooms", { ...params }),
  available: (params: AvailableParams) => http.get<Room[]>("/api/v1/rooms/available", { ...params }),
  get: (id: number) => http.get<Room>(`/api/v1/rooms/${id}`),
  create: (payload: RoomPayload) => http.post<Room>("/api/v1/rooms", payload),
  update: (id: number, payload: RoomPayload) => http.put<Room>(`/api/v1/rooms/${id}`, payload),
  remove: (id: number) => http.delete(`/api/v1/rooms/${id}`),
  updateEquipment: (id: number, items: { equipmentId: number; quantity: number }[]) =>
    http.put<Room>(`/api/v1/rooms/${id}/equipment`, items),
  closures: (id: number) => http.get<RoomClosure[]>(`/api/v1/rooms/${id}/closures`),
  addClosure: (id: number, payload: { startTime: string; endTime: string; reason: string }) =>
    http.post<RoomClosure>(`/api/v1/rooms/${id}/closures`, payload),
  removeClosure: (id: number, closureId: number) => http.delete(`/api/v1/rooms/${id}/closures/${closureId}`),
};

export const equipmentApi = {
  list: () => http.get<Equipment[]>("/api/v1/equipment"),
  create: (payload: { name: string; description?: string }) => http.post<Equipment>("/api/v1/equipment", payload),
  update: (id: number, payload: { name: string; description?: string }) =>
    http.put<Equipment>(`/api/v1/equipment/${id}`, payload),
  remove: (id: number) => http.delete(`/api/v1/equipment/${id}`),
};

export const roomTypeApi = {
  list: () => http.get<RoomType[]>("/api/v1/room-types"),
  create: (payload: { name: string; description?: string }) => http.post<RoomType>("/api/v1/room-types", payload),
  update: (id: number, payload: { name: string; description?: string }) =>
    http.put<RoomType>(`/api/v1/room-types/${id}`, payload),
  remove: (id: number) => http.delete(`/api/v1/room-types/${id}`),
};
