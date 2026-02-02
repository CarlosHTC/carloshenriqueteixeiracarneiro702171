import type { IAuditableEntity } from "./IAuditableEntity";

export interface IAlbumCapa extends IAuditableEntity {
    id: number;
    album: number;
    objectKey: string;
    fileName: string;
    contentType: string;
    sizeBytes: number;
    principal: boolean;
}

export interface ICapaPrincipal {
    id: number;
    fileName: string;
    contentType: string;
    sizeBytes: number;
    url: string;
}
