"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { onUnauthorized, tokenStorage } from "@/lib/api";
import { authApi, RegisterPayload, User } from "@/lib/auth";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<User>;
  register: (payload: RegisterPayload) => Promise<User>;
  logout: () => void;
  setUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [user, setUserState] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    tokenStorage.clear();
    setUserState(null);
    router.push("/login");
  }, [router]);

  useEffect(() => {
    onUnauthorized(logout);
    return () => onUnauthorized(null);
  }, [logout]);

  useEffect(() => {
    let cancelled = false;
    const restore = async () => {
      if (!tokenStorage.get()) {
        setLoading(false);
        return;
      }
      try {
        const me = await authApi.me();
        if (!cancelled) setUserState(me);
      } catch {
        tokenStorage.clear();
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    restore();
    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login(email, password);
    tokenStorage.set(response.accessToken);
    setUserState(response.user);
    return response.user;
  }, []);

  const register = useCallback(async (payload: RegisterPayload) => {
    const response = await authApi.register(payload);
    tokenStorage.set(response.accessToken);
    setUserState(response.user);
    return response.user;
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ user, loading, login, register, logout, setUser: setUserState }),
    [user, loading, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth ต้องใช้ภายใน AuthProvider");
  return context;
}
