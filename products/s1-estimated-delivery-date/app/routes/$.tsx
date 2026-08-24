import { data } from "react-router";

export function loader() {
  return data(null, {
    status: 404,
    headers: { "X-Robots-Tag": "noindex, nofollow" },
  });
}

export const meta = () => [
  { title: "Page not found | SupaDatewise" },
  { name: "robots", content: "noindex, nofollow" },
];

export default function NotFound() {
  return (
    <main>
      <h1>Page not found</h1>
      <p>The SupaDatewise page you requested is not available.</p>
    </main>
  );
}
