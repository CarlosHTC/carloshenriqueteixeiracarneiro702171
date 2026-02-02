import type { IAuditableEntity } from "./IAuditableEntity";
import type { IFaixa } from "./IFaixa";
import type { IGenero } from "./IGenero";
import type { IAlbumCapa, ICapaPrincipal } from "./IAlbumCapa";

export interface IAlbum extends IAuditableEntity {
    id: number;
    nome: string;
    artista: number;
    faixas: Array<IFaixa>;
    generos: Array<IGenero>;
    capas: Array<IAlbumCapa>;
}

export interface IAlbumComCapa extends IAuditableEntity {
    id: number;
    nome: string;
    artista: number;
    faixas: Array<IFaixa>;
    generos: Array<IGenero>;
    capaPrincipal: ICapaPrincipal
}