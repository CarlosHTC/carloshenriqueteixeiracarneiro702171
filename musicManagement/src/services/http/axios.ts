import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { authService } from "../auth/auth.service";

function notifySessionExpired(message: string) {
  window.dispatchEvent(
    new CustomEvent("auth:expired", { detail: { message } })
  );
}

type ToastSeverity = "success" | "info" | "warn" | "error";

function notifyToast(severity: ToastSeverity, summary: string, detail: string, life = 3500) {
  window.dispatchEvent(
    new CustomEvent("toast:show", {
      detail: {
        severity,
        summary,
        detail,
        life,
      },
    })
  );
}

function extractErrorMessage(error: AxiosError): string {
  const data: any = error.response?.data;
  if (typeof data === "string" && data.trim()) return data;
  if (data?.message && typeof data.message === "string") return data.message;
  if (data?.error && typeof data.error === "string") return data.error;
  if (error.message) return error.message;
  return "Erro inesperado ao comunicar com o servidor.";
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30_000,
});

function shouldSkipRefresh(config: InternalAxiosRequestConfig): boolean {
  const url = (config.url ?? "").toLowerCase();
  if (url.includes("/api/v1/auth/login")) return true;
  if (url.includes("/api/v1/auth/refresh")) return true;
  if (authService.getAccessTokenRemainingSeconds() > 10) return true;
  if ((config.headers as any)?.["X-Skip-Refresh"]) return true;

  return false;
}

let refreshPromise: Promise<string> | null = null;
let lastPresenceToastAt = 0;

function maybeNotifySessionExpiring(config: InternalAxiosRequestConfig) {
  const url = (config.url ?? "").toLowerCase();
  if (url.includes("/api/v1/auth/login") || url.includes("/api/v1/auth/refresh")) return;

  const accessRemaining = authService.getAccessTokenRemainingSeconds();
  const refreshRemaining = authService.getRefreshTokenRemainingSeconds();
  const isAccessExpired = accessRemaining <= 0;
  const isRefreshNearExpiry = refreshRemaining > 0 && refreshRemaining < 120;

  if (!isAccessExpired || !isRefreshNearExpiry) return;

  const now = Date.now();
  if (now - lastPresenceToastAt < 60_000) return;
  lastPresenceToastAt = now;

  notifyToast(
    "warn",
    "Sessão prestes a expirar",
    "Sua sessão está prestes a expirar. Você ainda está presente?",
    7000
  );
}

http.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  maybeNotifySessionExpiring(config);

  if (shouldSkipRefresh(config)) {
    if (authService.getAccessTokenRemainingSeconds() > 10) { 
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${authService.getAccessToken()}`;
    } else if (authService.getRefreshTokenRemainingSeconds() > 10){
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${authService.getRefreshToken()}`;
    } else {
      authService.logout();
    }

    return config;
  }

  if (!authService.isAuthenticated()) {
    notifySessionExpired("Sessão expirada. Faça login novamente.");
    authService.logout();

    throw new axios.CanceledError("Sessão expirada");
  }

  try {
    if (!refreshPromise) {
      refreshPromise = authService.refresh();
    }

    const newAccessToken = await refreshPromise;

    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${newAccessToken}`;
    return config;
  } catch (err) {
    notifySessionExpired("Sessão expirada. Faça login novamente.");
    authService.logout();

    throw new axios.CanceledError("Sessão expirada");
  } finally {
    refreshPromise = null;
  }
});

http.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const status = error.response?.status;

    if (status === 401) {
      notifySessionExpired("Sessão expirada. Faça login novamente.");
      authService.logout();
    }

    if (!axios.isCancel(error)) {
      notifyToast("error", "Erro na requisição", extractErrorMessage(error));
    }

    return Promise.reject(error);
  }
);
