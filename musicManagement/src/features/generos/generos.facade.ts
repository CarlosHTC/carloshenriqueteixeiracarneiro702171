import { BehaviorSubject } from "rxjs";
import type { IGenero } from "../../shared/types";
import { listarGeneros } from "./generos.api";

export interface GenerosState {
    generos: IGenero[];
    loading: boolean;
    loaded: boolean;
}

const initialState: GenerosState = {
    generos: [],
    loading: false,
    loaded: false,
};

class GenerosFacade {
    private readonly _state$ = new BehaviorSubject<GenerosState>(initialState);
    readonly state$ = this._state$.asObservable();

    getSnapshot() {
        return this._state$.getValue();
    }

    ensureLoaded() {
        const state = this._state$.getValue();
        if (state.loaded || state.loading) return;
        void this.loadOnce();
    }

    async loadOnce() {
        const state = this._state$.getValue();
        if (state.loaded || state.loading) return;

        this.patch({ loading: true });
        try {
            const generos = await listarGeneros();
            this.patch({ generos, loaded: true });
        } catch {
            this.patch({ generos: [], loaded: false });
        } finally {
            this.patch({ loading: false });
        }
    }

    private patch(partial: Partial<GenerosState>) {
        this._state$.next({ ...this._state$.getValue(), ...partial });
    }
}

export const generosFacade = new GenerosFacade();
