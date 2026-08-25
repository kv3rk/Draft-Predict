package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.BestTrio;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Participants.Entity.ParticipantsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BestTrioRequests extends JpaRepository<ParticipantsEntity, UUID> {

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
                        p1.champion = :champion1
                        and p2.champion = :champion2
                        and p1.champion <> p3.champion
                        and p2.champion <> p3.champion
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
                    	win_rate desc;
                    """
    )
    List<BestTrio> getBestTrioChampionsNoRole(
            @Param("patch") String patch,
            @Param("champion1") String champion1,
            @Param("champion2") String champion2
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
                    limit 10;
                    """
    )
    List<BestTrio> getTop10TrioChampions(
            @Param("role1") String role1,
            @Param("role2") String role2,
            @Param("role3") String role3,
            @Param("patch") String patch,
            @Param("champion1") String champion1,
            @Param("champion2") String champion2
    );
}
