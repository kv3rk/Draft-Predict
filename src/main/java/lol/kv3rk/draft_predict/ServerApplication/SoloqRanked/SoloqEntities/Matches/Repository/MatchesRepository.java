package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Matches.Repository;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchesRepository extends JpaRepository<MatchesEntity, String> {
    MatchesEntity findByMatchId(String matchID);
}
