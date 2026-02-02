import { describe, expect, it, vi } from "vitest";

vi.mock("../../services/http/axios", () => ({
  http: {
    post: vi.fn(),
    get: vi.fn(),
    delete: vi.fn(),
  },
}));

describe("album-capas.api", () => {
  it("uploads multiple album covers with multipart/form-data", async () => {
    const { http } = await import("../../services/http/axios");
    vi.mocked(http.post).mockResolvedValueOnce({
      data: [
        {
          id: 1,
          fileName: "cover1.png",
          contentType: "image/png",
          sizeBytes: 10,
          principal: true,
          url: "http://example/cover1.png",
        },
      ],
    });

    const { uploadCapas } = await import("./album-capas.api");

    const file1 = new File(["data"], "cover1.png", { type: "image/png" });
    const file2 = new File(["data"], "cover2.png", { type: "image/png" });

    const res = await uploadCapas(5, [file1, file2]);

    expect(res).toHaveLength(1);
    const [url, body, config] = vi.mocked(http.post).mock.calls[0];
    expect(url).toBe("/api/v1/albuns/5/capas");
    expect(body).toBeInstanceOf(FormData);
    const formData = body as FormData;
    expect(formData.getAll("files")).toEqual([file1, file2]);
    expect(config?.headers?.["Content-Type"]).toBe("multipart/form-data");
  });
});
