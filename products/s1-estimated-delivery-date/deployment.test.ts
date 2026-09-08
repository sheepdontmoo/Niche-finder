import assert from "node:assert/strict";
import { createServer } from "node:http";
import { readFileSync } from "node:fs";
import { createRequire } from "node:module";
import test from "node:test";

import { sanitizeRequestPath } from "./request-log-redaction.mjs";

const require = createRequire(import.meta.url);
const morgan = require("morgan") as (
  format: string,
  options: { stream: { write: (line: string) => void } },
) => (
  request: import("node:http").IncomingMessage,
  response: import("node:http").ServerResponse,
  next: () => void,
) => void;

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

test("the production server preloads the request-log redaction", () => {
  assert.equal(
    packageJson.scripts.start,
    "node --import=./request-log-redaction.mjs ./node_modules/@react-router/serve/bin.js ./build/server/index.js",
  );
  assert.match(
    dockerfile,
    /^COPY --from=build \/app\/request-log-redaction\.mjs \.\/request-log-redaction\.mjs$/m,
  );
  assert.equal(packageJson.dependencies.morgan, "1.12.0");

  const morganVersions = Object.entries(packageLock.packages)
    .filter(([packagePath]) => /(^|\/)node_modules\/morgan$/.test(packagePath))
    .map(([, packageMetadata]) => packageMetadata.version);

  assert.deepEqual(morganVersions, ["1.12.0"]);
});

test("the production query parser is pinned to the patched release", () => {
  assert.equal(packageJson.overrides.qs, "6.16.0");
  const qsVersions = Object.entries(packageLock.packages)
    .filter(([packagePath]) => /(^|\/)node_modules\/qs$/.test(packagePath))
    .map(([, packageMetadata]) => packageMetadata.version);

  assert.deepEqual(qsVersions, ["6.16.0"]);
});

test("the request-log token returns only a normalized path", () => {
  const requestTarget =
    "/app/setup?id_token=fake-id-token&session=fake-session&hmac=fake-hmac&shop=example.myshopify.com";

  assert.equal(sanitizeRequestPath(requestTarget), "/app/setup");
  assert.equal(
    sanitizeRequestPath(
      "https://example.invalid/auth/callback?code=fake-code#ignored",
    ),
    "/auth/callback",
  );
});

test("Morgan tiny logs method, path, status and latency without a query", async (t) => {
  const lines: string[] = [];
  const logger = morgan("tiny", {
    stream: { write: (line) => lines.push(line.trim()) },
  });
  const server = createServer((request, response) => {
    logger(request, response, () => {
      response.statusCode = 204;
      response.end();
    });
  });

  await new Promise<void>((resolve, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", resolve);
  });
  t.after(
    () =>
      new Promise<void>((resolve, reject) => {
        server.close((error) => (error ? reject(error) : resolve()));
      }),
  );

  const address = server.address();
  assert.ok(address && typeof address !== "string");

  const response = await fetch(
    `http://127.0.0.1:${address.port}/app?id_token=fake-id-token&session=fake-session&hmac=fake-hmac`,
  );
  await response.arrayBuffer();
  await new Promise((resolve) => setImmediate(resolve));

  assert.equal(lines.length, 1);
  assert.match(lines[0], /^GET \/app 204 /);
  assert.match(lines[0], / - [0-9.]+ ms$/);
  assert.doesNotMatch(lines[0], /\?|id_token|session|hmac|fake-/);
});
