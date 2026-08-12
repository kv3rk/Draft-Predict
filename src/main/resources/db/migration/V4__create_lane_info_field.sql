alter table participants
    add column xp int,
    add column farm int,
    add column gold int;

update participants
set xp = 1,
    farm = 1,
    gold = 1;

alter table participants
    alter column xp set not null,
    alter column farm set not null,
    alter column gold set not null;