import { http } from "../../services/http/axios";
import type { IArtista, TipoArtista } from "../../shared/types";

export type CreateArtistaRequest = {
    nome: string;
    tipo: TipoArtista;
    regionalIds?: number[];
};

export type UpdateArtistaRequest = {
    nome: string;
    tipo: TipoArtista;
    regionalIds?: number[];
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

export type ListarArtistasParams = {
    page?: number;
    size?: number;
    nome?: string;
    sort?: string;
    regionalId?: number;
    tipo?: TipoArtista;
};

export async function listarArtistas(params: ListarArtistasParams = {}): Promise<PagedResult<IArtista>> {
    const fallback: PageInfo = {
        number: params.page ?? 0,
        size: params.size ?? 10,
        totalElements: 0,
        totalPages: 0,
    };
    const { data } = await http.get<IArtista[] | SpringPage<IArtista>>("/api/v1/artistas", {
        params,
    });
    return normalizePage<IArtista>(data, fallback);
}

export async function buscarArtistaPorId(id: number): Promise<IArtista> {
    const { data } = await http.get<IArtista>(`/api/v1/artistas/${id}`);
    return data;
}

export async function criarArtista(payload: CreateArtistaRequest): Promise<IArtista> {
    const { data } = await http.post<IArtista>("/api/v1/artistas", payload);
    return data;
}

export async function atualizarArtista(id: number, payload: UpdateArtistaRequest): Promise<IArtista> {
    const { data } = await http.put<IArtista>(`/api/v1/artistas/${id}`, payload);
    return data;
}

export async function removerArtista(id: number): Promise<void> {
    await http.delete(`/api/v1/artistas/${id}`);
}
