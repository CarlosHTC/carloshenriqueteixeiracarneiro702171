import type { IAuditableEntity } from "./IAuditableEntity";

export interface IArtista extends IAuditableEntity {
    id: number;
    nome: string;
    tipo: TipoArtista;
    qtdAlbuns?: number;
    fotoUrl?: string | null;
    regionalIds?: number[];
}

export type TipoArtista = "SOLO" | "BANDA" | "DJ" | "ORQUESTRA";
