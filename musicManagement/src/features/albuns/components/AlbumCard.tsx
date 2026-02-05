import React from "react";
import { Button } from "primereact/button";
import { Menu } from "primereact/menu";
import type { IAlbumComCapa } from "../../../shared/types/IAlbum";

export interface AlbumCardProps {
    album: IAlbumComCapa;
    onEdit: () => void;
    onDelete: () => void;
}

export default function AlbumCard({ album, onEdit, onDelete }: AlbumCardProps) {
    const menuRef = React.useRef<Menu>(null);

    const menuItems = [
        {
            label: "Editar",
            icon: "pi pi-pencil",
            command: () => {
                onEdit();
            },
        },
        {
            label: "Excluir",
            icon: "pi pi-trash",
            command: () => {
                onDelete();
            },
            className: "text-red-500",
        },
    ];

    const capaPrincipal = album.capas.find((capa) => capa.principal) ?? album.capas[0];

    return (
        <div
            className="relative group"
            style={{
                border: "1px solid rgba(255,255,255,.06)",
                borderRadius: 14,
                overflow: "hidden",
                background: "rgba(255,255,255,.02)",
                transition: "all 0.3s ease",
                cursor: "pointer",
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = "rgba(255,255,255,.12)";
                e.currentTarget.style.background = "rgba(255,255,255,.04)";
                e.currentTarget.style.transform = "translateY(-4px)";
                e.currentTarget.style.boxShadow = "0 8px 24px rgba(0,0,0,.2)";
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = "rgba(255,255,255,.06)";
                e.currentTarget.style.background = "rgba(255,255,255,.02)";
                e.currentTarget.style.transform = "translateY(0)";
                e.currentTarget.style.boxShadow = "none";
            }}
        >
            <div
                style={{
                    aspectRatio: "1 / 1",
                    background: "rgba(255,255,255,.04)",
                    position: "relative",
                    overflow: "hidden",
                }}
            >
                {capaPrincipal ? (
                    <img
                        src={capaPrincipal.url}
                        alt={album.nome}
                        style={{
                            width: "100%",
                            height: "100%",
                            objectFit: "cover",
                            transition: "transform 0.3s ease",
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = "scale(1.05)";
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = "scale(1)";
                        }}
                    />
                ) : (
                    <div className="flex align-items-center justify-content-center h-full">
                        <i className="pi pi-image" style={{ fontSize: 22, opacity: 0.6 }} />
                    </div>
                )}
                <div
                    className="absolute"
                    style={{
                        inset: 0,
                        background: "linear-gradient(to top, rgba(0,0,0,.4), transparent)",
                        opacity: 0,
                        transition: "opacity 0.3s ease",
                    }}
                    onMouseEnter={(e) => {
                        e.currentTarget.style.opacity = "1";
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.opacity = "0";
                    }}
                />
            </div>

            <div className="flex flex-column gap-2 p-3">
                <div className="flex align-items-start justify-content-between gap-2">
                    <div className="flex-1" style={{ minWidth: 0 }}>
                        <div
                            className="font-semibold"
                            style={{
                                whiteSpace: "nowrap",
                                overflow: "hidden",
                                textOverflow: "ellipsis",
                                fontSize: 14,
                                lineHeight: "1.4",
                            }}
                        >
                            {album.nome}
                        </div>
                    </div>
                    <div className="flex align-items-start justify-content-end">
                        <Menu model={menuItems} popup ref={menuRef} />
                        <Button
                            icon="pi pi-ellipsis-v"
                            rounded
                            text
                            severity="info"
                            size="small"
                            onClick={(e: any) => {
                                menuRef.current?.toggle(e);
                            }}
                            style={{
                                width: 32,
                                height: 32,
                            }}
                        />
                    </div>
                </div>

                <div className="flex align-items-center gap-3 text-xs" style={{ opacity: 0.8, marginBottom: 8 }}>
                    {album.faixas.length > 0 && (
                        <div className="flex align-items-center gap-1">
                            <i className="pi pi-music" style={{ fontSize: 12 }} />
                            <span>{album.faixas.length} {album.faixas.length === 1 ? "faixa" : "faixas"}</span>
                        </div>
                    )}
                </div>

                {album.generos.length > 0 && (
                    <div className="mt-2">
                        <span
                            className="text-xs"
                            style={{
                                display: "inline-block",
                                borderRadius: 12,
                                background: "rgba(255,255,255,.1)",
                                padding: "4px 10px",
                                fontWeight: 500,
                            }}
                        >
                            {album.generos[0].nome}
                        </span>
                        {album.generos.length > 1 && (
                            <span
                                className="text-xs ml-1"
                                style={{
                                    opacity: 0.7,
                                }}
                            >
                                +{album.generos.length - 1}
                            </span>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
