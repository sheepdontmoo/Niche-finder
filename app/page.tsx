"use client";

import { useEffect, useRef, useState } from "react";
import type { Analysis } from "@/lib/analysis";

const FREE_SCANS = 3;
const SCANS_KEY = "chart-detector-scans-used";
const MAX_DIMENSION = 2000;

type Status = "idle" | "ready" | "analyzing" | "done" | "error";

function readScansUsed(): number {
  const raw = localStorage.getItem(SCANS_KEY);
  const n = raw ? parseInt(raw, 10) : 0;
  return Number.isFinite(n) && n >= 0 ? n : 0;
}

async function fileToResizedJpeg(
  file: File,
): Promise<{ base64: string; mediaType: string }> {
  const dataUrl = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(new Error("Could not read file"));
    reader.readAsDataURL(file);
  });

  const img = await new Promise<HTMLImageElement>((resolve, reject) => {
    const el = new Image();
    el.onload = () => resolve(el);
    el.onerror = () => reject(new Error("Could not decode image"));
    el.src = dataUrl;
  });

  const scale = Math.min(1, MAX_DIMENSION / Math.max(img.width, img.height));
  const canvas = document.createElement("canvas");
  canvas.width = Math.round(img.width * scale);
  canvas.height = Math.round(img.height * scale);
  const ctx = canvas.getContext("2d");
  if (!ctx) throw new Error("Canvas unavailable");
  ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

  const jpegUrl = canvas.toDataURL("image/jpeg", 0.85);
  return { base64: jpegUrl.split(",")[1], mediaType: "image/jpeg" };
}

export default function Home() {
  const [status, setStatus] = useState<Status>("idle");
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [file, setFile] = useState<File | null>(null);
  const [analysis, setAnalysis] = useState<Analysis | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [scansUsed, setScansUsed] = useState(0);
  const [showPaywall, setShowPaywall] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setScansUsed(readScansUsed());
  }, []);

  const scansLeft = Math.max(0, FREE_SCANS - scansUsed);

  function handleFile(f: File) {
    if (!f.type.startsWith("image/")) {
      setError("Please choose an image file.");
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
      const { base64, mediaType } = await fileToResizedJpeg(file);
      const res = await fetch("/api/analyze", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ image: base64, mediaType }),
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.error || "Analysis failed");
      }

      const result: Analysis = data.analysis;
      setAnalysis(result);
      setStatus("done");

      if (result.is_chart) {
        const used = readScansUsed() + 1;
        localStorage.setItem(SCANS_KEY, String(used));
        setScansUsed(used);
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

  return (
    <main>
      <div className="header">
        <div className="logo">
          Chart<span>Detector</span>
        </div>
        <div className="scans-pill">
          {scansLeft > 0 ? `${scansLeft} free scan${scansLeft === 1 ? "" : "s"} left` : "Upgrade for more"}
        </div>
      </div>

      {status === "idle" && (
        <div className="hero">
          <h1>
            Snap a chart.
            <br />
            Get an instant AI read.
          </h1>
          <p>
            Take a photo or screenshot of any trading chart — stocks, crypto,
            forex — and get trend, patterns, key levels and trade ideas in
            seconds.
          </p>
        </div>
      )}

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

      {!previewUrl && (
        <div
          className="dropzone"
          role="button"
          tabIndex={0}
          onClick={() => inputRef.current?.click()}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " ") inputRef.current?.click();
          }}
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            e.preventDefault();
            const f = e.dataTransfer.files?.[0];
            if (f) handleFile(f);
          }}
        >
          <div className="icon">📈</div>
          <div className="title">Tap to snap or upload a chart</div>
          <div className="sub">Camera, screenshot, or drag &amp; drop</div>
        </div>
      )}

      {previewUrl && (
        <div className="preview">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={previewUrl} alt="Selected chart" />
        </div>
      )}

      {status === "ready" && (
        <>
          <button className="btn btn-primary" onClick={analyze}>
            Analyze chart
          </button>
          <button className="btn btn-ghost" onClick={reset}>
            Choose a different image
          </button>
        </>
      )}

      {status === "analyzing" && (
        <div className="loading">
          <div className="spinner" />
          Reading candles, levels and patterns…
        </div>
      )}

      {status === "error" && (
        <>
          {error && <div className="error-box">{error}</div>}
          <button className="btn btn-primary" onClick={analyze} disabled={!file}>
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
            {analysis.not_chart_reason ? ` — ${analysis.not_chart_reason}` : "."}{" "}
            Try a clearer screenshot of a price chart.
          </div>
          <button className="btn btn-primary" onClick={reset}>
            Try another image
          </button>
        </>
      )}

      {status === "done" && analysis && analysis.is_chart && (
        <>
          <div className="card">
            <div className="verdict">
              <div>
                <h3>AI Verdict</h3>
                <div className={`label ${analysis.bias}`}>{analysis.bias}</div>
              </div>
              <div style={{ minWidth: 120 }}>
                <h3>Confidence {Math.round(analysis.confidence)}%</h3>
                <div className="confidence-bar">
                  <div
                    className="confidence-fill"
                    style={{ width: `${Math.min(100, Math.max(0, analysis.confidence))}%` }}
                  />
                </div>
              </div>
            </div>
            <div className="meta-row">
              {analysis.asset && <span className="tag">{analysis.asset}</span>}
              {analysis.timeframe && (
                <span className="tag">{analysis.timeframe}</span>
              )}
              <span className="tag">trend: {analysis.trend}</span>
            </div>
          </div>

          <div className="card">
            <h3>Summary</h3>
            <p className="body-text">{analysis.summary}</p>
          </div>

          {analysis.patterns.length > 0 && (
            <div className="card">
              <h3>Patterns detected</h3>
              {analysis.patterns.map((p, i) => (
                <div className="pattern" key={i}>
                  <div className="name">{p.name}</div>
                  <div className="impl">{p.implication}</div>
                </div>
              ))}
            </div>
          )}

          {(analysis.support_levels.length > 0 ||
            analysis.resistance_levels.length > 0) && (
            <div className="card">
              <h3>Key levels</h3>
              {analysis.resistance_levels.map((r, i) => (
                <div className="kv" key={`r${i}`}>
                  <span className="k">Resistance</span>
                  <span className="v">{r}</span>
                </div>
              ))}
              {analysis.support_levels.map((s, i) => (
                <div className="kv" key={`s${i}`}>
                  <span className="k">Support</span>
                  <span className="v">{s}</span>
                </div>
              ))}
            </div>
          )}

          {(analysis.entry_idea ||
            analysis.stop_loss_idea ||
            analysis.take_profit_ideas.length > 0) && (
            <div className="card">
              <h3>Hypothetical setup</h3>
              {analysis.entry_idea && (
                <div className="kv">
                  <span className="k">Entry</span>
                  <span className="v">{analysis.entry_idea}</span>
                </div>
              )}
              {analysis.stop_loss_idea && (
                <div className="kv">
                  <span className="k">Invalidation</span>
                  <span className="v">{analysis.stop_loss_idea}</span>
                </div>
              )}
              {analysis.take_profit_ideas.map((t, i) => (
                <div className="kv" key={i}>
                  <span className="k">Target {i + 1}</span>
                  <span className="v">{t}</span>
                </div>
              ))}
            </div>
          )}

          {analysis.indicators.length > 0 && (
            <div className="card">
              <h3>Indicator read</h3>
              <ul className="plain">
                {analysis.indicators.map((ind, i) => (
                  <li key={i}>{ind}</li>
                ))}
              </ul>
            </div>
          )}

          <div className="card">
            <h3>Risk notes</h3>
            <p className="body-text muted">{analysis.risk_notes}</p>
          </div>

          <button className="btn btn-primary" onClick={reset}>
            Scan another chart
          </button>
        </>
      )}

      <p className="disclaimer">
        ChartDetector provides AI-generated educational analysis only. It is
        not financial advice, and past patterns do not predict future results.
        Trading involves substantial risk of loss — always do your own
        research.
      </p>

      {showPaywall && (
        <div className="paywall-overlay" onClick={() => setShowPaywall(false)}>
          <div className="paywall" onClick={(e) => e.stopPropagation()}>
            <h2>You&apos;re out of free scans</h2>
            <p>Go Pro for unlimited chart scans, deeper analysis and history.</p>
            <div className="price">$9.99</div>
            <div className="per">per week — cancel anytime</div>
            <button
              className="btn btn-primary"
              onClick={() => setShowPaywall(false)}
            >
              Continue
            </button>
            <button className="btn btn-ghost" onClick={() => setShowPaywall(false)}>
              Not now
            </button>
          </div>
        </div>
      )}
    </main>
  );
}
