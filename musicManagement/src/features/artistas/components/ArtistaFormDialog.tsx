import { useEffect, useRef, useState } from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Dropdown } from "primereact/dropdown";
import { MultiSelect } from "primereact/multiselect";
import { FileUpload } from "primereact/fileupload";
import type { IArtista, IRegional, TipoArtista } from "../../../shared/types";
import { listarRegionais } from "../../regionais/regionais.api";

export interface ArtistaFormDialogProps {
    visible: boolean;
    artista?: IArtista | null;
    onHide: () => void;
    onSave: (data: { id?: number; nome: string; tipo: TipoArtista; regionalIds?: number[], artistaFoto?: File | null }) => void;
}

const TIPO_OPTIONS: Array<{ label: string; value: TipoArtista }> = [
    { label: "Solo", value: "SOLO" },
    { label: "Banda", value: "BANDA" },
    { label: "DJ", value: "DJ" },
    { label: "Orquestra", value: "ORQUESTRA" },
];

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

export default function ArtistaFormDialog({
    visible,
    onHide,
    onSave,
    artista
}: ArtistaFormDialogProps) {
    const isEditing = !!artista;

    const [nome, setNome] = useState("");
    const [tipo, setTipo] = useState<TipoArtista | null>(null);
    const [imagePreview, setImagePreview] = useState<string>("");
    const [artistaFoto, setArtistaFoto] = useState<File | null>(null);
    const [regionalIds, setRegionalIds] = useState<number[]>([]);
    const [regionais, setRegionais] = useState<IRegional[]>([]);
    const [regionaisLoading, setRegionaisLoading] = useState(false);
    const fileUploadRef = useRef<FileUpload>(null);

    useEffect(() => {
        if (!visible) {
            resetForm();
            return;
        }

        setNome(artista?.nome ?? "");
        setTipo(artista?.tipo ?? null);
        setImagePreview("");
        setArtistaFoto(null);
        setRegionalIds(artista?.regionalIds ?? []);
    }, [visible, artista]);

    useEffect(() => {
        if (!visible) return;
        if (regionais.length > 0 || regionaisLoading) return;

        setRegionaisLoading(true);
        listarRegionais()
            .then((data) => setRegionais(data))
            .catch(() => setRegionais([]))
            .finally(() => setRegionaisLoading(false));
    }, [visible, regionais.length, regionaisLoading]);

    const resetForm = () => {
        if (!artista) {
            setNome("");
            setTipo(null);
            setImagePreview("");
            setArtistaFoto(null);
            setRegionalIds([]);
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
        setArtistaFoto(file);
        const reader = new FileReader();
        reader.onloadend = () => {
            setImagePreview(reader.result as string);
        };
        reader.readAsDataURL(file);
        fileUploadRef.current?.clear();
    };

    const handleRemoveImage = () => {
        setImagePreview("");
        setArtistaFoto(null);
        fileUploadRef.current?.clear();
    };

    const handleSubmit = () => {
        if (!nome || !tipo) return;

        onSave({
            id: artista?.id,
            nome,
            tipo,
            regionalIds: regionalIds.length > 0 ? regionalIds : undefined,
            artistaFoto
        });
        onHide();
    };

    const footer = (
        <div className="flex justify-content-end gap-2">
            <Button label="Cancelar" icon="pi pi-times" severity="secondary" onClick={onHide} />
            <Button
                label={isEditing ? "Salvar Alterações" : "Cadastrar Artista"}
                onClick={handleSubmit}
                disabled={!nome || !tipo}
            />
        </div>
    );

    return (
        <Dialog
            header={isEditing ? "Editar Artista" : "Novo Artista"}
            visible={visible}
            style={{ width: "min(560px, 92vw)" }}
            onHide={onHide}
            modal
            footer={footer}
            closeIcon="pi pi-times"
        >
            <div className="flex flex-column gap-4">
                <div className="flex flex-column gap-2">
                    <label className="text-sm font-medium">Foto do Artista</label>
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
                            {imagePreview ? (
                                <>
                                    <img
                                        src={imagePreview}
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
                                chooseLabel="Selecionar Imagem"
                                onSelect={handleFileSelect}
                                auto={false}
                                customUpload
                                uploadHandler={() => {}}
                                style={{ width: "100%" }}
                            />
                            <p className="text-xs mt-1" style={{ opacity: 0.75 }}>
                                Apenas arquivos de imagem. Max {MAX_IMAGE_MB}MB. Upload via MinIO.
                            </p>
                        </div>
                    </div>
                </div>

                <div className="flex flex-column gap-2">
                    <label htmlFor="nome" className="text-sm font-medium">
                        Nome do Artista
                    </label>
                    <InputText
                        id="nome"
                        value={nome}
                        onChange={(e) => setNome(e.target.value)}
                        placeholder="Digite o nome do artista"
                        required
                    />
                </div>

                <div className="flex flex-column gap-2">
                    <label htmlFor="tipo" className="text-sm font-medium">
                        Tipo de Artista
                    </label>
                    <Dropdown
                        id="tipo"
                        value={tipo}
                        options={TIPO_OPTIONS}
                        optionLabel="label"
                        optionValue="value"
                        onChange={(e) => setTipo(e.value)}
                        placeholder="Selecione o tipo"
                        className="w-full"
                    />
                </div>

                <div className="flex flex-column gap-2">
                    <label htmlFor="regional" className="text-sm font-medium">
                        Regionais
                    </label>
                    <MultiSelect
                        id="regional"
                        value={regionalIds}
                        options={regionais}
                        optionLabel="nome"
                        optionValue="id"
                        onChange={(e) => setRegionalIds(e.value || [])}
                        placeholder={regionaisLoading ? "Carregando regionais..." : "Selecione a(s) regional(is)"}
                        className="w-full"
                        display="chip"
                        maxSelectedLabels={2}
                        disabled={regionaisLoading}
                        closeIcon="pi pi-times"
                    />
                </div>
            </div>
        </Dialog>
    );
}
