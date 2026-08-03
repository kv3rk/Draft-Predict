package lol.kv3rk.draft_predict.RankedEntities.Matches.Repository;

import lol.kv3rk.draft_predict.RankedEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchesRepository extends JpaRepository<MatchesEntity, String> {

    MatchesEntity findByMatchId(String matchId);
}
