# LinkedIn Daily Content Engine — remote1stjobs.com
# Run every day with /linkedin to generate 3 posts + 3 graphics

## STEP 1 — SETUP
Check LINKEDIN_PROGRESS.md — if it doesn't exist, create it.
Upload logo to Magnific: https://www.remote1stjobs.com/assets/logo-nav-CQD0ncv2.png
Store the Magnific identifier for use in all 3 graphics.
Create folder: C:\claude code\remote1stjobs\linkedin\graphics-[TODAY'S DATE]\

HEADLINES: generate every post's hook and graphic headline with the `/headline`
engine. Rotate a DIFFERENT framework per post (MrBeast / Hormozi / Sabri Suby) so the
three daily graphics each have a distinct voice — check HEADLINE_PROGRESS.md and never
repeat a framework twice in a row. Use the chosen ≤6-word line on the graphic; longer
variants become the post hook.

AUDIENCE: remote1stjobs covers ALL remote roles, not just tech. Vary job categories
across graphics — Marketing, Sales, Design, Customer Success, Ops, Finance, Data,
Product, Tech — never a tech-only board.

## STEP 2 — POST 1: NEWS ANGLE
Search the web for a company in the news TODAY that is hiring, laying off, expanding remotely, or changing work policy.
Write a LinkedIn post with remote1stjobs.com's take on what it means for UK + Europe remote workers.

Rules:
- Headline: generate via `/headline` (news angle) — put the chosen ≤6-word line on the graphic, use a longer variant as the post hook
- Bold hook line that stops the scroll — never start with "I"
- 3-4 short punchy paragraphs, max 2 lines each
- End with a question to drive comments
- Link to relevant jobs page on remote1stjobs.com
- 5 hashtags: mix of #remotework #remotejobs #ukjobs #europejobs + 1 topical tag
- Check LINKEDIN_PROGRESS.md — never repeat a company or angle already used

Then generate POST 1 GRAPHIC via Magnific MCP:
- mode: gpt-2
- quality: high
- resolution: 2k
- aspectRatio: 1:1
- references: use uploaded logo identifier
- prompt: "LinkedIn square post graphic, dark navy #141b23 background, bold Akira Expanded heavy white text headline matching today's news topic, lime green #98f83d underline accent stroke, key stat or quote from post in small lime green text, professional person photorealistic cutout right side arms crossed, remote1stjobs snowflake logo top left with wordmark, royal blue #015af5 decorative brand pattern shapes top right, white bottom strip with remote1stjobs.com URL, clean modern professional tech design, 1:1 square format"
Save graphic to: C:\claude code\remote1stjobs\linkedin\graphics-[DATE]\post1.jpg

## STEP 3 — POST 2: JOB SPOTLIGHT
Fetch 3-5 of the best live jobs currently on remote1stjobs.com — check the live site or Supabase for real current listings.
Write a "jobs worth applying for this week" LinkedIn post.

Rules:
- Headline: generate via `/headline` (jobs angle) — chosen ≤6-word line on the graphic, longer variant as the post hook
- Hook line that stops the scroll
- Job list with emoji per role, title, company, salary if available, direct link to job on remote1stjobs.com
- CTA to browse all jobs at remote1stjobs.com
- End with a question
- 5 hashtags
- Never repeat jobs already spotlighted in LINKEDIN_PROGRESS.md

Then generate POST 2 GRAPHIC via Magnific MCP:
- mode: gpt-2
- quality: high
- resolution: 2k
- aspectRatio: 1:1
- references: use uploaded logo identifier
- prompt: "LinkedIn square post graphic, royal blue #015af5 background with subtle dot pattern, bold Akira Expanded heavy white text 'JOBS WORTH APPLYING FOR THIS WEEK', floating dark navy #141b23 job listing cards showing 3 role titles with lime green #98f83d icons, each card shows role name and Remote UK+Europe label, remote1stjobs snowflake logo top left white version, lime green arrow accents, bottom strip shows remote1stjobs.com in lime green pill button, clean professional tech design, 1:1 square format"
Save graphic to: C:\claude code\remote1stjobs\linkedin\graphics-[DATE]\post2.jpg

## STEP 4 — POST 3: DANIEL PRIESTLEY FRAMEWORK
Check LINKEDIN_PROGRESS.md for last Priestley type used.
Rotate in this order: SCARY → SEXY → FAMILIAR → EDUCATIONAL → repeat.

SCARY: Uncomfortable truth about remote work, job hunting, or hiring in Europe that people need to hear. Make it bold and provocative.
SEXY: Aspirational post about what remote work freedom actually looks like — income, lifestyle, location independence. Make people want it.
FAMILIAR: Relatable experience every remote job seeker has had — US-only roles, timezone rejection, ghosting. Make people feel seen.
EDUCATIONAL: Something genuinely useful and specific — how to spot fake remote jobs, how to negotiate remote salary, EU visa rules for remote work.

Rules for all types:
- Headline: generate via `/headline` (matched to the Priestley type) — chosen ≤6-word line on the graphic, longer variant as the post hook
- Hook line that stops the scroll — never start with "I"
- Short punchy paragraphs, max 2 lines each
- Personal voice — written as Darren, not as a brand
- End with a question to drive comments
- 5 hashtags
- Never repeat a topic already in LINKEDIN_PROGRESS.md

Then generate POST 3 GRAPHIC via Magnific MCP:
- mode: gpt-2
- quality: high
- resolution: 2k
- aspectRatio: 1:1
- references: use uploaded logo identifier
- SCARY prompt: "LinkedIn square post graphic, very dark navy #001136 background, bold Akira Expanded white WARNING style headline matching scary topic, large lime green #98f83d exclamation or alert icon accent, unsettling but professional atmosphere, remote1stjobs snowflake logo top left white, small remote1stjobs.com URL bottom, dramatic high contrast design, 1:1 square"
- SEXY prompt: "LinkedIn square post graphic, bright royal blue #015af5 gradient background, bold Akira Expanded white aspirational headline, lifestyle elements — laptop beach or city skyline subtle background, lime green #98f83d accent elements, person looking free and confident photorealistic cutout, remote1stjobs logo top left, remote1stjobs.com bottom, vibrant energetic design, 1:1 square"
- FAMILIAR prompt: "LinkedIn square post graphic, warm white background with subtle texture, royal blue #015af5 card center, bold white relatable headline on card, lime green #98f83d underline, person nodding or laughing photorealistic right side, remote1stjobs logo top left dark version, warm approachable design, 1:1 square"
- EDUCATIONAL prompt: "LinkedIn square post graphic, clean white background, bold dark navy #141b23 Akira Expanded headline, lime green #98f83d numbered list or checklist elements showing 3-4 key tips, minimal infographic style, remote1stjobs logo top left, professional clean editorial design, 1:1 square"
Save graphic to: C:\claude code\remote1stjobs\linkedin\graphics-[DATE]\post3.jpg

## STEP 5 — SAVE AND REPORT
Save all 3 posts to: C:\claude code\remote1stjobs\linkedin\posts-[DATE].md

Format in that file:
---
POST 1 — NEWS ANGLE ([company/topic])
[full post text]
Character count: [X]
Graphic: graphics-[DATE]/post1.jpg

POST 2 — JOB SPOTLIGHT
[full post text]
Character count: [X]
Graphic: graphics-[DATE]/post2.jpg

POST 3 — PRIESTLEY ([SCARY/SEXY/FAMILIAR/EDUCATIONAL])
[full post text]
Character count: [X]
Graphic: graphics-[DATE]/post3.jpg
---

Update LINKEDIN_PROGRESS.md:
- Add today's date
- Log Post 1 company/topic used
- Log Post 2 jobs spotlighted
- Log Post 3 Priestley type and topic used

Report back:
- Confirm all 3 posts saved
- Confirm all 3 graphics generated and saved
- Show preview of each post
- Flag anything that needs attention

## GOLDEN RULES
- Always check LINKEDIN_PROGRESS.md before creating anything — never repeat
- Headlines come from `/headline` — rotate frameworks (MrBeast/Hormozi/Sabri Suby), never twice in a row, log in HEADLINE_PROGRESS.md
- All remote roles, NOT just tech — vary job categories across graphics
- News must be real and from today — web search first
- Jobs must be real and live — check remote1stjobs.com
- Graphics use gpt-2 mode always — never default model
- Re-upload logo at the start of every session — identifiers expire
- Voice is Darren's — direct, no corporate speak, no "excited to share"
- Every post ends with a question
- Never start any post with "I"
