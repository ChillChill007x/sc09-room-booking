/**
 * API client กลางของทั้งระบบ ทุกหน้าเรียก backend ผ่านไฟล์นี้เท่านั้น
 * แนบ JWT ให้อัตโนมัติ และแปลง ErrorResponse ของ backend เป็น ApiError
 */

export const API_URL = (process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080").replace(/\/$/, "");

const TOKEN_KEY = "sc09.token";

export const tokenStorage = {
  get(): string | null {
    if (typeof window === "undefined") return null;
    try {
      return window.localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },
  set(token: string) {
    try {
      window.localStorage.setItem(TOKEN_KEY, token);
    } catch {
      // private mode: token อยู่ได้แค่ใน memory ของหน้านี้
    }
  },
  clear() {
    try {
      window.localStorage.removeItem(TOKEN_KEY);
    } catch {
      // ignore
    }
  },
};

export interface FieldError {
  field: string;
  message: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: FieldError[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: FieldError[];

  constructor(status: number, message: string, fieldErrors: FieldError[] = []) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }

  fieldMessage(field: string): string | undefined {
    return this.fieldErrors.find((error) => error.field === field)?.message;
  }
}

/** ดึงข้อความ error ที่แสดงให้ผู้ใช้อ่านได้ */
export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.fieldErrors.length > 0) {
      return `${error.message}: ${error.fieldErrors.map((f) => f.message).join(", ")}`;
    }
    return error.message;
  }
  return "เกิดข้อผิดพลาดที่ไม่คาดคิด";
}

type QueryValue = string | number | boolean | null | undefined | Array<string | number>;
export type Query = Record<string, QueryValue>;

export function toQuery(params?: Query): string {
  if (!params) return "";
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    if (Array.isArray(value)) {
      value.forEach((item) => search.append(key, String(item)));
    } else {
      search.append(key, String(value));
    }
  });
  const text = search.toString();
  return text ? `?${text}` : "";
}

let unauthorizedHandler: (() => void) | null = null;

/** AuthContext ลงทะเบียนไว้ เพื่อ logout อัตโนมัติเมื่อ token หมดอายุ */
export function onUnauthorized(handler: (() => void) | null) {
  unauthorizedHandler = handler;
}

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  query?: Query;
}

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const token = tokenStorage.get();
  const headers: Record<string, string> = { Accept: "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;
  if (options.body !== undefined) headers["Content-Type"] = "application/json";

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}${toQuery(options.query)}`, {
      method: options.method ?? "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
  } catch {
    throw new ApiError(0, "ติดต่อเซิร์ฟเวอร์ไม่ได้ กรุณาลองใหม่อีกครั้ง");
  }

  if (response.status === 204) return undefined as T;

  const data: unknown = await response.json().catch(() => null);
  if (!response.ok) {
    const error = (data ?? {}) as Partial<ErrorResponse>;
    if (response.status === 401 && token) unauthorizedHandler?.();
    throw new ApiError(response.status, error.message ?? `เกิดข้อผิดพลาด (${response.status})`, error.fieldErrors ?? []);
  }
  return data as T;
}

export const http = {
  get: <T>(path: string, query?: Query) => api<T>(path, { query }),
  post: <T>(path: string, body?: unknown) => api<T>(path, { method: "POST", body }),
  put: <T>(path: string, body?: unknown) => api<T>(path, { method: "PUT", body }),
  patch: <T>(path: string, body?: unknown) => api<T>(path, { method: "PATCH", body }),
  delete: (path: string) => api<void>(path, { method: "DELETE" }),
};
