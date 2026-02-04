import React from "react";
import { Button } from "primereact/button";
import { Menu } from "primereact/menu";
import type { IArtista } from "../../../shared/types";

export interface ArtistaCardProps {
    artista: IArtista;
    onClick: () => void;
    onEdit: () => void;
    onDelete: () => void;
}

export default function ArtistaCard({ artista, onClick, onEdit, onDelete }: ArtistaCardProps) {
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

    return (
        <div
            className="relative group"
            style={{
                minWidth: 180,
                width: 180,
                cursor: "pointer",
                flexShrink: 0,
                transition: "transform 0.3s ease",
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.transform = "translateY(-6px)";
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.transform = "translateY(0)";
            }}
        >
            <div
                style={{
                    border: "1px solid rgba(255,255,255,.06)",
                    borderRadius: 14,
                    overflow: "hidden",
                    background: "rgba(255,255,255,.02)",
                    height: "100%",
                    display: "flex",
                    flexDirection: "column",
                    transition: "all 0.3s ease",
                }}
                onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = "rgba(255,255,255,.12)";
                    e.currentTarget.style.background = "rgba(255,255,255,.04)";
                    e.currentTarget.style.boxShadow = "0 8px 24px rgba(0,0,0,.2)";
                }}
                onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = "rgba(255,255,255,.06)";
                    e.currentTarget.style.background = "rgba(255,255,255,.02)";
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
                    onClick={onClick}
                >
                    <div className="flex align-items-center justify-content-center h-full">
                        {artista.fotoUrl != null ? (
                            <img
                                src={artista.fotoUrl}
                                alt=""
                                style={{ width: "100%", height: "100%", objectFit: "cover" }}
                            />
                        ) : (
                            <i className="pi pi-image" style={{ fontSize: 28, opacity: 0.6, transition: "transform 0.3s ease" }} />
                        )}
                    </div>
                    <div
                        className="absolute"
                        style={{
                            inset: 0,
                            background: "linear-gradient(to top, rgba(0,0,0,.3), transparent)",
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

                <div className="p-3 flex-1" style={{ display: "flex", flexDirection: "column" }}>
                    <div
                        className="font-semibold mb-1"
                        style={{
                            whiteSpace: "nowrap",
                            overflow: "hidden",
                            textOverflow: "ellipsis",
                            fontSize: 14,
                            lineHeight: "1.4",
                        }}
                        onClick={onClick}
                    >
                        {artista.nome}
                    </div>

                    <div style={{ opacity: 0.75, fontSize: 12, marginBottom: 8, lineHeight: "1.4" }}>
                        {artista.tipo}
                    </div>

                    <div
                        className="flex align-items-center gap-2"
                        style={{
                            opacity: 0.8,
                            fontSize: 13,
                            marginTop: "auto",
                        }}
                    >
                        <i className="pi pi-disc" style={{ fontSize: 14 }} />
                        <span>
                            {artista.qtdAlbuns != null ? artista.qtdAlbuns : 0} {artista.qtdAlbuns === 1 ? "álbum" : "álbuns"}
                        </span>
                    </div>
                </div>

                <div
                    className="absolute"
                    style={{
                        right: 8,
                        top: 8,
                        opacity: 1,
                        transition: "opacity 0.2s ease",
                    }}
                    // onMouseEnter={(e) => {
                    //     e.currentTarget.style.opacity = "1";
                    // }}
                    // onMouseLeave={(e) => {
                        // e.currentTarget.style.opacity = "0";
                    // }}
                    onClick={(e) => e.stopPropagation()}
                >
                    <Menu model={menuItems} popup ref={menuRef} />
                    <Button
                        icon="pi pi-ellipsis-v"
                        rounded
                        text
                        severity="info"
                        size="small"
                        onClick={(e) => {
                            menuRef.current?.toggle(e);
                            e.stopPropagation();
                        }}
                        style={{
                            width: 32,
                            height: 32,
                        }}
                    />
                </div>
            </div>
        </div>
    );
}
