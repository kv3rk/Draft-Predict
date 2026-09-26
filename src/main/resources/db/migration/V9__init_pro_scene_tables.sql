create table pro_match
(
    match_id   varchar(25) not null,
    league     varchar(10) not null,
    patch      varchar(10) not null,
    match_date date        not null,
    CONSTRAINT pk_pro_match PRIMARY KEY (match_id)
);

create table pro_team
(
    id         UUID        not null,
    match_id   varchar(25) not null,
    league     varchar(10) not null,
    team_name  varchar(50) not null,
    side       varchar(10) not null,
    first_pick boolean     not null,
    result     boolean     not null,
    constraint pk_pro_team primary key (id),
    constraint fk_pro_team_to_pro_match foreign key (match_id) references pro_match (match_id)
);

create table pro_ban
(
    id      UUID        not null,
    team_id UUID        not null,
    ban1    varchar(20) not null,
    ban2    varchar(20) not null,
    ban3    varchar(20) not null,
    ban4    varchar(20) not null,
    ban5    varchar(20) not null,
    constraint pk_pro_ban primary key (id),
    constraint fk_pro_ban_to_pro_team foreign key (team_id) references pro_team (id)
);

create table pro_pick
(
    id              UUID        not null,
    team_id         UUID        not null,
    champion        varchar(20) not null,
    position        varchar(10) not null,
    player_name     text        not null,
    player_id       text        not null,
    pick_order      int         not null,
    gold_diff_at_15 int         not null,
    xp_diff_at_15   int         not null,
    cs_diff_at_15   int         not null,
    constraint pk_pro_pick primary key (id),
    constraint fk_pro_pick_to_pro_team foreign key (team_id) references pro_team (id)
);

create table general_info
(
    parsed_lines int default 109009
);