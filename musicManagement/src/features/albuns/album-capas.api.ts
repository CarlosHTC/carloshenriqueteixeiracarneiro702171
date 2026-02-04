import { http } from "../../services/http/axios";

export interface AlbumCapaResponse {
    id: number;
    fileName: string;
    contentType: string;
    sizeBytes: number;
    principal: boolean;
    url: string;
}

export async function uploadCapas(albumId: number, files: File[]): Promise<AlbumCapaResponse[]> {
    const formData = new FormData();
    files.forEach((file) => {
        formData.append("files", file);
    });

    const { data } = await http.post<AlbumCapaResponse[]>(
        `/api/v1/albuns/${albumId}/capas`,
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );
    return Array.isArray(data) ? data : [];
}

export async function listarCapas(albumId: number): Promise<AlbumCapaResponse[]> {
    const { data } = await http.get<AlbumCapaResponse[]>(`/api/v1/albuns/${albumId}/capas`);
    return Array.isArray(data) ? data : [];
}

export async function removerCapa(albumId: number, capaId: number): Promise<void> {
    await http.delete(`/api/v1/albuns/${albumId}/capas/${capaId}`);
}

export async function definirCapaPrincipal(albumId: number, capaId: number): Promise<void> {
    await http.patch(`/api/v1/albuns/${albumId}/capas/${capaId}/principal`);
}
