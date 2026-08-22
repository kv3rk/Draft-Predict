package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.Entity.ParticipantsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PickRateRequests extends JpaRepository<ParticipantsEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches as (
                    select
                    	COUNT(*) as total
                    from
                    	matches as m
                    where m.patch like :patch
                    )
                    select
                    	p.champion as champion,
                    	COUNT(*) * 100.0 / tm.total as pick_rate,
                    	AVG(case when p.win then 1.0 else 0.0 end) * 100 as win_rate
                    from
                    	participants p
                    join matches m on m.match_id = p.match_id
                    cross join total_matches tm
                    where m.patch like :patch
                    group by
                    	champion,
                    	tm.total
                    order by
                    	pick_rate desc
                    limit 15;
                    """
    )
    List<TopPerformingChampions> getTopPerformingChampionsByPickRateEarlyDraftPhase(String patch);

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches as (
                    select
                    	COUNT(*) as total
                    from
                    	matches as m
                    where m.patch like :patch
                    )
                    select
                    	p.champion as champion,
                    	COUNT(*) * 100.0 / tm.total as pick_rate,
                    	AVG(case when p.win then 1.0 else 0.0 end) * 100 as win_rate
                    from
                    	participants p
                    join matches m on m.match_id = p.match_id
                    cross join total_matches tm
                    where m.patch like :patch
                    group by
                    	champion,
                    	tm.total
                    order by
                    	pick_rate desc
                    limit 30;
                    """
    )
    List<TopPerformingChampions> getTopPerformingChampionsByPickRateLateDraftPhase(String patch);
}
