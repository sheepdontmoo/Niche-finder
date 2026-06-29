-- Niche Finder SQLite schema — single source of truth.
--
-- Three layers:
--   1. Run / input    : runs, niches
--   2. Raw cache       : raw_responses (every external call, by request hash)
--   3. Intermediate    : keywords, clusters, keyword_metrics, reddit_posts,
--                        pain_points, serp_results, money_questions,
--                        aieo_grades, monetisation, trends
--   4. Scored          : metric_scores, scores (re-writable; never re-scrapes)
--
-- `niches` is the hub; everything fans out by niche_id. The scored layer is
-- keyed by (niche_id, run_id) so re-scoring writes a fresh run without
-- touching steps 1-7.

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------- run + input
CREATE TABLE IF NOT EXISTS runs (
    run_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    started_at   TEXT NOT NULL,
    finished_at  TEXT,
    mode         TEXT NOT NULL,            -- live | record | fixture | auto
    config_json  TEXT NOT NULL,            -- snapshot of settings + effective per-source modes
    weights_json TEXT,                     -- snapshot of weights used (for the run's own scoring)
    kind         TEXT NOT NULL DEFAULT 'pipeline'  -- pipeline | score
);

CREATE TABLE IF NOT EXISTS niches (
    niche_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    seed_term   TEXT NOT NULL UNIQUE,
    notes       TEXT,
    created_at  TEXT NOT NULL
);

-- ---------------------------------------------------------------- raw cache
CREATE TABLE IF NOT EXISTS raw_responses (
    resp_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    source        TEXT NOT NULL,           -- ahrefs | reddit | autocomplete | anthropic | trends | perplexity
    request_hash  TEXT NOT NULL UNIQUE,    -- sha256(source, endpoint, normalised params)
    request_json  TEXT NOT NULL,
    response_json TEXT,
    status        TEXT NOT NULL,           -- ok | error | synthetic
    fetched_at    TEXT NOT NULL,
    run_id        INTEGER REFERENCES runs(run_id)
);
CREATE INDEX IF NOT EXISTS idx_raw_source ON raw_responses(source);

-- ---------------------------------------------------------------- step 1
CREATE TABLE IF NOT EXISTS clusters (
    cluster_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    niche_id     INTEGER NOT NULL REFERENCES niches(niche_id) ON DELETE CASCADE,
    label        TEXT NOT NULL,
    member_count INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_clusters_niche ON clusters(niche_id);

CREATE TABLE IF NOT EXISTS keywords (
    keyword_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    niche_id          INTEGER NOT NULL REFERENCES niches(niche_id) ON DELETE CASCADE,
    phrase            TEXT NOT NULL,
    depth             INTEGER NOT NULL DEFAULT 0,
    source            TEXT NOT NULL DEFAULT 'autocomplete',
    cluster_id        INTEGER REFERENCES clusters(cluster_id),
    is_buyer_modifier INTEGER NOT NULL DEFAULT 0,  -- contains best/review/vs/buy/price...
    UNIQUE(niche_id, phrase)
);
CREATE INDEX IF NOT EXISTS idx_keywords_niche ON keywords(niche_id);
CREATE INDEX IF NOT EXISTS idx_keywords_cluster ON keywords(cluster_id);

-- ---------------------------------------------------------------- step 2
CREATE TABLE IF NOT EXISTS keyword_metrics (
    keyword_id         INTEGER PRIMARY KEY REFERENCES keywords(keyword_id) ON DELETE CASCADE,
    volume             INTEGER,
    kd                 REAL,
    cpc                REAL,
    traffic_value      REAL,
    serp_features_json TEXT,
    has_forum_on_p1    INTEGER NOT NULL DEFAULT 0,
    fetched_at         TEXT
);

-- ---------------------------------------------------------------- step 3
CREATE TABLE IF NOT EXISTS reddit_posts (
    post_id              INTEGER PRIMARY KEY AUTOINCREMENT,
    niche_id             INTEGER NOT NULL REFERENCES niches(niche_id) ON DELETE CASCADE,
    subreddit            TEXT,
    permalink            TEXT,
    title                TEXT,
    body                 TEXT,
    score                INTEGER,
    created_utc          REAL,
    matched_signals_json TEXT,
    raw_resp_id          INTEGER REFERENCES raw_responses(resp_id)
);
CREATE INDEX IF NOT EXISTS idx_reddit_niche ON reddit_posts(niche_id);

CREATE TABLE IF NOT EXISTS pain_points (
    pain_id           INTEGER PRIMARY KEY AUTOINCREMENT,
    niche_id          INTEGER NOT NULL REFERENCES niches(niche_id) ON DELETE CASCADE,
    post_id           INTEGER REFERENCES reddit_posts(post_id),
    question_norm     TEXT NOT NULL,        -- normalised recurring question / complaint
    quote             TEXT,                 -- representative phrasing in the user's words
    intensity         REAL,                 -- 0-10
    frequency         INTEGER,              -- recurrence count
    alternatives_json TEXT,                 -- what people use now + why it falls short
    buildability      REAL,                 -- 0-10
    model             TEXT,
    prompt_version    TEXT
);
CREATE INDEX IF NOT EXISTS idx_pain_niche ON pain_points(niche_id);

-- ---------------------------------------------------------------- step 4
CREATE TABLE IF NOT EXISTS serp_results (
    serp_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    niche_id    INTEGER NOT NULL REFERENCES niches(niche_id) ON DELETE CASCADE,
    keyword_id  INTEGER REFERENCES keywords(keyword_id) ON DELETE CASCADE,
    position    INTEGER,
    url         TEXT,
    domain      TEXT,
    dr          REAL,                        -- domain rating
    is_forum    INTEGER NOT NULL DEFAULT 0,
    is_thin     INTEGER NOT NULL DEFAULT 0,
    raw_resp_id INTEGER REFERENCES raw_responses(resp_id)
);
CREATE INDEX IF NOT EXISTS idx_serp_niche ON serp_results(niche_id);

-- ---------------------------------------------------------------- step 5
CREATE TABLE IF NOT EXISTS money_questions (
    mq_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    niche_id  INTEGER NOT NULL REFERENCES niches(niche_id) ON DELETE CASCADE,
    question  TEXT NOT NULL,
    source    TEXT NOT NULL DEFAULT 'generated'
);
CREATE INDEX IF NOT EXISTS idx_mq_niche ON money_questions(niche_id);

CREATE TABLE IF NOT EXISTS aieo_grades (
    grade_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    mq_id          INTEGER NOT NULL REFERENCES money_questions(mq_id) ON DELETE CASCADE,
    provider       TEXT NOT NULL,           -- anthropic | perplexity
    depth          REAL,                    -- 0-10
    specificity    REAL,                    -- 0-10
    recency        REAL,                    -- 0-10
    citation       REAL,                    -- 0-10
    weakness       REAL,                    -- 0-10, higher = worse current answer = bigger gap
    notes          TEXT,
    model          TEXT,
    prompt_version TEXT,
    raw_resp_id    INTEGER REFERENCES raw_responses(resp_id)
);
CREATE INDEX IF NOT EXISTS idx_grade_mq ON aieo_grades(mq_id);

-- ---------------------------------------------------------------- step 6
CREATE TABLE IF NOT EXISTS monetisation (
    niche_id                INTEGER PRIMARY KEY REFERENCES niches(niche_id) ON DELETE CASCADE,
    primary_path            TEXT,            -- affiliate | own-product | lead-gen | display
    paths_json              TEXT,
    confidence              REAL,            -- 0-1
    affiliate_programs_json TEXT,
    rationale               TEXT,
    model                   TEXT,
    prompt_version          TEXT
);

-- ---------------------------------------------------------------- step 7
CREATE TABLE IF NOT EXISTS trends (
    niche_id    INTEGER PRIMARY KEY REFERENCES niches(niche_id) ON DELETE CASCADE,
    slope       REAL,                        -- normalised slope over the window
    direction   TEXT,                        -- growing | stable | declining
    series_json TEXT,
    fetched_at  TEXT,
    raw_resp_id INTEGER REFERENCES raw_responses(resp_id)
);

-- ---------------------------------------------------------------- scored layer
CREATE TABLE IF NOT EXISTS metric_scores (
    niche_id       INTEGER NOT NULL REFERENCES niches(niche_id) ON DELETE CASCADE,
    run_id         INTEGER NOT NULL REFERENCES runs(run_id) ON DELETE CASCADE,
    metric         TEXT NOT NULL,            -- commercial_intent | monetisation_fit | ...
    raw_value      REAL,
    normalized_0_10 REAL,
    PRIMARY KEY (niche_id, run_id, metric)
);

CREATE TABLE IF NOT EXISTS scores (
    niche_id     INTEGER NOT NULL REFERENCES niches(niche_id) ON DELETE CASCADE,
    run_id       INTEGER NOT NULL REFERENCES runs(run_id) ON DELETE CASCADE,
    composite    REAL NOT NULL,
    rank         INTEGER,
    confidence   REAL,                        -- 0-1 data-coverage flag
    weights_json TEXT NOT NULL,
    PRIMARY KEY (niche_id, run_id)
);
