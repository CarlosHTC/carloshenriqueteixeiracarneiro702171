import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../artistas.api", () => ({
  buscarArtistaPorId: vi.fn(),
  atualizarArtista: vi.fn(),
  removerArtista: vi.fn(),
}));

vi.mock("../../albuns/albuns.api", () => ({
  buscarAlbumPorId: vi.fn(),
  listarAlbunsPorArtista: vi.fn(),
  criarAlbum: vi.fn(),
  atualizarAlbum: vi.fn(),
  removerAlbum: vi.fn(),
}));

vi.mock("../../albuns/album-capas.api", () => ({
  uploadCapas: vi.fn(),
  removerCapa: vi.fn(),
  definirCapaPrincipal: vi.fn(),
}));

vi.mock("../artista-foto.api", () => ({
  uploadFotoArtista: vi.fn(),
}));

describe("artistaDetalheFacade", () => {
  beforeEach(() => {
    vi.resetModules();
    vi.clearAllMocks();
  });

  it("loads artist details and albums when artistaId is set", async () => {
    const { buscarArtistaPorId } = await import("../artistas.api");
    const { listarAlbunsPorArtista } = await import("../../albuns/albuns.api");

    vi.mocked(buscarArtistaPorId).mockResolvedValueOnce({
      id: 10,
      nome: "Artista Teste",
      tipo: "BANDA",
    });

    vi.mocked(listarAlbunsPorArtista).mockResolvedValueOnce({
      items: [
        {
          id: 1,
          nome: "Album 1",
          artista: 10,
          generos: [],
          faixas: [],
          capaPrincipal: {
            id: 1,
            fileName: "cover.png",
            contentType: "image/png",
            sizeBytes: 10,
            url: "http://example/cover.png",
          },
          createdAt: "",
          updatedAt: "",
          version: 0,
        },
      ],
      page: { number: 0, size: 12, totalElements: 1, totalPages: 1 },
    });

    const { artistaDetalheFacade } = await import("./artistaDetalhe.facade");

    artistaDetalheFacade.setArtistaId(10);
    await new Promise((r) => setTimeout(r, 0));

    const state = artistaDetalheFacade.getSnapshot();
    expect(state.artista?.id).toBe(10);
    expect(state.albums).toHaveLength(1);
  });

  it("uploads cover when saving album with a new cover", async () => {
    const { criarAlbum } = await import("../../albuns/albuns.api");
    const { uploadCapas } = await import("../../albuns/album-capas.api");
    const { definirCapaPrincipal } = await import("../../albuns/album-capas.api");
    const { artistaDetalheFacade } = await import("./artistaDetalhe.facade");

    vi.mocked(criarAlbum).mockResolvedValueOnce({
      id: 33,
      nome: "Novo Album",
      artista: 10,
      generos: [],
      faixas: [],
      capas: [],
      createdAt: "",
      updatedAt: "",
      version: 0,
    });

    vi.mocked(uploadCapas).mockResolvedValueOnce([
      {
        id: 1,
        fileName: "cover.png",
        contentType: "image/png",
        sizeBytes: 10,
        principal: true,
        url: "http://example/cover.png",
      },
    ]);

    const buscarAlbunsSpy = vi.spyOn(artistaDetalheFacade, "buscarAlbuns").mockResolvedValueOnce();

    const file = new File(["data"], "cover.png", { type: "image/png" });

    const ok = await artistaDetalheFacade.saveAlbum(10, {
      nome: "Novo Album",
      generoIds: [],
      capas: [
        {
          localId: "new-1",
          file,
          previewUrl: "data:image/png;base64,abc",
          fileName: "cover.png",
          contentType: "image/png",
          sizeBytes: 10,
          principal: true,
          source: "new",
        },
      ],
      removedCapaIds: [],
    });

    expect(ok).toBe(true);
    expect(uploadCapas).toHaveBeenCalledWith(33, [file]);
    expect(definirCapaPrincipal).toHaveBeenCalledWith(33, 1);
    expect(buscarAlbunsSpy).toHaveBeenCalledWith(10, 0);
  });

  it("uploads artist photo when updating artist with a new file", async () => {
    const { buscarArtistaPorId, atualizarArtista } = await import("../artistas.api");
    const { uploadFotoArtista } = await import("../artista-foto.api");
    const { artistaDetalheFacade } = await import("./artistaDetalhe.facade");

    vi.mocked(buscarArtistaPorId).mockResolvedValueOnce({
      id: 12,
      nome: "Artista",
      tipo: "BANDA",
    });

    vi.mocked(atualizarArtista).mockResolvedValueOnce({
      id: 12,
      nome: "Artista",
      tipo: "BANDA",
    });

    artistaDetalheFacade.setArtistaId(12);
    await new Promise((r) => setTimeout(r, 0));

    const foto = new File(["data"], "foto.png", { type: "image/png" });
    const ok = await artistaDetalheFacade.updateArtista({
      id: 12,
      nome: "Artista",
      tipo: "BANDA",
      artistaFoto: foto,
    });

    expect(ok).toBe(true);
    expect(uploadFotoArtista).toHaveBeenCalledWith(12, foto);
  });
});
