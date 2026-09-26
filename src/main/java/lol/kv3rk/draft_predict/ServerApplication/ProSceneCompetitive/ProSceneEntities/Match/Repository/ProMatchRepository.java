package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Match.Repository;

import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Match.Entity.ProMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProMatchRepository extends JpaRepository<ProMatchEntity, String> {
    Optional<ProMatchEntity> findByGameId(String gameId);
}
