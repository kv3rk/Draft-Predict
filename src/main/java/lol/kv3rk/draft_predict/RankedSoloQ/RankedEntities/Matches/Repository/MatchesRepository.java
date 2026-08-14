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
            value = """
                    select
                        COUNT(m.match_id)
                    from matches as m
                    cross join actual_patch() ap(patch)
                    where m.patch = ap.patch
                    """
    )
    Optional<Long> countMatches();

    @Query(
            nativeQuery = true,
            value = """
                    select actual_patch();
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
