import { beforeEach, describe, expect, it, vi } from "vitest";

import * as authApi from "./auth.api";
import { tokenStorage } from "./token.storage";
import { authService } from "./auth.service";

vi.mock("./auth.api", () => ({
  login: vi.fn(),
  refresh: vi.fn(),
}));

describe("authService", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it("stores tokens on login with computed expiration", async () => {
    const now = new Date("2026-02-01T12:00:00.000Z");
    vi.useFakeTimers();
    vi.setSystemTime(now);

    vi.mocked(authApi.login).mockResolvedValueOnce({
      accessToken: "access-1",
      refreshToken: "refresh-1",
      accessExpiresInMs: 10_000,
      refreshExpiresInMs: 20_000,
    });

    await authService.login("user", "pass");

    const stored = tokenStorage.get();
    expect(stored).toEqual({
      accessToken: "access-1",
      refreshToken: "refresh-1",
      accessExpiresAtMs: now.getTime() + 10_000,
      refreshExpiresAtMs: now.getTime() + 20_000,
    });

    vi.useRealTimers();
  });

  it("refreshes tokens and keeps refresh token when API omits it", async () => {
    const now = new Date("2026-02-01T12:00:00.000Z");
    vi.useFakeTimers();
    vi.setSystemTime(now);

    tokenStorage.set({
      accessToken: "old-access",
      refreshToken: "old-refresh",
      accessExpiresAtMs: now.getTime() - 5_000,
      refreshExpiresAtMs: now.getTime() + 60_000,
    });

    vi.mocked(authApi.refresh).mockResolvedValueOnce({
      accessToken: "new-access",
      refreshToken: undefined,
      accessExpiresInMs: 15_000,
      refreshExpiresInMs: 120_000,
    } as any);

    const token = await authService.refresh();

    expect(token).toBe("new-access");
    expect(tokenStorage.get()).toEqual({
      accessToken: "new-access",
      refreshToken: "old-refresh",
      accessExpiresAtMs: now.getTime() + 15_000,
      refreshExpiresAtMs: now.getTime() + 120_000,
    });

    vi.useRealTimers();
  });

  it("isAuthenticated returns false when refresh token is missing or expired", () => {
    const now = new Date("2026-02-01T12:00:00.000Z");
    vi.useFakeTimers();
    vi.setSystemTime(now);

    expect(authService.isAuthenticated()).toBe(false);

    tokenStorage.set({
      accessToken: "access",
      refreshToken: "refresh",
      accessExpiresAtMs: now.getTime() + 30_000,
      refreshExpiresAtMs: now.getTime() + 5_000,
    });

    expect(authService.isAuthenticated()).toBe(false);

    vi.useRealTimers();
  });
});
