package lol.kv3rk.draft_predict.RankedEntities.Matches.Repository;

import lol.kv3rk.draft_predict.RankedEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Locale;
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
            value = "select m.patch from matches as m group by m.patch order by m.patch desc"
    )
    Optional<Double> actualPatch();
}
