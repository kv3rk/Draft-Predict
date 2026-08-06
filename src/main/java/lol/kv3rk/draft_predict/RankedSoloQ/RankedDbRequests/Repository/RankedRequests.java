package lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.Repository;

import lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.DTO.BestDuo;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.DTO.BestTrio;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Matches.Entity.MatchesEntity;
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
                    	matches
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
                    cross join total_matches tm
                    where
                    	p.position = :role1
                    	and ap.position = :role2
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
            @Param("role2") String role2
    );

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches as (
                    select
                    	COUNT(*) as total
                    from
                    	matches
                    ),
                    trio_stats as (
                    select
                    	p1.champion as champion_1,
                    	p2.champion as champion_2,
                    	p3.champion as champion_3,
                    	ROUND(
                                COUNT(*) * 100.0 / tm.total,
                                2
                            ) as pick_rate,
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
                    cross join total_matches tm
                    where
                    	p1.position = :role1
                    	and p2.position = :role2
                    	and p3.position = :role3
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
            @Param("role3") String role3
    );
}
