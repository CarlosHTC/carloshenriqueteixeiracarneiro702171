import { useNavigate, useSearchParams } from "react-router-dom";
import type { IArtista, IRegional, TipoArtista } from "../../../shared/types";
import { useEffect, useMemo, useRef, useState } from "react";
import { Button } from "primereact/button";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { InputText } from "primereact/inputtext";
import { Dropdown } from "primereact/dropdown";
import { Dialog } from "primereact/dialog";
import { ConfirmDialog, confirmDialog } from "primereact/confirmdialog";
import ArtistaCard from "../components/ArtistaCard";
import ArtistaFormDialog from "../components/ArtistaFormDialog";
import { useObservable } from "../../../hooks/useObservable";
import { artistasFacade, ARTISTAS_TABLE_PAGE_SIZE } from "../facades/artistas.facade";
import { Tag } from "primereact/tag";
import { listarRegionais } from "../../regionais/regionais.api";
import { buscarArtistaPorId } from "../artistas.api";

const TIPO_OPTIONS: Array<{ label: string; value: TipoArtista }> = [
    { label: "Solo", value: "SOLO" },
    { label: "Banda", value: "BANDA" },
    { label: "DJ", value: "DJ" },
    { label: "Orquestra", value: "ORQUESTRA" },
];

export default function ArtistasPage() {
    const navigate = useNavigate();
    const [params, setParams] = useSearchParams();

    const state = useObservable(artistasFacade.state$, artistasFacade.getSnapshot());
    const { artistas, search, sortOrder, viewMode, loading, page, totalRecords, totalPages, tipo, regionalId } = state;

    const sentinelRef = useRef<HTMLDivElement | null>(null);

    const [selectedArtista, setSelectedArtista] = useState<IArtista | null>(null);
    const [filterOpen, setFilterOpen] = useState(false);
    const [filterTipo, setFilterTipo] = useState<TipoArtista | null>(null);
    const [filterRegionalId, setFilterRegionalId] = useState<number | null>(null);
    const [regionais, setRegionais] = useState<IRegional[]>([]);
    const [regionaisLoading, setRegionaisLoading] = useState(false);

    const dialog = params.get("dialog");
    const artistaDialogOpen = dialog === "novo-artista" || dialog === "editar-artista";

    // const [deleteOpen, setDeleteOpen] = useState(false);

    useEffect(() => {
        artistasFacade.ensureLoaded();
    }, []);

    useEffect(() => {
        if (viewMode !== "grid") return;
        if (!sentinelRef.current) return;
        if (loading) return;

        const observer = new IntersectionObserver(
            (entries) => {
                if (!entries[0]?.isIntersecting) return;
                if (loading) return;
                if (page + 1 >= totalPages) return;
                artistasFacade.loadPage(page + 1, true);
            },
            { rootMargin: "200px" }
        );

        observer.observe(sentinelRef.current);
        return () => observer.disconnect();
    }, [loading, page, totalPages, viewMode]);

    useEffect(() => {
        function onAlbumChanged(e: any) {
            const payload = e?.detail?.payload;
            if (!payload) return;

            requestAnimationFrame(() => {
                artistasFacade.refreshAfterAlbumCreated();
            });
        }

        window.addEventListener("ws:album.created", onAlbumChanged as any);
        return () => window.removeEventListener("ws:album.created", onAlbumChanged as any);
    }, []);

    useEffect(() => {
        if (!filterOpen) return;
        if (regionais.length > 0 || regionaisLoading) return;

        setRegionaisLoading(true);
        listarRegionais()
            .then((data) => setRegionais(data))
            .catch(() => setRegionais([]))
            .finally(() => setRegionaisLoading(false));
    }, [filterOpen, regionais.length, regionaisLoading]);

    const sortedArtistas = useMemo(() => {
        const result = [...artistas].sort((a, b) => {
            const cmp = a.nome.localeCompare(b.nome);
            return sortOrder === "asc" ? cmp : -cmp;
        });
        return result;
    }, [artistas, sortOrder]);

    function openCreateArtista() {
        setSelectedArtista(null);
        params.set("dialog", "novo-artista");
        setParams(params, { replace: true });
    }

    async function openEditArtista(artista: IArtista) {
        setSelectedArtista(artista);
        params.set("dialog", "editar-artista");
        setParams(params, { replace: true });
        try {
            const detalhe = await buscarArtistaPorId(artista.id);
            setSelectedArtista(detalhe);
        } catch {
            // fallback to current data if request fails
        }
    }

    function closeDialog() {
        params.delete("dialog");
        setParams(params, { replace: true });
        setSelectedArtista(null);
    }

    function openFilterDialog() {
        setFilterTipo(tipo ?? null);
        setFilterRegionalId(regionalId ?? null);
        setFilterOpen(true);
    }

    function applyFilters() {
        artistasFacade.setFilters({ tipo: filterTipo, regionalId: filterRegionalId });
        setFilterOpen(false);
    }

    function clearFilters() {
        setFilterTipo(null);
        setFilterRegionalId(null);
    }

    // function openDeleteArtista(artista: IArtista) {
    // setSelectedArtista(artista);
    // setDeleteOpen(true);
    // }

    function onSelectArtista(artista: IArtista) {
        artistasFacade.setSelectedArtista(artista);
        navigate(`/artistas/${artista.id}`);
    }

    async function handleSaveArtista(data: { id?: number; nome: string; tipo: IArtista["tipo"]; regionalIds?: number[]; artistaFoto?: File | null }) {
        const ok = await artistasFacade.saveArtista(data);
        if (ok) {
            closeDialog();
        }
    }

    async function onConfirmDelete(artista: IArtista) {
        await artistasFacade.deleteArtista(artista);
    }

    function handleDeleteArtista(artista: IArtista) {
        confirmDialog({
            message: `Tem certeza que deseja excluir o artista "${artista.nome}" e todos os seus álbuns? Esta ação não pode ser desfeita.`,
            header: "Excluir Artista",
            icon: "pi pi-exclamation-triangle",
            acceptClassName: "p-button-danger",
            acceptLabel: "sim",
            rejectLabel: "não",
            closeIcon: "pi pi-times",
            accept: () => {
                onConfirmDelete(artista);
                closeDialog();
            },
            reject: () => {
                closeDialog();
            },
        });
    }



    return (
        <div className="flex flex-column gap-4" style={{ width: "100%", height: "100%" }}>
            <div className="grid col-12 align-items-center p-0">
                <h1 className="col-12 m-0" style={{ fontSize: 32, fontWeight: 700, marginBottom: 8 }}>
                    Artistas
                </h1>
                <p className="col-10 m-0" style={{ opacity: 0.75, fontSize: 15 }}>
                    Gerencie sua coleção de artistas e álbuns musicais.
                </p>
                <div className="col-2 m-0 grid justify-content-end">
                    <Button
                        label="Novo Artista"
                        icon="pi pi-user-plus"
                        onClick={openCreateArtista}
                        className="col-12 p-2"
                    />
                </div>
            </div>

            <div className="grid col-12 grid-row justify-content-between p-0">
                <div className="col-10 align-items-center justify-content-end p-1">
                    <InputText
                        value={search}
                        onChange={(e) => artistasFacade.setSearch(e.target.value)}
                        placeholder="Buscar artistas..."
                        className="col-12 p-2"
                    />
                </div>

                <div className="col-2 align-items-center justify-content-end p-1" >
                    <Button
                        label="Pesquisar"
                        icon="pi pi-search"
                        onClick={() => {
                            artistasFacade.reloadFirstPage();
                        }}
                        className="col-12 p-2"
                    />
                </div>
            </div>

            <div className="col-12 grid align-items-center justify-content-between p-0">
                <div className="flex align-items-center gap-2 p-1">
                    <Button
                        label="Filtrar"
                        icon="pi pi-filter"
                        onClick={openFilterDialog}
                        size="small"
                        outlined
                    />
                </div>

                <div className="flex align-items-center gap-2 p-1">
                    <div className="p-1">
                        <span
                            style={{
                                opacity: 0.75,
                                fontSize: 13,
                                padding: "6px 12px",
                                borderRadius: 12,
                                background: "rgba(255,255,255,.06)",
                                whiteSpace: "nowrap",
                            }}
                        >
                            {totalRecords} {totalRecords === 1 ? "artista" : "artistas"}
                        </span>
                    </div>

                    <div style={{ borderRadius: 8, background: "rgba(255,255,255,.04)", padding: 2 }}>
                        <Button
                            icon={viewMode === "grid" ? "pi pi-th-large" : "pi pi-list"}
                            text
                            size="small"
                            onClick={() => artistasFacade.setViewMode(viewMode === "grid" ? "table" : "grid")}
                            style={{
                                background: viewMode === "grid" ? "rgba(255,255,255,.1)" : "transparent",
                            }}
                            tooltip={viewMode === "grid" ? "Visualização em grade" : "Visualização em tabela"}
                            tooltipOptions={{ position: "bottom" }}
                        />
                    </div>

                    <Button
                        icon={sortOrder === "asc" ? "pi pi-sort-alpha-down" : "pi pi-sort-alpha-up"}
                        text
                        size="small"
                        onClick={() => artistasFacade.setSortOrder(sortOrder === "asc" ? "desc" : "asc")}
                        tooltip={`Ordenar ${sortOrder === "asc" ? "A-Z" : "Z-A"}`}
                        tooltipOptions={{ position: "bottom" }}
                    />
                </div>
            </div>

            {!loading && totalRecords === 0 ? (
                <div
                    className="grid col-12 align-items-center justify-content-center"
                    style={{
                        padding: 64,
                        borderRadius: 14,
                        border: "1px dashed rgba(255,255,255,.10)",
                        background: "rgba(255,255,255,.02)",
                    }}
                >
                    <div className="grid col-12 gap-2 align-items-center justify-content-center">
                        <i className="pi pi-user" style={{ fontSize: 48, opacity: 0.4, marginBottom: 16 }} />
                        <div className="font-semibold" style={{ fontSize: 18, marginBottom: 8 }}>
                            Nenhum artista encontrado
                        </div>
                    </div>
                    <div className="grid col-12 align-items-center justify-content-center">
                        {search ? "Tente uma busca diferente." : "Cadastre seu primeiro artista para começar."}
                    </div>
                    {!search && (
                        <Button
                            className="mt-4"
                            icon="pi pi-user-plus"
                            label="Cadastrar Primeiro Artista"
                            onClick={openCreateArtista}
                            size="small"
                        />
                    )}
                </div>
            ) : viewMode === "grid" ? (
                <div className="col-12 justify-content-center p-0 mb-2">
                    <div
                        className="flex mt-2"
                        style={{
                            width: "100%",
                            gap: 16,
                            flexWrap: "wrap",
                            alignItems: "stretch",
                        }}
                    >
                        {sortedArtistas.map((artista) => (
                            <ArtistaCard
                                key={artista.id}
                                artista={artista}
                                onClick={() => onSelectArtista(artista)}
                                onEdit={() => openEditArtista(artista)}
                                onDelete={() => handleDeleteArtista(artista)}
                            />
                        ))}
                    </div>
                    <div ref={sentinelRef} style={{ height: 1 }} />
                </div>
            ) : (
                <div className="grid col-12 p-0">
                    <DataTable
                        value={sortedArtistas}
                        stripedRows
                        paginator
                        lazy
                        rows={ARTISTAS_TABLE_PAGE_SIZE}
                        first={page * ARTISTAS_TABLE_PAGE_SIZE}
                        totalRecords={totalRecords}
                        loading={loading}
                        onPage={(e) => {
                            const nextPage = e.page ?? 0;
                            artistasFacade.loadPage(nextPage, false);
                        }}
                        onRowClick={(e) => onSelectArtista(e.data as IArtista)}
                        className="col-12"
                    >
                        <Column field="nome" header="Nome" />
                        <Column field="tipo" header="Tipo" />
                        <Column
                            header="Álbuns"
                            body={(row: IArtista) => <Tag value={row.qtdAlbuns && row.qtdAlbuns >= 1 ? row.qtdAlbuns : 0} />}
                            style={{ width: 120, textAlign: "center" }}
                        />
                    </DataTable>
                </div>
            )}

            <ArtistaFormDialog
                visible={artistaDialogOpen}
                artista={selectedArtista}
                onHide={closeDialog}
                onSave={handleSaveArtista}
            />

            <Dialog
                header="Filtrar Artistas"
                visible={filterOpen}
                onHide={() => setFilterOpen(false)}
                style={{ width: "min(520px, 92vw)" }}
                modal
                closeIcon="pi pi-times"
                footer={
                    <div className="flex justify-content-end gap-2">
                        <Button label="Limpar" severity="warning" onClick={clearFilters} />
                        <Button label="Aplicar" onClick={applyFilters} />
                    </div>
                }
            >
                <div className="flex flex-column gap-4">
                    <div className="flex flex-column gap-2">
                        <label htmlFor="filtro-tipo" className="text-sm font-medium">
                            Tipo de Artista
                        </label>
                        <Dropdown
                            id="filtro-tipo"
                            value={filterTipo}
                            options={TIPO_OPTIONS}
                            optionLabel="label"
                            optionValue="value"
                            onChange={(e) => setFilterTipo(e.value ?? null)}
                            placeholder="Selecione o tipo"
                            showClear
                            className="w-full"
                        />
                    </div>

                    <div className="flex flex-column gap-2">
                        <label htmlFor="filtro-regional" className="text-sm font-medium">
                            Regional
                        </label>
                        <Dropdown
                            id="filtro-regional"
                            value={filterRegionalId}
                            options={regionais}
                            optionLabel="nome"
                            optionValue="id"
                            onChange={(e) => setFilterRegionalId(e.value ?? null)}
                            placeholder={regionaisLoading ? "Carregando regionais..." : "Selecione a regional"}
                            showClear
                            className="w-full"
                            disabled={regionaisLoading}
                        />
                    </div>
                </div>
            </Dialog>

            <ConfirmDialog />
        </div>
    );
}
