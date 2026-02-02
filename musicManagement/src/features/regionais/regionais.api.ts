import { http } from "../../services/http/axios";
import type { IRegional } from "../../shared/types";

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

export async function listarRegionais(ativo = true): Promise<IRegional[]> {
    const { data } = await http.get<IRegional[] | SpringPage<IRegional>>("/api/v1/regionais", {
        params: { ativo },
    });
    return normalizeList<IRegional>(data);
}
