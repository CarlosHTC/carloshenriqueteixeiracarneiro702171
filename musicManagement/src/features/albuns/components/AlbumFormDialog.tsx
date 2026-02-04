import { useEffect, useRef, useState } from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Dropdown } from "primereact/dropdown";
import { MultiSelect } from "primereact/multiselect";
import { FileUpload } from "primereact/fileupload";
import type { IAlbum, IArtista, IGenero } from "../../../shared/types";
import { listarGeneros } from "../../generos/generos.api";
import { listarCapas } from "../album-capas.api";
import type { AlbumCapaDraft } from "../types";

export interface AlbumFormDialogProps {
    visible: boolean;
    album?: IAlbum | null;
    artista?: IArtista | null;
    artistas?: IArtista[];
    generos?: IGenero[];
    onHide: () => void;
    onSave: (
        artistaId: number,
        data: {
            id?: number;
            nome: string;
            generoIds: number[];
            capas: AlbumCapaDraft[];
            removedCapaIds: number[];
        }
    ) => void;
}

function showInvalidImageToast() {
    window.dispatchEvent(
        new CustomEvent("toast:show", {
            detail: {
                severity: "warn",
                summary: "Imagem inválida",
                detail: "A imagem inserida não é valida. Envie um arquivo de imagem.",
                life: 3500,
            },
        })
    );
}

const MAX_IMAGE_MB = 10;
const MAX_IMAGE_BYTES = MAX_IMAGE_MB * 1024 * 1024;

function showSizeLimitToast() {
    window.dispatchEvent(
        new CustomEvent("toast:show", {
            detail: {
                severity: "warn",
                summary: "Limite excedido",
                detail: `O total de capas não pode ultrapassar ${MAX_IMAGE_MB}MB.`,
                life: 3500,
            },
        })
    );
}

const currentYear = new Date().getFullYear();
const years = Array.from({ length: 75 }, (_, i) => currentYear - i);

function createLocalId() {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return crypto.randomUUID();
    }
    return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function readFileAsDataUrl(file: File) {
    return new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result as string);
        reader.onerror = () => reject(reader.error);
        reader.readAsDataURL(file);
    });
}

function formatBytes(bytes: number) {
    if (!Number.isFinite(bytes) || bytes <= 0) return "0 MB";
    const mb = bytes / (1024 * 1024);
    return `${mb.toFixed(2)} MB`;
}

export default function AlbumFormDialog({
    visible,
    album,
    artista,
    artistas = [],
    generos = [],
    onHide,
    onSave,
}: AlbumFormDialogProps) {
    const isEditing = !!album;

    const [selectedArtistaId, setSelectedArtistaId] = useState<number | null>(null);
    const [nome, setNome] = useState("");
    const [anoLancamento, setAnoLancamento] = useState<number | null>(null);
    const [generoIds, setGeneroIds] = useState<number[]>([]);
    const [capas, setCapas] = useState<AlbumCapaDraft[]>([]);
    const [initialCapaIds, setInitialCapaIds] = useState<number[]>([]);
    const [listaGeneros, setListaGeneros] = useState<IGenero[]>([]);
    const fileUploadRef = useRef<FileUpload>(null);

    useEffect(() => {
        if (!visible) {
            resetForm();
            return;
        }

        setSelectedArtistaId(artista?.id ?? album?.artista ?? null);
        setNome(album?.nome ?? "");
        setAnoLancamento(null);
        setGeneroIds(album?.generos?.map((g) => g.id) ?? []);
        if (!album?.id) {
            setCapas([]);
            setInitialCapaIds([]);
            return;
        }

        let cancelled = false;
        const loadCapas = async () => {
            try {
                const data = await listarCapas(album.id);
                if (cancelled) return;
                const draft = data.map((c) => ({
                    localId: `existing-${c.id}`,
                    id: c.id,
                    previewUrl: c.url,
                    fileName: c.fileName,
                    contentType: c.contentType,
                    sizeBytes: c.sizeBytes,
                    principal: c.principal,
                    source: "existing" as const,
                }));
                const hasPrincipal = draft.some((c) => c.principal);
                if (!hasPrincipal && draft.length > 0) {
                    draft[0].principal = true;
                }
                setCapas(draft);
                setInitialCapaIds(draft.map((c) => c.id!).filter((id) => Number.isFinite(id)));
            } catch {
                if (!cancelled) {
                    setCapas([]);
                    setInitialCapaIds([]);
                }
            }
        };
        void loadCapas();
        return () => {
            cancelled = true;
        };
    }, [visible, album, artista]);

    async function listaGenero() {
        const generosData = await listarGeneros();
        setListaGeneros(generosData);
    }

    useEffect(() => {
        if (visible) {
            listaGenero();
        }
    }, [visible]);

    const resetForm = () => {
        if (!album) {
            setNome("");
            setAnoLancamento(null);
            setGeneroIds([]);
            setCapas([]);
            setInitialCapaIds([]);
            if (!artista) setSelectedArtistaId(null);
        }
    };

    const handleFileSelect = async (event: any) => {
        const files: File[] = Array.isArray(event?.files) ? event.files : [];
        if (!files.length) {
            showInvalidImageToast();
            return;
        }

        const currentTotalBytes = capas.reduce(
            (total, capa) => total + (capa.sizeBytes ?? capa.file?.size ?? 0),
            0
        );

        const validFiles = files.filter((file) => {
            if (!file.type || !file.type.startsWith("image/")) return false;
            if (file.size > MAX_IMAGE_BYTES) return false;
            return true;
        });

        if (validFiles.length !== files.length) {
            showInvalidImageToast();
        }

        if (!validFiles.length) {
            return;
        }

        let runningTotal = currentTotalBytes;
        const limitedFiles: File[] = [];
        for (const file of validFiles) {
            if (runningTotal + file.size > MAX_IMAGE_BYTES) {
                break;
            }
            limitedFiles.push(file);
            runningTotal += file.size;
        }

        if (limitedFiles.length !== validFiles.length) {
            showSizeLimitToast();
        }

        if (!limitedFiles.length) {
            return;
        }

        const previews = await Promise.all(
            limitedFiles.map(async (file) => ({
                localId: createLocalId(),
                file,
                previewUrl: await readFileAsDataUrl(file),
                fileName: file.name,
                contentType: file.type,
                sizeBytes: file.size,
                principal: false,
                source: "new" as const,
            }))
        );

        setCapas((prev) => {
            const hasPrincipal = prev.some((c) => c.principal);
            const merged = [...prev, ...previews];
            if (!hasPrincipal && previews.length > 0) {
                const firstNewIndex = prev.length;
                return merged.map((c, idx) => ({ ...c, principal: idx === firstNewIndex }));
            }
            return merged;
        });

        fileUploadRef.current?.clear();
    };

    const handleRemoveCapa = (localId: string) => {
        setCapas((prev) => {
            const next = prev.filter((c) => c.localId !== localId);
            if (next.length === 0) return next;
            const hasPrincipal = next.some((c) => c.principal);
            if (!hasPrincipal) {
                next[0] = { ...next[0], principal: true };
            }
            return next;
        });
    };

    const definirPrincipal = (localId: string) => {
        setCapas((prev) =>
            prev.map((c) => ({
                ...c,
                principal: c.localId === localId,
            }))
        );
    };

    const handleSubmit = () => {
        const artistaId = artista?.id || selectedArtistaId;
        if (!nome || !artistaId) {
            return;
        }

        let capasAtualizadas = capas;
        if (capasAtualizadas.length > 0 && !capasAtualizadas.some((c) => c.principal)) {
            capasAtualizadas = capasAtualizadas.map((c, idx) => ({ ...c, principal: idx === 0 }));
            setCapas(capasAtualizadas);
        }

        const existentesIds = capasAtualizadas.map((c) => c.id).filter((id): id is number => typeof id === "number");
        const removedCapaIds = initialCapaIds.filter((id) => !existentesIds.includes(id));

        onSave(artistaId, {
            id: album?.id,
            nome,
            generoIds,
            capas: capasAtualizadas,
            removedCapaIds,
        });
    };

    const artistaOptions = artistas.map((a) => ({
        label: a.nome,
        value: a.id,
    }));

    const generoSource = (() => {
        const merged: IGenero[] = [];
        const seen = new Set<number>();
        const add = (items: IGenero[]) => {
            items.forEach((g) => {
                if (!seen.has(g.id)) {
                    seen.add(g.id);
                    merged.push(g);
                }
            });
        };

        add(listaGeneros);
        add(generos);
        return merged;
    })();

    const generoOptions = generoSource.map((g) => ({
        label: g.nome,
        value: g.id,
    }));

    const yearOptions = years.map((year) => ({
        label: year.toString(),
        value: year,
    }));

    const totalCapasBytes = capas.reduce(
        (total, capa) => total + (capa.sizeBytes ?? capa.file?.size ?? 0),
        0
    );
    const totalCapasPercent = Math.min(100, (totalCapasBytes / MAX_IMAGE_BYTES) * 100);

    const footer = (
        <div className="flex justify-content-end gap-2">
            <Button label="Cancelar" icon="pi pi-times" severity="danger" onClick={onHide} />
            <Button
                label={isEditing ? "Salvar Alterações" : "Cadastrar Álbum"}
                onClick={handleSubmit}
                disabled={!nome || !generoIds.length || !anoLancamento || (!artista && !selectedArtistaId)}
                severity="success"
            />
        </div>
    );

    return (
        <Dialog
            header={isEditing ? "Editar Álbum" : "Novo Álbum"}
            visible={visible}
            style={{ width: "min(640px, 92vw)", maxHeight: "90vh" }}
            onHide={onHide}
            modal
            footer={footer}
            closeIcon="pi pi-times"
        >
            <div className="flex flex-column gap-4" style={{ maxHeight: "70vh", overflowY: "auto" }}>
                {!artista && (
                    <div className="flex flex-column gap-2">
                        <label className="text-sm font-medium">Artista</label>
                        <Dropdown
                            value={selectedArtistaId}
                            options={artistaOptions}
                            optionLabel="label"
                            optionValue="value"
                            onChange={(e) => setSelectedArtistaId(e.value)}
                            placeholder="Selecione o artista"
                            className="w-full"
                            required
                        />
                    </div>
                )}

                <div className="flex flex-column gap-2">
                    <label className="text-sm font-medium">Capas do Álbum</label>
                    <div className="flex flex-column gap-3">
                        <div
                            className="flex gap-3"
                            style={{
                                overflowX: "auto",
                                paddingBottom: 4,
                                flexWrap: "wrap",
                            }}
                        >
                            {capas.length > 0 ? (
                                capas.map((capa) => (
                                    <div
                                        key={capa.localId}
                                        className="relative"
                                        style={{
                                            width: 110,
                                            borderRadius: 10,
                                            overflow: "hidden",
                                            background: "rgba(255,255,255,.04)",
                                            border: capa.principal
                                                ? "2px solid rgba(var(--primary-rgb), .7)"
                                                : "1px solid rgba(255,255,255,.08)",
                                            flexShrink: 0,
                                        }}
                                    >
                                        <div style={{ width: "100%", height: 110 }}>
                                            <img
                                                src={capa.previewUrl}
                                                alt={capa.fileName || "Capa"}
                                                style={{ width: "100%", height: "100%", objectFit: "cover" }}
                                            />
                                        </div>
                                        <div
                                            className="flex flex-column gap-2 p-2"
                                            style={{ background: "rgba(0,0,0,.35)" }}
                                        >
                                            <button
                                                type="button"
                                                onClick={() => definirPrincipal(capa.localId)}
                                                disabled={capa.principal}
                                                className="w-full"
                                                style={{
                                                    border: "1px solid rgba(255,255,255,.2)",
                                                    borderRadius: 8,
                                                    background: capa.principal ? "rgba(var(--primary-rgb), .2)" : "transparent",
                                                    color: "inherit",
                                                    padding: "4px 6px",
                                                    fontSize: 11,
                                                    cursor: capa.principal ? "default" : "pointer",
                                                }}
                                            >
                                                {capa.principal ? "Principal" : "Definir principal"}
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() => handleRemoveCapa(capa.localId)}
                                                className="w-full"
                                                style={{
                                                    border: "1px solid rgba(255,255,255,.2)",
                                                    borderRadius: 8,
                                                    background: "rgba(255,255,255,.06)",
                                                    color: "inherit",
                                                    padding: "4px 6px",
                                                    fontSize: 11,
                                                    cursor: "pointer",
                                                }}
                                            >
                                                Remover
                                            </button>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <div
                                    className="flex align-items-center justify-content-center"
                                    style={{
                                        width: 110,
                                        height: 110,
                                        borderRadius: 10,
                                        background: "rgba(255,255,255,.04)",
                                    }}
                                >
                                    <i className="pi pi-image" style={{ fontSize: 24, opacity: 0.6 }} />
                                </div>
                            )}
                        </div>
                        <div className="flex-1">
                            <FileUpload
                                ref={fileUploadRef}
                                mode="basic"
                                accept="image/*"
                                chooseLabel="Selecionar Capas"
                                onSelect={handleFileSelect}
                                auto={false}
                                customUpload
                                uploadHandler={() => {}}
                                style={{ width: "100%" }}
                                multiple
                            />
                            <div
                                className="mt-2"
                                style={{
                                    height: 6,
                                    borderRadius: 999,
                                    background: "rgba(255,255,255,.08)",
                                    overflow: "hidden",
                                }}
                            >
                                <div
                                    style={{
                                        height: "100%",
                                        width: `${totalCapasPercent}%`,
                                        background: "rgba(var(--primary-rgb), .7)",
                                    }}
                                />
                            </div>
                            <div className="text-xs mt-1" style={{ opacity: 0.75 }}>
                                {formatBytes(totalCapasBytes)} / {MAX_IMAGE_MB}MB
                            </div>
                            <p className="text-xs mt-1" style={{ opacity: 0.75 }}>
                                Capas do álbum via MinIO. Apenas arquivos de imagem. Max {MAX_IMAGE_MB}MB.
                            </p>
                        </div>
                    </div>
                </div>

                <div className="flex flex-column gap-2">
                    <label htmlFor="nome" className="text-sm font-medium">
                        Título do Álbum
                    </label>
                    <InputText
                        id="nome"
                        value={nome}
                        onChange={(e) => setNome(e.target.value)}
                        placeholder="Digite o título do álbum"
                        required
                    />
                </div>

                <div className="grid">
                    <div className="col-6 flex flex-column gap-2">
                        <label htmlFor="ano" className="text-sm font-medium">
                            Ano de Lançamento
                        </label>
                        <Dropdown
                            id="ano"
                            value={anoLancamento}
                            options={yearOptions}
                            optionLabel="label"
                            optionValue="value"
                            onChange={(e) => setAnoLancamento(e.value)}
                            placeholder="Ano"
                            className="w-full"
                            required
                        />
                    </div>

                    <div className="col-6 flex flex-column gap-2">
                        <label htmlFor="genero" className="text-sm font-medium">
                            Gênero{generoIds.length > 1 ? "s" : ""}
                        </label>
                        <MultiSelect
                            id="genero"
                            value={generoIds}
                            options={generoOptions}
                            optionLabel="label"
                            optionValue="value"
                            onChange={(e) => setGeneroIds(e.value || [])}
                            placeholder="Selecione o(s) gênero(s)"
                            className="w-full"
                            display="chip"
                            maxSelectedLabels={2}
                            closeIcon="pi pi-times"
                            required
                        />
                    </div>
                </div>
            </div>
        </Dialog>
    );
}
