import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "com.litmas.app",
  appName: "Litmas",
  webDir: "out",
  backgroundColor: "#0b0d10",
  android: {
    allowMixedContent: false,
  },
  ios: {
    contentInset: "automatic",
  },
};

export default config;
