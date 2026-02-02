import { http } from "../../services/http/axios";
import type { IFaixa } from "../../shared/types";

export type CreateFaixaRequest = {
    numero: number;
    titulo: string;
    duracaoSegundos: number;
    explicita: boolean;
};

export type UpdateFaixaRequest = {
    numero: number;
    titulo: string;
    duracaoSegundos: number;
    explicita: boolean;
};

export async function listarFaixasPorAlbum(albumId: number): Promise<IFaixa[]> {
    const { data } = await http.get<IFaixa[]>(`/api/v1/albuns/${albumId}/faixas`);
    return Array.isArray(data) ? data : [];
}

export async function buscarFaixaPorId(albumId: number, faixaId: number): Promise<IFaixa> {
    const { data } = await http.get<IFaixa>(`/api/v1/albuns/${albumId}/faixas/${faixaId}`);
    return data;
}

export async function criarFaixa(albumId: number, payload: CreateFaixaRequest): Promise<IFaixa> {
    const { data } = await http.post<IFaixa>(`/api/v1/albuns/${albumId}/faixas`, payload);
    return data;
}

export async function atualizarFaixa(
    albumId: number,
    faixaId: number,
    payload: UpdateFaixaRequest
): Promise<IFaixa> {
    const { data } = await http.put<IFaixa>(`/api/v1/albuns/${albumId}/faixas/${faixaId}`, payload);
    return data;
}

export async function removerFaixa(albumId: number, faixaId: number): Promise<void> {
    await http.delete(`/api/v1/albuns/${albumId}/faixas/${faixaId}`);
}
