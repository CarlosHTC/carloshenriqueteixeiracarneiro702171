import { describe, expect, it, vi } from "vitest";

vi.mock("../../services/http/axios", () => ({
  http: {
    post: vi.fn(),
    get: vi.fn(),
    delete: vi.fn(),
  },
}));

describe("artista-foto.api", () => {
  it("uploads artist photo with multipart/form-data", async () => {
    const { http } = await import("../../services/http/axios");
    vi.mocked(http.post).mockResolvedValueOnce({
      data: { id: 1, contentType: "image/png", sizeBytes: 10, url: "http://example/foto.png" },
    });

    const { uploadFotoArtista } = await import("./artista-foto.api");

    const file = new File(["data"], "foto.png", { type: "image/png" });
    const res = await uploadFotoArtista(10, file);

    expect(res.url).toBe("http://example/foto.png");
    expect(http.post).toHaveBeenCalledTimes(1);
    const [url, body, config] = vi.mocked(http.post).mock.calls[0];
    expect(url).toBe("/api/v1/artistas/10/foto");
    expect(body).toBeInstanceOf(FormData);
    expect((body as FormData).get("file")).toBe(file);
    expect(config?.headers?.["Content-Type"]).toBe("multipart/form-data");
  });
});
