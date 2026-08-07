package lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Matches.Repository;

import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MatchesRepository extends JpaRepository<MatchesEntity, String> {

    MatchesEntity findByMatchId(String matchId);

    @Query(
            nativeQuery = true,
            value = "select COUNT(m.match_id) from matches as m"
    )
    Optional<Long> countMatches();

    @Query(
            nativeQuery = true,
            value = """
                    with last_match as(
                    	select
                    		match_date
                    	from matches m
                    	group by m.match_date
                    	order by m.match_date desc
                    	limit 1
                    )
                    select
                    	patch
                    from
                    	matches m
                    cross join last_match lm
                    where
                    	m.match_date = lm.match_date
                    group by
                    	m.patch
                    limit 1;
                    """
    )
    Optional<String> actualPatch();

    @Query(
            nativeQuery = true,
            value = """
                    select m.match_date from matches m
                    group by m.match_date
                    order by m.match_date desc
                    limit 1
                    """
    )
    Optional<LocalDate> getDateOfLastMatch();
}
