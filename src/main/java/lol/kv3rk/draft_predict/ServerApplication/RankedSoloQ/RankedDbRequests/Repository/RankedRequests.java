package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.*;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankedRequests extends JpaRepository<MatchesEntity, String> {

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches as (
                    select
                    	COUNT(*) as total
                    from
                    	matches as m
                    where m.patch like :patch
                    ),
                    duo_stats as (
                    select
                    	p.champion as champion_1,
                    	ap.champion as champion_2,
                    	ROUND(
                                COUNT(*) * 100.0 / tm.total,
                                2
                            ) as pick_rate,
                    	ROUND(
                                AVG(case when p.win then 1 else 0 end) * 100,
                                1
                            ) as win_rate
                    from
                    	participants p
                    join participants ap
                          on
                    	p.match_id = ap.match_id
                    	and p.team_id = ap.team_id
                    join matches m
                        on
                        m.match_id = p.match_id
                    cross join total_matches tm
                    where
                    	p.position = :role1
                    	and ap.position = :role2
                        and m.patch like :patch
                        and p.champion = :champion
                    group by
                    	p.champion,
                    	ap.champion,
                    	tm.total
                    ),
                    avg_pick as (
                    select
                    	AVG(pick_rate) as avg_pick
                    from
                    	duo_stats
                    )
                    select
                    	champion_1 as champion1,
                    	champion_2 as champion2,
                    	pick_rate,
                    	win_rate
                    from
                    	duo_stats
                    cross join avg_pick
                    where
                    	pick_rate > avg_pick
                    order by
                    	pick_rate desc,
                    	win_rate desc
                    limit 20;
                    """
    )
    List<BestDuo> getBestDuoChampions(
            @Param("role1") String role1,
            @Param("role2") String role2,
            @Param("patch") String patch,
            @Param("champion") String champion
    );

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches as (
                    select
                    	COUNT(*) as total
                    from
                    	matches as m
                    where m.patch like :patch
                    ),
                    trio_stats as (
                    select
                    	p1.champion as champion_1,
                    	p2.champion as champion_2,
                    	p3.champion as champion_3,
                    	COUNT(*) as pick_rate,
                    	ROUND(
                                AVG(case when p1.win then 1 else 0 end) * 100,
                                1
                            ) as win_rate
                    from
                    	participants p1
                    join participants p2
                          on
                    	p1.match_id = p2.match_id
                    	and p1.team_id = p2.team_id
                    join participants p3
                    	on
                    	p3.match_id = p2.match_id
                    	and p3.team_id = p2.team_id
                    join matches m
                        on
                        p1.match_id = m.match_id
                    cross join total_matches tm
                    where
                    	p1.position = :role1
                    	and p2.position = :role2
                    	and p3.position = :role3
                        and m.patch like :patch
                        and p1.champion = :champion1
                        and p2.champion = :champion2
                    group by
                    	p1.champion,
                    	p2.champion,
                    	p3.champion,
                    	tm.total
                    ),
                    avg_pick as (
                    select
                    	AVG(pick_rate) as avg_pick
                    from
                    	trio_stats
                    )
                    select
                    	champion_1 as champion1,
                    	champion_2 as champion2,
                    	champion_3 as champion3,
                    	pick_rate,
                    	win_rate
                    from
                    	trio_stats
                    cross join avg_pick
                    where
                    	pick_rate > avg_pick
                    order by
                    	pick_rate desc,
                    	win_rate desc
                    limit 20;
                    """
    )
    List<BestTrio> getBestTrioChampions(
            @Param("role1") String role1,
            @Param("role2") String role2,
            @Param("role3") String role3,
            @Param("patch") String patch,
            @Param("champion1") String champion1,
            @Param("champion2") String champion2
    );


    @Query(
            nativeQuery = true,
            value = """
                    with grouped as (
                        select
                            p.champion,
                            p.position,
                            count(*) as total
                        from participants p
                        join matches m on m.match_id = p.match_id
                        where m.patch like :patch
                        group by p.champion, p.position
                    ),
                    flex as (
                        select
                            champion,
                            sum(case when position = 'TOP' then total end) as total_t,
                            sum(case when position = 'JUNGLE' then total end) as total_j,
                            sum(case when position = 'MIDDLE' then total end) as total_m,
                            sum(case when position = 'BOTTOM' then total end) as total_b,
                            sum(case when position = 'UTILITY' then total end) as total_u,
                            sum(total) as total_matches
                        from grouped
                        group by champion
                    ),
                    avg_flex as (
                        select
                            round(avg(total_t * 100.0 / total_matches), 1) as avg_top,
                            round(avg(total_j * 100.0 / total_matches), 1) as avg_jungle,
                            round(avg(total_m * 100.0 / total_matches), 1) as avg_middle,
                            round(avg(total_b * 100.0 / total_matches), 1) as avg_bottom,
                            round(avg(total_u * 100.0 / total_matches), 1) as avg_utility
                        from flex
                    )
                    select
                        f.champion,
                        case when (f.total_t * 100.0 / f.total_matches) > a.avg_top then round(f.total_t * 100.0 / f.total_matches, 1) end as top,
                        case when (f.total_j * 100.0 / f.total_matches) > a.avg_jungle then round(f.total_j * 100.0 / f.total_matches, 1) end as jungle,
                        case when (f.total_m * 100.0 / f.total_matches) > a.avg_middle then round(f.total_m * 100.0 / f.total_matches, 1) end as middle,
                        case when (f.total_b * 100.0 / f.total_matches) > a.avg_bottom then round(f.total_b * 100.0 / f.total_matches, 1) end as bottom,
                        case when (f.total_u * 100.0 / f.total_matches) > a.avg_utility then round(f.total_u * 100.0 / f.total_matches, 1) end as utility
                    from flex f
                    cross join avg_flex a
                    where f.champion = :name;
                    
                    """
    )
    ChampionFlexibility getChampionFlexibility(
            @Param("name") String name,
            @Param("patch") String patch
    );

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches_table as(
                        select
                            count(m.match_id) as total_matches
                        from matches m
                        where m.patch like :patch
                    ),
                    total_ban_list as(
                        select
                            b.champion,
                            b.match_id
                        from
                            bans b
                        join matches m on m.match_id = b.match_id
                        where
                            b.champion <> ''
                            and m.patch like :patch
                        group by
                            b.champion,
                            b.match_id
                    ),
                    total_pick_list as (
                        select
                            p.champion,
                            p.match_id
                        from
                            participants p
                        join matches m on m.match_id = p.match_id
                        where m.patch like :patch
                        group by
                            p.champion,
                            p.match_id
                    ),
                    total_amount as (
                        select
                            tpl.champion
                        from
                            total_pick_list tpl
                        union all
                        (
                            select
                                tbl.champion
                            from
                                total_ban_list tbl
                        )
                    )
                    select
                        ta.champion as champion,
                        round(count(ta.champion) * 100.0 / tm.total_matches, 2) as presence
                    from
                        total_amount ta
                    cross join total_matches_table tm
                    group by
                        champion,
                        tm.total_matches
                    order by
                        presence desc
                    limit 30;
                    """
    )
    List<ChampionPresence> getChampionDraftPresence(
            @Param("patch") String patch
    );

    @Query(
            nativeQuery = true,
            value = """
                    with total_position_matches as (
                    select
                    	p.champion,
                    	count(p.champion) as total
                    from
                    	participants p
                    join matches m on
                    	m.match_id = p.match_id
                    where
                    	m.patch like :patch
                    	and p.position = :lane
                    group by
                    	p.champion
                    ),
                    avg_position_matches as(
                    	select
                    	round(avg(tpm.total), 0) as total_avg
                    from
                    	total_position_matches tpm
                    )
                    select
                    	p.champion as champion1,
                    	p2.champion as champion2,
                    	round(avg(p.gold - p2.gold), 1) as gold,
                    	round(avg(p.xp - p2.xp), 1) as xp,
                    	round(avg(p.farm - p2.farm), 1) as farm
                    from
                    	participants p
                    join participants p2
                        on
                    	p.match_id = p2.match_id
                    	and p.position = p2.position
                    join matches m
                        on
                    	m.match_id = p.match_id
                    join total_position_matches tpm on
                    	tpm.champion = p2.champion
                    cross join avg_position_matches apm
                    where
                    	p.xp > 1
                    	and p2.xp > 1
                    	and p.champion = :champion1
                    	and p.position = :lane
                    	and m.patch like :patch
                    	and apm.total_avg < tpm.total
                    	and p.champion <> p2.champion
                    group by
                    	p.champion,
                    	p2.champion
                    having
                    	round(avg(p.gold - p2.gold), 1) >= 0
                    order by
                    	gold desc,
                    	xp desc,
                    	farm desc;
                    """
    )
    List<CounterPick> getCounterPicks(
            @Param("champion1") String champion1,
            @Param("lane") String lane,
            @Param("patch") String patch
    );

}
