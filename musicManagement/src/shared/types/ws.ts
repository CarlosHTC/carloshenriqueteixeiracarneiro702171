export type GeneroSummaryResponse = {
    id: number;
    nome: string;
};

export type AlbumCreatedWsMessage = {
    id: number;
    nome: string;
    artistaId: number;
    generos: GeneroSummaryResponse[];
    createdAt: string;
    updatedAt: string;
};