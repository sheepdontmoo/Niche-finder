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
  trend: z.enum(["up", "down", "sideways"]).describe("Direction of the trend"),
  call: z
    .enum(["BUY", "HOLD", "SELL"])
    .describe(
      "The plain-language read on this chart as it stands. BUY when the technicals favour opening or adding to a long, SELL when they favour exiting or shorting, HOLD when the chart is unclear, mid-range, or the setup has not confirmed.",
    ),
  call_reason: z
    .string()
    .describe(
      "One sentence, plain English, explaining the call in terms a non-technical trader understands",
    ),
  confidence: z
    .number()
    .describe("Confidence in the call, 0-100. Be honest — low when the chart is unclear."),
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
  invalidation: z
    .string()
    .nullable()
    .describe("The level or condition that would prove this read wrong"),
  risk_notes: z
    .string()
    .describe("Key risks, caveats, or conflicting signals in this chart"),
  summary: z
    .string()
    .describe("2-3 sentence plain-language summary of the chart"),
});

export type Analysis = z.infer<typeof AnalysisSchema>;
export type Call = Analysis["call"];
