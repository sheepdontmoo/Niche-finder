"""Streamlit dashboard for browsing results.

Run with: ``niche-finder dashboard`` (which calls ``streamlit run`` on this
file), or directly: ``streamlit run src/niche_finder/reporting/dashboard.py``.
Reads the SQLite DB read-only.
"""

from __future__ import annotations

from pathlib import Path

try:
    import streamlit as st
except ImportError:  # pragma: no cover - dashboard is an optional extra
    st = None

from ..config import METRICS, load_config
from ..reporting.csv_export import build_dataframe
from ..scoring.explain import deep_dive
from ..storage.db import connect
from ..storage.repository import Repository


def main() -> None:
    if st is None:
        raise SystemExit("streamlit is not installed. Install with: pip install 'niche-finder[dashboard]'")

    st.set_page_config(page_title="Niche Finder", layout="wide")
    st.title("🔍 Niche & Product Opportunity Finder")

    config = load_config()
    db_path = config.abs_path(config.paths.db)
    if not Path(db_path).exists():
        st.warning(f"No database at {db_path}. Run the pipeline first.")
        return

    conn = connect(db_path)
    repo = Repository(conn)
    run_id = repo.latest_score_run()
    if run_id is None:
        st.warning("No scored run found. Run `niche-finder run` then `niche-finder score`.")
        return

    df = build_dataframe(repo, run_id)
    st.subheader(f"Ranked shortlist (run #{run_id})")

    # Metric filters.
    with st.sidebar:
        st.header("Filters")
        thresholds = {
            m: st.slider(m.replace("_", " "), 0.0, 10.0, 0.0, 0.5) for m in METRICS
        }
        min_conf = st.slider("min confidence", 0.0, 1.0, 0.0, 0.05)

    filtered = df.copy()
    for m, t in thresholds.items():
        if m in filtered:
            filtered = filtered[filtered[m].fillna(0) >= t]
    if "confidence" in filtered:
        filtered = filtered[filtered["confidence"].fillna(0) >= min_conf]

    st.dataframe(filtered, use_container_width=True, hide_index=True)

    st.subheader("Deep dive")
    niches = list(df["niche"]) if not df.empty else []
    if niches:
        choice = st.selectbox("Select a niche", niches)
        nid = next(n["niche_id"] for n in repo.list_niches() if n["seed_term"] == choice)
        d = deep_dive(repo, nid, run_id)
        col1, col2 = st.columns(2)
        with col1:
            st.metric("Composite", f"{d['composite']:.2f}/10")
            st.write("**Pain points**")
            for p in d["pains"]:
                st.write(f"- {p['question']} — _\"{p['quote']}\"_")
            st.write("**Money keywords**")
            st.table(d["money_keywords"])
        with col2:
            st.write("**AIEO gaps (priority pages)**")
            for q in d["aieo"]["weak_questions"]:
                st.write(f"- {q['question']} (weakness {q['weakness']})")
            st.write("**Monetisation**")
            st.json(d["monetisation"])
            st.write("**Trend**")
            st.json(d["trend"])


if __name__ == "__main__":
    main()
