import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../artistas.api", () => ({
  listarArtistas: vi.fn(),
  criarArtista: vi.fn(),
  atualizarArtista: vi.fn(),
  removerArtista: vi.fn(),
}));

vi.mock("../artista-foto.api", () => ({
  uploadFotoArtista: vi.fn(),
}));

describe("artistasFacade", () => {
  beforeEach(() => {
    vi.resetModules();
    vi.clearAllMocks();
  });

  it("loads paginated data with search filter and sort order", async () => {
    const { listarArtistas } = await import("../artistas.api");
    vi.mocked(listarArtistas).mockResolvedValueOnce({
      items: [{ id: 1, nome: "Ana", tipo: "BANDA" }],
      page: { number: 0, size: 10, totalElements: 1, totalPages: 1 },
    });

    const { artistasFacade } = await import("./artistas.facade");
    artistasFacade.setSearch("  Ana  ");

    await artistasFacade.loadPage(0, false);

    expect(listarArtistas).toHaveBeenCalledWith({
      page: 0,
      size: 24,
      nome: "Ana",
      sort: "nome,asc",
      regionalId: undefined,
      tipo: undefined,
    });

    const state = artistasFacade.getSnapshot();
    expect(state.artistas).toHaveLength(1);
    expect(state.totalRecords).toBe(1);
  });

  it("appends results when requested", async () => {
    const { listarArtistas } = await import("../artistas.api");
    vi.mocked(listarArtistas)
      .mockResolvedValueOnce({
        items: [{ id: 1, nome: "Ana", tipo: "BANDA" }],
        page: { number: 0, size: 24, totalElements: 2, totalPages: 1 },
      })
      .mockResolvedValueOnce({
        items: [{ id: 2, nome: "Bia", tipo: "SOLO" }],
        page: { number: 1, size: 24, totalElements: 2, totalPages: 1 },
      });

    const { artistasFacade } = await import("./artistas.facade");

    await artistasFacade.loadPage(0, false);
    await artistasFacade.loadPage(1, true);

    const state = artistasFacade.getSnapshot();
    expect(state.artistas.map((a) => a.id)).toEqual([1, 2]);
  });
});
