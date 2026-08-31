import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";

const dockerfile = readFileSync(new URL("./Dockerfile", import.meta.url), "utf8");

test("Docker build and runtime stages satisfy the current Shopify CLI engine", () => {
  assert.match(dockerfile, /^FROM node:22-alpine AS build$/m);
  assert.match(dockerfile, /^FROM node:22-alpine AS runtime$/m);
  assert.doesNotMatch(dockerfile, /^FROM node:20(?:-|\s)/m);
});
