import { useEffect } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";

import { Button } from "primereact/button";
import { ConfirmDialog, confirmDialog } from "primereact/confirmdialog";
import { Paginator } from "primereact/paginator";

import type { IArtista } from "../../../shared/types/IArtista";
import type { IAlbumComCapa } from "../../../shared/types/IAlbum";
import type { AlbumCapaDraft } from "../../albuns/types";
import AlbumCard from "../../albuns/components/AlbumCard";
import AlbumFormDialog from "../../albuns/components/AlbumFormDialog";
import ArtistaFormDialog from "../components/ArtistaFormDialog";
import { useObservable } from "../../../hooks/useObservable";
import { artistaDetalheFacade, ALBUMS_PAGE_SIZE } from "../facades/artistaDetalhe.facade";
import { artistasFacade } from "../facades/artistas.facade";

export default function ArtistaDetalhePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();

  const state = useObservable(artistaDetalheFacade.state$, artistaDetalheFacade.getSnapshot());
  const albumSelecionado = useObservable(
    artistaDetalheFacade.albumSelecionado$,
    artistaDetalheFacade.getAlbumSelecionadoSnapshot()
  );
  const { artista, albums, albumPage, albumTotalRecords, loading, error } = state;

  const dialog = params.get("dialog");
  const albumDialogOpen = dialog === "novo-album" || dialog === "editar-album";

  useEffect(() => {
    if (!id) {
      artistaDetalheFacade.setArtistaId(null);
      return;
    }

    const artistaId = Number(id);
    if (!Number.isFinite(artistaId)) {
      artistaDetalheFacade.setArtistaId(null);
      artistaDetalheFacade.setError("ID invalido.");
      return;
    }

    const selected = artistasFacade.getSnapshot().selectedArtista;
    if (selected && selected.id === artistaId) {
      artistaDetalheFacade.setArtistaFromList(selected);
    } else {
      artistaDetalheFacade.setArtistaId(artistaId);
    }
    return () => {
      artistaDetalheFacade.setArtistaId(null);
    };
  }, [id]);

  useEffect(() => {
    function onAlbumChanged(e: any) {
      const payload = e?.detail?.payload;
      if (!payload) return;

      const artistaIdTela = Number(id);
      if (!Number.isFinite(artistaIdTela)) return;

      if (payload.artistaId !== artistaIdTela) return;

      artistaDetalheFacade.buscarAlbuns(artistaIdTela, albumPage);
    }

    window.addEventListener("ws:album.created", onAlbumChanged as any);
    return () => window.removeEventListener("ws:album.created", onAlbumChanged as any);
  }, [id, albumPage]);

  function closeDialog() {
    params.delete("dialog");
    setParams(params, { replace: true });
    artistaDetalheFacade.setAlbumSelecionado(null);
  }

  function openNewAlbum() {
    artistaDetalheFacade.setAlbumSelecionado(null);
    params.set("dialog", "novo-album");
    setParams(params, { replace: true });
  }

  function openEditAlbum(album: IAlbumComCapa) {
    artistaDetalheFacade.buscarAlbum(album.id);
    params.set("dialog", "editar-album");
    setParams(params, { replace: true });
  }

  function openEditArtista() {
    params.set("dialog", "editar-artista");
    setParams(params, { replace: true });
  }

  function openDeleteArtista() {
    handleDeleteArtista();
  }

  async function handleSaveAlbum(
    artistaId: number,
    data: { id?: number; nome: string; generoIds: number[]; capas: AlbumCapaDraft[]; removedCapaIds: number[] }
  ) {
    const ok = await artistaDetalheFacade.saveAlbum(artistaId, data);
    if (ok) {
      closeDialog();
    }
  }

  async function handleDeleteAlbum(album: IAlbumComCapa) {
    confirmDialog({
      message: `Tem certeza que deseja excluir o álbum "${album.nome}"? Esta ação não pode ser desfeita.`,
      header: "Excluir Álbum",
      icon: "pi pi-exclamation-triangle",
      acceptClassName: "p-button-danger",
      acceptLabel: "sim",
      rejectLabel: "não",
      closeIcon: "pi pi-times",
      accept: async () => {
        await artistaDetalheFacade.deleteAlbum(album);
      },
    });
  }

  async function handleUpdateArtista(data: { id?: number; nome: string; tipo: IArtista["tipo"]; regionalIds?: number[], artistaFoto?: File | null }) {
    const ok = await artistaDetalheFacade.updateArtista(data);
    if (ok) {
      const updated = artistaDetalheFacade.getSnapshot().artista;
      if (updated) {
        artistasFacade.setSelectedArtista(updated);
      }
      closeDialog();
    }
  }

  async function handleDeleteArtista() {
    if (!artista) return;

    confirmDialog({
      message: `Tem certeza que deseja excluir o artista "${artista.nome}" e todos os seus álbuns? Esta ação não pode ser desfeita.`,
      header: "Excluir Artista",
      icon: "pi pi-exclamation-triangle",
      acceptClassName: "p-button-danger",
      acceptLabel: "sim",
      rejectLabel: "não",
      closeIcon: "pi pi-times",
      accept: async () => {
        const ok = await artistaDetalheFacade.deleteArtista();
        if (ok) {
          await artistasFacade.reloadFirstPage();
          artistasFacade.setSelectedArtista(null);
          navigate("/artistas");
        }
      },
    });
  }

  if (loading && !artista) {
    return (
      <div style={{ width: "100%", height: "100%" }}>
        <Button icon="pi pi-arrow-left" label="Voltar" onClick={() => navigate("/artistas")} />
        <div className="mt-4" style={{ opacity: 0.75 }}>
          Carregando artista...
        </div>
      </div>
    );
  }

  if (error && !artista) {
    return (
      <div style={{ width: "100%", height: "100%" }}>
        <Button icon="pi pi-arrow-left" label="Voltar" onClick={() => navigate("/artistas")} />
        <div className="mt-4" style={{ opacity: 0.75 }}>
          {error}
        </div>
      </div>
    );
  }

  if (!artista) {
    return (
      <div style={{ width: "100%", height: "100%" }}>
        <Button icon="pi pi-arrow-left" label="Voltar" onClick={() => navigate("/artistas")} />
        <div className="mt-4" style={{ opacity: 0.75 }}>
          Artista não encontrado.
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-column gap-4" style={{ width: "100%", height: "100%" }}>
      <div
        className="relative overflow-hidden"
        style={{
          borderRadius: 16,
          padding: 24,
          border: "1px solid rgba(255,255,255,.06)",
          background: "linear-gradient(135deg, rgba(255,255,255,.05), rgba(255,255,255,.02))",
          position: "relative",
        }}
      >
        <div
          className="absolute"
          style={{
            inset: 0,
            background: "linear-gradient(90deg, rgba(var(--primary-rgb), 0.05), transparent)",
            pointerEvents: "none",
          }}
        />

        <Button
          icon="pi pi-arrow-left"
          text
          onClick={() => navigate("/artistas")}
          aria-label="Voltar"
          style={{
            position: "absolute",
            top: 16,
            left: 16,
            zIndex: 2,
          }}
        />

        <div className="relative flex flex-column md:flex-row gap-6 md:align-items-end">
          <div
            className="relative mx-auto md:mx-0"
            style={{
              width: 160,
              height: 160,
              borderRadius: 14,
              background: "rgba(255,255,255,.04)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexShrink: 0,
              boxShadow: "0 8px 32px rgba(0,0,0,.3)",
            }}
          >
            {artista.fotoUrl ? (
              <img
                src={artista.fotoUrl}
                alt={`Foto de ${artista.nome}`}
                style={{
                  width: "100%",
                  height: "100%",
                  objectFit: "cover",
                }}
              />
            ) : (
              <i className="pi pi-image" style={{ fontSize: 32, opacity: 0.6 }} />
            )}
          </div>

          <div className="flex-1 text-center md:text-left">
            <div
              style={{
                fontSize: 11,
                letterSpacing: 2,
                opacity: 0.8,
                fontWeight: 600,
                textTransform: "uppercase",
                marginBottom: 4,
              }}
            >
              Artista
            </div>
            <div
              style={{
                fontSize: 36,
                fontWeight: 800,
                lineHeight: "40px",
                marginBottom: 12,
              }}
            >
              {artista.nome}
            </div>

            <div className="flex flex-wrap gap-3 mt-3 align-items-center justify-content-center md:justify-content-start">
              <span
                className="flex align-items-center gap-2"
                style={{
                  opacity: 0.75,
                  fontSize: 14,
                  padding: "6px 12px",
                  borderRadius: 20,
                  background: "rgba(255,255,255,.06)",
                }}
              >
                <i className="pi pi-disc" style={{ fontSize: 14 }} />
                {albumTotalRecords} {albumTotalRecords === 1 ? "álbum" : "álbuns"}
              </span>
              <span
                style={{
                  opacity: 0.75,
                  fontSize: 14,
                  padding: "6px 12px",
                  borderRadius: 20,
                  background: "rgba(255,255,255,.06)",
                }}
              >
                {artista.tipo}
              </span>
            </div>

            <div className="flex flex-wrap gap-2 mt-5 justify-content-center md:justify-content-start">
              <Button
                icon="pi pi-plus"
                label="Adicionar Álbum"
                onClick={openNewAlbum}
                size="small"
                severity="info"
              />
              <Button
                icon="pi pi-pencil"
                label="Editar Artista"
                outlined
                onClick={openEditArtista}
                size="small"
              />
              <Button
                icon="pi pi-trash"
                label="Excluir"
                severity="danger"
                outlined
                onClick={openDeleteArtista}
                size="small"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="flex flex-column sm:flex-row sm:align-items-center sm:justify-content-between gap-2">
        <h2 className="m-0" style={{ fontSize: 22, fontWeight: 600 }}>Discografia</h2>
        <span
          style={{
            opacity: 0.75,
            fontSize: 13,
            padding: "4px 12px",
            borderRadius: 12,
            background: "rgba(255,255,255,.06)",
          }}
        >
          {albumTotalRecords} {albumTotalRecords === 1 ? "álbum" : "álbuns"}
        </span>
      </div>

      {!loading && albumTotalRecords === 0 ? (
        <div
          className="flex flex-column align-items-center justify-content-center"
          style={{
            padding: 64,
            borderRadius: 14,
            border: "1px dashed rgba(255,255,255,.10)",
            background: "rgba(255,255,255,.02)",
          }}
        >
          <i className="pi pi-disc" style={{ fontSize: 48, opacity: 0.4, marginBottom: 16 }} />
          <div className="font-semibold" style={{ fontSize: 18, marginBottom: 8 }}>
            Nenhum álbum cadastrado
          </div>
          <div style={{ opacity: 0.75, fontSize: 14, marginBottom: 20, textAlign: "center" }}>
            Este artista ainda não possui álbuns registrados.
          </div>
          <Button
            icon="pi pi-plus"
            label="Adicionar Primeiro Álbum"
            onClick={openNewAlbum}
            size="small"
          />
        </div>
      ) : (
        <div className="grid">
          {albums.map((album) => (
            <div key={album.id} className="col-6 md:col-3 lg:col-2">
              <AlbumCard
                album={album}
                onEdit={() => openEditAlbum(album)}
                onDelete={() => handleDeleteAlbum(album)}
              />
            </div>
          ))}
        </div>
      )}

      {albumTotalRecords > ALBUMS_PAGE_SIZE && (
        <div className="flex justify-content-center mt-3">
          <Paginator
            first={albumPage * ALBUMS_PAGE_SIZE}
            rows={ALBUMS_PAGE_SIZE}
            totalRecords={albumTotalRecords}
            onPageChange={(event) => {
              if (!artista) return;
              artistaDetalheFacade.buscarAlbuns(artista.id, event.page ?? 0);
            }}
          />
        </div>
      )}

      <ArtistaFormDialog
        visible={dialog == "editar-artista"}
        artista={artista}
        onHide={closeDialog}
        onSave={handleUpdateArtista}
      />


      <AlbumFormDialog
        visible={albumDialogOpen}
        album={albumSelecionado}
        artista={artista}
        generos={albumSelecionado?.generos}
        onHide={closeDialog}
        onSave={handleSaveAlbum}
      />

      <ConfirmDialog />
    </div>
  );
}
