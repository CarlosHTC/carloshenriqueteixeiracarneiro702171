import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { authService } from "../auth/auth.service";

function notifySessionExpired(message: string) {
  window.dispatchEvent(
    new CustomEvent("auth:expired", { detail: { message } })
  );
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

http.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
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

    return Promise.reject(error);
  }
);
