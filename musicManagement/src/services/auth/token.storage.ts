export type StoredTokens = {
  accessToken: string;
  refreshToken: string;
  accessExpiresAtMs: number;
  refreshExpiresAtMs: number;
};

const KEY = "auth_tokens_v1";

export const tokenStorage = {
  get(): StoredTokens | null {
    const raw = localStorage.getItem(KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as StoredTokens;
    } catch {
      return null;
    }
  },

  set(tokens: StoredTokens) {
    localStorage.setItem(KEY, JSON.stringify(tokens));
  },

  clear() {
    localStorage.removeItem(KEY);
  },
};
