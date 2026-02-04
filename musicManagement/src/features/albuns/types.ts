export type AlbumCapaDraft = {
    localId: string;
    id?: number;
    file?: File;
    previewUrl: string;
    fileName?: string;
    contentType?: string;
    sizeBytes?: number;
    principal: boolean;
    source: "existing" | "new";
};
