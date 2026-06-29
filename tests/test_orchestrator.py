"""End-to-end offline run: pipeline → scored niches → CSV + one-pagers."""

from __future__ import annotations

from niche_finder.orchestrator import run_pipeline
from niche_finder.reporting.csv_export import build_dataframe, export_csv
from niche_finder.reporting.onepager import generate_onepagers


def _write_niches(tmp_path):
    p = tmp_path / "niches.csv"
    p.write_text("seed_term,notes\nmeal prep,food\nhome fitness,bodyweight\n", encoding="utf-8")
    return p


def test_end_to_end_offline(repo, config, tmp_path):
    input_csv = _write_niches(tmp_path)
    report = run_pipeline(repo, config, input_csv)

    assert len(report.scored) == 2
    assert report.scored[0]["rank"] == 1
    assert report.scored[0]["composite"] >= report.scored[1]["composite"]

    # Every metric present and in range.
    for r in report.scored:
        assert set(r["metrics"]) == {
            "commercial_intent", "monetisation_fit", "seo_winnability",
            "aieo_gap", "pain_density", "trend_direction",
        }
        assert all(0 <= v <= 10 for v in r["metrics"].values())

    # CSV has one row per niche, sorted by rank, with all metric columns.
    df = build_dataframe(repo, report.run_id)
    assert len(df) == 2
    assert list(df["rank"]) == [1, 2]
    for m in r["metrics"]:
        assert m in df.columns

    csv_path = export_csv(repo, report.run_id, tmp_path / "out" / "ranked.csv")
    assert csv_path.exists()

    pages = generate_onepagers(repo, report.run_id, top=2, out_dir=tmp_path / "out" / "dd")
    assert len(pages) == 2
    body = pages[0].read_text(encoding="utf-8")
    assert "Money keyword cluster" in body
    assert "AIEO angle" in body


def test_run_is_resumable(repo, config, tmp_path):
    """A second run reuses cached raw responses (no crash, stable scores)."""
    input_csv = _write_niches(tmp_path)
    r1 = run_pipeline(repo, config, input_csv)
    r2 = run_pipeline(repo, config, input_csv)
    s1 = {x["seed_term"]: x["composite"] for x in r1.scored}
    s2 = {x["seed_term"]: x["composite"] for x in r2.scored}
    assert s1 == s2
