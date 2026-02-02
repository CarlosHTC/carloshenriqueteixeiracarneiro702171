import { beforeEach, describe, expect, it, vi } from "vitest";

const connectSpy = vi.fn();
const disconnectSpy = vi.fn();
const subscribeSpy = vi.fn().mockImplementation(() => vi.fn());

vi.mock("../services/websocket/stompClient", () => ({
  stompWsClient: {
    connect: connectSpy,
    disconnect: disconnectSpy,
    subscribeJson: subscribeSpy,
  },
}));

describe("albumWsFacade", () => {
  beforeEach(() => {
    vi.resetModules();
    connectSpy.mockClear();
    disconnectSpy.mockClear();
    subscribeSpy.mockClear();
  });

  it("starts websocket and subscribes to album topic", async () => {
    const { albumWsFacade } = await import("./albumWs.facade");

    albumWsFacade.start();

    expect(connectSpy).toHaveBeenCalledTimes(1);
    expect(subscribeSpy).toHaveBeenCalledWith("/topic/albuns", expect.any(Function));
  });

  it("stops websocket and unsubscribes", async () => {
    const { albumWsFacade } = await import("./albumWs.facade");

    const unsubscribe = vi.fn();
    (albumWsFacade as any).unsubscribe = unsubscribe;
    albumWsFacade.stop();

    expect(unsubscribe).toHaveBeenCalledTimes(1);
    expect(disconnectSpy).toHaveBeenCalledTimes(1);
  });
});
