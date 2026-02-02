import { BehaviorSubject } from "rxjs";
import { stompWsClient } from "../services/websocket/stompClient";
import type { AlbumCreatedWsMessage } from "../shared/types/ws";


class AlbumWsFacade {
    private readonly _connected$ = new BehaviorSubject<boolean>(false);
    readonly connected$ = this._connected$.asObservable();

    private readonly _albumCreated$ = new BehaviorSubject<AlbumCreatedWsMessage | null>(null);
    readonly albumCreated$ = this._albumCreated$.asObservable();

    private unsubscribe?: () => void;

    start() {
        stompWsClient.connect();

        this.unsubscribe = stompWsClient.subscribeJson<AlbumCreatedWsMessage>(
            "/topic/albuns",
            (payload) => this._albumCreated$.next(payload)
        );
    }

    stop() {
        this.unsubscribe?.();
        this.unsubscribe = undefined;
        stompWsClient.disconnect();
    }
}

export const albumWsFacade = new AlbumWsFacade();
