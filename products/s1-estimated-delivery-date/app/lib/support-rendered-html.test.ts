import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

import { supportFaqs } from "./site-data.ts";

test("production server output renders the featured weekend answer as H2 then paragraph", async (context) => {
  let serverOutput: string;
  try {
    serverOutput = await readFile(
      new URL("../../build/server/index.js", import.meta.url),
      "utf8"
    );
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") {
      context.skip("production server output is checked after npm run build");
      return;
    }
    throw error;
  }

  const featuredFaq = supportFaqs[0];
  assert.ok(serverOutput.includes(featuredFaq.question));
  assert.ok(serverOutput.includes(featuredFaq.answer));
  assert.match(
    serverOutput,
    /jsx\("h2",\s*\{\s*children: featuredFaq\.question\s*\}\),\s*\/\* @__PURE__ \*\/ jsx\("p",\s*\{\s*children: featuredFaq\.answer/
  );
});
