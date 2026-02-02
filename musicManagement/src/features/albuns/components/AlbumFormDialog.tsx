import { useEffect, useRef, useState } from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Dropdown } from "primereact/dropdown";
import { MultiSelect } from "primereact/multiselect";
import { FileUpload } from "primereact/fileupload";
import type { IAlbum, IArtista, IGenero } from "../../../shared/types";
import { listarGeneros } from "../../generos/generos.api";

export interface AlbumFormDialogProps {
    visible: boolean;
    album?: IAlbum | null;
    artista?: IArtista | null;
    artistas?: IArtista[];
    generos?: IGenero[];
    onHide: () => void;
    onSave: (artistaId: number, data: { id?: number; nome: string; generoIds: number[]; coverFile?: File | null; coverPreview?: string }) => void;
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

const currentYear = new Date().getFullYear();
const years = Array.from({ length: 75 }, (_, i) => currentYear - i);

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
    const [coverPreview, setCoverPreview] = useState<string>("");
    const [coverFile, setCoverFile] = useState<File | null>(null);
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
        setCoverPreview(album?.capas?.[0]?.objectKey ?? "");
        setCoverFile(null);
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
            setCoverPreview("");
            setCoverFile(null);
            if (!artista) setSelectedArtistaId(null);
        }
    };

    const handleFileSelect = (event: any) => {
        const file = event.files?.[0];
        if (!file) {
            showInvalidImageToast();
            return;
        }
        if (!file.type || !file.type.startsWith("image/")) {
            showInvalidImageToast();
            return;
        }
        if (file.size > MAX_IMAGE_BYTES) {
            showInvalidImageToast();
            return;
        }
        setCoverFile(file);
        const reader = new FileReader();
        reader.onloadend = () => {
            setCoverPreview(reader.result as string);
        };
        reader.readAsDataURL(file);
        fileUploadRef.current?.clear();
    };

    const handleRemoveImage = () => {
        setCoverPreview("");
        setCoverFile(null);
        fileUploadRef.current?.clear();
    };

    const handleSubmit = () => {
        const artistaId = artista?.id || selectedArtistaId;
        if (!nome || !artistaId) {
            return;
        }

        onSave(artistaId, {
            id: album?.id,
            nome,
            generoIds,
            coverFile,
            coverPreview,
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

    const footer = (
        <div className="flex justify-content-end gap-2">
            <Button label="Cancelar" icon="pi pi-times" severity="secondary" onClick={onHide} />
            <Button
                label={isEditing ? "Salvar Alterações" : "Cadastrar Álbum"}
                onClick={handleSubmit}
                disabled={!nome || (!artista && !selectedArtistaId)}
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
                    <label className="text-sm font-medium">Capa do Álbum</label>
                    <div className="flex align-items-center gap-4">
                        <div
                            className="relative"
                            style={{
                                width: 96,
                                height: 96,
                                borderRadius: 8,
                                overflow: "hidden",
                                background: "rgba(255,255,255,.04)",
                                flexShrink: 0,
                            }}
                        >
                            {coverPreview ? (
                                <>
                                    <img
                                        src={coverPreview}
                                        alt="Preview"
                                        style={{
                                            width: "100%",
                                            height: "100%",
                                            objectFit: "cover",
                                        }}
                                    />
                                    <button
                                        type="button"
                                        onClick={handleRemoveImage}
                                        className="absolute"
                                        style={{
                                            right: 4,
                                            top: 4,
                                            borderRadius: "50%",
                                            background: "rgba(0,0,0,.7)",
                                            border: "none",
                                            width: 24,
                                            height: 24,
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                            cursor: "pointer",
                                            color: "white",
                                        }}
                                    >
                                        <i className="pi pi-times" style={{ fontSize: 12 }} />
                                    </button>
                                </>
                            ) : (
                                <div className="flex align-items-center justify-content-center h-full">
                                    <i className="pi pi-image" style={{ fontSize: 24, opacity: 0.6 }} />
                                </div>
                            )}
                        </div>
                        <div className="flex-1">
                            <FileUpload
                                ref={fileUploadRef}
                                mode="basic"
                                accept="image/*"
                                chooseLabel="Selecionar Capa"
                                onSelect={handleFileSelect}
                                auto={false}
                                customUpload
                                uploadHandler={() => {}}
                                style={{ width: "100%" }}
                            />
                            <p className="text-xs mt-1" style={{ opacity: 0.75 }}>
                                Capa do álbum via MinIO. Apenas arquivos de imagem. Max {MAX_IMAGE_MB}MB.
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
                        />
                    </div>
                </div>
            </div>
        </Dialog>
    );
}
