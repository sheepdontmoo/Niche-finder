\set ON_ERROR_STOP on
\set QUIET on
\pset tuples_only on
\pset format unaligned

insert into auth.users (id, email) values
  ('11111111-1111-1111-1111-111111111111','alice@example.com'),
  ('22222222-2222-2222-2222-222222222222','bob@example.com');

select id as alice_ws from workspaces w where exists (select 1 from workspace_members m
  where m.workspace_id=w.id and m.user_id='11111111-1111-1111-1111-111111111111') \gset

set role authenticated;
set request.jwt.claim.sub = '11111111-1111-1111-1111-111111111111';
insert into businesses (workspace_id, name, model) values (:'alice_ws','Acme Roofing','Services');
select id as alice_biz from businesses where name='Acme Roofing' \gset
insert into snapshots (business_id, revenue, cac, ltgp) values (:'alice_biz', 1800000, 1400, 5200);
reset role;

-- Stash ids as GUCs so DO blocks can read them (psql does not interpolate
-- :vars inside dollar-quoted bodies).
set acq.alice_ws = :'alice_ws';
set acq.alice_biz = :'alice_biz';

-- NB: this check must run as `authenticated`, not as the superuser. Postgres
-- bypasses RLS for superusers and table owners by design, so running it with
-- `reset role` in effect reports every workspace and looks like a policy hole.
set role authenticated;
set request.jwt.claim.sub = '11111111-1111-1111-1111-111111111111';
select (case when count(*)=1 then 'PASS' else 'FAIL' end)||' | alice sees exactly 1 workspace (her own)' from workspaces;

set request.jwt.claim.sub = '22222222-2222-2222-2222-222222222222';

select (case when count(*)=0 then 'PASS' else 'FAIL' end)||' | bob CANNOT see alice''s business' from businesses where workspace_id=current_setting('acq.alice_ws')::uuid;
select (case when count(*)=0 then 'PASS' else 'FAIL' end)||' | bob CANNOT see alice''s snapshots' from snapshots where business_id=current_setting('acq.alice_biz')::uuid;
select (case when count(*)=0 then 'PASS' else 'FAIL' end)||' | bob CANNOT enumerate alice''s membership rows' from workspace_members where workspace_id=current_setting('acq.alice_ws')::uuid;

do $$ begin
  insert into public.businesses (workspace_id, name)
    values (current_setting('acq.alice_ws')::uuid, 'trespass');
  raise notice 'FAIL | bob INSERTED a business into alice''s workspace';
exception when insufficient_privilege then
  raise notice 'PASS | bob CANNOT insert into alice''s workspace';
end $$;

do $$ begin
  insert into public.snapshots (business_id, revenue)
    values (current_setting('acq.alice_biz')::uuid, 999);
  raise notice 'FAIL | bob INSERTED a snapshot onto alice''s business';
exception when insufficient_privilege then
  raise notice 'PASS | bob CANNOT insert a snapshot onto alice''s business';
end $$;

do $$ begin
  update public.snapshots set revenue = 0 where business_id = current_setting('acq.alice_biz')::uuid;
  if found then raise notice 'FAIL | bob UPDATED alice''s snapshot';
  else raise notice 'PASS | bob''s update of alice''s snapshot matched no rows'; end if;
end $$;

do $$ begin
  delete from public.businesses where id = current_setting('acq.alice_biz')::uuid;
  if found then raise notice 'FAIL | bob DELETED alice''s business';
  else raise notice 'PASS | bob''s delete of alice''s business matched no rows'; end if;
end $$;

do $$ begin
  insert into public.funnel_benchmarks (model,cv_optin,cv_booked,cv_showed,cv_closed)
    values ('Other',1,1,1,1);
  raise notice 'FAIL | client WROTE to reference benchmarks';
exception when insufficient_privilege then
  raise notice 'PASS | client CANNOT write reference benchmarks';
  when unique_violation then
  raise notice 'FAIL | client reached reference benchmarks (blocked only by PK)';
end $$;

-- Alice adds Bob as a viewer.
reset role;
insert into workspace_members (workspace_id, user_id, role)
  values (current_setting('acq.alice_ws')::uuid,'22222222-2222-2222-2222-222222222222','viewer');
set role authenticated;
set request.jwt.claim.sub = '22222222-2222-2222-2222-222222222222';

select (case when count(*)=1 then 'PASS' else 'FAIL' end)||' | viewer bob CAN now read alice''s business' from businesses where workspace_id=current_setting('acq.alice_ws')::uuid;

do $$ begin
  insert into public.businesses (workspace_id, name)
    values (current_setting('acq.alice_ws')::uuid,'viewer write');
  raise notice 'FAIL | viewer WROTE a business';
exception when insufficient_privilege then
  raise notice 'PASS | viewer CANNOT write a business (read-only enforced)';
end $$;

do $$ begin
  insert into public.workspace_invites (workspace_id, email)
    values (current_setting('acq.alice_ws')::uuid,'x@y.com');
  raise notice 'FAIL | non-owner CREATED an invite';
exception when insufficient_privilege then
  raise notice 'PASS | non-owner CANNOT create an invite';
end $$;

do $$ begin
  update public.workspace_members set role='owner'
    where workspace_id=current_setting('acq.alice_ws')::uuid
      and user_id='22222222-2222-2222-2222-222222222222';
  if found then raise notice 'FAIL | viewer ESCALATED themselves to owner';
  else raise notice 'PASS | viewer CANNOT escalate their own role'; end if;
end $$;
reset role;
