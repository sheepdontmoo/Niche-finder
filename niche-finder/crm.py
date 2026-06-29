"""
crm.py — tiny file-based CRM + revenue ledger to drive toward $25k.

Tracks each prospect through the funnel and totals revenue so you always know
how close you are to the goal and which niche/message converts best.

Stages: new -> contacted -> replied -> call -> won / lost

Usage:
    python niche-finder/crm.py import                 # pull demo'd prospects into CRM
    python niche-finder/crm.py list [--stage call]    # show pipeline
    python niche-finder/crm.py set <slug> <stage>     # move a prospect
    python niche-finder/crm.py won <slug> --amount 899 [--mrr 59]
    python niche-finder/crm.py report                 # funnel + revenue vs $25k goal
"""
from __future__ import annotations

import argparse

from common import CRM_FILE, PROSPECTS_FILE, read_json, write_json

GOAL = 25000
STAGES = ["new", "contacted", "replied", "call", "won", "lost"]


def _load():
    return read_json(CRM_FILE, {"prospects": {}})


def _save(crm):
    write_json(CRM_FILE, crm)


def cmd_import(args):
    crm = _load()
    prospects = read_json(PROSPECTS_FILE, [])
    pool = [p for p in prospects if p.get("demo_url")] or prospects
    added = 0
    for p in pool:
        if p["slug"] in crm["prospects"]:
            continue
        crm["prospects"][p["slug"]] = {
            "name": p["name"], "niche": p.get("niche", ""),
            "phone": p.get("phone", ""), "email": p.get("email", ""),
            "demo_url": p.get("demo_url", ""), "pain_score": p.get("pain_score", 0),
            "stage": "new", "amount": 0, "mrr": 0, "notes": "",
        }
        added += 1
    _save(crm)
    print(f"Imported {added} prospects ({len(crm['prospects'])} total in CRM).")


def cmd_list(args):
    crm = _load()
    rows = sorted(crm["prospects"].items(),
                  key=lambda kv: (STAGES.index(kv[1]["stage"]), -kv[1]["pain_score"]))
    print(f"{'SLUG':<28}{'STAGE':<11}{'PAIN':>5}{'$':>7}  NAME")
    print("-" * 70)
    for slug, r in rows:
        if args.stage and r["stage"] != args.stage:
            continue
        print(f"{slug[:27]:<28}{r['stage']:<11}{r['pain_score']:>5}{r['amount']:>7}  {r['name'][:24]}")


def cmd_set(args):
    crm = _load()
    r = crm["prospects"].get(args.slug)
    if not r:
        print(f"No such prospect: {args.slug}"); return
    if args.stage not in STAGES:
        print(f"Stage must be one of {STAGES}"); return
    r["stage"] = args.stage
    if args.note:
        r["notes"] = (r["notes"] + " | " + args.note).strip(" |")
    _save(crm)
    print(f"{args.slug} → {args.stage}")


def cmd_won(args):
    crm = _load()
    r = crm["prospects"].get(args.slug)
    if not r:
        print(f"No such prospect: {args.slug}"); return
    r["stage"] = "won"
    r["amount"] = args.amount
    r["mrr"] = args.mrr
    _save(crm)
    print(f"🎉 WON {args.slug}: ${args.amount} build + ${args.mrr}/mo")
    cmd_report(args)


def cmd_report(args):
    crm = _load()
    ps = list(crm["prospects"].values())
    by_stage = {s: sum(1 for p in ps if p["stage"] == s) for s in STAGES}
    won = [p for p in ps if p["stage"] == "won"]
    one_time = sum(p["amount"] for p in won)
    mrr = sum(p["mrr"] for p in won)
    # count 3 months of MRR toward the goal as a conservative booked figure
    booked = one_time + mrr * 3

    print("\n=== FUNNEL ===")
    for s in STAGES:
        print(f"  {s:<10} {by_stage[s]}")
    contacted = sum(by_stage[s] for s in ("contacted", "replied", "call", "won", "lost"))
    if contacted:
        print(f"\n  reply rate : {by_stage['replied']+by_stage['call']+by_stage['won']}/{contacted}"
              f" = {100*(by_stage['replied']+by_stage['call']+by_stage['won'])/contacted:.0f}%")
        print(f"  close rate : {by_stage['won']}/{contacted} = {100*by_stage['won']/contacted:.0f}%")

    print("\n=== REVENUE vs $25,000 GOAL ===")
    print(f"  one-time builds : ${one_time:,}")
    print(f"  recurring MRR   : ${mrr:,}/mo  (~${mrr*3:,} booked at 3 mo)")
    pct = min(100, round(100 * booked / GOAL))
    bar = "█" * (pct // 4) + "·" * (25 - pct // 4)
    print(f"  toward goal     : ${booked:,} / ${GOAL:,}")
    print(f"  [{bar}] {pct}%")
    remaining = max(0, GOAL - booked)
    if remaining and won:
        avg = one_time / len(won)
        print(f"  ~{remaining/avg:.0f} more sales at your current avg (${avg:,.0f}) to hit goal.")
    elif remaining:
        print(f"  Land your first sale to start the ledger.")


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description="Niche-finder CRM + revenue ledger.")
    sub = ap.add_subparsers(dest="cmd", required=True)
    sub.add_parser("import").set_defaults(func=cmd_import)
    p_list = sub.add_parser("list"); p_list.add_argument("--stage"); p_list.set_defaults(func=cmd_list)
    p_set = sub.add_parser("set"); p_set.add_argument("slug"); p_set.add_argument("stage")
    p_set.add_argument("--note", default=""); p_set.set_defaults(func=cmd_set)
    p_won = sub.add_parser("won"); p_won.add_argument("slug")
    p_won.add_argument("--amount", type=int, required=True); p_won.add_argument("--mrr", type=int, default=0)
    p_won.set_defaults(func=cmd_won)
    sub.add_parser("report").set_defaults(func=cmd_report)
    args = ap.parse_args(argv)
    args.func(args)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
