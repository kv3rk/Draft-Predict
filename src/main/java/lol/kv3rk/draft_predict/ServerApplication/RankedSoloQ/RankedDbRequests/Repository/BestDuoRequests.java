package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.BestDuo;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.Entity.ParticipantsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BestDuoRequests extends JpaRepository<ParticipantsEntity, UUID> {

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
                        m.patch like :patch
                        and p.champion = :champion
                        and p.champion <> ap.champion
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
                        and win_rate > 50.0
                    order by
                    	win_rate desc,
                    	pick_rate desc;
                    """
    )
    List<BestDuo> getBestDuoChampionsWithoutRoleConstraint(
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
                    duo_stats as (
                    select
                    	p.champion as champion_1,
                    	ap.champion as champion_2,
                        COUNT(*) as pick_rate,
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
                    limit 10;
                    """
    )
    List<BestDuo> getTop10DuoChampions(
            @Param("role1") String role1,
            @Param("role2") String role2,
            @Param("patch") String patch,
            @Param("champion") String champion
    );
}
