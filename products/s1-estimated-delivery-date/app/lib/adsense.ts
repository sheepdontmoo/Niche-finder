export const ADSENSE_CLIENT = "ca-pub-6271772629726247";

export const ADSENSE_SELLER_RECORD =
  "google.com, pub-6271772629726247, DIRECT, f08c47fec0942fa0";

const ADSENSE_PUBLIC_PATHS = new Set(["/", "/support", "/privacy"]);

export const shouldLoadAdsense = (pathname: string) => {
  const normalizedPath =
    pathname !== "/" ? pathname.replace(/\/$/, "") : pathname;

  return ADSENSE_PUBLIC_PATHS.has(normalizedPath);
};

export const CONSENT_DEFAULT = `window.dataLayer=window.dataLayer||[];
function gtag(){dataLayer.push(arguments);}
gtag('consent','default',{
  ad_storage:'denied',
  ad_user_data:'denied',
  ad_personalization:'denied',
  analytics_storage:'denied',
  wait_for_update:500
});`;
