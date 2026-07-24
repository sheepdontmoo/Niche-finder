# Project memory

## The Frank Fellers branding framework

Apply this 5-step framework to every product/brand decision in this repo:

1. **Focus** — create a focus that's a little different than the competition.
2. **Slogan** — create a slogan that reinforces that focus. Simple and memorable.
3. **Name** — create a name that's different from the competition: memorable, **two syllables**, non-generic.
4. **Logo & color** — create a logo that reinforces the name, then use **one color** in all marketing and advertising that's different from the competition.
5. **Deliver** — work your ass off to make sure the company comes through on the promise of that brand.

## Chart-analysis app: branding status

**⏸ PAUSED — the user is choosing the name. Do not rebrand until they provide it.**

- The code currently uses the **placeholder** name "ChartDetector" everywhere (app UI, `capacitor.config.ts`, Android/iOS bundle id `com.chartdetector.app`, store assets). This name CANNOT ship — "Chart Detector" is the competitor.
- User's requirements for the real name (per the framework + their notes): different from the competition, memorable, **two syllables**, non-generic, and **"detector" energy** — you scan a chart, it finds info and advises.
- Candidates discussed so far: Frank Charts · Ask Frank · CandleFrank · StraightCharts · Candor · Sonar. None confirmed.
- Focus (step 1) direction that resonated: **the honest chart read** — risk notes, invalidation, and "this setup is weak" honesty as the differentiator vs hype-driven competitors.
- One-color rule (step 4): current UI accent is teal/green like most fintech competitors — expect to change it to a differentiated single brand color when the name lands.

When the user supplies the name: apply it in one pass to app UI copy, `app/layout.tsx` metadata, legal pages, `capacitor.config.ts` (appId + appName), Android (`build.gradle` namespace/applicationId, `MainActivity` package, `strings.xml`), iOS (`Info.plist`, `project.pbxproj`), `scripts/make-assets.py` + `scripts/make-store-assets.py`, regenerate `resources/` + platform icons + `store-assets/`, and update `README.md` + `docs/play-store.md`. **Do this before the first Play upload — the applicationId is permanent once published.**
