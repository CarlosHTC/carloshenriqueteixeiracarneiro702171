import "@testing-library/jest-dom/vitest";

type StorageMap = Map<string, string>;

function createLocalStorage(): Storage {
  const store: StorageMap = new Map();

  return {
    get length() {
      return store.size;
    },
    clear() {
      store.clear();
    },
    getItem(key: string) {
      return store.has(key) ? store.get(key) ?? null : null;
    },
    key(index: number) {
      return Array.from(store.keys())[index] ?? null;
    },
    removeItem(key: string) {
      store.delete(key);
    },
    setItem(key: string, value: string) {
      store.set(key, String(value));
    },
  };
}

if (typeof globalThis.localStorage === "undefined" || typeof globalThis.localStorage?.clear !== "function") {
  globalThis.localStorage = createLocalStorage();
}
