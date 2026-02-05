import { BehaviorSubject } from "rxjs";
import type { IArtista } from "../../../shared/types/IArtista";
import type { IAlbum, IAlbumComCapa } from "../../../shared/types/IAlbum";
import type { AlbumCapaDraft } from "../../albuns/types";
import { atualizarArtista, buscarArtistaPorId, removerArtista } from "../artistas.api";
import { atualizarAlbum, criarAlbum, listarAlbunsPorArtista, removerAlbum } from "../../albuns/albuns.api";
import { definirCapaPrincipal, removerCapa, uploadCapas } from "../../albuns/album-capas.api";
import { uploadFotoArtista } from "../artista-foto.api";

export interface ArtistaDetalheState {
    artista: IArtista | null;
    albums: IAlbumComCapa[];
    albumPage: number;
    albumTotalRecords: number;
    loading: boolean;
    error: string;
}

const ALBUMS_PAGE_SIZE = 12;

const initialState: ArtistaDetalheState = {
    artista: null,
    albums: [],
    albumPage: 0,
    albumTotalRecords: 0,
    loading: false,
    error: "",
};

class ArtistaDetalheFacade {
    private readonly _state$ = new BehaviorSubject<ArtistaDetalheState>(initialState);
    readonly state$ = this._state$.asObservable();

    private readonly _albumSelecionado$ = new BehaviorSubject<IAlbumComCapa | null>(null);
    readonly albumSelecionado$ = this._albumSelecionado$.asObservable();

    private artistaId: number | null = null;

    getSnapshot() {
        return this._state$.getValue();
    }

    getAlbumSelecionadoSnapshot() {
        return this._albumSelecionado$.getValue();
    }

    setArtistaId(id: number | null) {
        if (this.artistaId === id) {
            const state = this._state$.getValue();
            if (id != null && !state.loading && !state.artista) {
                void this.buscarArtista(id);
                void this.buscarAlbuns(id, 0);
            }
            return;
        }

        this.artistaId = id;
        this.resetState();

        if (id == null) return;

        void this.buscarArtista(id);
        void this.buscarAlbuns(id, 0);
    }

    setArtistaFromList(artista: IArtista) {
        this.artistaId = artista.id;
        this.patch({
            artista,
            albumTotalRecords: artista.qtdAlbuns ?? 0,
            loading: false,
            error: "",
        });
        void this.buscarAlbuns(artista.id, 0);
    }

    resetState() {
        this._state$.next({ ...initialState });
        this._albumSelecionado$.next(null);
    }

    setError(message: string) {
        this.patch({ error: message });
    }

    clearError() {
        this.patch({ error: "" });
    }

    setAlbumSelecionado(album: IAlbumComCapa | null) {
        this._albumSelecionado$.next(album);
    }

    async buscarArtista(id: number) {
        this.clearError();
        this.patch({ loading: true });
        try {
            const artistaData = await buscarArtistaPorId(id);
            this.patch({ artista: artistaData });
        } catch {
            this.patch({
                error: "Falha ao carregar dados.",
                albums: [],
                albumTotalRecords: 0,
            });
        } finally {
            this.patch({ loading: false });
        }
    }

    async buscarAlbuns(artistaId: number, page = 0) {
        this.clearError();
        this.patch({ loading: true });
        try {
            const result = await listarAlbunsPorArtista(artistaId, {
                page,
                size: ALBUMS_PAGE_SIZE,
                sort: "nome,asc",
            });
            this.patch({
                albums: result.items,
                albumPage: result.page.number,
                albumTotalRecords: result.page.totalElements,
            });
        } catch {
            this.patch({ error: "Falha ao carregar dados.", albums: [] });
        } finally {
            this.patch({ loading: false });
        }
    }

    async saveAlbum(
        artistaId: number,
        data: { id?: number; nome: string; generoIds: number[]; capas: AlbumCapaDraft[]; removedCapaIds: number[] }
    ) {
        if (!data.nome) return false;

        this.clearError();
        this.patch({ loading: true });
        try {
            let album: IAlbum;
            if (data.id) {
                album = await atualizarAlbum(data.id, {
                    nome: data.nome,
                    artistaId,
                    generoIds: data.generoIds,
                });
            } else {
                album = await criarAlbum({
                    nome: data.nome,
                    artistaId,
                    generoIds: data.generoIds,
                });
            }

            if (album.id) {
                const removidas = data.removedCapaIds ?? [];
                if (removidas.length > 0) {
                    await Promise.all(
                        removidas.map(async (id) => {
                            try {
                                await removerCapa(album.id!, id);
                            } catch (err) {
                                console.error("Erro ao remover capa:", err);
                            }
                        })
                    );
                }

                const novas = (data.capas ?? []).filter((c) => c.file);
                let uploadResponses: Awaited<ReturnType<typeof uploadCapas>> = [];
                if (novas.length > 0) {
                    try {
                        uploadResponses = await uploadCapas(
                            album.id,
                            novas.map((c) => c.file as File)
                        );
                    } catch (err) {
                        console.error("Erro ao fazer upload da capa:", err);
                    }
                }

                const principalSelecionada = (data.capas ?? []).find((c) => c.principal);
                let principalId: number | null = null;

                if (principalSelecionada?.id) {
                    principalId = principalSelecionada.id;
                } else if (principalSelecionada?.localId && novas.length > 0) {
                    const index = novas.findIndex((c) => c.localId === principalSelecionada.localId);
                    if (index >= 0 && uploadResponses[index]) {
                        principalId = uploadResponses[index].id;
                    }
                }

                if (!principalId && (data.capas ?? []).length > 0) {
                    const existente = (data.capas ?? []).find((c) => c.id);
                    if (existente?.id) {
                        principalId = existente.id;
                    } else if (uploadResponses[0]) {
                        principalId = uploadResponses[0].id;
                    }
                }

                if (principalId) {
                    try {
                        await definirCapaPrincipal(album.id, principalId);
                    } catch (err) {
                        console.error("Erro ao definir capa principal:", err);
                    }
                }
            }

            await this.buscarAlbuns(artistaId, 0);
            return true;
        } catch (err) {
            this.patch({ error: "Falha ao salvar álbum." });
            console.error(err);
            return false;
        } finally {
            this.patch({ loading: false });
        }
    }

    async deleteAlbum(album: IAlbumComCapa) {
        this.clearError();
        this.patch({ loading: true });
        try {
            await removerAlbum(album.id);
            const artista = this._state$.getValue().artista;
            if (artista) {
                await this.buscarAlbuns(artista.id, 0);
            }
            return true;
        } catch (err) {
            this.patch({ error: "Falha ao excluir álbum." });
            console.error(err);
            return false;
        } finally {
            this.patch({ loading: false });
        }
    }

    async updateArtista(data: { id?: number; nome: string; tipo: IArtista["tipo"]; regionalIds?: number[], artistaFoto?: File | null}) {
        const artista = this._state$.getValue().artista;
        if (!artista || !data.nome || !data.tipo) return false;

        this.clearError();
        this.patch({ loading: true });
        try {
            await atualizarArtista(artista.id, { nome: data.nome, tipo: data.tipo, regionalIds: data.regionalIds });
            let fotoUrl = artista.fotoUrl;

            if (data.artistaFoto && artista.id) {
                try {
                    const foto = await uploadFotoArtista(artista.id, data.artistaFoto);
                    fotoUrl = foto?.url ?? fotoUrl;
                } catch (err) {
                    console.error("Erro ao fazer upload da capa:", err);
                }
            }

            this.patch({
                artista: {
                    ...artista,
                    nome: data.nome,
                    tipo: data.tipo,
                    regionalIds: data.regionalIds,
                    fotoUrl,
                },
            });

            await this.buscarAlbuns(artista.id, 0);
            return true;
        } catch (err) {
            this.patch({ error: "Falha ao atualizar artista." });
            console.error(err);
            return false;
        } finally {
            this.patch({ loading: false });
        }
    }

    async deleteArtista() {
        const artista = this._state$.getValue().artista;
        if (!artista) return false;

        this.clearError();
        this.patch({ loading: true });
        try {
            await removerArtista(artista.id);
            return true;
        } catch (err) {
            this.patch({ error: "Falha ao excluir artista." });
            console.error(err);
            return false;
        } finally {
            this.patch({ loading: false });
        }
    }

    private patch(partial: Partial<ArtistaDetalheState>) {
        this._state$.next({ ...this._state$.getValue(), ...partial });
    }
}

export const artistaDetalheFacade = new ArtistaDetalheFacade();
export { ALBUMS_PAGE_SIZE };
