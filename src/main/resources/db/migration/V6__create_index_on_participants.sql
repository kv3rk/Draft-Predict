create index if not exists idx_participants_champion on participants(champion);
create index if not exists idx_participants_position on participants(position);
create index if not exists idx_participants_match_id on participants(match_id);

create index if not exists idx_matches_patch on matches(patch);
