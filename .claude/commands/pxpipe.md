---
description: Set up, run, and manage the pxpipe token-saving proxy for Claude Code.
argument-hint: [setup|start|status|stop|models <list>]
---

# /pxpipe

You are running the **pxpipe** management workflow
(https://github.com/teamchong/pxpipe). The user's input is:

> $ARGUMENTS

pxpipe is a local proxy that rewrites the bulky parts of Claude Code requests
(system prompt, tool docs, older history) into compact PNGs before they leave
the machine, cutting input-token cost on dense content. It never touches the
model's output — only outgoing requests are rewritten.

If `$ARGUMENTS` is empty, ask the user which action they want: **setup**,
**start**, **status**, **stop**, or changing the **model allowlist** — don't
guess.

## Actions

### setup
- Confirm Node/npm is available (`node -v`).
- Explain that `npx pxpipe-proxy` runs the proxy with no separate install step
  (npx fetches it on first run). Ask before running any install/network
  command.
- Note the dashboard lives at `http://127.0.0.1:47821/` once running.

### start
- Run `npx pxpipe-proxy` (confirm first — this starts a background local
  server and pulls a package from npm).
- Tell the user to point Claude Code at it via
  `ANTHROPIC_BASE_URL=http://127.0.0.1:47821 claude` for new sessions.
- Surface the dashboard URL for live token-savings, per-request
  before/after, and the kill switch.

### status
- Check whether something is already listening on port 47821
  (e.g. `curl -sf http://127.0.0.1:47821/ -o /dev/null && echo running`).
- If running, point the user to the dashboard for savings numbers; the raw
  event log is `~/.pxpipe/events.jsonl`.

### stop
- Explain the dashboard has a kill switch, or the process can be stopped
  directly (find and confirm before killing the PID bound to 47821).

### models
- Default allowlist is `PXPIPE_MODELS=claude-fable-5,gpt-5.6` — dense,
  well-imaged models. Opus 4.7/4.8 and GPT 5.5 are opt-in
  (`PXPIPE_MODELS=...` or the dashboard chips) because they misread a
  meaningful fraction of imaged renders. `PXPIPE_MODELS=off` disables
  imaging entirely.
- If the user asks to change the allowlist, show the env var / dashboard
  toggle — don't edit files outside this repo without asking.

## Guardrails
- pxpipe is **lossy**: byte-exact values (IDs, hashes, secrets, exact hex/
  strings) can be silently misread on imaged content, especially outside the
  default model allowlist. Warn the user before enabling it for workflows
  that depend on verbatim accuracy, and mention the subagent escape hatch
  (`CLAUDE_CODE_SUBAGENT_MODEL=claude-sonnet-4-6`, or `model: sonnet` in
  agent frontmatter) for byte-exact work.
- It is a **local network proxy** sitting between Claude Code and the
  Anthropic API — always confirm before starting it, changing
  `ANTHROPIC_BASE_URL`, or running any install/fetch command.
- Never send real credentials/secrets through it without the user
  understanding the lossy-imaging tradeoff above.
