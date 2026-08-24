import { LAST_VERIFIED, SITE_URL } from "../lib/site-data";

export function loader() {
  const urls = ["/", "/support", "/privacy"]
    .map(
      (path) =>
        `  <url><loc>${SITE_URL}${path}</loc><lastmod>${LAST_VERIFIED}</lastmod></url>`
    )
    .join("\n");
  return new Response(
    `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urls}\n</urlset>\n`,
    { headers: { "Content-Type": "application/xml; charset=utf-8" } }
  );
}
