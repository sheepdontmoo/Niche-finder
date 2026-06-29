"""Typer CLI entry point: init-db / run / score / report / dashboard."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

import typer

from .config import load_config
from .orchestrator import run_pipeline
from .reporting.csv_export import export_csv
from .reporting.onepager import generate_onepagers
from .scoring.scorer import score_run
from .storage.db import connect, init_db
from .storage.repository import Repository

app = typer.Typer(add_completion=False, help="Niche & Product Opportunity Finder")


def _repo(config):
    return Repository(connect(config.abs_path(config.paths.db)))


def _parse_steps(spec: str | None) -> set[int] | None:
    if not spec:
        return None
    out: set[int] = set()
    for part in spec.split(","):
        part = part.strip()
        if "-" in part:
            a, b = part.split("-")
            out.update(range(int(a), int(b) + 1))
        elif part:
            out.add(int(part))
    return out or None


@app.command("init-db")
def init_db_cmd(
    settings: str = typer.Option(None, help="Path to settings.yaml"),
):
    """Create the SQLite schema."""
    config = load_config(settings_path=settings)
    db = config.abs_path(config.paths.db)
    init_db(db)
    typer.echo(f"Initialised database at {db}")


@app.command()
def run(
    input: str = typer.Option("config/niches.example.csv", help="CSV of seed niches"),
    mode: str = typer.Option(None, help="live | record | fixture | auto (override settings)"),
    steps: str = typer.Option(None, help="Steps to run, e.g. '1-7' or '1,2,5'"),
    settings: str = typer.Option(None, help="Path to settings.yaml"),
    weights: str = typer.Option(None, help="Path to weights.yaml"),
):
    """Run the full pipeline over the seed niches and score them."""
    overrides = {"mode": mode} if mode else None
    config = load_config(settings_path=settings, weights_path=weights, overrides=overrides)
    db = config.abs_path(config.paths.db)
    init_db(db)
    repo = _repo(config)
    report = run_pipeline(repo, config, config.abs_path(input),
                          steps=_parse_steps(steps), log=typer.echo)
    typer.echo("\nTop niches:")
    for r in report.scored[:10]:
        typer.echo(f"  #{r['rank']:>2}  {r['composite']:>5.2f}  {r['seed_term']}"
                   f"  (conf {r['confidence']:.0%})")
    typer.echo(f"\nRun {report.run_id} complete. Use `niche-finder report` for outputs.")


@app.command()
def score(
    weights: str = typer.Option(None, help="Path to weights.yaml to re-score with"),
    settings: str = typer.Option(None, help="Path to settings.yaml"),
):
    """Re-score stored data with (possibly new) weights — no scraping."""
    config = load_config(settings_path=settings, weights_path=weights)
    repo = _repo(config)
    run_id = repo.create_run(mode="score",
                             config_json={"kind": "rescore"},
                             weights=config.weights, kind="score")
    results = score_run(repo, config, run_id)
    repo.finish_run(run_id)
    typer.echo(f"Re-scored {len(results)} niches (run #{run_id}):")
    for r in results[:10]:
        typer.echo(f"  #{r['rank']:>2}  {r['composite']:>5.2f}  {r['seed_term']}")


@app.command()
def report(
    top: int = typer.Option(5, help="How many niches to deep-dive"),
    out: str = typer.Option("out", help="Output directory"),
    run_id: int = typer.Option(None, help="Score run id (default: latest)"),
    settings: str = typer.Option(None, help="Path to settings.yaml"),
):
    """Write the ranked CSV and top-N deep-dive one-pagers."""
    config = load_config(settings_path=settings)
    repo = _repo(config)
    rid = run_id or repo.latest_score_run()
    if rid is None:
        typer.echo("No scored run found. Run `niche-finder run` first.", err=True)
        raise typer.Exit(1)

    out_dir = config.abs_path(out)
    csv_path = export_csv(repo, rid, out_dir / "ranked_niches.csv")
    pages = generate_onepagers(repo, rid, top, out_dir / "deep_dives")
    typer.echo(f"Ranked CSV: {csv_path}")
    typer.echo(f"Deep dives ({len(pages)}):")
    for p in pages:
        typer.echo(f"  {p}")


@app.command()
def dashboard(
    settings: str = typer.Option(None, help="Path to settings.yaml"),
):
    """Launch the Streamlit dashboard."""
    dash = Path(__file__).parent / "reporting" / "dashboard.py"
    subprocess.run([sys.executable, "-m", "streamlit", "run", str(dash)], check=False)


if __name__ == "__main__":
    app()
