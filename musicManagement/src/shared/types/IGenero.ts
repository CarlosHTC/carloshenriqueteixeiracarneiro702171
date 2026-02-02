import type { IAuditableEntity } from "./IAuditableEntity";

export interface IGenero extends IAuditableEntity {
    id: number;
    nome: string;
}
