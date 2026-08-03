create table matches
(
    match_id text             not null,
    patch    double precision not null,
    server   text             not null,
    CONSTRAINT pk_matches PRIMARY KEY (match_id)
);

create table participants
(
    id       UUID    not null,
    match_id text    not null,
    champion text    not null,
    position text    not null,
    team_id  int     not null,
    win      boolean not null,
    CONSTRAINT pk_participants PRIMARY KEY (id),
    CONSTRAINT fk_participants_to_matches FOREIGN KEY (match_id) REFERENCES matches (match_id)
);

create table bans
(
    id       UUID not null,
    match_id text not null,
    champion text,
    team_id  int  not null,
    CONSTRAINT pk_bans PRIMARY KEY (id),
    CONSTRAINT fk_bans_to_matches FOREIGN KEY (match_id) REFERENCES matches (match_id)
);