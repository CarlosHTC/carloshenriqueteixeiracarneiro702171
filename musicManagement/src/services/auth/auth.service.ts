import { tokenStorage } from "./token.storage";
import * as authApi from "./auth.api";

const SKEW_SECONDS = 10;

function expiresInSeconds(expiresAtMs: number): number {
  return Math.floor((expiresAtMs - Date.now()) / 1000);
}

function computeExpiresAtMs(expiresInMs: number): number {
  return Date.now() + Math.max(0, expiresInMs);
}

export const authService = {
  isAuthenticated(): boolean {
    const tokens = tokenStorage.get();
    if (!tokens?.refreshToken) return false;

    return expiresInSeconds(tokens.refreshExpiresAtMs) > SKEW_SECONDS;
  },

  getAccessToken(): string | null {
    return tokenStorage.get()?.accessToken ?? null;
  },

  getRefreshToken(): string | null {
    return tokenStorage.get()?.refreshToken ?? null;
  },

  getAccessTokenRemainingSeconds(): number {
    const t = tokenStorage.get();
    if (!t) return 0;
    return Math.max(0, expiresInSeconds(t.accessExpiresAtMs));
  },

  getRefreshTokenRemainingSeconds(): number {
    const t = tokenStorage.get();
    if (!t) return 0;
    return Math.max(0, expiresInSeconds(t.refreshExpiresAtMs));
  },

  async login(username: string, password: string) {
    const res = await authApi.login({ username, password });

    tokenStorage.set({
      accessToken: res.accessToken,
      refreshToken: res.refreshToken,
      accessExpiresAtMs: computeExpiresAtMs(res.accessExpiresInMs),
      refreshExpiresAtMs: computeExpiresAtMs(res.refreshExpiresInMs),
    });
  },

  async refresh(): Promise<string> {
    const current = tokenStorage.get();
    if (!current?.refreshToken) throw new Error("Missing refresh token");

    if (expiresInSeconds(current.refreshExpiresAtMs) <= SKEW_SECONDS) {
      throw new Error("Refresh token expired");
    }

    const res = await authApi.refresh(current.refreshToken);

    tokenStorage.set({
      accessToken: res.accessToken,
      refreshToken: res.refreshToken ?? current.refreshToken,
      accessExpiresAtMs: computeExpiresAtMs(res.accessExpiresInMs),
      refreshExpiresAtMs: computeExpiresAtMs(res.refreshExpiresInMs),
    });

    return res.accessToken;
  },

  logout() {
    tokenStorage.clear();
  },
};
