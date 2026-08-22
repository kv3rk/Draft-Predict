package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.ChampionPresence;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DraftPresenceRequests extends JpaRepository<MatchesEntity, String> {

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
                    limit 15;
                    """
    )
    List<ChampionPresence> getChampionDraftPresenceEarlyDraftPhase(
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
    List<ChampionPresence> getChampionDraftPresenceLateDraftPhase(
            @Param("patch") String patch
    );
}
