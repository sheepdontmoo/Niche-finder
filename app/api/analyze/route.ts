import { NextRequest, NextResponse } from "next/server";
import Anthropic from "@anthropic-ai/sdk";
import { zodOutputFormat } from "@anthropic-ai/sdk/helpers/zod";
import { AnalysisSchema } from "@/lib/analysis";

export const runtime = "nodejs";
export const maxDuration = 120;

const ALLOWED_MEDIA_TYPES = [
  "image/jpeg",
  "image/png",
  "image/webp",
  "image/gif",
] as const;

type AllowedMediaType = (typeof ALLOWED_MEDIA_TYPES)[number];

// ~10MB of base64 — plenty for a downscaled screenshot, blocks abuse
const MAX_BASE64_LENGTH = 14_000_000;

const SYSTEM_PROMPT = `You are an expert technical analyst. The user sends a photo or screenshot of a trading chart and you produce a structured technical read of it.

Ground every claim in what is actually visible in the image: candles, wicks, trendlines, volume bars, indicator panes, axis labels. If price labels are readable, use real numbers for levels; if not, describe levels relative to the visible range ("the recent swing low", "the upper edge of the range").

If the image is not a financial chart, set is_chart to false and leave the analysis fields empty or null.

Phrase entry, stop and target fields as hypothetical, conditional scenarios ("if price reclaims X..."), never as instructions to trade. Be honest in risk_notes about anything that weakens the setup — low-quality photo, missing context, conflicting signals, or a chart too zoomed-in to judge trend. This output is educational analysis, not financial advice, and the app shows the user a disclaimer to that effect.`;

export async function POST(req: NextRequest) {
  if (!process.env.ANTHROPIC_API_KEY) {
    return NextResponse.json(
      { error: "Server is missing ANTHROPIC_API_KEY" },
      { status: 500 },
    );
  }

  let body: { image?: string; mediaType?: string };
  try {
    body = await req.json();
  } catch {
    return NextResponse.json({ error: "Invalid JSON body" }, { status: 400 });
  }

  const { image, mediaType } = body;
  if (!image || typeof image !== "string") {
    return NextResponse.json({ error: "Missing image" }, { status: 400 });
  }
  if (image.length > MAX_BASE64_LENGTH) {
    return NextResponse.json({ error: "Image too large" }, { status: 413 });
  }
  if (!ALLOWED_MEDIA_TYPES.includes(mediaType as AllowedMediaType)) {
    return NextResponse.json({ error: "Unsupported image type" }, { status: 400 });
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
      return NextResponse.json(
        { error: "The analysis could not be completed for this image." },
        { status: 422 },
      );
    }

    return NextResponse.json({ analysis: response.parsed_output });
  } catch (error) {
    if (error instanceof Anthropic.RateLimitError) {
      return NextResponse.json(
        { error: "Too many requests right now — try again in a minute." },
        { status: 429 },
      );
    }
    if (error instanceof Anthropic.APIError) {
      console.error("Anthropic API error", error.status, error.message);
      return NextResponse.json(
        { error: "Analysis service error — try again." },
        { status: 502 },
      );
    }
    console.error("Unexpected error in /api/analyze", error);
    return NextResponse.json({ error: "Unexpected error" }, { status: 500 });
  }
}
