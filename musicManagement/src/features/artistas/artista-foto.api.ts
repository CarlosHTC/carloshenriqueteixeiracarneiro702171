import { http } from "../../services/http/axios";

export interface ArtistaFotoResponse {
    id: number;
    contentType: string;
    sizeBytes: number;
    url: string;
}

export async function uploadFotoArtista(artistaId: number, file: File): Promise<ArtistaFotoResponse> {
    const formData = new FormData();
    formData.append("file", file);

    const { data } = await http.post<ArtistaFotoResponse>(
        `/api/v1/artistas/${artistaId}/foto`,
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );
    return data;
}

export async function buscarFotosArtista(artistaId: number): Promise<ArtistaFotoResponse> {
    const { data } = await http.get<ArtistaFotoResponse>(`/api/v1/artistas/${artistaId}/foto`);
    return data;
}

export async function removerCapa(artistaId: number, fotoId: number): Promise<void> {
    await http.delete(`/api/v1/artista/${artistaId}/foto/${fotoId}`);
}
