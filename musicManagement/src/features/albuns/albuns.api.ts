import { http } from "../../services/http/axios";
import type { IAlbum } from "../../shared/types";
import type { IAlbumComCapa } from "../../shared/types/IAlbum";
import type { ICapaPrincipal } from "../../shared/types/IAlbumCapa";

export type CreateAlbumRequest = {
    nome: string;
    artistaId: number;
    generoIds?: number[];
};

export type UpdateAlbumRequest = {
    nome: string;
    artistaId: number;
    generoIds?: number[];
};

type ArtistaSummaryResponse = {
    id: number;
    nome: string;
    tipo: string;
};

type GeneroSummaryResponse = {
    id: number;
    nome: string;
};

type AlbumResponseBackend = {
    id: number;
    nome: string;
    artista: ArtistaSummaryResponse;
    generos: GeneroSummaryResponse[] | Set<GeneroSummaryResponse>;
    createdAt: string;
    updatedAt: string;
};

type AlbumComCapaResponseBackend = {
    id: number;
    nome: string;
    artista: ArtistaSummaryResponse;
    generos: GeneroSummaryResponse[] | Set<GeneroSummaryResponse>;
    capaPrincipal: ICapaPrincipal;
    createdAt: string;
    updatedAt: string;
};

type SpringPage<T> = {
    content: T[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
};

export type PageInfo = {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
};

export type PagedResult<T> = {
    items: T[];
    page: PageInfo;
};

function normalizeList<T>(data: any): T[] {
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.content)) return data.content;
    return [];
}

function normalizePage<T>(data: any, fallback: PageInfo): PagedResult<T> {
    if (Array.isArray(data)) {
        return {
            items: data,
            page: {
                number: 0,
                size: data.length,
                totalElements: data.length,
                totalPages: data.length ? 1 : 0,
            },
        };
    }
    if (data && Array.isArray(data.content)) {
        return {
            items: data.content,
            page: {
                number: Number.isFinite(data.number) ? data.number : fallback.number,
                size: Number.isFinite(data.size) ? data.size : fallback.size,
                totalElements: Number.isFinite(data.totalElements) ? data.totalElements : data.content.length,
                totalPages: Number.isFinite(data.totalPages) ? data.totalPages : 1,
            },
        };
    }
    return {
        items: [],
        page: {
            number: fallback.number,
            size: fallback.size,
            totalElements: 0,
            totalPages: 0,
        },
    };
}

function adaptAlbumResponse(backend: AlbumResponseBackend): IAlbum {
    const generosArray = Array.isArray(backend.generos) 
        ? backend.generos 
        : Array.from(backend.generos || []);
    
    return {
        id: backend.id,
        nome: backend.nome,
        artista: backend.artista?.id || 0,
        generos: generosArray.map((g) => ({
            id: g.id,
            nome: g.nome,
            createdAt: backend.createdAt,
            updatedAt: backend.updatedAt,
            version: 0,
        })),
        faixas: [], 
        capas: [],
        createdAt: backend.createdAt,
        updatedAt: backend.updatedAt,
        version: 0,
    };
}

function adaptAlbumComCapaResponse(backend: AlbumComCapaResponseBackend): IAlbumComCapa {
    const generosArray = Array.isArray(backend.generos) 
        ? backend.generos 
        : Array.from(backend.generos || []);
    
    return {
        id: backend.id,
        nome: backend.nome,
        artista: backend.artista?.id || 0,
        generos: generosArray.map((g) => ({
            id: g.id,
            nome: g.nome,
            createdAt: backend.createdAt,
            updatedAt: backend.updatedAt,
            version: 0,
        })),
        faixas: [], 
        capaPrincipal: backend.capaPrincipal,
        createdAt: backend.createdAt,
        updatedAt: backend.updatedAt,
        version: 0,
    };
}

export async function listarAlbuns(): Promise<IAlbum[]> {
    const { data } = await http.get<AlbumResponseBackend[] | SpringPage<AlbumResponseBackend>>("/api/v1/albuns");
    const albums = normalizeList<AlbumResponseBackend>(data);
    return albums.map(adaptAlbumResponse);
}

export async function buscarAlbumPorId(id: number): Promise<IAlbum> {
    const { data } = await http.get<AlbumResponseBackend>(`/api/v1/albuns/${id}`);
    return adaptAlbumResponse(data);
}

export type ListarAlbunsPorArtistaParams = {
    page?: number;
    size?: number;
    sort?: string;
};

export async function listarAlbunsPorArtista(
    artistaId: number,
    params: ListarAlbunsPorArtistaParams = {}
): Promise<PagedResult<IAlbumComCapa>> {
    const fallback: PageInfo = {
        number: params.page ?? 0,
        size: params.size ?? 10,
        totalElements: 0,
        totalPages: 0,
    };
    const { data } = await http.get<AlbumComCapaResponseBackend[] | SpringPage<AlbumComCapaResponseBackend>>(
        "/api/v1/albuns/artista",
        {
            params: {
                artistaId,
                ...params,
            },
        }
    );
    const page = normalizePage<AlbumComCapaResponseBackend>(data, fallback);
    return {
        items: page.items.map(adaptAlbumComCapaResponse),
        page: page.page,
    };
}

export async function criarAlbum(payload: CreateAlbumRequest): Promise<IAlbum> {
    const { data } = await http.post<AlbumResponseBackend>("/api/v1/albuns", payload);
    return adaptAlbumResponse(data);
}

export async function atualizarAlbum(id: number, payload: UpdateAlbumRequest): Promise<IAlbum> {
    const { data } = await http.put<AlbumResponseBackend>(`/api/v1/albuns/${id}`, payload);
    return adaptAlbumResponse(data);
}

export async function removerAlbum(id: number): Promise<void> {
    await http.delete(`/api/v1/albuns/${id}`);
}
