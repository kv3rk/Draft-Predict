package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Repository;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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

    @Query(
            nativeQuery = true,
            value = """
                    select
                    	m.patch
                    from
                    	matches m
                    group by
                    	patch
                    order by
                    	patch;
                    """
    )
    List<String> getPatchList();
}
