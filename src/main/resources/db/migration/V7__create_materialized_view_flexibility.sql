create materialized view flex_stats_16 as
select
    p.champion,
    p.position,
    count(*) as total
from participants p
         join matches m on m.match_id = p.match_id
where m.patch like '16.%'
group by p.champion, p.position;

create materialized view flex_agg_16 as
select
    champion,
    sum(case when position = 'TOP' then total end) as total_t,
    sum(case when position = 'JUNGLE' then total end) as total_j,
    sum(case when position = 'MIDDLE' then total end) as total_m,
    sum(case when position = 'BOTTOM' then total end) as total_b,
    sum(case when position = 'UTILITY' then total end) as total_u,
    sum(total) as total_matches
from flex_stats_16
group by champion;

create materialized view flex_avg_16 as
select
    round(avg(total_t * 100.0 / total_matches), 1) as avg_top,
    round(avg(total_j * 100.0 / total_matches), 1) as avg_jungle,
    round(avg(total_m * 100.0 / total_matches), 1) as avg_middle,
    round(avg(total_b * 100.0 / total_matches), 1) as avg_bottom,
    round(avg(total_u * 100.0 / total_matches), 1) as avg_utility
from flex_agg_16;

create index if not exists idx_flex_agg_16_champion on flex_agg_16(champion);
