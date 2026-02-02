import { BehaviorSubject } from "rxjs";
import type { IArtista, TipoArtista } from "../../../shared/types/IArtista";
import type { SortOrder, ViewMode } from "../../../shared/types/type";
import { atualizarArtista, criarArtista, listarArtistas, removerArtista } from "../artistas.api";
import { uploadFotoArtista } from "../artista-foto.api";

export interface ArtistasState {
    artistas: IArtista[];
    selectedArtista: IArtista | null;
    search: string;
    sortOrder: SortOrder;
    viewMode: ViewMode;
    loading: boolean;
    page: number;
    totalRecords: number;
    totalPages: number;
    regionalId: number | null;
    tipo: TipoArtista | null;
}

export const ARTISTAS_GRID_PAGE_SIZE = 24;
export const ARTISTAS_TABLE_PAGE_SIZE = 10;

function getPageSize(viewMode: ViewMode) {
    return viewMode === "grid" ? ARTISTAS_GRID_PAGE_SIZE : ARTISTAS_TABLE_PAGE_SIZE;
}

const initialState: ArtistasState = {
    artistas: [],
    selectedArtista: null,
    search: "",
    sortOrder: "asc",
    viewMode: "grid",
    loading: false,
    page: 0,
    totalRecords: 0,
    totalPages: 0,
    regionalId: null,
    tipo: null,
};

class ArtistasFacade {
    private readonly _state$ = new BehaviorSubject<ArtistasState>(initialState);
    readonly state$ = this._state$.asObservable();

    getSnapshot() {
        return this._state$.getValue();
    }

    ensureLoaded() {
        const state = this._state$.getValue();
        if (state.loading || state.artistas.length > 0) return;
        void this.loadPage(0, false);
    }

    setSearch(search: string) {
        this.patch({ search });
        // void this.reloadFirstPage();
    }

    setSelectedArtista(artista: IArtista | null) {
        this.patch({ selectedArtista: artista });
    }

    updateSelectedArtista(updates: Partial<IArtista>) {
        const state = this._state$.getValue();
        if (!state.selectedArtista) return;
        this.patch({ selectedArtista: { ...state.selectedArtista, ...updates } });
    }

    setFilters(filters: { tipo: TipoArtista | null; regionalId: number | null }) {
        this.patch({ tipo: filters.tipo, regionalId: filters.regionalId });
        void this.reloadFirstPage();
    }

    setSortOrder(sortOrder: SortOrder) {
        this.patch({ sortOrder });
        void this.reloadFirstPage();
    }

    setViewMode(viewMode: ViewMode) {
        const state = this._state$.getValue();
        if (state.viewMode === viewMode) return;
        this.patch({ viewMode });
        void this.reloadFirstPage();
    }

    async loadPage(targetPage: number, append: boolean) {
        const state = this._state$.getValue();
        const pageSize = getPageSize(state.viewMode);

        this.patch({ loading: true });
        try {
            const result = await listarArtistas({
                page: targetPage,
                size: pageSize,
                nome: state.search ? state.search.trim() : undefined,
                sort: `nome,${state.sortOrder}`,
                regionalId: state.regionalId != null ? state.regionalId : undefined,
                tipo: state.tipo != null ? state.tipo : undefined
            });

            const artistas = append ? [...state.artistas, ...result.items] : result.items;

            this.patch({
                artistas,
                page: result.page.number,
                totalRecords: result.page.totalElements,
                totalPages: result.page.totalPages,
            });
        } catch {
        } finally {
            this.patch({ loading: false });
        }
    }

    async reloadFirstPage() {
        this.patch({
            artistas: [],
            page: 0,
            totalRecords: 0,
            totalPages: 0,
        });
        await this.loadPage(0, false);
    }

    async refreshAfterAlbumCreated() {
        const state = this._state$.getValue();
        if (state.viewMode === "grid") {
            await this.reloadUpToPage(state.page);
            return;
        }

        await this.loadPage(state.page, false);
    }

    async saveArtista(data: { id?: number; nome: string; tipo: IArtista["tipo"]; regionalIds?: number[], artistaFoto?: File | null }) {
        if (!data.nome || !data.tipo) return false;

        this.patch({ loading: true });
        try {
            let artista: IArtista;
            if (data.id) {
                artista = await atualizarArtista(data.id, { nome: data.nome, tipo: data.tipo, regionalIds: data.regionalIds });
            } else {
                artista = await criarArtista({ nome: data.nome, tipo: data.tipo, regionalIds: data.regionalIds });
            }

            if (data.artistaFoto && artista.id) {
                try {
                    const artistaFoto = await uploadFotoArtista(artista.id, data.artistaFoto);
                    artista = { ...artista, fotoUrl: artistaFoto.url };
                } catch (err) {
                    console.error("Erro ao fazer upload da capa:", err);
                }
            }

            await this.reloadFirstPage();
            return true;
        } catch {
            return false;
        } finally {
            this.patch({ loading: false });
        }
    }

    async deleteArtista(artista: IArtista) {
        if (!artista) return false;

        this.patch({ loading: true });
        try {
            await removerArtista(artista.id);
            await this.reloadFirstPage();
            return true;
        } catch {
            return false;
        } finally {
            this.patch({ loading: false });
        }
    }

    private async reloadUpToPage(page: number) {
        const state = this._state$.getValue();
        const pageSize = getPageSize(state.viewMode);

        this.patch({ loading: true });
        try {
            const pagesToLoad = page + 1;
            let all: IArtista[] = [];
            let totalRecords = state.totalRecords;
            let totalPages = state.totalPages;

            for (let p = 0; p < pagesToLoad; p++) {
                const result = await listarArtistas({
                    page: p,
                    size: pageSize,
                    nome: state.search ? state.search.trim() : undefined,
                    sort: `nome,${state.sortOrder}`,
                    regionalId: state.regionalId != null ? state.regionalId : undefined,
                    tipo: state.tipo != null ? state.tipo : undefined,
                });

                all = p === 0 ? result.items : [...all, ...result.items];
                totalRecords = result.page.totalElements;
                totalPages = result.page.totalPages;
            }

            this.patch({
                artistas: all,
                page,
                totalRecords,
                totalPages,
            });
        } catch {
        } finally {
            this.patch({ loading: false });
        }
    }

    private patch(partial: Partial<ArtistasState>) {
        this._state$.next({ ...this._state$.getValue(), ...partial });
    }
}

export const artistasFacade = new ArtistasFacade();
