import {
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
  useLocation,
} from "react-router";
import {
  ADSENSE_CLIENT,
  CONSENT_DEFAULT,
  shouldLoadAdsense,
} from "./lib/adsense";

export default function App() {
  const { pathname } = useLocation();
  const showPublicAds = shouldLoadAdsense(pathname);

  return (
    <html lang="en">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width,initial-scale=1" />
        {showPublicAds ? (
          <>
            <meta name="google-adsense-account" content={ADSENSE_CLIENT} />
            <script dangerouslySetInnerHTML={{ __html: CONSENT_DEFAULT }} />
            <script
              async
              crossOrigin="anonymous"
              src={`https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${ADSENSE_CLIENT}`}
            />
          </>
        ) : null}
        <link rel="preconnect" href="https://cdn.shopify.com/" />
        <link
          rel="stylesheet"
          href="https://cdn.shopify.com/static/fonts/inter/v4/styles.css"
        />
        <Meta />
        <Links />
      </head>
      <body>
        <Outlet />
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  );
}
