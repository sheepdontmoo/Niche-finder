import type { Analysis } from "./analysis";

// Saved analyses live on the device. Nothing is uploaded — the history is
// yours, and clearing app data clears it.

const KEY = "litmas-history";
const MAX_ENTRIES = 30;

export type HistoryEntry = {
  id: string;
  saved_at: number;
  thumbnail: string | null;
  analysis: Analysis;
};

export function readHistory(): HistoryEntry[] {
  if (typeof localStorage === "undefined") return [];
  try {
    const raw = localStorage.getItem(KEY);
    const parsed: unknown = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? (parsed as HistoryEntry[]) : [];
  } catch {
    return [];
  }
}

function write(entries: HistoryEntry[]) {
  try {
    localStorage.setItem(KEY, JSON.stringify(entries));
  } catch {
    // Storage full (thumbnails are the bulk) — drop the oldest half and retry
    // once so a long history never blocks saving a new scan.
    try {
      localStorage.setItem(
        KEY,
        JSON.stringify(entries.slice(0, Math.ceil(entries.length / 2))),
      );
    } catch {
      /* give up silently; history is a convenience, not the product */
    }
  }
}

export function saveEntry(
  analysis: Analysis,
  thumbnail: string | null,
): HistoryEntry[] {
  const entry: HistoryEntry = {
    id: crypto.randomUUID(),
    saved_at: Date.now(),
    thumbnail,
    analysis,
  };
  const next = [entry, ...readHistory()].slice(0, MAX_ENTRIES);
  write(next);
  return next;
}

export function deleteEntry(id: string): HistoryEntry[] {
  const next = readHistory().filter((e) => e.id !== id);
  write(next);
  return next;
}

export function clearHistory(): HistoryEntry[] {
  try {
    localStorage.removeItem(KEY);
  } catch {
    /* ignore */
  }
  return [];
}

export function formatWhen(ts: number): string {
  const mins = Math.round((Date.now() - ts) / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.round(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.round(hrs / 24);
  if (days < 7) return `${days}d ago`;
  return new Date(ts).toLocaleDateString();
}
