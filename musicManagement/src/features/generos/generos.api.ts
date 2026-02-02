import { http } from "../../services/http/axios";
import type { IGenero } from "../../shared/types";

export type CreateGeneroRequest = {
    nome: string;
};

export type UpdateGeneroRequest = {
    nome: string;
};

type SpringPage<T> = {
    content: T[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
};

function normalizeList<T>(data: any): T[] {
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.content)) return data.content;
    return [];
}

export async function listarGeneros(): Promise<IGenero[]> {
    const { data } = await http.get<IGenero[] | SpringPage<IGenero>>("/api/v1/generos");
    return normalizeList<IGenero>(data);
}

export async function buscarGeneroPorId(id: number): Promise<IGenero> {
    const { data } = await http.get<IGenero>(`/api/v1/generos/${id}`);
    return data;
}

export async function criarGenero(payload: CreateGeneroRequest): Promise<IGenero> {
    const { data } = await http.post<IGenero>("/api/v1/generos", payload);
    return data;
}

export async function atualizarGenero(id: number, payload: UpdateGeneroRequest): Promise<IGenero> {
    const { data } = await http.put<IGenero>(`/api/v1/generos/${id}`, payload);
    return data;
}

export async function removerGenero(id: number): Promise<void> {
    await http.delete(`/api/v1/generos/${id}`);
}
