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
                    limit 5;
                    """
    )
    List<BestDuo> getBestDuoChampions(
            @Param("role1") String role1,
            @Param("role2") String role2,
            @Param("patch") String patch
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
                    limit 5;
                    """
    )
    List<BestTrio> getBestTrioChampions(
            @Param("role1") String role1,
            @Param("role2") String role2,
            @Param("role3") String role3,
            @Param("patch") String patch
    );


    @Query(
            nativeQuery = true,
            value = """
                with top_flex as(
                    select
                        p.champion,
                        count(p.champion) as total_t
                    from
                        participants p
                    join matches m on m.match_id = p.match_id
                    where
                        p.position = 'TOP'
                        and m.patch like :patch
                    group by
                        p.champion
                ),
                jungle_flex as(
                    select
                        p.champion,
                        count(p.champion) as total_j
                    from
                        participants p
                    join matches m on m.match_id = p.match_id
                    where
                        p.position = 'JUNGLE'
                        and m.patch like :patch
                    group by
                        p.champion
                ),
                middle_flex as(
                    select
                        p.champion,
                        count(p.champion) as total_m
                    from
                        participants p
                    join matches m on m.match_id = p.match_id
                    where
                        p.position = 'MIDDLE'
                        and m.patch like :patch
                    group by
                        p.champion
                ),
                bottom_flex as(
                    select
                        p.champion,
                        count(p.champion) as total_b
                    from
                        participants p
                    join matches m on m.match_id = p.match_id
                    where
                        p.position = 'BOTTOM'
                        and m.patch like :patch
                    group by
                        p.champion
                ),
                utility_flex as(
                    select
                        p.champion,
                        count(p.champion) as total_u
                    from
                        participants p
                    join matches m on m.match_id = p.match_id
                    where
                        p.position = 'UTILITY'
                        and m.patch like :patch
                    group by
                        p.champion
                ),
                total_matches as (
                    select
                        p.champion,
                        count(p.champion) as total
                    from
                        participants p
                    join matches m on m.match_id = p.match_id
                    where
                        m.patch like :patch
                    group by
                        p.champion
                ),
                result_table as (
                    select
                        tm.champion,
                        case
                            when (tf.total_t * 100.0 / tm.total) > 0 then round(tf.total_t * 100.0 / tm.total, 1)
                            else 0
                        end as flexibility_top,
                        case
                            when (jf.total_j * 100.0 / tm.total) > 0 then round(jf.total_j * 100.0 / tm.total, 1)
                            else 0
                        end as flexibility_jungle,
                        case
                            when (mf.total_m * 100.0 / tm.total) > 0 then round(mf.total_m * 100.0 / tm.total, 1)
                            else 0
                        end as flexibility_middle,
                        case
                            when (bf.total_b * 100.0 / tm.total) > 0 then round(bf.total_b * 100.0 / tm.total, 1)
                            else 0
                        end as flexibility_bottom,
                        case
                            when (uf.total_u * 100.0 / tm.total) > 0 then round(uf.total_u * 100.0 / tm.total, 1)
                            else 0
                        end as flexibility_utility
                    from
                        total_matches tm
                    left join top_flex tf on tf.champion = tm.champion
                    left join jungle_flex jf on jf.champion = tm.champion
                    left join middle_flex mf on mf.champion = tm.champion
                    left join bottom_flex bf on bf.champion = tm.champion
                    left join utility_flex uf on uf.champion = tm.champion
                ),
                avg_flexibility as (
                    select
                        round(avg(rt.flexibility_top), 1) avg_top,
                        round(avg(rt.flexibility_jungle), 1) avg_jungle,
                        round(avg(rt.flexibility_middle), 1) avg_middle,
                        round(avg(rt.flexibility_bottom), 1) avg_bottom,
                        round(avg(rt.flexibility_utility), 1) avg_utility
                    from
                        result_table rt
                )
                select
                    rt.champion as champion,
                    case
                        when rt.flexibility_top > avg_flex.avg_top then rt.flexibility_top
                    end as top,
                    case
                        when rt.flexibility_jungle > avg_flex.avg_jungle then rt.flexibility_jungle
                    end as jungle,
                    case
                        when rt.flexibility_middle > avg_flex.avg_middle then rt.flexibility_middle
                    end as middle,
                    case
                        when rt.flexibility_bottom > avg_flex.avg_bottom then rt.flexibility_bottom
                    end as bottom,
                    case
                        when rt.flexibility_utility > avg_flex.avg_utility then rt.flexibility_utility
                    end as utility
                from
                    result_table rt
                cross join avg_flexibility avg_flex
                where
                    champion = :name;
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
                where
                    champion = :name
                group by
                    champion,
                    tm.total_matches
                order by
                    presence desc
                limit 1;
                """
    )
    ChampionPresence getChampionDraftPresence(
            @Param("name") String name,
            @Param("patch") String patch
    );

    @Query(
            nativeQuery = true,
            value = """
                    select
                    	p.champion as champion1,
                    	p2.champion as champion2,
                    	round(avg(p.xp - p2.xp), 1) as xp,
                    	round(avg(p.farm - p2.farm), 1) as farm,
                    	round(avg(p.gold - p2.gold), 1) as gold
                    from
                    	participants p
                    join participants p2
                        on
                    	p.match_id = p2.match_id
                    	and p.position = p2.position
                    join matches m
                        on
                    	m.match_id = p.match_id
                    where
                    	p.xp > 1
                    	and p2.xp > 1
                    	and p.champion = :champion1
                    	and p2.champion = :champion2
                    	and p.position = :lane
                    	and m.patch like :patch
                    group by
                    	p.champion,
                    	p2.champion;
                    
                    """
    )
    CounterPick getCounterPicks(
            @Param("champion1") String champion1,
            @Param("champion2") String champion2,
            @Param("lane") String lane,
            @Param("patch") String patch
    );

}
