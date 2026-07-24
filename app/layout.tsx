import type { Metadata, Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Litmas — AI candlestick & chart detector",
  description:
    "Check every trade. Snap any chart and get a second opinion in seconds — pattern detection, support and resistance, trend direction, and a plain-language BUY, HOLD or SELL read with a confidence score.",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  themeColor: "#0b0d10",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
