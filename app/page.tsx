"use client";

import { useEffect, useId, useRef, useState } from "react";
import type { Analysis, Call } from "@/lib/analysis";
import {
  clearHistory,
  formatWhen,
  readHistory,
  saveEntry,
  type HistoryEntry,
} from "@/lib/history";
import {
  getPlans,
  initBilling,
  isSubscribed,
  purchase,
  restore,
  type Plan,
} from "@/lib/billing";

const FREE_SCANS = 3;
const SCANS_KEY = "litmas-scans-used";
const DEVICE_KEY = "litmas-device-id";
const MAX_DIMENSION = 2000;
const THUMB_DIMENSION = 180;

// Store product configuration. Prices must match what is configured in the
// Play Console / App Store Connect subscription products.
const PLANS = {
  monthly: { id: "litmas_monthly", price: "$9.99", per: "per month" },
  yearly: { id: "litmas_yearly", price: "$59.99", per: "per year" },
} as const;
type PlanKey = keyof typeof PLANS;

// Empty for the web deployment (same-origin); set NEXT_PUBLIC_API_BASE_URL
// for the Capacitor mobile builds, which call the hosted backend.
const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

type Status = "idle" | "ready" | "analyzing" | "done" | "error";
type View = "scan" | "history";

function getDeviceId(): string {
  let id = localStorage.getItem(DEVICE_KEY);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(DEVICE_KEY, id);
  }
  return id;
}

function readScansUsed(): number {
  const raw = localStorage.getItem(SCANS_KEY);
  const n = raw ? parseInt(raw, 10) : 0;
  return Number.isFinite(n) && n >= 0 ? n : 0;
}

/** The logo: a single test strip with one solid band. */
function StripMark({
  level = "top",
  className,
  style,
}: {
  level?: "top" | "middle" | "bottom";
  className?: string;
  style?: React.CSSProperties;
}) {
  const clip = useId().replace(/:/g, "");
  const y = level === "top" ? 0 : level === "middle" ? 10.67 : 21.33;
  return (
    <svg className={className} style={style} viewBox="0 0 12 32" aria-hidden="true">
      <defs>
        <clipPath id={clip}>
          <rect width="12" height="32" rx="6" />
        </clipPath>
      </defs>
      <g clipPath={`url(#${clip})`}>
        <rect width="12" height="32" fill="currentColor" opacity="0.2" />
        <rect y={y} width="12" height="10.67" fill="currentColor" />
      </g>
    </svg>
  );
}

function levelForCall(call: Call): "top" | "middle" | "bottom" {
  return call === "BUY" ? "top" : call === "SELL" ? "bottom" : "middle";
}

async function loadImage(file: File): Promise<HTMLImageElement> {
  const dataUrl = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(new Error("Could not read that file"));
    reader.readAsDataURL(file);
  });
  return new Promise((resolve, reject) => {
    const el = new Image();
    el.onload = () => resolve(el);
    el.onerror = () => reject(new Error("Could not read that image"));
    el.src = dataUrl;
  });
}

function toJpeg(img: HTMLImageElement, maxDim: number, quality: number): string {
  const scale = Math.min(1, maxDim / Math.max(img.width, img.height));
  const canvas = document.createElement("canvas");
  canvas.width = Math.max(1, Math.round(img.width * scale));
  canvas.height = Math.max(1, Math.round(img.height * scale));
  const ctx = canvas.getContext("2d");
  if (!ctx) throw new Error("Canvas unavailable on this device");
  ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
  return canvas.toDataURL("image/jpeg", quality);
}

function EducationalNotice() {
  return (
    <p className="edu">
      <b>Educational only — not financial advice.</b> Litmas reads the chart
      image you send and describes what it sees. It cannot know your position,
      your risk, or what happens next. Trading involves substantial risk of
      loss.
    </p>
  );
}

function ResultView({ analysis }: { analysis: Analysis }) {
  const a = analysis;
  return (
    <>
      <div className="card">
        <div className="call">
          <StripMark level={levelForCall(a.call)} className="mark" />
          <div className="call-body">
            <div className="call-word">{a.call}</div>
            <div className="call-reason">{a.call_reason}</div>
          </div>
        </div>

        <div className="confidence">
          <div className="confidence-head">
            <span>Confidence</span>
            <span>{Math.round(a.confidence)}%</span>
          </div>
          <div className="confidence-track">
            <div
              className="confidence-fill"
              style={{
                width: `${Math.min(100, Math.max(0, a.confidence))}%`,
              }}
            />
          </div>
        </div>

        <div className="meta-row">
          {a.asset && <span className="tag">{a.asset}</span>}
          {a.timeframe && <span className="tag">{a.timeframe}</span>}
          <span className="tag">trend: {a.trend}</span>
        </div>
      </div>

      <EducationalNotice />

      <div className="card">
        <h3>What the chart shows</h3>
        <p className="body-text">{a.summary}</p>
      </div>

      {a.patterns.length > 0 && (
        <div className="card">
          <h3>Patterns detected</h3>
          {a.patterns.map((p, i) => (
            <div className="pattern" key={i}>
              <div className="name">{p.name}</div>
              <div className="impl">{p.implication}</div>
            </div>
          ))}
        </div>
      )}

      {(a.support_levels.length > 0 || a.resistance_levels.length > 0) && (
        <div className="card">
          <h3>Key levels</h3>
          {a.resistance_levels.map((r, i) => (
            <div className="kv" key={`r${i}`}>
              <span className="k">Resistance</span>
              <span className="v">{r}</span>
            </div>
          ))}
          {a.support_levels.map((s, i) => (
            <div className="kv" key={`s${i}`}>
              <span className="k">Support</span>
              <span className="v">{s}</span>
            </div>
          ))}
        </div>
      )}

      {a.invalidation && (
        <div className="card">
          <h3>What would prove this wrong</h3>
          <p className="body-text">{a.invalidation}</p>
        </div>
      )}

      {a.indicators.length > 0 && (
        <div className="card">
          <h3>Indicator read</h3>
          <ul className="plain">
            {a.indicators.map((ind, i) => (
              <li key={i}>{ind}</li>
            ))}
          </ul>
        </div>
      )}

      <div className="card">
        <h3>Risk notes</h3>
        <p className="body-text dim">{a.risk_notes}</p>
      </div>
    </>
  );
}

export default function Home() {
  const [view, setView] = useState<View>("scan");
  const [status, setStatus] = useState<Status>("idle");
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [file, setFile] = useState<File | null>(null);
  const [analysis, setAnalysis] = useState<Analysis | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [scansUsed, setScansUsed] = useState(0);
  const [showPaywall, setShowPaywall] = useState(false);
  const [plan, setPlan] = useState<PlanKey>("yearly");
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  const [openEntry, setOpenEntry] = useState<HistoryEntry | null>(null);
  const [pro, setPro] = useState(false);
  const [livePlans, setLivePlans] = useState<Plan[]>([]);
  const [busy, setBusy] = useState(false);
  const [billingMsg, setBillingMsg] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setScansUsed(readScansUsed());
    setHistory(readHistory());
    (async () => {
      if (!(await initBilling(getDeviceId()))) return;
      setPro(await isSubscribed());
      setLivePlans(await getPlans());
    })();
  }, []);

  const scansLeft = pro ? Infinity : Math.max(0, FREE_SCANS - scansUsed);

  async function buy() {
    const chosen = livePlans.find((p) => p.key === plan);
    if (!chosen) {
      setBillingMsg(
        "Subscriptions aren't available here — open Litmas on your phone to subscribe.",
      );
      return;
    }
    setBusy(true);
    setBillingMsg(null);
    const outcome = await purchase(chosen);
    setBusy(false);
    if (outcome.status === "subscribed") {
      setPro(true);
      setShowPaywall(false);
    } else if (outcome.status === "error") {
      setBillingMsg(outcome.message);
    } else if (outcome.status === "unavailable") {
      setBillingMsg("Subscriptions aren't available on this device.");
    }
  }

  async function restorePurchases() {
    setBusy(true);
    setBillingMsg(null);
    const outcome = await restore();
    setBusy(false);
    if (outcome.status === "subscribed") {
      setPro(true);
      setShowPaywall(false);
    } else if (outcome.status === "error") {
      setBillingMsg(outcome.message);
    } else if (outcome.status === "unavailable") {
      setBillingMsg("Subscriptions aren't available on this device.");
    }
  }

  function handleFile(f: File) {
    if (!f.type.startsWith("image/")) {
      setError("That file isn't an image. Pick a photo or screenshot of a chart.");
      setStatus("error");
      return;
    }
    setFile(f);
    setPreviewUrl(URL.createObjectURL(f));
    setAnalysis(null);
    setError(null);
    setStatus("ready");
  }

  async function analyze() {
    if (!file) return;
    if (scansLeft <= 0) {
      setShowPaywall(true);
      return;
    }

    setStatus("analyzing");
    setError(null);

    try {
      const img = await loadImage(file);
      const full = toJpeg(img, MAX_DIMENSION, 0.85);
      const thumb = toJpeg(img, THUMB_DIMENSION, 0.6);

      const res = await fetch(`${API_BASE}/api/analyze`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Device-Id": getDeviceId(),
        },
        body: JSON.stringify({
          image: full.split(",")[1],
          mediaType: "image/jpeg",
        }),
      });

      const data = await res.json();

      if (res.status === 402) {
        localStorage.setItem(SCANS_KEY, String(data.scans_used ?? FREE_SCANS));
        setScansUsed(data.scans_used ?? FREE_SCANS);
        setStatus("ready");
        setShowPaywall(true);
        return;
      }
      if (!res.ok) throw new Error(data.error || "That scan didn't complete");

      const result: Analysis = data.analysis;
      setAnalysis(result);
      setStatus("done");

      if (result.is_chart) {
        const used =
          typeof data.scans_used === "number"
            ? data.scans_used
            : readScansUsed() + 1;
        localStorage.setItem(SCANS_KEY, String(used));
        setScansUsed(used);
        setHistory(saveEntry(result, thumb));
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong");
      setStatus("error");
    }
  }

  function reset() {
    setFile(null);
    setPreviewUrl(null);
    setAnalysis(null);
    setError(null);
    setStatus("idle");
    if (inputRef.current) inputRef.current.value = "";
  }

  function goto(next: View) {
    setOpenEntry(null);
    setView(next);
  }

  return (
    <main>
      <div className="header">
        <div className="brand">
          <StripMark className="mark" />
          <div className="names">
            <div className="wordmark">Litmas</div>
            <div className="subtitle">AI candlestick &amp; chart detector</div>
          </div>
        </div>
        <div className={`pill ${pro || scansLeft > 0 ? "" : "on"}`}>
          {pro
            ? "Pro"
            : scansLeft > 0
              ? `${scansLeft} free scan${scansLeft === 1 ? "" : "s"}`
              : "Start free trial"}
        </div>
      </div>

      <div className="tabs" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={view === "scan"}
          onClick={() => goto("scan")}
        >
          Scan
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={view === "history"}
          onClick={() => goto("history")}
        >
          History{history.length ? ` (${history.length})` : ""}
        </button>
      </div>

      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        style={{ display: "none" }}
        onChange={(e) => {
          const f = e.target.files?.[0];
          if (f) handleFile(f);
        }}
      />

      {view === "scan" && (
        <>
          {status === "idle" && (
            <div className="hero">
              <h1>Check every trade.</h1>
              <p>
                Snap the chart before you enter — or while you&apos;re holding —
                and get a second opinion in seconds.
              </p>
            </div>
          )}

          {!previewUrl && status !== "analyzing" && (
            <div
              className="dropzone"
              role="button"
              tabIndex={0}
              onClick={() => inputRef.current?.click()}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  e.preventDefault();
                  inputRef.current?.click();
                }
              }}
              onDragOver={(e) => e.preventDefault()}
              onDrop={(e) => {
                e.preventDefault();
                const f = e.dataTransfer.files?.[0];
                if (f) handleFile(f);
              }}
            >
              <StripMark className="mark" />
              <div className="title">Tap to scan a chart</div>
              <div className="sub">Camera, screenshot, or drag &amp; drop</div>
            </div>
          )}

          {previewUrl && status !== "analyzing" && (
            <div className="preview">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src={previewUrl} alt="The chart you selected" />
            </div>
          )}

          {status === "ready" && (
            <>
              <button className="btn btn-primary" onClick={analyze}>
                Check this trade
              </button>
              <button className="btn btn-ghost" onClick={reset}>
                Pick a different image
              </button>
            </>
          )}

          {status === "analyzing" && (
            <div className="loading">
              <div className="bars" aria-hidden="true">
                <i />
                <i />
                <i />
              </div>
              Reading candles, levels and patterns…
            </div>
          )}

          {status === "error" && (
            <>
              {error && <div className="error-box">{error}</div>}
              <button
                className="btn btn-primary"
                onClick={analyze}
                disabled={!file}
              >
                Try again
              </button>
              <button className="btn btn-ghost" onClick={reset}>
                Start over
              </button>
            </>
          )}

          {status === "done" && analysis && !analysis.is_chart && (
            <>
              <div className="error-box">
                That doesn&apos;t look like a trading chart
                {analysis.not_chart_reason
                  ? ` — ${analysis.not_chart_reason}`
                  : "."}{" "}
                Try a clearer shot of a price chart. This one didn&apos;t use a
                scan.
              </div>
              <button className="btn btn-primary" onClick={reset}>
                Scan another
              </button>
            </>
          )}

          {status === "done" && analysis?.is_chart && (
            <>
              <ResultView analysis={analysis} />
              <button className="btn btn-primary" onClick={reset}>
                Check another trade
              </button>
            </>
          )}
        </>
      )}

      {view === "history" && !openEntry && (
        <>
          {history.length === 0 ? (
            <div className="hist-empty">
              <StripMark className="mark" />
              Every chart you check is saved here, so you can look back at what
              you were seeing at the time.
            </div>
          ) : (
            <>
              <div className="hist-list">
                {history.map((entry) => (
                  <button
                    key={entry.id}
                    type="button"
                    className="hist-item"
                    onClick={() => setOpenEntry(entry)}
                  >
                    {entry.thumbnail ? (
                      /* eslint-disable-next-line @next/next/no-img-element */
                      <img
                        className="thumb"
                        src={entry.thumbnail}
                        alt=""
                      />
                    ) : (
                      <span className="thumb blank">
                        <StripMark
                          level={levelForCall(entry.analysis.call)}
                          style={{ width: 9, height: 24 }}
                        />
                      </span>
                    )}
                    <span className="hist-meta">
                      <span className="hist-top">
                        <span className="hist-call">{entry.analysis.call}</span>
                        <span className="hist-asset">
                          {entry.analysis.asset ?? "Chart"}
                          {entry.analysis.timeframe
                            ? ` · ${entry.analysis.timeframe}`
                            : ""}
                        </span>
                      </span>
                      <span className="hist-sub">
                        {Math.round(entry.analysis.confidence)}% confidence ·{" "}
                        {formatWhen(entry.saved_at)}
                      </span>
                    </span>
                  </button>
                ))}
              </div>
              <EducationalNotice />
              <button
                className="btn-quiet"
                style={{ display: "block", margin: "14px auto 0" }}
                onClick={() => setHistory(clearHistory())}
              >
                Clear history
              </button>
            </>
          )}
        </>
      )}

      {view === "history" && openEntry && (
        <>
          <button
            className="btn-quiet"
            style={{ marginBottom: 4 }}
            onClick={() => setOpenEntry(null)}
          >
            ← All scans
          </button>
          {openEntry.thumbnail && (
            <div className="preview">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src={openEntry.thumbnail} alt="The chart you scanned" />
            </div>
          )}
          <ResultView analysis={openEntry.analysis} />
          <button className="btn btn-ghost" onClick={() => setOpenEntry(null)}>
            Back to history
          </button>
        </>
      )}

      <div className="footer-links">
        <a href="/privacy">Privacy Policy</a>
        <a href="/terms">Terms of Use</a>
      </div>

      {showPaywall && (
        <div className="sheet-overlay" onClick={() => setShowPaywall(false)}>
          <div
            className="sheet"
            role="dialog"
            aria-modal="true"
            aria-label="Subscribe to Litmas"
            onClick={(e) => e.stopPropagation()}
          >
            <StripMark className="mark" />
            <h2>Check every trade</h2>
            <p className="sell">
              You&apos;ve used your free scans. Start a 3-day free trial for
              unlimited chart checks and full history.
            </p>

            <div className="plans">
              {(Object.keys(PLANS) as PlanKey[]).map((key) => {
                const live = livePlans.find((p) => p.key === key);
                return (
                  <button
                    key={key}
                    type="button"
                    className="plan"
                    aria-pressed={plan === key}
                    onClick={() => setPlan(key)}
                  >
                    <span>
                      <span className="name">
                        {key === "yearly" ? "Yearly" : "Monthly"}
                      </span>
                      <span className="terms">
                        3 days free, then billed{" "}
                        {key === "yearly" ? "annually" : "monthly"}
                      </span>
                    </span>
                    <span className="price">
                      {live?.priceString ?? PLANS[key].price}
                      <small>{PLANS[key].per}</small>
                    </span>
                  </button>
                );
              })}
            </div>

            {billingMsg && (
              <div className="error-box" style={{ marginTop: 12 }}>
                {billingMsg}
              </div>
            )}

            <button className="btn btn-primary" onClick={buy} disabled={busy}>
              {busy ? "Working…" : "Start 3-day free trial"}
            </button>
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                gap: 18,
                marginTop: 10,
              }}
            >
              <button
                className="btn-quiet"
                onClick={restorePurchases}
                disabled={busy}
              >
                Restore purchase
              </button>
              <button className="btn-quiet" onClick={() => setShowPaywall(false)}>
                Not now
              </button>
            </div>

            <p className="renew">
              Your subscription renews automatically at the end of each period
              unless cancelled at least 24 hours before it ends. Cancel any time
              in your store account settings. See our{" "}
              <a href="/terms">Terms</a> and <a href="/privacy">Privacy Policy</a>.
            </p>
          </div>
        </div>
      )}
    </main>
  );
}
