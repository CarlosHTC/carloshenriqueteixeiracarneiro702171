export function parseJwtPayload(token: string): any | null {
    try {
      const parts = token.split(".");
      if (parts.length !== 3) return null;
  
      const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
      const json = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
  
      return JSON.parse(json);
    } catch {
      return null;
    }
  }
  
  export function getJwtExpMs(token: string): number | null {
    const payload = parseJwtPayload(token);
    if (!payload?.exp) return null;
    return Number(payload.exp) * 1000;
  }
  