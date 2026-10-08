import { http, PageResponse } from "./api";

export type Role = "STUDENT" | "LECTURER" | "STAFF" | "ADMIN";
export type UserStatus = "ACTIVE" | "INACTIVE";

export const ROLE_LABELS: Record<Role, string> = {
  STUDENT: "นักศึกษา",
  LECTURER: "อาจารย์",
  STAFF: "เจ้าหน้าที่",
  ADMIN: "ผู้ดูแลระบบ",
};

export interface User {
  id: number;
  email: string;
  role: Role;
  status: UserStatus;
  fullName: string | null;
  studentCode: string | null;
  phone: string | null;
  department: string | null;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface RegisterPayload {
  email: string;
  password: string;
  fullName: string;
  studentCode?: string;
  phone?: string;
  department?: string;
  role?: Role;
}

export interface ProfilePayload {
  fullName: string;
  studentCode?: string;
  phone?: string;
  department?: string;
}

export interface UpdateUserPayload extends ProfilePayload {
  role: Role;
  status: UserStatus;
}

export const STAFF_ROLES: Role[] = ["STAFF", "ADMIN"];

export function isStaff(user: User | null | undefined): boolean {
  return !!user && STAFF_ROLES.includes(user.role);
}

export const authApi = {
  login: (email: string, password: string) => http.post<AuthResponse>("/api/v1/auth/login", { email, password }),
  register: (payload: RegisterPayload) => http.post<AuthResponse>("/api/v1/auth/register", payload),
  me: () => http.get<User>("/api/v1/users/me"),
  updateProfile: (payload: ProfilePayload) => http.put<User>("/api/v1/users/me/profile", payload),
};

export const userApi = {
  list: (params: { page?: number; size?: number; sort?: string; role?: Role | "" }) =>
    http.get<PageResponse<User>>("/api/v1/users", params),
  get: (id: number) => http.get<User>(`/api/v1/users/${id}`),
  update: (id: number, payload: UpdateUserPayload) => http.put<User>(`/api/v1/users/${id}`, payload),
  deactivate: (id: number) => http.delete(`/api/v1/users/${id}`),
};
