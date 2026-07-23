import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "com.chartdetector.app",
  appName: "ChartDetector",
  webDir: "out",
  backgroundColor: "#0b0f14",
  android: {
    allowMixedContent: false,
  },
  ios: {
    contentInset: "automatic",
  },
};

export default config;
