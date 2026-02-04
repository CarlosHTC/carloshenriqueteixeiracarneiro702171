import React, { useEffect, useMemo } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { Toast } from "primereact/toast"
import { Button } from "primereact/button";
import { Toolbar } from "primereact/toolbar";
import { Tag } from "primereact/tag";

import { authService } from "../../services/auth/auth.service";
import { parseJwtPayload } from "../../services/auth/jwt";
import { useObservable } from "../../hooks/useObservable";
import { albumWsFacade } from "../../facades/albumWs.facade";

function getLoggedUserLabel(): string {
    const token = authService.getAccessToken();
    if (!token) return "Usuário";

    const payload = parseJwtPayload(token);

    return (
        payload?.preferred_username ||
        payload?.username ||
        payload?.sub ||
        payload?.email ||
        "Usuário"
    );
}

export default function AppLayout() {
    const navigate = useNavigate();

    const albumCreated = useObservable(albumWsFacade.albumCreated$, null);

    const toastRef = React.useRef<Toast>(null);

    const loggedUser = useMemo(() => getLoggedUserLabel(), []);

    useEffect(() => {
        albumWsFacade.start();
        return () => albumWsFacade.stop();
    }, []);

    useEffect(() => {
        if (!albumCreated) return;

        toastRef.current?.show({
            severity: "info",
            summary: "Novo álbum cadastrado",
            detail: `${albumCreated.nome}`,
            life: 3000,
          });
        
          window.dispatchEvent(
            new CustomEvent("ws:album.created", { detail: { payload: albumCreated } })
          );
    }, [albumCreated])

    useEffect(() => {
        function onToastEvent(e: any) {
            const detail = e?.detail ?? {};
            if (!detail) return;
            toastRef.current?.show({
                severity: detail.severity ?? "info",
                summary: detail.summary ?? "Aviso",
                detail: detail.detail ?? "",
                life: detail.life ?? 3000,
            });
        }

        window.addEventListener("toast:show", onToastEvent as any);
        return () => window.removeEventListener("toast:show", onToastEvent as any);
    }, []);

    useEffect(() => {
        function handler(e: any) {
            const message = e?.detail?.message ?? "Sessão expirada.";

            toastRef.current?.show({
                severity: "warn",
                summary: "Acesso expirado",
                detail: message,
                life: 3500,
            });

            setTimeout(() => {
                navigate("/login", { replace: true });
            }, 300);
        }

        window.addEventListener("auth:expired", handler as any);
        return () => window.removeEventListener("auth:expired", handler as any);
    }, [navigate]);

    const startContent = (
        <div className="flex align-items-center gap-3">
            <div style={{
                width: 36,
                height: 36,
                borderRadius: 10,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                background: "var(--green-500)",
            }}>
                <i className="pi pi-volume-up" style={{ fontSize: 16, color: "var(--surface-900)" }} />
            </div>

            <div className="flex flex-column">
                <span style={{ fontWeight: 700, fontSize: 16, lineHeight: "18px" }}>Music Manager</span>
                <span style={{ opacity: 0.7, fontSize: 12 }}>
                    Gerenciamento de Artistas e Álbuns
                </span>
            </div>
        </div>
    )

    const endContent = (
        <div className="flex align-items-center gap-2">
            <Tag
                icon="pi pi-user"
                value={loggedUser}
                style={{
                    background: "rgba(255,255,255,.06)",
                    border: "1px solid rgba(255,255,255,.10)",
                    color: "var(--surface-0)",
                }}
                className="p-2"
            />

            <Button
                label="Sair"
                icon="pi pi-sign-out"
                severity="danger"
                outlined
                onClick={() => {
                    authService.logout();
                    navigate("/login", { replace: true });
                }}
            />
        </div>
    )

    return (
        <div
            className="flex flex-column"
            style={{
                background: "var(--surface-900)",
                width: "100%",
                height: "100%",
                minHeight: "100vh"
            }}
        >
            <Toast ref={toastRef} position="top-right" />

            <Toolbar
                start={startContent}
                end={endContent}
                className="p-4 border-none"
                style={{
                    borderRadius: 0,
                    background: "linear-gradient(180deg, rgba(0,0,0,.65), rgba(0,0,0,.25))",
                    borderBottom: "1px solid rgba(255,255,255,.06)",
                    flexShrink: 0,
                }}
            />

            <main
                className="p-4 md:p-6 flex-1"
                style={{
                    overflow: "auto",
                    width: "100%",
                    display: "flex",
                    flexDirection: "column"
                }}
            >
                <div className="mx-auto flex flex-column" style={{ maxWidth: 1280, width: "100%", height: "100%" }}>
                    <Outlet />
                </div>
            </main>
        </div>
    );
}