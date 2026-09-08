import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";

const dockerfile = readFileSync(new URL("./Dockerfile", import.meta.url), "utf8");
const packageJson = JSON.parse(
  readFileSync(new URL("./package.json", import.meta.url), "utf8"),
);
const packageLock: {
  packages: Record<string, { version?: string }>;
} = JSON.parse(
  readFileSync(new URL("./package-lock.json", import.meta.url), "utf8"),
);

test("Docker build and runtime stages satisfy the current Shopify CLI engine", () => {
  assert.match(dockerfile, /^FROM node:22-alpine AS build$/m);
  assert.match(dockerfile, /^FROM node:22-alpine AS runtime$/m);
  assert.doesNotMatch(dockerfile, /^FROM node:20(?:-|\s)/m);
});

test("the production query parser is pinned to the patched release", () => {
  assert.equal(packageJson.overrides.qs, "6.16.0");
  const qsVersions = Object.entries(packageLock.packages)
    .filter(([packagePath]) => /(^|\/)node_modules\/qs$/.test(packagePath))
    .map(([, packageMetadata]) => packageMetadata.version);

  assert.deepEqual(qsVersions, ["6.16.0"]);
});
