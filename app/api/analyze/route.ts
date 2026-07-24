import { NextRequest, NextResponse } from "next/server";
import Anthropic from "@anthropic-ai/sdk";
import { zodOutputFormat } from "@anthropic-ai/sdk/helpers/zod";
import { AnalysisSchema } from "@/lib/analysis";
import {
  FREE_SCANS,
  getScansUsed,
  incrementScans,
  isPro,
  meteringEnabled,
} from "@/lib/metering";

export const runtime = "nodejs";
export const maxDuration = 120;

// The Capacitor shell serves the UI from capacitor://localhost (iOS) or
// https://localhost (Android), so the hosted API must answer cross-origin.
const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, X-Device-Id",
};

function json(body: unknown, status = 200) {
  return NextResponse.json(body, { status, headers: CORS_HEADERS });
}

export function OPTIONS() {
  return new NextResponse(null, { status: 204, headers: CORS_HEADERS });
}

const ALLOWED_MEDIA_TYPES = [
  "image/jpeg",
  "image/png",
  "image/webp",
  "image/gif",
] as const;

type AllowedMediaType = (typeof ALLOWED_MEDIA_TYPES)[number];

// ~10MB of base64 — plenty for a downscaled screenshot, blocks abuse
const MAX_BASE64_LENGTH = 14_000_000;

const SYSTEM_PROMPT = `You are the second opinion on a trade. The user sends a photo or screenshot of a trading chart — often one they are about to enter, or already holding — and you give them a straight read of it.

Ground every claim in what is actually visible in the image: candles, wicks, trendlines, volume bars, indicator panes, axis labels. If price labels are readable, use real numbers for levels; if not, describe levels relative to the visible range ("the recent swing low", "the upper edge of the range").

If the image is not a financial chart, set is_chart to false and leave the analysis fields empty or null.

The call (BUY / HOLD / SELL) is a plain-language read of what the chart itself is showing, written for someone who does not speak in technical jargon. Use HOLD freely — it is the honest answer when the chart is mid-range, unconfirmed, or too unclear to judge, and it is better than a confident call you cannot support. Set confidence to genuinely reflect how readable the chart is: a blurry photo, a cropped view, or conflicting signals should pull it down.

Be honest in risk_notes about anything that weakens the read — low-quality image, missing context, conflicting indicators, or a chart too zoomed-in to judge trend. Always give the invalidation: the level or condition that would prove this read wrong.

This output is educational analysis, not financial advice, and never an instruction to trade. The app shows the user that disclaimer on every screen that displays a call.`;

export async function POST(req: NextRequest) {
  if (!process.env.ANTHROPIC_API_KEY) {
    return json({ error: "Server is missing ANTHROPIC_API_KEY" }, 500);
  }

  let body: { image?: string; mediaType?: string };
  try {
    body = await req.json();
  } catch {
    return json({ error: "Invalid JSON body" }, 400);
  }

  const { image, mediaType } = body;
  if (!image || typeof image !== "string") {
    return json({ error: "Missing image" }, 400);
  }
  if (image.length > MAX_BASE64_LENGTH) {
    return json({ error: "Image too large" }, 413);
  }
  if (!ALLOWED_MEDIA_TYPES.includes(mediaType as AllowedMediaType)) {
    return json({ error: "Unsupported image type" }, 400);
  }

  // Server-side free-scan gate (enforced when Upstash is configured).
  const deviceId = req.headers.get("x-device-id")?.slice(0, 64) ?? null;
  let metered = false;
  if (meteringEnabled() && deviceId) {
    try {
      if (!(await isPro(deviceId))) {
        const used = await getScansUsed(deviceId);
        if (used >= FREE_SCANS) {
          return json(
            {
              error: "Out of free scans",
              code: "limit_reached",
              scans_used: used,
              scans_limit: FREE_SCANS,
            },
            402,
          );
        }
        metered = true;
      }
    } catch (e) {
      // Metering outage should not block paying-intent users entirely;
      // log and fall through to the client-side gate.
      console.error("Metering unavailable", e);
    }
  }

  const client = new Anthropic();

  try {
    const response = await client.messages.parse({
      model: "claude-opus-4-8",
      max_tokens: 16000,
      thinking: { type: "adaptive" },
      system: SYSTEM_PROMPT,
      messages: [
        {
          role: "user",
          content: [
            {
              type: "image",
              source: {
                type: "base64",
                media_type: mediaType as AllowedMediaType,
                data: image,
              },
            },
            {
              type: "text",
              text: "Analyze this trading chart and return the structured technical analysis.",
            },
          ],
        },
      ],
      output_config: { format: zodOutputFormat(AnalysisSchema) },
    });

    if (response.stop_reason === "refusal" || !response.parsed_output) {
      return json(
        { error: "The analysis could not be completed for this image." },
        422,
      );
    }

    const analysis = response.parsed_output;

    let scansUsed: number | null = null;
    if (metered && deviceId && analysis.is_chart) {
      try {
        scansUsed = await incrementScans(deviceId);
      } catch (e) {
        console.error("Failed to record scan", e);
      }
    }

    return json({
      analysis,
      scans_used: scansUsed,
      scans_limit: meteringEnabled() ? FREE_SCANS : null,
    });
  } catch (error) {
    if (error instanceof Anthropic.RateLimitError) {
      return json(
        { error: "Too many requests right now — try again in a minute." },
        429,
      );
    }
    if (error instanceof Anthropic.APIError) {
      console.error("Anthropic API error", error.status, error.message);
      return json({ error: "Analysis service error — try again." }, 502);
    }
    console.error("Unexpected error in /api/analyze", error);
    return json({ error: "Unexpected error" }, 500);
  }
}
