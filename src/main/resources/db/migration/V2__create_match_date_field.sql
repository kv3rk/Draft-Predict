alter table matches
    add column match_date date;

update matches
    set match_date = '2026-08-04';

alter table matches
    alter column match_date set not null;