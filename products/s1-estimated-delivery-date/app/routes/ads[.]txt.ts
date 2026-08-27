import { ADSENSE_SELLER_RECORD } from "../lib/adsense";

export function loader() {
  return new Response(`${ADSENSE_SELLER_RECORD}\n`, {
    headers: { "Content-Type": "text/plain; charset=utf-8" },
  });
}
