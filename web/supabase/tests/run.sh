#!/usr/bin/env bash
# RLS isolation suite. Needs a running Postgres and a superuser psql.
# Must be run as a NON-superuser client role — Postgres bypasses RLS for
# superusers and table owners, so a run as `postgres` proves nothing.
set -euo pipefail
DB=${DB:-acqtest}
RUN() { su postgres -c "psql -q -v ON_ERROR_STOP=1 -d $DB"; }
su postgres -c "dropdb --if-exists $DB; createdb $DB"
RUN < "$(dirname "$0")/local_auth_stub.sql"
RUN < "$(dirname "$0")/../migrations/0001_init.sql"
su postgres -c "psql -q -d $DB -c \"grant usage on schema public, auth to authenticated;
  grant select, insert, update, delete on all tables in schema public to authenticated;
  grant execute on all functions in schema public, auth to authenticated;\""
out=$(su postgres -c "psql -d $DB -v ON_ERROR_STOP=1" < "$(dirname "$0")/rls_test.sql" 2>&1 | sed 's/^NOTICE:  //' | grep -E '^(PASS|FAIL)')
echo "$out"
fails=$(echo "$out" | grep -c '^FAIL' || true)
echo "---"; echo "PASS: $(echo "$out" | grep -c '^PASS')  FAIL: $fails"
[ "$fails" -eq 0 ]
