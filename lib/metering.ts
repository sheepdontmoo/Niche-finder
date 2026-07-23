// Server-side scan metering backed by Upstash Redis (REST API).
// If UPSTASH_REDIS_REST_URL / UPSTASH_REDIS_REST_TOKEN are not configured,
// metering degrades to "not enforced" and the client-side gate is the only
// limit — fine for local dev, not for production.

export const FREE_SCANS = 3;

const url = () => process.env.UPSTASH_REDIS_REST_URL;
const token = () => process.env.UPSTASH_REDIS_REST_TOKEN;

export function meteringEnabled(): boolean {
  return Boolean(url() && token());
}

async function redis(command: (string | number)[]): Promise<unknown> {
  const res = await fetch(url() as string, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token()}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(command),
    // Metering must never take down analysis for long — fail fast.
    signal: AbortSignal.timeout(5000),
  });
  if (!res.ok) throw new Error(`Redis error ${res.status}`);
  const data = (await res.json()) as { result: unknown };
  return data.result;
}

export async function getScansUsed(deviceId: string): Promise<number> {
  const result = await redis(["GET", `scans:${deviceId}`]);
  const n = typeof result === "string" ? parseInt(result, 10) : 0;
  return Number.isFinite(n) && n >= 0 ? n : 0;
}

export async function incrementScans(deviceId: string): Promise<number> {
  const result = await redis(["INCR", `scans:${deviceId}`]);
  return typeof result === "number" ? result : 0;
}

export async function isPro(deviceId: string): Promise<boolean> {
  // Set `pro:{deviceId}` to "1" from your billing webhook (RevenueCat /
  // Play Billing / Stripe) to unlock unlimited scans for a device.
  const result = await redis(["GET", `pro:${deviceId}`]);
  return result === "1";
}
