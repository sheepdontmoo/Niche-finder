import { z } from "zod";

export const AnalysisSchema = z.object({
  is_chart: z
    .boolean()
    .describe("True only if the image is a financial trading chart"),
  not_chart_reason: z
    .string()
    .nullable()
    .describe("If is_chart is false, a short note on what the image shows"),
  asset: z
    .string()
    .nullable()
    .describe("Ticker/pair visible on the chart, e.g. BTC/USDT, AAPL, or null"),
  timeframe: z
    .string()
    .nullable()
    .describe("Chart timeframe if visible, e.g. 15m, 1H, 1D"),
  trend: z.enum(["bullish", "bearish", "sideways"]),
  bias: z
    .enum(["long", "short", "neutral"])
    .describe("Overall directional bias suggested by the technicals"),
  confidence: z
    .number()
    .describe("Confidence in the analysis, 0-100"),
  patterns: z
    .array(
      z.object({
        name: z.string().describe("Pattern name, e.g. Bull flag, Double top"),
        implication: z.string().describe("What this pattern typically signals"),
      }),
    )
    .describe("Chart patterns visible in the image"),
  support_levels: z
    .array(z.string())
    .describe("Approximate support price levels read from the chart"),
  resistance_levels: z
    .array(z.string())
    .describe("Approximate resistance price levels read from the chart"),
  indicators: z
    .array(z.string())
    .describe("Observations about any visible indicators (RSI, MACD, volume, MAs)"),
  entry_idea: z
    .string()
    .nullable()
    .describe("A hypothetical entry scenario, phrased conditionally"),
  stop_loss_idea: z
    .string()
    .nullable()
    .describe("Where invalidation of the setup would occur"),
  take_profit_ideas: z
    .array(z.string())
    .describe("Hypothetical targets if the setup plays out"),
  risk_notes: z
    .string()
    .describe("Key risks, caveats, or conflicting signals in this chart"),
  summary: z
    .string()
    .describe("2-3 sentence plain-language summary of the chart"),
});

export type Analysis = z.infer<typeof AnalysisSchema>;
