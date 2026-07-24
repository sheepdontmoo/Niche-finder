// Builds the static web bundle for the Capacitor (iOS/Android) shells.
//
// Next.js can't statically export a dynamic API route, so app/api is moved
// aside for the duration of the export — the mobile app calls the hosted
// backend at NEXT_PUBLIC_API_BASE_URL instead.
import { execSync } from "node:child_process";
import { existsSync, renameSync } from "node:fs";

const API_DIR = "app/api";
const BACKUP_DIR = ".api-backup";

const apiBase = process.env.NEXT_PUBLIC_API_BASE_URL;
if (!apiBase) {
  console.error(
    "NEXT_PUBLIC_API_BASE_URL is required for a mobile build — it must point " +
      "at your deployed web app, e.g. https://chartdetector.vercel.app",
  );
  process.exit(1);
}

if (existsSync(BACKUP_DIR)) {
  console.error(`${BACKUP_DIR} already exists — clean up a previous failed build first.`);
  process.exit(1);
}

renameSync(API_DIR, BACKUP_DIR);
try {
  execSync("npx next build", {
    stdio: "inherit",
    env: { ...process.env, STATIC_EXPORT: "1" },
  });
} finally {
  renameSync(BACKUP_DIR, API_DIR);
}

console.log(`\nStatic bundle written to ./out (API base: ${apiBase})`);
console.log("Next: npx cap sync");
