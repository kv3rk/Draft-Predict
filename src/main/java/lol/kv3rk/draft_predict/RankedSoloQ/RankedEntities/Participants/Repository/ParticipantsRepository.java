package lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Participants.Repository;

import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Participants.Entity.ParticipantsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantsRepository extends JpaRepository<ParticipantsEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches as (
                    select
                    	COUNT(*) as total
                    from
                    	matches
                    ), last_match as(
                                         	select
                                         		match_date
                                         	from matches m
                                         	group by m.match_date
                                         	order by m.match_date desc
                                         	limit 1
                                         ),
                                         actual_patch as(
                                         	select
                                         	patch
                                         from
                                         	matches m
                                         cross join last_match lm
                                         where
                                         	m.match_date = lm.match_date
                                         group by
                                         	m.patch
                                         limit 1
                                         )
                    select
                    	p.champion as champion,
                    	COUNT(*) * 100.0 / tm.total as pick_rate,
                    	AVG(case when p.win then 1.0 else 0.0 end) * 100 as win_rate
                    from
                    	participants p
                    join matches m on m.match_id = p.match_id
                    cross join total_matches tm
                    cross join actual_patch ap
                    where ap.patch = m.patch
                    group by
                    	champion,
                    	tm.total
                    order by
                    	pick_rate desc
                    limit 5;
                    """
    )
    List<TopPerformingChampions> getTopPerformingChampionsByPickRate();

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches as (
                    select
                    	COUNT(*) as total
                    from
                    	matches
                    ), actual_patch as (
                    	select
                    		MAX(m.patch) as patch
                    	from
                    		matches m
                    )
                    select
                    	p.champion as champion,
                    	COUNT(*) * 100.0 / tm.total as pick_rate,
                    	AVG(case when p.win then 1.0 else 0.0 end) * 100 as win_rate
                    from
                    	participants p
                    join matches m on m.match_id = p.match_id
                    cross join total_matches tm
                    cross join actual_patch ap
                    where ap.patch = m.patch
                    group by
                    	champion,
                    	tm.total
                    order by
                    	win_rate desc
                    limit 5;
                    """
    )
    List<TopPerformingChampions> getTopPerformingChampionsByWinRate();

}
