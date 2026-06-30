# /headline — Scroll-Stopping Headline Engine for remote1stjobs.com

Generate click-stopping headlines for LinkedIn graphics and posts using a rotating
blend of three direct-response masters — **MrBeast**, **Alex Hormozi**, and
**Sabri Suby** — tuned to remote1stjobs.com's ICP. Pick a framework at random each
run so the feed stays varied.

Usage: `/headline <topic / angle / stat / job data>` — e.g. `/headline 86 fresh remote jobs this week`.
If no topic is given, ask what the headline is for before generating.

## ICP — who every headline must resonate with
Remote job seekers across the **UK, Europe & EMEA** — across **ALL roles, not just tech**
(marketing, sales, design, customer success, ops, finance, HR, data, product, admin,
engineering, and more).

- **Their pains:** "remote" roles that are secretly US-only or hybrid; timezone rejection;
  getting ghosted; fake/recycled listings; feeling locked out of good remote work.
- **Their desires:** real remote freedom, location independence, better pay, work that fits
  life, and being *seen* as a serious candidate.

Write to that emotional core every time.

## How to run
1. Read the post context (topic, angle, any number/stat/live job data).
2. **Pick a framework at random** — rotate so the feed stays fresh. Check
   `HEADLINE_PROGRESS.md`; never use the same framework twice in a row. Log the one used.
3. Generate **6 headline options** in that framework's voice.
4. Choose the single strongest as the **graphic headline** — must be **≤ 6 words**, bold,
   and fit large on a 1:1 square. Offer the other 5 as alternates (longer ones can be the
   LinkedIn post hook line).
5. Apply the Golden Rules below.

## The frameworks

### MRBEAST — Curiosity + Stakes + Specificity
The headline *is* the product. Open a curiosity gap that's believable but irresistible;
use concrete numbers, escalating stakes, relatability and FOMO. Never bait-and-switch —
the post must pay it off.
Formulas:
- "[Specific number] [desirable thing] (and [surprising twist])"
- "[Bold claim most won't believe] — here's the proof"
- "Everyone [common behaviour]. They're all doing it wrong."
- "Found [number] remote jobs nobody's posting about"
Tone: punchy, big, scroll-stopping, surprising — but always true.

### HORMOZI — Value Equation + Big Specific Promise
Value = (**Dream Outcome × Perceived Likelihood**) ÷ (**Time Delay × Effort**).
Maximise the dream outcome and believability; minimise the time and effort implied.
Formulas:
- "How to [dream outcome] without [pain/effort]"
- "[Dream outcome] in [short timeframe] — even if [objection]"
- "The fastest way to [outcome] (no [common requirement] needed)"
- "Get [specific outcome] without [risk/effort/time]"
Tone: direct, specific, outcome-obsessed, status-raising.

### SABRI SUBY — Big Promise + Intrigue + Pain (Sell Like Crazy)
Hit the deepest pain or desire, make an irresistible promise, add intrigue.
Problem → Agitate → Solve.
Formulas:
- "Warning: [thing the ICP trusts] is [costing them X]"
- "Who else wants [dream outcome]?"
- "The [ugly truth / secret] about [topic] no one tells you"
- "Give me [tiny effort] and I'll [big outcome]"
- "[Number] [audience] are [doing X] — here's why you're not"
Tone: bold, provocative, pain-aware, promise-led.

## Output format
```
Framework used: [MRBEAST / HORMOZI / SABRI]
GRAPHIC HEADLINE (chosen, ≤6 words): "..."
Alternates:
1. ...
2. ...
3. ...
4. ...
5. ...
Why it lands for our ICP: [one line]
```
Then update `HEADLINE_PROGRESS.md`: date, framework used, headline used.

## Golden rules
- **Rotate frameworks** — log in `HEADLINE_PROGRESS.md`, never the same one twice in a row.
- **Clickbait that delivers** — the post must pay off the promise. A headline that lies kills trust.
- **All remote roles**, not just tech.
- **Never start with "I".**
- Graphic headline **≤ 6 words**, bold and punchy; save longer variants for the post hook.
- UK/Europe spelling and references.

## Hooks into /linkedin
When running `/linkedin`, call this engine to generate the headline for each post's graphic
(STEP 2 news, STEP 3 jobs, STEP 4 Priestley). Rotate a different framework per post so the
three daily graphics each have a distinct voice.
