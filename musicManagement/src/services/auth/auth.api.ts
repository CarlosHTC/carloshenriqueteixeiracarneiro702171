import { http } from "../http/axios";

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  accessExpiresInMs: number;
  refreshExpiresInMs: number;
};

export async function login(req: LoginRequest): Promise<LoginResponse> {
  const { data } = await http.post<LoginResponse>("/api/v1/auth/login", req);
  return data;
}

export type RefreshResponse = {
  accessToken: string;
  refreshToken: string;
  accessExpiresInMs: number;
  refreshExpiresInMs: number;
};

export async function refresh(refreshToken: string): Promise<RefreshResponse> {
  const { data } = await http.post<RefreshResponse>(
    "/api/v1/auth/refresh", {
      refreshToken: refreshToken
    },
  );
  return data;
}
