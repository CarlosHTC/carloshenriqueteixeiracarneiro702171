import type { IAuditableEntity } from "./IAuditableEntity";

export interface IFaixa extends IAuditableEntity {
    id: number;
    album: number;
    numero: number;
    titulo: string;
    duracaoSegundos: number;
    explicita: boolean;
}