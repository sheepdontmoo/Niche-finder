import morgan from "morgan";

const FALLBACK_PATH = "/";
const MAX_LOGGED_PATH_LENGTH = 2048;

function normalizePath(pathname) {
  const path = pathname || FALLBACK_PATH;
  let sanitized = "";

  for (const character of path.slice(0, MAX_LOGGED_PATH_LENGTH)) {
    const codePoint = character.codePointAt(0) ?? 0;
    sanitized += codePoint < 32 || codePoint === 127 ? "�" : character;
  }

  return sanitized;
}

/**
 * Return only the URL path. Shopify embeds short-lived authentication material
 * in query parameters, so query strings must never reach production logs.
 */
export function sanitizeRequestPath(requestTarget) {
  if (typeof requestTarget !== "string" || requestTarget.length === 0) {
    return FALLBACK_PATH;
  }

  try {
    return normalizePath(
      new URL(requestTarget, "http://request.invalid").pathname,
    );
  } catch {
    return normalizePath(requestTarget.split(/[?#]/, 1)[0]);
  }
}

// @react-router/serve uses Morgan's public `url` token in its `tiny` format.
// A Node preload installs this override before the server CLI imports Morgan.
morgan.token("url", (request) =>
  sanitizeRequestPath(request.originalUrl || request.url),
);
