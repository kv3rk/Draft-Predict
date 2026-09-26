package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Team.Repository;

import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Match.Entity.ProMatchEntity;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Team.Entity.ProTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProTeamRepository extends JpaRepository<ProTeamEntity, UUID> {
    Optional<ProTeamEntity> findByMatchIdAndTeamName(ProMatchEntity matchId, String teamName);
}
