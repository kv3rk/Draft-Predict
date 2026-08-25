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
                    select
                        f.champion,
                        case when (f.total_t * 100.0 / f.total_matches) > a.avg_top then round(f.total_t * 100.0 / f.total_matches, 1) end as top,
                        case when (f.total_j * 100.0 / f.total_matches) > a.avg_jungle then round(f.total_j * 100.0 / f.total_matches, 1) end as jungle,
                        case when (f.total_m * 100.0 / f.total_matches) > a.avg_middle then round(f.total_m * 100.0 / f.total_matches, 1) end as middle,
                        case when (f.total_b * 100.0 / f.total_matches) > a.avg_bottom then round(f.total_b * 100.0 / f.total_matches, 1) end as bottom,
                        case when (f.total_u * 100.0 / f.total_matches) > a.avg_utility then round(f.total_u * 100.0 / f.total_matches, 1) end as utility
                    from flex_agg_16 f
                    cross join flex_avg_16 a
                    where f.champion = :name;
                    """
    )
    ChampionFlexibility getChampionFlexibility(
            @Param("name") String name
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
                    limit 10;
                    """
    )
    List<ChampionPresence> getChampionDraftPresence(
            @Param("patch") String patch
    );

}
