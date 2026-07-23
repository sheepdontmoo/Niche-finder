const isStaticExport = process.env.STATIC_EXPORT === "1";

/** @type {import('next').NextConfig} */
const nextConfig = {
  // Static export is used for the Capacitor (iOS/Android) builds; the API
  // route is served by the regular web deployment and reached via
  // NEXT_PUBLIC_API_BASE_URL from the mobile shell.
  ...(isStaticExport
    ? { output: "export", images: { unoptimized: true } }
    : {}),
};

export default nextConfig;
