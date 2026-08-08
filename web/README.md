# ACQ Console — web app

Multi-user version of the constraint diagnostic. Runs entirely on your machine
first; the same code deploys unchanged.

**Status:** backend is done and tested — engine, schema, tenancy and RLS.
The UI is not built yet.

---

## Architecture, and why

**All security lives in Postgres RLS policies, not in application code.** The
browser talks straight to Supabase; the database refuses unauthorised rows no
matter what the client asks for. That has one big consequence: there is no
backend to run or deploy. It's a static bundle.

That's also why this is Vite + React rather than Next.js — with RLS as the
security boundary there is nothing for a server to do, so SSR would add
auth-cookie complexity for no security benefit.

## Run it locally

**Requires:** Node 20+, and Docker (for the local Supabase stack).

```bash
cd web
npm install
npx supabase start          # Postgres + Auth + Studio on localhost
npx supabase db reset       # applies supabase/migrations/
cp .env.example .env.local  # paste the API URL + anon key supabase start prints
npm run dev
```

**No Docker?** Create a free project at supabase.com, run the migration in its
SQL editor, and put that project's URL and anon key in `.env.local`. Everything
else is identical — the migrations are the same either way.

## Tests

```bash
npm test          # engine unit tests (no database needed)
npm run test:rls  # RLS isolation suite (needs a running Postgres)
```

The engine tests cover the rules that would otherwise fail silently: nulls never
coerced to zero, growth-level boundaries, ladder ordering, exactly one binding
constraint, and model-specific benchmarks changing the verdict.

The RLS suite creates two users and tries to make one read and write the other's
data. **Run it as a non-superuser role** — Postgres bypasses RLS for superusers
and table owners, so a suite run as `postgres` passes everything and proves
nothing.

## Layout

```
src/lib/engine.ts        the diagnostic engine — pure logic, no I/O
src/lib/engine.test.ts   20 unit tests
supabase/migrations/     schema, RLS policies, signup trigger, benchmark seed
supabase/tests/          RLS isolation suite + local auth stub
```

## Data model

`workspaces` → `workspace_members` (owner / member / viewer) → `businesses` →
`snapshots`.

Every save writes a **new snapshot** rather than overwriting, so metrics are a
time series and you can see whether the constraint is actually moving.

`funnel_benchmarks` and `prescriptions` are reference tables — readable by any
signed-in user, writable by none. Benchmarks live in the database rather than in
code because a wrong benchmark produces a confidently wrong verdict, and that
should be a data fix, not a deploy.

## Roles

| | read | edit data | manage members |
|---|---|---|---|
| owner | ✓ | ✓ | ✓ |
| member | ✓ | ✓ | |
| viewer | ✓ | | |

Signing up auto-creates your own workspace with you as owner, and accepts any
pending invites addressed to your email.

## Verified

- Engine: **20/20** unit tests pass
- RLS: **13/13** isolation checks pass against real Postgres 16 — cross-workspace
  reads, writes, updates, deletes, privilege escalation and reference-table
  writes are all refused at the database level
