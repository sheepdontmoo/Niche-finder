import { SITE_URL } from "../lib/site-data";

export function loader() {
  return new Response(
    `User-agent: *\nAllow: /\nDisallow: /app\nDisallow: /auth\nDisallow: /webhooks\n\nSitemap: ${SITE_URL}/sitemap.xml\n`,
    { headers: { "Content-Type": "text/plain; charset=utf-8" } }
  );
}
