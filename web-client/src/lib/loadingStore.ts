/**
 * Tiny pub/sub store that tracks the number of in-flight API requests.
 * Works outside React so `apiFetch` (a plain function) can call it.
 */

type Listener = (count: number) => void;

let activeCount = 0;
const listeners = new Set<Listener>();

function notify() {
  listeners.forEach((fn) => fn(activeCount));
}

export const loadingStore = {
  /** Call when a request starts. */
  start() {
    activeCount++;
    notify();
  },

  /** Call when a request finishes (success or error). */
  end() {
    activeCount = Math.max(0, activeCount - 1);
    notify();
  },

  /** Subscribe — returns an unsubscribe function. */
  subscribe(fn: Listener): () => void {
    listeners.add(fn);
    return () => listeners.delete(fn);
  },

  /** Snapshot for React 18 useSyncExternalStore. */
  getSnapshot(): number {
    return activeCount;
  },
};
