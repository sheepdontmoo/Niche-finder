export const metadata = { title: "Privacy Policy — ChartDetector" };

export default function Privacy() {
  return (
    <main>
      <div className="header">
        <div className="logo">
          Chart<span>Detector</span>
        </div>
      </div>
      <div className="legal">
        <h1>Privacy Policy</h1>
        <p className="updated">Last updated: July 23, 2026</p>

        <h2>What we collect</h2>
        <p>
          <strong>Chart images.</strong> When you analyze a chart, the image
          you select is sent to our server and forwarded to Anthropic&apos;s
          Claude API to generate the analysis. Images are processed to produce
          your result and are not used to build a profile of you.
        </p>
        <p>
          <strong>Anonymous device identifier.</strong> The app generates a
          random identifier stored on your device. It is used only to count
          free scans and unlock purchases. It is not linked to your name,
          email, contacts, or any other personal information.
        </p>
        <p>
          <strong>What we do not collect.</strong> We do not collect your
          name, email address, location, contacts, or brokerage account
          information, and we do not access your photo library beyond the
          single image you explicitly choose.
        </p>

        <h2>Third parties</h2>
        <p>
          Chart images are processed by Anthropic (the AI provider) under
          their commercial data policies. Scan counts are stored with our
          hosting and database providers. We do not sell your data.
        </p>

        <h2>Data retention</h2>
        <p>
          We do not maintain a library of your uploaded images. Anonymous scan
          counters are retained to enforce plan limits.
        </p>

        <h2>Your choices</h2>
        <p>
          You can clear the app&apos;s stored data at any time via your device
          settings (or your browser&apos;s site data controls on the web),
          which removes the device identifier. To request deletion of
          server-side scan counters, contact us with your device identifier.
        </p>

        <h2>Not financial advice</h2>
        <p>
          ChartDetector provides AI-generated educational analysis only. It is
          not investment, financial, legal, or tax advice.
        </p>

        <h2>Contact</h2>
        <p>Questions? Email basdesignco@gmail.com.</p>

        <p>
          <a href="/">← Back to ChartDetector</a>
        </p>
      </div>
    </main>
  );
}
