import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs";

type MessageHandler<T> = (payload: T) => void;

type PendingSub = {
  destination: string;
  handler: (msg: IMessage) => void;
};

export class StompWsClient {
  private client: Client | null = null;
  private subs: StompSubscription[] = [];
  private pending: PendingSub[] = [];
  private isConnecting = false;

  connect(): void {
    if (this.client?.connected || this.isConnecting) return;

    const apiUrl = import.meta.env.VITE_API_BASE_URL as string;
    const wsBase = apiUrl.replace(/^http/, "ws");
    const brokerURL = `${wsBase}/ws`;

    const client = new Client({
      brokerURL,
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (msg) => {
        if (import.meta.env.DEV) console.log("[WS]", msg);
      },
    });

    this.isConnecting = true;

    client.onConnect = () => {
      this.isConnecting = false;

      const pendings = [...this.pending];
      this.pending = [];

      pendings.forEach((p) => {
        const sub = client.subscribe(p.destination, p.handler);
        this.subs.push(sub);
      });
    };

    client.onStompError = (frame) => {
      console.error("[WS] stomp error:", frame.headers["message"], frame.body);
    };

    client.onWebSocketError = (evento) => {
      console.error("[WS] websocket error:", evento);
    };

    client.onWebSocketClose = () => {
      this.isConnecting = false;
    };

    this.client = client;
    client.activate();
  }

  subscribeJson<T>(destination: string, handler: MessageHandler<T>): () => void {
    if (!this.client && !this.isConnecting) {
      this.connect();
    }
    if (!this.client) {
      const pendingHandler = (message: IMessage) => this.handleJson(message, handler);
      this.pending.push({ destination, handler: pendingHandler });
      return () => this.removePending(destination, pendingHandler);
    }

    const wrapped = (message: IMessage) => this.handleJson(message, handler);

    if (this.client.connected) {
      const sub = this.client.subscribe(destination, wrapped);
      this.subs.push(sub);
      return () => sub.unsubscribe();
    }

    this.pending.push({ destination, handler: wrapped });
    return () => this.removePending(destination, wrapped);
  }

  disconnect(): void {
    this.pending = [];

    this.subs.forEach((s) => s.unsubscribe());
    this.subs = [];

    if (this.client?.active) {
      this.client.deactivate();
    }

    this.client = null;
    this.isConnecting = false;
  }

  private handleJson<T>(message: IMessage, handler: MessageHandler<T>) {
    try {
      handler(JSON.parse(message.body) as T);
    } catch (error) {
      console.error("[WS] invalid JSON:", error, message.body);
    }
  }

  private removePending(destination: string, fn: (msg: IMessage) => void) {
    this.pending = this.pending.filter((p) => !(p.destination === destination && p.handler === fn));
  }
}

export const stompWsClient = new StompWsClient();
