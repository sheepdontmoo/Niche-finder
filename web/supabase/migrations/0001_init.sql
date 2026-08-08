-- ACQ Console — schema, tenancy and row-level security.
--
-- Security model: RLS is the ONLY security boundary. The client talks straight
-- to Postgres, so every restriction must be expressed as a policy here. UI-level
-- checks are convenience, never protection.
--
-- Roles: owner (full control incl. members), member (edit data), viewer (read-only).

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------- tables

create table if not exists public.workspaces (
  id          uuid primary key default gen_random_uuid(),
  name        text not null check (length(trim(name)) > 0),
  created_at  timestamptz not null default now()
);

create type public.member_role as enum ('owner', 'member', 'viewer');

create table if not exists public.workspace_members (
  workspace_id uuid not null references public.workspaces(id) on delete cascade,
  user_id      uuid not null references auth.users(id) on delete cascade,
  role         public.member_role not null default 'member',
  created_at   timestamptz not null default now(),
  primary key (workspace_id, user_id)
);

create table if not exists public.workspace_invites (
  id           uuid primary key default gen_random_uuid(),
  workspace_id uuid not null references public.workspaces(id) on delete cascade,
  email        text not null,
  role         public.member_role not null default 'member',
  token        uuid not null unique default gen_random_uuid(),
  status       text not null default 'pending' check (status in ('pending','accepted','revoked')),
  created_at   timestamptz not null default now(),
  unique (workspace_id, email)
);

create type public.business_model as enum
  ('Services','Product','SaaS','Ecommerce','Local','Info','Other');

create table if not exists public.businesses (
  id           uuid primary key default gen_random_uuid(),
  workspace_id uuid not null references public.workspaces(id) on delete cascade,
  name         text not null check (length(trim(name)) > 0),
  model        public.business_model not null default 'Services',
  created_at   timestamptz not null default now()
);
create index if not exists businesses_workspace_idx on public.businesses(workspace_id);

-- Every save writes a new row, so metrics are a time series rather than a
-- single mutable record. All metric columns are nullable: null means
-- "not tracked" and must never be read as zero.
create table if not exists public.snapshots (
  id                    uuid primary key default gen_random_uuid(),
  business_id           uuid not null references public.businesses(id) on delete cascade,
  taken_at              timestamptz not null default now(),
  created_by            uuid references auth.users(id) on delete set null,
  revenue               numeric,
  gross_profit          numeric,
  owner_hours           numeric,
  cac                   numeric,
  ltgp                  numeric,
  fecc                  numeric,
  payback_days          numeric,
  customers_per_month   numeric,
  cv_optin              numeric,
  cv_booked             numeric,
  cv_showed             numeric,
  cv_closed             numeric,
  leads_per_month       numeric,
  speed_to_lead_seconds numeric,
  page_load_seconds     numeric,
  closers_count         integer,
  channels              text[] not null default '{}',
  offer_types           text[] not null default '{}',
  has_rubric            boolean not null default false
);
create index if not exists snapshots_business_taken_idx
  on public.snapshots(business_id, taken_at desc);

-- Reference data. Editable by admins out-of-band, readable by all signed-in
-- users. Benchmarks live here rather than in code so a wrong benchmark — which
-- produces a confidently wrong verdict — is a data fix, not a deploy.
create table if not exists public.funnel_benchmarks (
  model     public.business_model primary key,
  cv_optin  numeric not null,
  cv_booked numeric not null,
  cv_showed numeric not null,
  cv_closed numeric not null
);

create table if not exists public.prescriptions (
  id         uuid primary key default gen_random_uuid(),
  rung       text not null check (rung in ('conversion','leads','sales','ltv','cash','people')),
  sort_order integer not null default 0,
  action     text not null,
  detail     text not null,
  watch      text not null
);

-- ---------------------------------------------------------------- helpers
--
-- SECURITY DEFINER so these bypass RLS. Without that, a policy on
-- workspace_members that queries workspace_members recurses infinitely.
-- search_path is pinned to defeat search-path hijacking.

create or replace function public.is_workspace_member(w uuid)
returns boolean language sql security definer stable set search_path = public, pg_temp as $$
  select exists (
    select 1 from public.workspace_members
    where workspace_id = w and user_id = auth.uid()
  );
$$;

create or replace function public.workspace_role(w uuid)
returns public.member_role language sql security definer stable set search_path = public, pg_temp as $$
  select role from public.workspace_members
  where workspace_id = w and user_id = auth.uid();
$$;

create or replace function public.can_write(w uuid)
returns boolean language sql security definer stable set search_path = public, pg_temp as $$
  select public.workspace_role(w) in ('owner','member');
$$;

create or replace function public.is_owner(w uuid)
returns boolean language sql security definer stable set search_path = public, pg_temp as $$
  select public.workspace_role(w) = 'owner';
$$;

create or replace function public.business_workspace(b uuid)
returns uuid language sql security definer stable set search_path = public, pg_temp as $$
  select workspace_id from public.businesses where id = b;
$$;

-- ---------------------------------------------------------------- RLS

alter table public.workspaces        enable row level security;
alter table public.workspace_members enable row level security;
alter table public.workspace_invites enable row level security;
alter table public.businesses        enable row level security;
alter table public.snapshots         enable row level security;
alter table public.funnel_benchmarks enable row level security;
alter table public.prescriptions     enable row level security;

-- workspaces
create policy workspaces_select on public.workspaces
  for select using (public.is_workspace_member(id));
create policy workspaces_insert on public.workspaces
  for insert with check (auth.uid() is not null);
create policy workspaces_update on public.workspaces
  for update using (public.is_owner(id)) with check (public.is_owner(id));
create policy workspaces_delete on public.workspaces
  for delete using (public.is_owner(id));

-- workspace_members
create policy members_select on public.workspace_members
  for select using (public.is_workspace_member(workspace_id));
-- A user may add THEMSELVES to a workspace only when no members exist yet
-- (bootstrapping a brand-new workspace); otherwise only an owner may add rows.
create policy members_insert on public.workspace_members
  for insert with check (
    public.is_owner(workspace_id)
    or (
      user_id = auth.uid()
      and not exists (select 1 from public.workspace_members m where m.workspace_id = workspace_id)
    )
  );
create policy members_update on public.workspace_members
  for update using (public.is_owner(workspace_id)) with check (public.is_owner(workspace_id));
-- Owners may remove anyone; anyone may remove themselves (leave).
create policy members_delete on public.workspace_members
  for delete using (public.is_owner(workspace_id) or user_id = auth.uid());

-- workspace_invites — owners manage; members can see who's pending.
create policy invites_select on public.workspace_invites
  for select using (public.is_workspace_member(workspace_id));
create policy invites_insert on public.workspace_invites
  for insert with check (public.is_owner(workspace_id));
create policy invites_update on public.workspace_invites
  for update using (public.is_owner(workspace_id)) with check (public.is_owner(workspace_id));
create policy invites_delete on public.workspace_invites
  for delete using (public.is_owner(workspace_id));

-- businesses — viewers read, members and owners write.
create policy businesses_select on public.businesses
  for select using (public.is_workspace_member(workspace_id));
create policy businesses_insert on public.businesses
  for insert with check (public.can_write(workspace_id));
create policy businesses_update on public.businesses
  for update using (public.can_write(workspace_id)) with check (public.can_write(workspace_id));
create policy businesses_delete on public.businesses
  for delete using (public.can_write(workspace_id));

-- snapshots — reached through the parent business's workspace.
create policy snapshots_select on public.snapshots
  for select using (public.is_workspace_member(public.business_workspace(business_id)));
create policy snapshots_insert on public.snapshots
  for insert with check (public.can_write(public.business_workspace(business_id)));
create policy snapshots_update on public.snapshots
  for update using (public.can_write(public.business_workspace(business_id)))
  with check (public.can_write(public.business_workspace(business_id)));
create policy snapshots_delete on public.snapshots
  for delete using (public.can_write(public.business_workspace(business_id)));

-- Reference data: readable by any signed-in user, writable by none.
-- No INSERT/UPDATE/DELETE policy exists, so writes are denied to every client
-- role. Seed and edit via a service-role connection or a migration.
create policy benchmarks_select on public.funnel_benchmarks
  for select using (auth.uid() is not null);
create policy prescriptions_select on public.prescriptions
  for select using (auth.uid() is not null);

-- ---------------------------------------------------------------- signup

-- New user gets their own workspace and owner membership. SECURITY DEFINER
-- because it runs before the user has any membership to authorise against.
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public, pg_temp as $$
declare
  ws_id uuid;
  invite record;
begin
  insert into public.workspaces (name)
  values (coalesce(nullif(split_part(new.email, '@', 1), ''), 'My workspace') || '''s workspace')
  returning id into ws_id;

  insert into public.workspace_members (workspace_id, user_id, role)
  values (ws_id, new.id, 'owner');

  -- Honour any pending invites addressed to this email.
  for invite in
    select * from public.workspace_invites
    where lower(email) = lower(new.email) and status = 'pending'
  loop
    insert into public.workspace_members (workspace_id, user_id, role)
    values (invite.workspace_id, new.id, invite.role)
    on conflict do nothing;
    update public.workspace_invites set status = 'accepted' where id = invite.id;
  end loop;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------- seed

insert into public.funnel_benchmarks (model, cv_optin, cv_booked, cv_showed, cv_closed) values
  ('Services',  30, 40, 70, 25),
  ('Local',     30, 45, 75, 30),
  ('Info',      35, 30, 60, 15),
  ('SaaS',      25, 30, 70, 20),
  ('Ecommerce', 20, 15, 90, 40),
  ('Product',   25, 30, 80, 30),
  ('Other',     30, 40, 70, 25)
on conflict (model) do nothing;
