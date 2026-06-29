"""Typed CRUD / upsert helpers over the SQLite schema.

Steps and scoring talk to the database only through this class — no raw SQL
leaks into the pipeline logic. JSON columns are (de)serialised here.
"""

from __future__ import annotations

import json
import sqlite3
from datetime import datetime, timezone
from typing import Any

from ..models import (
    AieoGrade,
    KeywordMetric,
    Monetisation,
    PainPoint,
    RedditPost,
    SerpResult,
    Trend,
)


def _now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _loads(val: Any, default: Any) -> Any:
    if not val:
        return default
    try:
        return json.loads(val)
    except (json.JSONDecodeError, TypeError):
        return default


class Repository:
    def __init__(self, conn: sqlite3.Connection):
        self.conn = conn

    def commit(self) -> None:
        self.conn.commit()

    # ---- runs --------------------------------------------------------------
    def create_run(self, mode: str, config_json: dict, weights: dict | None = None,
                   kind: str = "pipeline") -> int:
        cur = self.conn.execute(
            "INSERT INTO runs(started_at, mode, config_json, weights_json, kind) "
            "VALUES (?,?,?,?,?)",
            (_now(), mode, json.dumps(config_json), json.dumps(weights or {}), kind),
        )
        self.conn.commit()
        return int(cur.lastrowid)

    def finish_run(self, run_id: int) -> None:
        self.conn.execute("UPDATE runs SET finished_at=? WHERE run_id=?", (_now(), run_id))
        self.conn.commit()

    # ---- niches ------------------------------------------------------------
    def upsert_niche(self, seed_term: str, notes: str = "") -> int:
        self.conn.execute(
            "INSERT INTO niches(seed_term, notes, created_at) VALUES (?,?,?) "
            "ON CONFLICT(seed_term) DO UPDATE SET notes=excluded.notes",
            (seed_term, notes, _now()),
        )
        row = self.conn.execute(
            "SELECT niche_id FROM niches WHERE seed_term=?", (seed_term,)
        ).fetchone()
        self.conn.commit()
        return int(row["niche_id"])

    def list_niches(self) -> list[sqlite3.Row]:
        return self.conn.execute("SELECT * FROM niches ORDER BY niche_id").fetchall()

    def get_niche(self, niche_id: int) -> sqlite3.Row | None:
        return self.conn.execute(
            "SELECT * FROM niches WHERE niche_id=?", (niche_id,)
        ).fetchone()

    # ---- raw responses -----------------------------------------------------
    def get_raw(self, request_hash: str) -> dict | None:
        row = self.conn.execute(
            "SELECT response_json FROM raw_responses WHERE request_hash=?", (request_hash,)
        ).fetchone()
        if row is None:
            return None
        return _loads(row["response_json"], None)

    def save_raw(self, source: str, request_hash: str, request: dict,
                 response: Any, status: str = "ok", run_id: int | None = None) -> int:
        cur = self.conn.execute(
            "INSERT INTO raw_responses(source, request_hash, request_json, response_json, "
            "status, fetched_at, run_id) VALUES (?,?,?,?,?,?,?) "
            "ON CONFLICT(request_hash) DO UPDATE SET response_json=excluded.response_json, "
            "status=excluded.status, fetched_at=excluded.fetched_at",
            (source, request_hash, json.dumps(request), json.dumps(response),
             status, _now(), run_id),
        )
        self.conn.commit()
        return int(cur.lastrowid)

    # ---- step 1: keywords + clusters --------------------------------------
    def add_keyword(self, niche_id: int, phrase: str, depth: int, source: str,
                    is_buyer_modifier: bool) -> int:
        cur = self.conn.execute(
            "INSERT INTO keywords(niche_id, phrase, depth, source, is_buyer_modifier) "
            "VALUES (?,?,?,?,?) ON CONFLICT(niche_id, phrase) DO NOTHING",
            (niche_id, phrase, depth, source, int(is_buyer_modifier)),
        )
        if cur.lastrowid:
            return int(cur.lastrowid)
        row = self.conn.execute(
            "SELECT keyword_id FROM keywords WHERE niche_id=? AND phrase=?",
            (niche_id, phrase),
        ).fetchone()
        return int(row["keyword_id"])

    def create_cluster(self, niche_id: int, label: str, member_count: int) -> int:
        cur = self.conn.execute(
            "INSERT INTO clusters(niche_id, label, member_count) VALUES (?,?,?)",
            (niche_id, label, member_count),
        )
        return int(cur.lastrowid)

    def assign_cluster(self, keyword_id: int, cluster_id: int) -> None:
        self.conn.execute(
            "UPDATE keywords SET cluster_id=? WHERE keyword_id=?", (cluster_id, keyword_id)
        )

    def keywords_for(self, niche_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT * FROM keywords WHERE niche_id=? ORDER BY keyword_id", (niche_id,)
        ).fetchall()

    def clusters_for(self, niche_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT * FROM clusters WHERE niche_id=? ORDER BY member_count DESC", (niche_id,)
        ).fetchall()

    # ---- step 2: keyword metrics ------------------------------------------
    def save_keyword_metric(self, keyword_id: int, m: KeywordMetric) -> None:
        self.conn.execute(
            "INSERT INTO keyword_metrics(keyword_id, volume, kd, cpc, traffic_value, "
            "serp_features_json, has_forum_on_p1, fetched_at) VALUES (?,?,?,?,?,?,?,?) "
            "ON CONFLICT(keyword_id) DO UPDATE SET volume=excluded.volume, kd=excluded.kd, "
            "cpc=excluded.cpc, traffic_value=excluded.traffic_value, "
            "serp_features_json=excluded.serp_features_json, "
            "has_forum_on_p1=excluded.has_forum_on_p1, fetched_at=excluded.fetched_at",
            (keyword_id, m.volume, m.kd, m.cpc, m.traffic_value,
             json.dumps(m.serp_features), int(m.has_forum_on_p1), _now()),
        )

    def metrics_for(self, niche_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT k.phrase, k.is_buyer_modifier, k.cluster_id, m.* "
            "FROM keyword_metrics m JOIN keywords k ON k.keyword_id=m.keyword_id "
            "WHERE k.niche_id=?",
            (niche_id,),
        ).fetchall()

    # ---- idempotency: clear a niche's rows for a step before re-running ----
    def clear_keywords(self, niche_id: int) -> None:
        # Deleting keywords cascades keyword_metrics + serp_results (keyword_id FK);
        # clusters are then orphan-free and safe to delete.
        self.conn.execute("DELETE FROM keywords WHERE niche_id=?", (niche_id,))
        self.conn.execute("DELETE FROM clusters WHERE niche_id=?", (niche_id,))

    def clear_pain(self, niche_id: int) -> None:
        self.conn.execute("DELETE FROM reddit_posts WHERE niche_id=?", (niche_id,))
        self.conn.execute("DELETE FROM pain_points WHERE niche_id=?", (niche_id,))

    def clear_serp(self, niche_id: int) -> None:
        self.conn.execute("DELETE FROM serp_results WHERE niche_id=?", (niche_id,))

    def clear_aieo(self, niche_id: int) -> None:
        # money_questions cascade-deletes their aieo_grades (FK ON DELETE CASCADE).
        self.conn.execute("DELETE FROM money_questions WHERE niche_id=?", (niche_id,))

    # ---- step 3: reddit + pain --------------------------------------------
    def save_reddit_post(self, niche_id: int, p: RedditPost,
                         raw_resp_id: int | None = None) -> int:
        cur = self.conn.execute(
            "INSERT INTO reddit_posts(niche_id, subreddit, permalink, title, body, score, "
            "created_utc, matched_signals_json, raw_resp_id) VALUES (?,?,?,?,?,?,?,?,?)",
            (niche_id, p.subreddit, p.permalink, p.title, p.body, p.score,
             p.created_utc, json.dumps(p.matched_signals), raw_resp_id),
        )
        return int(cur.lastrowid)

    def save_pain_point(self, niche_id: int, pp: PainPoint, post_id: int | None,
                        model: str, prompt_version: str) -> int:
        cur = self.conn.execute(
            "INSERT INTO pain_points(niche_id, post_id, question_norm, quote, intensity, "
            "frequency, alternatives_json, buildability, model, prompt_version) "
            "VALUES (?,?,?,?,?,?,?,?,?,?)",
            (niche_id, post_id, pp.question_norm, pp.quote, pp.intensity, pp.frequency,
             json.dumps(pp.alternatives), pp.buildability, model, prompt_version),
        )
        return int(cur.lastrowid)

    def pain_for(self, niche_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT * FROM pain_points WHERE niche_id=? ORDER BY intensity DESC", (niche_id,)
        ).fetchall()

    # ---- step 4: serp ------------------------------------------------------
    def save_serp_result(self, niche_id: int, keyword_id: int | None, r: SerpResult,
                         raw_resp_id: int | None = None) -> None:
        self.conn.execute(
            "INSERT INTO serp_results(niche_id, keyword_id, position, url, domain, dr, "
            "is_forum, is_thin, raw_resp_id) VALUES (?,?,?,?,?,?,?,?,?)",
            (niche_id, keyword_id, r.position, r.url, r.domain, r.dr,
             int(r.is_forum), int(r.is_thin), raw_resp_id),
        )

    def serp_for(self, niche_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT * FROM serp_results WHERE niche_id=?", (niche_id,)
        ).fetchall()

    # ---- step 5: money questions + grades ---------------------------------
    def add_money_question(self, niche_id: int, question: str,
                           source: str = "generated") -> int:
        cur = self.conn.execute(
            "INSERT INTO money_questions(niche_id, question, source) VALUES (?,?,?)",
            (niche_id, question, source),
        )
        return int(cur.lastrowid)

    def save_aieo_grade(self, mq_id: int, g: AieoGrade, model: str, prompt_version: str,
                        raw_resp_id: int | None = None) -> None:
        self.conn.execute(
            "INSERT INTO aieo_grades(mq_id, provider, depth, specificity, recency, citation, "
            "weakness, notes, model, prompt_version, raw_resp_id) "
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
            (mq_id, g.provider, g.depth, g.specificity, g.recency, g.citation,
             g.weakness, g.notes, model, prompt_version, raw_resp_id),
        )

    def money_questions_for(self, niche_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT * FROM money_questions WHERE niche_id=?", (niche_id,)
        ).fetchall()

    def grades_for(self, niche_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT g.* FROM aieo_grades g JOIN money_questions q ON q.mq_id=g.mq_id "
            "WHERE q.niche_id=?",
            (niche_id,),
        ).fetchall()

    # ---- step 6: monetisation ---------------------------------------------
    def save_monetisation(self, niche_id: int, m: Monetisation, model: str,
                          prompt_version: str) -> None:
        self.conn.execute(
            "INSERT INTO monetisation(niche_id, primary_path, paths_json, confidence, "
            "affiliate_programs_json, rationale, model, prompt_version) "
            "VALUES (?,?,?,?,?,?,?,?) "
            "ON CONFLICT(niche_id) DO UPDATE SET primary_path=excluded.primary_path, "
            "paths_json=excluded.paths_json, confidence=excluded.confidence, "
            "affiliate_programs_json=excluded.affiliate_programs_json, "
            "rationale=excluded.rationale, model=excluded.model, "
            "prompt_version=excluded.prompt_version",
            (niche_id, m.primary_path, json.dumps(m.paths), m.confidence,
             json.dumps(m.affiliate_programs), m.rationale, model, prompt_version),
        )

    def monetisation_for(self, niche_id: int) -> sqlite3.Row | None:
        return self.conn.execute(
            "SELECT * FROM monetisation WHERE niche_id=?", (niche_id,)
        ).fetchone()

    # ---- step 7: trends ----------------------------------------------------
    def save_trend(self, niche_id: int, t: Trend, raw_resp_id: int | None = None) -> None:
        self.conn.execute(
            "INSERT INTO trends(niche_id, slope, direction, series_json, fetched_at, raw_resp_id) "
            "VALUES (?,?,?,?,?,?) "
            "ON CONFLICT(niche_id) DO UPDATE SET slope=excluded.slope, "
            "direction=excluded.direction, series_json=excluded.series_json, "
            "fetched_at=excluded.fetched_at, raw_resp_id=excluded.raw_resp_id",
            (niche_id, t.slope, t.direction, json.dumps(t.series), _now(), raw_resp_id),
        )

    def trend_for(self, niche_id: int) -> sqlite3.Row | None:
        return self.conn.execute(
            "SELECT * FROM trends WHERE niche_id=?", (niche_id,)
        ).fetchone()

    # ---- scored layer ------------------------------------------------------
    def save_metric_score(self, niche_id: int, run_id: int, metric: str,
                          raw_value: float, normalized: float) -> None:
        self.conn.execute(
            "INSERT INTO metric_scores(niche_id, run_id, metric, raw_value, normalized_0_10) "
            "VALUES (?,?,?,?,?) "
            "ON CONFLICT(niche_id, run_id, metric) DO UPDATE SET raw_value=excluded.raw_value, "
            "normalized_0_10=excluded.normalized_0_10",
            (niche_id, run_id, metric, raw_value, normalized),
        )

    def save_score(self, niche_id: int, run_id: int, composite: float, rank: int,
                   confidence: float, weights: dict) -> None:
        self.conn.execute(
            "INSERT INTO scores(niche_id, run_id, composite, rank, confidence, weights_json) "
            "VALUES (?,?,?,?,?,?) "
            "ON CONFLICT(niche_id, run_id) DO UPDATE SET composite=excluded.composite, "
            "rank=excluded.rank, confidence=excluded.confidence, weights_json=excluded.weights_json",
            (niche_id, run_id, composite, rank, confidence, json.dumps(weights)),
        )

    def latest_score_run(self) -> int | None:
        row = self.conn.execute(
            "SELECT MAX(run_id) AS r FROM scores"
        ).fetchone()
        return int(row["r"]) if row and row["r"] is not None else None

    def scores_for_run(self, run_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT s.*, n.seed_term, n.notes FROM scores s "
            "JOIN niches n ON n.niche_id=s.niche_id "
            "WHERE s.run_id=? ORDER BY s.rank",
            (run_id,),
        ).fetchall()

    def metric_scores_for_run(self, run_id: int) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT * FROM metric_scores WHERE run_id=?", (run_id,)
        ).fetchall()
